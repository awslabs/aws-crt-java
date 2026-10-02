/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.s3;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.crt.Log;

/**
 * Internal. A Java-owned buffer pool used as the destination memory for
 * {@code aws-c-s3} transfers. Customers configure it through
 * {@link S3DirectBufferPoolOptions}; {@link S3Client} creates one pool per
 * client at construction ({@link #fromOptions}) and closes it when its
 * shutdown completes. The pool is never shared between clients: each
 * client's native pool state keeps its own pending-reserve list, and a
 * lease released by one client could never wake a reservation pended by
 * another.
 *
 * <h2>Layout (mirrors aws-c-s3's default pool)</h2>
 * Memory is allocated in blocks of up to {@value #BLOCK_SLOTS} contiguous
 * slots, each block one {@link ByteBuffer#allocateDirect direct ByteBuffer}.
 * A slot holds one part, so slot size is the client's resolved part size
 * (the native factory re-checks equality at client creation). Reservations
 * are served three ways:
 * <ul>
 *   <li>Up to one part: a single slot.</li>
 *   <li>Up to {@value #MAX_GROUP_SLOTS} parts (for example aws-c-s3's
 *       automatic download ranges): a run of contiguous slots inside one
 *       block, like the default pool's multi-chunk primary allocations. No
 *       new memory is allocated when a backed block has a free run.</li>
 *   <li>Larger: a dedicated direct buffer owned by the meta request that
 *       needs it (uploads raised past S3's 10,000-part limit, resumed
 *       uploads with a larger part size, very large download ranges). It is
 *       retained and reused only by that request while the request is
 *       active, never trimmed from under it, and freed when the request
 *       finishes ({@link #releaseRequest}). Making room for it frees
 *       fully unused blocks, floor included. A pool that cannot grow
 *       (floor == ceiling) never allocates after construction, so it
 *       serves no dedicated buffers.</li>
 * </ul>
 * Blocks and dedicated buffers share one byte budget,
 * {@code maxSlots * partSize}, so the ceiling is hard.
 *
 * <h2>Behaviour details (customer docs summarize these)</h2>
 * <ul>
 *   <li>Trim frees fully unused blocks above the floor; aws-c-s3 schedules
 *       it 5 seconds after the client goes idle and skips it if any request
 *       is in flight at either point.</li>
 *   <li>Growth backs a whole block with {@code allocateDirect} (which
 *       zero-fills) on the reserving thread, usually an aws-c-s3 event-loop
 *       thread, while holding {@code lock} and the native pending_lock, so
 *       other reserves and releases wait for it.</li>
 *   <li>Dedicated buffers are allocated (and zero-filled) per request.
 *       The default native pool instead keeps same-size "special" blocks
 *       shared across requests ({@code add_special_size}, left NULL here).
 *       With aws-c-s3's default sizing, automatic download ranges fit in
 *       {@value #MAX_GROUP_SLOTS} slots; a large explicit memory limit with
 *       few connections can produce larger ranges.</li>
 *   <li>Making room for a dedicated buffer frees fully unused blocks,
 *       floor included; freed floor blocks are backed again on demand.</li>
 *   <li>The native wait queue is strict FIFO for anything that consumes
 *       capacity, so a large waiting request holds back smaller ones queued
 *       behind it (a request may still reuse its own idle dedicated
 *       buffer).</li>
 *   <li>A pool that cannot grow pins download ranges to the part size
 *       ({@link S3Client}), so downloads never need more than one slot.</li>
 * </ul>
 *
 * <h2>Concurrency</h2>
 * Every method synchronizes on {@code lock}. Native callers (reserve,
 * ticket release, trim, request finish) additionally hold the native pool
 * state's {@code pending_lock} around their JNI calls, so acquires and
 * releases are serialized and pending reservations cannot be stranded.
 * Lock order: native pending_lock, then {@code lock}. Nothing here calls
 * into native code that takes pending_lock.
 *
 * <p>Lifetime: leases held by unclosed {@link S3BorrowedBuffer}s outlive
 * both the client and {@link #close()}; their memory is freed when the last
 * lease on it is released (or recovered by the buffer's GC fallback).</p>
 */
final class S3DirectBufferPool {

    /** Slots per block (aws-c-s3's default pool uses 16 chunks per block). */
    static final int BLOCK_SLOTS = 16;

    /** Largest contiguous run served from a block; larger requests get a dedicated buffer (native: 4 chunks). */
    static final int MAX_GROUP_SLOTS = 4;

    /* tryAcquire results besides a lease handle (handles are >= 0). */
    static final long EXHAUSTED = -1;
    static final long IMPOSSIBLE = -2;

    /** Handle tag for dedicated buffers; slot-run handles have it clear. */
    private static final long DEDICATED_TAG = 1L << 62;

    private final int partSize;
    private final int maxSlots;
    private final int numBlocks;
    /** Blocks allocated at construction and never trimmed (the warm floor). */
    private final int floorBlocks;

    /** Byte budget shared by blocks and dedicated buffers: {@code maxSlots * partSize}. */
    private final long ceilingBytes;

    /**
     * Memory limit S3Client hands the native client when none is set, so
     * aws-c-s3's max-part-size check and range sizing match this pool: the
     * resolved limit for auto pools, the ceiling otherwise.
     */
    private final long nativeMemoryLimitBytes;

    /** Native pool state pointer (set by the native factory); passed back on meta requests. */
    private volatile long nativePoolState;

    private final Object lock = new Object();

    /**
     * Backing buffer per block; null until first needed (or after trim).
     * WARNING: must outlive every native read of a lease into it; anchored
     * by the native pool state's JNI global ref on this object.
     */
    private final ByteBuffer[] blocks;
    /** Cached native base address per block; 0 when unbacked. */
    private final long[] blockAddresses;
    /** Per-block occupancy bitmask (bit i = slot i leased). */
    private final int[] usedMask;

    /** Backed block bytes plus every dedicated buffer; never exceeds ceilingBytes. */
    private long committedBytes;

    /** One dedicated buffer. */
    private static final class Dedicated {
        final ByteBuffer buffer;
        final long address;
        final long owner;
        /** Owner's meta request finished (or pool closed): free on release. */
        boolean orphaned;

        Dedicated(ByteBuffer buffer, long address, long owner) {
            this.buffer = buffer;
            this.address = address;
            this.owner = owner;
        }
    }

    private final Map<Long, Dedicated> leasedDedicated = new HashMap<>();
    /** Released dedicated buffers kept for reuse by their owning request. */
    private final Map<Long, ArrayList<Dedicated>> idleDedicatedByOwner = new HashMap<>();
    private long nextDedicatedId;

    /** Set by {@link #close()}: acquires throw; released memory is freed instead of kept. */
    private volatile boolean closed;

    /**
     * Private: {@link S3Client} builds pools via {@link #fromOptions}.
     * {@code initialSlots} rounds up to whole blocks for the eager floor.
     *
     * @throws IllegalArgumentException for invalid sizes
     */
    private S3DirectBufferPool(int partSize, int initialSlots, int maxSlots, long nativeMemoryLimitBytes) {
        if (partSize <= 0)       throw new IllegalArgumentException("partSize must be > 0");
        if (initialSlots < 0)    throw new IllegalArgumentException("initialSlots must be >= 0");
        if (maxSlots < 1)        throw new IllegalArgumentException("maxSlots must be >= 1");
        if (initialSlots > maxSlots) {
            throw new IllegalArgumentException(
                "initialSlots (" + initialSlots + ") must be <= maxSlots (" + maxSlots + ")");
        }
        if ((long) Math.min(BLOCK_SLOTS, maxSlots) * partSize > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                "partSize (" + partSize + ") is too large: one " + BLOCK_SLOTS
              + "-slot block must fit in a single direct buffer");
        }

        this.partSize = partSize;
        this.maxSlots = maxSlots;
        this.numBlocks = (maxSlots + BLOCK_SLOTS - 1) / BLOCK_SLOTS;
        this.floorBlocks = (initialSlots + BLOCK_SLOTS - 1) / BLOCK_SLOTS;
        this.ceilingBytes = (long) maxSlots * partSize;
        this.nativeMemoryLimitBytes = nativeMemoryLimitBytes;
        this.blocks = new ByteBuffer[numBlocks];
        this.blockAddresses = new long[numBlocks];
        this.usedMask = new int[numBlocks];

        // Eager floor. On partial OOM: free the blocks already allocated
        // right away (direct memory is scarce at exactly this point, so do
        // not leave them for GC), log, and rethrow.
        try {
            for (int b = 0; b < floorBlocks; b++) {
                backBlock(b);
            }
        } catch (OutOfMemoryError e) {
            Log.log(Log.LogLevel.Warn, Log.LogSubject.JavaCrtS3,
                "S3DirectBufferPool: OutOfMemoryError during eager allocation of " + floorBlocks
              + " block(s) (" + BLOCK_SLOTS + " x " + partSize + " bytes each). "
              + "Consider raising -XX:MaxDirectMemorySize or reducing pool size.");
            for (int b = 0; b < numBlocks; b++) {
                if (blocks[b] != null) {
                    DirectBufferCleaner.free(blocks[b]);
                }
                blocks[b] = null;
                blockAddresses[b] = 0L;
            }
            committedBytes = 0;
            throw e;
        }
    }

    /**
     * Builds the pool for one client. Called by the {@link S3Client}
     * constructor before native client creation.
     *
     * @param poolOptions   the customer's sizing choice
     * @param clientOptions the client's options; supplies part size,
     *                      throughput target, and memory limit
     * @return a pool whose slot size equals the client's resolved part size
     * @throws IllegalArgumentException if the sizing yields no slot
     * @throws IllegalStateException    if the ceiling does not fit in
     *                                  {@code -XX:MaxDirectMemorySize}
     */
    static S3DirectBufferPool fromOptions(S3DirectBufferPoolOptions poolOptions, S3ClientOptions clientOptions) {
        int partSize = resolvePartSize(clientOptions);
        switch (poolOptions.getMode()) {
            case FIXED:
                checkMemoryLimitMatches(clientOptions, poolOptions.getMemoryLimitBytes() / partSize * partSize, partSize);
                return createFixed(poolOptions.getMemoryLimitBytes(), partSize);
            case ELASTIC:
                checkMemoryLimitMatches(clientOptions, poolOptions.getMaxBytes() / partSize * partSize, partSize);
                return createElastic(poolOptions.getMinBytes(), poolOptions.getMaxBytes(), partSize);
            case AUTO:
            default:
                return createAuto(clientOptions, partSize);
        }
    }

    /**
     * For fixed/elastic pools the pool ceiling IS the client's memory; an
     * explicit, different memoryLimitInBytes would give aws-c-s3 a
     * different limit than the pool enforces. Refuse rather than override.
     * The ceiling is a whole number of parts, so a fixed() size that is not
     * a multiple of partSize rounds down; the message says so, because the
     * caller may have passed the same number to both options.
     */
    private static void checkMemoryLimitMatches(S3ClientOptions clientOptions, long ceilingBytes, int partSize) {
        long explicit = clientOptions.getMemoryLimitInBytes();
        if (explicit > 0 && explicit != ceilingBytes) {
            throw new IllegalArgumentException(
                "S3ClientOptions.memoryLimitInBytes (" + explicit + ") conflicts with the direct buffer pool's "
              + "ceiling (" + ceilingBytes + " bytes). The ceiling is a whole number of parts (" + partSize
              + " bytes each), rounded down from the pool size. Leave memoryLimitInBytes unset (the pool ceiling "
              + "is used), set it to " + ceilingBytes + ", or use S3DirectBufferPoolOptions.auto() to size the "
              + "pool from it.");
        }
    }

    /**
     * The client's part size, or aws-c-s3's 8 MiB default when unset
     * (must match g_default_part_size_fallback in aws-c-s3's s3_util.c).
     * Must match the native client's resolution; the native factory
     * fails client creation if it does not.
     */
    private static int resolvePartSize(S3ClientOptions clientOptions) {
        long partSize = clientOptions.getPartSize();
        if (partSize <= 0) {
            return 8 * 1024 * 1024;
        }
        if (partSize > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                "partSize (" + partSize + ") exceeds the direct buffer pool's maximum slot size");
        }
        return (int) partSize;
    }

    /**
     * Sized like aws-c-s3's default buffer pool: ceiling = the client's
     * memory limit, resolved in {@code aws_s3_client_new}'s order
     * (explicit {@code memoryLimitInBytes}, then the
     * {@code AWS_CRT_S3_MEMORY_LIMIT_IN_MB} / {@code _IN_GIB} env var,
     * then {@code aws_s3_default_memory_limit_for_throughput}); floor =
     * one block.
     */
    private static S3DirectBufferPool createAuto(S3ClientOptions clientOptions, int partSize) {
        long memoryLimitBytes = clientOptions.getMemoryLimitInBytes();
        if (memoryLimitBytes <= 0) {
            memoryLimitBytes = resolveEnvOverrideBytes();
        }
        if (memoryLimitBytes <= 0) {
            memoryLimitBytes = S3Client.defaultMemoryLimitForThroughput(clientOptions.getThroughputTargetGbps());
        }
        int maxSlots = (int) Math.min(Integer.MAX_VALUE, memoryLimitBytes / partSize);
        if (maxSlots < 1) {
            // Without this, the constructor's generic "maxSlots must be >= 1"
            // hides the real cause (limit smaller than one part).
            throw new IllegalArgumentException(
                "resolved memory limit (" + memoryLimitBytes + " bytes) is smaller than one part ("
              + partSize + " bytes). Raise the memory limit or reduce partSize");
        }
        int initialSlots = Math.min(BLOCK_SLOTS, maxSlots);  // warm floor: one block
        validateDirectMemoryCapacity((long) maxSlots * partSize);
        return new S3DirectBufferPool(partSize, initialSlots, maxSlots, memoryLimitBytes);
    }

    /** Floor == ceiling == {@code memoryLimitBytes / partSize}: fully eager, never grows or trims. */
    private static S3DirectBufferPool createFixed(long memoryLimitBytes, int partSize) {
        if (memoryLimitBytes < partSize) {
            throw new IllegalArgumentException(
                "memoryLimitBytes (" + memoryLimitBytes
              + ") must be >= partSize (" + partSize + ")");
        }
        int slotCount = (int) Math.min(Integer.MAX_VALUE, memoryLimitBytes / partSize);
        validateDirectMemoryCapacity((long) slotCount * partSize);
        return new S3DirectBufferPool(partSize, slotCount, slotCount, (long) slotCount * partSize);
    }

    /**
     * Caller-chosen floor and ceiling in bytes (sign and ordering validated by
     * the options factory). Ceiling rounds down to whole parts, like
     * {@link #createFixed}; floor rounds up to whole parts, capped at the
     * ceiling (the constructor then rounds it up to whole blocks).
     */
    private static S3DirectBufferPool createElastic(long minBytes, long maxBytes, int partSize) {
        if (maxBytes < partSize) {
            throw new IllegalArgumentException(
                "maxBytes (" + maxBytes + ") must be >= partSize (" + partSize + ")");
        }
        int maxSlots = (int) Math.min(Integer.MAX_VALUE, maxBytes / partSize);
        int initialSlots = (int) Math.min(maxSlots, (minBytes + partSize - 1) / partSize);
        validateDirectMemoryCapacity((long) maxSlots * partSize);
        return new S3DirectBufferPool(partSize, initialSlots, maxSlots, (long) maxSlots * partSize);
    }

    /* ==================================================================== */
    /* Accessors + lifecycle                                                */
    /* ==================================================================== */

    /** @return the per-slot byte size (the client's resolved part size) */
    int partSize()       { return partSize; }
    /** @return the pool ceiling in slots */
    int maxSlots()       { return maxSlots; }
    /** @return the byte budget shared by blocks and dedicated buffers */
    long ceilingBytes()  { return ceilingBytes; }
    /** @return the memory limit S3Client passes to the native client when none is set explicitly */
    long nativeMemoryLimitBytes() { return nativeMemoryLimitBytes; }
    /** @return the largest reservation served from blocks, without a dedicated buffer */
    long maxGroupBytes() { return (long) Math.min(MAX_GROUP_SLOTS, Math.min(BLOCK_SLOTS, maxSlots)) * partSize; }
    /**
     * @return whether dedicated (larger than {@link #maxGroupBytes()}) buffers
     *         can be served; false when the floor covers every block, so the
     *         pool never allocates after construction
     */
    boolean servesOversize() { return floorBlocks < numBlocks; }
    /** @return bytes currently backed (blocks plus dedicated buffers); for diagnostics and tests */
    long committedBytes() {
        synchronized (lock) { return committedBytes; }
    }
    /** @return the native pool state pointer, 0 until the native factory runs */
    long nativePoolState() { return nativePoolState; }

    /**
     * Marks the pool closed and immediately frees every unused block and
     * idle dedicated buffer. Called by {@link S3Client} when its shutdown
     * completes (and on client-construction failure). Memory still leased by
     * unclosed {@link S3BorrowedBuffer}s is freed when released. Idempotent.
     */
    void close() {
        synchronized (lock) {
            closed = true;
            for (int b = 0; b < numBlocks; b++) {
                if (blocks[b] != null && usedMask[b] == 0) {
                    unbackBlock(b);
                }
            }
            for (ArrayList<Dedicated> idle : idleDedicatedByOwner.values()) {
                for (Dedicated d : idle) {
                    freeDedicated(d);
                }
            }
            idleDedicatedByOwner.clear();
            for (Dedicated d : leasedDedicated.values()) {
                d.orphaned = true;
            }
        }
    }

    /* ==================================================================== */
    /* Package-private JNI back-call surface                                */
    /* ==================================================================== */

    /*
     * Invoked FROM s3_java_buffer_pool.c via JNI. Signatures must remain
     * stable; the method IDs are cached in java_class_ids.c and listed in
     * jni-config.json. Handles: slot runs encode (block, start, count);
     * dedicated buffers carry DEDICATED_TAG.
     */

    /** Called once by the native factory. */
    void setNativePoolState(long state) {
        nativePoolState = state;
    }

    /**
     * Non-blocking acquire of {@code size} bytes for meta request
     * {@code owner}. MUST NOT block (runs on aws-c-s3 event-loop threads);
     * on {@link #EXHAUSTED} native pends its future.
     *
     * @return a lease handle; {@link #EXHAUSTED} when capacity is currently
     *         taken; or {@link #IMPOSSIBLE} when this pool can never serve
     *         the size (reason logged; native fails the reservation)
     * @throws IllegalStateException if the pool is closed
     */
    long tryAcquire(long size, long owner) {
        if (closed) throw new IllegalStateException("pool is closed");
        if (size <= 0) size = 1;
        long slotsNeeded = (size + partSize - 1) / partSize;
        synchronized (lock) {
            if (slotsNeeded <= MAX_GROUP_SLOTS && slotsNeeded <= Math.min(BLOCK_SLOTS, maxSlots)) {
                return acquireRunLocked((int) slotsNeeded);
            }
            return acquireDedicatedLocked(size, owner);
        }
    }

    /**
     * Reuse-only acquire: serves {@code size} from {@code owner}'s own idle
     * dedicated buffers, never consuming budget. Native uses it to let a
     * request past the FIFO queue (otherwise a request waiting at the head
     * for budget held by another request's retained buffers could deadlock
     * with that request's next part queued behind it).
     *
     * @return a lease handle, or {@link #EXHAUSTED}
     */
    long tryReuseOwnIdle(long size, long owner) {
        // Only requests that themselves need a dedicated buffer may reuse one.
        if (closed || size <= maxGroupBytes()) return EXHAUSTED;
        synchronized (lock) {
            return reuseIdleLocked(size, owner);
        }
    }

    /** @return the native address of a lease */
    long leaseAddress(long handle) {
        synchronized (lock) {
            if ((handle & DEDICATED_TAG) != 0) {
                Dedicated d = leasedDedicated.get(handle);
                if (d == null) throw new IllegalStateException("leaseAddress: dedicated lease not held");
                return d.address;
            }
            int b = runBlock(handle);
            if (blockAddresses[b] == 0L) throw new IllegalStateException("leaseAddress: block not backed");
            return blockAddresses[b] + (long) runStart(handle) * partSize;
        }
    }

    /**
     * Returns a lease. Slot runs go back to their block (a fully free block
     * on a closed pool is freed). A dedicated buffer returns to its owner's
     * idle list, or is freed when its owner has finished or the pool is
     * closed. After this returns the memory MAY be re-issued and
     * overwritten; any outstanding view of it is UNSAFE to read.
     */
    void release(long handle) {
        synchronized (lock) {
            if ((handle & DEDICATED_TAG) != 0) {
                Dedicated d = leasedDedicated.remove(handle);
                if (d == null) throw new IllegalStateException("release: dedicated lease not held");
                if (closed || d.orphaned) {
                    freeDedicated(d);
                } else {
                    idleDedicatedByOwner.computeIfAbsent(d.owner, k -> new ArrayList<>()).add(d);
                }
                return;
            }
            int b = runBlock(handle);
            int mask = runMask(handle);
            if ((usedMask[b] & mask) != mask) throw new IllegalStateException("release: slot run not leased");
            usedMask[b] &= ~mask;
            if (closed && usedMask[b] == 0 && blocks[b] != null) {
                unbackBlock(b);
            }
        }
    }

    /**
     * The meta request {@code owner} finished: free its idle dedicated
     * buffers and mark its still-leased ones (for example held by an
     * unclosed borrowed buffer) to be freed on release. Called from the
     * native finish callback, while the meta request is still alive, so its
     * address (the owner key) cannot yet belong to another request.
     */
    void releaseRequest(long owner) {
        synchronized (lock) {
            ArrayList<Dedicated> idle = idleDedicatedByOwner.remove(owner);
            if (idle != null) {
                for (Dedicated d : idle) {
                    freeDedicated(d);
                }
            }
            for (Dedicated d : leasedDedicated.values()) {
                if (d.owner == owner) {
                    d.orphaned = true;
                }
            }
        }
    }

    /**
     * Frees every fully unused block above the warm floor. Scheduled by
     * aws-c-s3 with the same idleness gating as the native pool (5-second
     * delay, skipped if {@code num_requests_in_flight > 0} at either schedule
     * or execution time; see {@code s_s3_client_schedule_buffer_pool_trim_synced}).
     * Dedicated buffers of active requests are never trimmed; they are freed
     * when their request finishes.
     */
    void trim() {
        synchronized (lock) {
            if (closed) return;
            for (int b = floorBlocks; b < numBlocks; b++) {
                if (blocks[b] != null && usedMask[b] == 0) {
                    unbackBlock(b);
                }
            }
        }
    }

    /* ==================================================================== */
    /* Allocation internals (caller holds lock)                             */
    /* ==================================================================== */

    /** First fit: a free run of {@code count} slots in a backed block, else back a new block. */
    private long acquireRunLocked(int count) {
        for (int b = 0; b < numBlocks; b++) {
            if (blocks[b] != null) {
                int start = findRun(b, count);
                if (start >= 0) {
                    return leaseRun(b, start, count);
                }
            }
        }
        for (int b = 0; b < numBlocks; b++) {
            if (blocks[b] == null && blockSlots(b) >= count) {
                long bytes = (long) blockSlots(b) * partSize;
                if (committedBytes + bytes > ceilingBytes) {
                    return EXHAUSTED;   // dedicated buffers hold the budget
                }
                backBlock(b);
                return leaseRun(b, 0, count);
            }
        }
        return EXHAUSTED;
    }

    private long acquireDedicatedLocked(long size, long owner) {
        if (!servesOversize()) {
            Log.log(Log.LogLevel.Error, Log.LogSubject.JavaCrtS3,
                "S3DirectBufferPool: a " + size + "-byte buffer was requested, larger than the " + maxGroupBytes()
              + " bytes this pool serves from its blocks, and the pool's floor equals its ceiling so it never "
              + "allocates past construction. Use S3DirectBufferPoolOptions.auto() or elastic(), or a larger partSize.");
            return IMPOSSIBLE;
        }
        if (size > ceilingBytes || size > Integer.MAX_VALUE) {
            Log.log(Log.LogLevel.Error, Log.LogSubject.JavaCrtS3,
                "S3DirectBufferPool: a " + size + "-byte buffer was requested, which exceeds the pool ceiling of "
              + ceilingBytes + " bytes. Raise the pool's memory limit or use a smaller part size.");
            return IMPOSSIBLE;
        }
        long reused = reuseIdleLocked(size, owner);
        if (reused != EXHAUSTED) {
            return reused;
        }
        // Make room by freeing fully unused blocks (highest first, floor included).
        for (int b = numBlocks - 1; b >= 0 && committedBytes + size > ceilingBytes; b--) {
            if (blocks[b] != null && usedMask[b] == 0) {
                unbackBlock(b);
            }
        }
        if (committedBytes + size > ceilingBytes) {
            return EXHAUSTED;
        }
        ByteBuffer dbb;
        try {
            dbb = ByteBuffer.allocateDirect((int) size);
        } catch (OutOfMemoryError e) {
            Log.log(Log.LogLevel.Warn, Log.LogSubject.JavaCrtS3,
                "S3DirectBufferPool: OutOfMemoryError allocating a " + size + "-byte dedicated buffer. "
              + "Consider raising -XX:MaxDirectMemorySize.");
            throw e;
        }
        committedBytes += dbb.capacity();
        return leaseDedicated(new Dedicated(dbb, nativeGetDirectBufferAddress(dbb), owner));
    }

    /** Best fit among the owner's idle dedicated buffers. */
    private long reuseIdleLocked(long size, long owner) {
        ArrayList<Dedicated> idle = idleDedicatedByOwner.get(owner);
        if (idle == null) {
            return EXHAUSTED;
        }
        int best = -1;
        for (int i = 0; i < idle.size(); i++) {
            int cap = idle.get(i).buffer.capacity();
            if (cap >= size && (best < 0 || cap < idle.get(best).buffer.capacity())) {
                best = i;
            }
        }
        if (best < 0) {
            return EXHAUSTED;
        }
        Dedicated d = idle.remove(best);
        if (idle.isEmpty()) {
            idleDedicatedByOwner.remove(owner);
        }
        return leaseDedicated(d);
    }

    private long leaseDedicated(Dedicated d) {
        long handle = DEDICATED_TAG | nextDedicatedId++;
        leasedDedicated.put(handle, d);
        return handle;
    }

    private int blockSlots(int b) {
        return Math.min(BLOCK_SLOTS, maxSlots - b * BLOCK_SLOTS);
    }

    private int findRun(int b, int count) {
        int slots = blockSlots(b);
        int want = (1 << count) - 1;
        for (int start = 0; start + count <= slots; start++) {
            if ((usedMask[b] & (want << start)) == 0) {
                return start;
            }
        }
        return -1;
    }

    private long leaseRun(int b, int start, int count) {
        usedMask[b] |= ((1 << count) - 1) << start;
        return ((long) b << 16) | ((long) start << 8) | count;
    }

    private static int runBlock(long handle) { return (int) (handle >>> 16); }
    private static int runStart(long handle) { return (int) ((handle >>> 8) & 0xFF); }
    private static int runMask(long handle) {
        int count = (int) (handle & 0xFF);
        return ((1 << count) - 1) << runStart(handle);
    }

    private void backBlock(int b) {
        ByteBuffer dbb = ByteBuffer.allocateDirect(blockSlots(b) * partSize);
        blocks[b] = dbb;
        blockAddresses[b] = nativeGetDirectBufferAddress(dbb);
        committedBytes += dbb.capacity();
    }

    /** Frees a fully unused block. Null before free so no stale address is ever handed out. */
    private void unbackBlock(int b) {
        ByteBuffer dbb = blocks[b];
        blocks[b] = null;
        blockAddresses[b] = 0L;
        committedBytes -= dbb.capacity();
        DirectBufferCleaner.free(dbb);
    }

    private void freeDedicated(Dedicated d) {
        committedBytes -= d.buffer.capacity();
        DirectBufferCleaner.free(d.buffer);
    }

    /* ==================================================================== */
    /* Internal helpers                                                     */
    /* ==================================================================== */

    /**
     * Fail-fast check: verify that the JVM's {@code MaxDirectMemorySize}
     * can accommodate the pool's maximum capacity. A ceiling above 80%
     * would OOM unpredictably mid-transfer; failing at construction gives
     * an actionable error. The 20% headroom is for other direct-buffer
     * users (NIO channels, networking libraries, SDK internals).
     *
     * @param poolCapacityBytes the pool's maximum byte capacity
     *                          ({@code maxSlots × partSize})
     * @throws IllegalStateException if the ceiling exceeds 80% of
     *                               {@code MaxDirectMemorySize}
     */
    private static void validateDirectMemoryCapacity(long poolCapacityBytes) {
        long maxDirectMemory = getMaxDirectMemory();
        if (maxDirectMemory <= 0) {
            // Unable to determine the limit (non-HotSpot JVM or reflective
            // access denied). Log a warning but don't block construction.
            Log.log(Log.LogLevel.Warn, Log.LogSubject.JavaCrtS3,
                "S3DirectBufferPool: unable to determine MaxDirectMemorySize. "
              + "Pool requires " + (poolCapacityBytes / (1024 * 1024)) + " MiB of direct memory. "
              + "Ensure -XX:MaxDirectMemorySize is set appropriately.");
            return;
        }

        // Reserve 20% of MaxDirectMemorySize for other direct buffer users.
        long availableForPool = (long) (maxDirectMemory * 0.8);

        if (poolCapacityBytes > availableForPool) {
            long poolMiB = poolCapacityBytes / (1024 * 1024);
            long maxMiB = maxDirectMemory / (1024 * 1024);
            long recommendedMiB = (long) (poolCapacityBytes * 1.25 / (1024 * 1024));
            throw new IllegalStateException(
                "S3DirectBufferPool requires " + poolMiB + " MiB of direct memory, "
              + "but MaxDirectMemorySize is " + maxMiB + " MiB "
              + "(80% usable = " + (availableForPool / (1024 * 1024)) + " MiB). "
              + "Either set -XX:MaxDirectMemorySize=" + recommendedMiB + "m, "
              + "or use S3DirectBufferPoolOptions.fixed(memoryLimitBytes) / "
              + "S3DirectBufferPoolOptions.elastic(minBytes, maxBytes) "
              + "to size the pool within available direct memory.");
        }
    }

    /** Returns the JVM's {@code MaxDirectMemorySize} via reflective probes, or -1 if it cannot be determined. */
    private static long getMaxDirectMemory() {
        // Try sun.misc.VM.maxDirectMemory(), available on HotSpot/OpenJDK 8-21+.
        try {
            Class<?> vmClass = Class.forName("sun.misc.VM");
            java.lang.reflect.Method method = vmClass.getDeclaredMethod("maxDirectMemory");
            return (Long) method.invoke(null);
        } catch (Exception ignored) {
            // Fall through to alternative.
        }

        // Try jdk.internal.misc.VM on newer JDKs (Java 9+).
        try {
            Class<?> vmClass = Class.forName("jdk.internal.misc.VM");
            java.lang.reflect.Method method = vmClass.getDeclaredMethod("maxDirectMemory");
            return (Long) method.invoke(null);
        } catch (Exception ignored) {
            // Cannot determine.
        }

        // Fallback: check the runtime args for an explicit
        // -XX:MaxDirectMemorySize. Accessed reflectively because
        // java.lang.management does not exist on Android.
        try {
            Class<?> mgmtFactory = Class.forName("java.lang.management.ManagementFactory");
            Object runtimeMxBean = mgmtFactory.getMethod("getRuntimeMXBean").invoke(null);
            @SuppressWarnings("unchecked")
            java.util.List<String> inputArgs = (java.util.List<String>) Class
                .forName("java.lang.management.RuntimeMXBean")
                .getMethod("getInputArguments")
                .invoke(runtimeMxBean);
            for (String arg : inputArgs) {
                if (arg.startsWith("-XX:MaxDirectMemorySize=")) {
                    String val = arg.substring("-XX:MaxDirectMemorySize=".length()).trim().toLowerCase();
                    long multiplier = 1;
                    if (val.endsWith("g")) {
                        multiplier = 1024L * 1024L * 1024L;
                        val = val.substring(0, val.length() - 1);
                    } else if (val.endsWith("m")) {
                        multiplier = 1024L * 1024L;
                        val = val.substring(0, val.length() - 1);
                    } else if (val.endsWith("k")) {
                        multiplier = 1024L;
                        val = val.substring(0, val.length() - 1);
                    }
                    return Long.parseLong(val) * multiplier;
                }
            }
        } catch (Exception ignored) {
            // Cannot determine.
        }

        return -1;
    }

    /**
     * Resolves {@code AWS_CRT_S3_MEMORY_LIMIT_IN_MB} (first) then
     * {@code _IN_GIB} to bytes, matching aws-c-s3's order. Returns
     * 0 when unset or unusable.
     */
    private static long resolveEnvOverrideBytes() {
        long mbBytes = parsePositiveEnvScaled("AWS_CRT_S3_MEMORY_LIMIT_IN_MB", 1024L * 1024L);
        if (mbBytes > 0) return mbBytes;

        return parsePositiveEnvScaled("AWS_CRT_S3_MEMORY_LIMIT_IN_GIB", 1024L * 1024L * 1024L);
    }

    /**
     * Parses the named environment variable as a positive integer and
     * returns {@code value * unitBytes}, or {@code 0} when unset,
     * empty, non-numeric, or non-positive.
     */
    private static long parsePositiveEnvScaled(String envVarName, long unitBytes) {
        String raw = System.getenv(envVarName);
        if (raw == null || raw.isEmpty()) return 0;
        try {
            long units = Long.parseLong(raw.trim());
            // multiplyExact: silent overflow would wrap to a bogus limit;
            // aws-c-s3 uses aws_mul_u64_checked for the same reason.
            if (units > 0) return Math.multiplyExact(units, unitBytes);
        } catch (NumberFormatException | ArithmeticException ignored) {
            // fall through
        }
        return 0;
    }

    // Implemented in src/native/s3_java_buffer_pool.c via JNI.
    private static native long nativeGetDirectBufferAddress(ByteBuffer dbb);

    /* javadoc on the package-private members is intentionally rich:
     * the JNI side cannot call private methods, so these signatures
     * are effectively a contract. Any change here must be coordinated
     * with java_class_ids.c. */
}
