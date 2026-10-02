/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.s3;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.crt.Log;

/**
 * Internal. A Java-owned buffer pool that replaces the default native
 * buffer pool as the memory for the S3 client's transfers. Customers configure it through
 * {@link S3DirectBufferPoolOptions}; {@link S3Client} creates one pool per
 * client at construction ({@link #fromOptions}) and closes it when its
 * shutdown completes. The pool is never shared between clients: each
 * client's native pool state keeps its own pending-reserve list, and a
 * lease released by one client could never wake a reservation pended by
 * another.
 *
 * <h2>Layout (mirrors the default native buffer pool)</h2>
 * Memory is allocated in blocks of up to {@value #BLOCK_SLOTS} contiguous
 * slots, each block one {@link ByteBuffer#allocateDirect direct ByteBuffer}.
 * A slot holds one part, so slot size is the client's resolved part size
 * (the native factory re-checks equality at client creation).
 *
 * <p>The ceiling ({@code maxSlots * partSize}) is the byte budget shared by
 * blocks and dedicated buffers, so it is hard. The floor is the leading
 * blocks wholly covered by the requested floor slots, so it never exceeds
 * what was asked for; it is backed at construction and kept by
 * {@link #trim}, which frees unused blocks above it. A fixed pool's floor is
 * the whole pool, and it never allocates after construction.</p>
 *
 * <p>Each reservation is a lease, identified by a handle, served three
 * ways:</p>
 * <ul>
 *   <li>Up to one part: a single slot.</li>
 *   <li>Up to {@value #MAX_GROUP_SLOTS} parts (for example the client's
 *       automatic download ranges): a run of contiguous slots inside one
 *       block, like the default pool's multi-chunk primary allocations. No
 *       new memory is allocated when a backed block has a free run.</li>
 *   <li>Larger: a dedicated direct buffer for that one reservation
 *       (uploads raised past S3's 10,000-part limit, resumed uploads with a
 *       larger part size, download ranges above {@value #MAX_GROUP_SLOTS}
 *       parts, which the client's default sizing only produces with a large
 *       explicit memory limit and few connections). It is allocated (and
 *       zero-filled) on acquire and freed on release, so it never holds
 *       budget while idle; the default native pool instead keeps same-size
 *       "special" blocks ({@code add_special_size}, left NULL here). Making
 *       room for one frees fully unused blocks, floor included; freed floor
 *       blocks are backed again on demand. Fixed pools serve none, and
 *       {@link S3Client} pins their download ranges to the part size so
 *       downloads never need more than one slot.</li>
 * </ul>
 *
 * <h2>Concurrency</h2>
 * Every method that reads or changes block or dedicated-buffer state
 * synchronizes on {@code lock}; the immutable accessors and the volatile
 * {@code closed} fast-path checks do not need it. Native callers (reserve,
 * ticket release, trim) additionally hold the native pool state's
 * {@code pending_lock} around their JNI calls, so acquires and releases are
 * serialized and pending reservations cannot be stranded. Lock order:
 * native pending_lock, then {@code lock}. Nothing here calls into native
 * code that takes pending_lock. Dedicated buffers are freed on release, so
 * idle memory never holds budget; only leases in use do, and with in-order
 * part reservation those always complete, keeping the strict FIFO queue
 * deadlock-free.
 *
 * <p>Growth backs a whole block with {@code allocateDirect} (which
 * zero-fills) on the reserving thread, usually a native event-loop
 * thread, while holding both locks, so other reserves and releases wait
 * for it. The native wait queue is strict FIFO for anything that consumes
 * capacity, so a large waiting request holds back smaller ones behind
 * it.</p>
 *
 * <p>Lifetime: leases held by unclosed {@link S3BorrowedBuffer}s outlive
 * both the client and {@link #close()}; their memory is freed when the last
 * lease on it is released (or recovered by the buffer's GC fallback).</p>
 */
final class S3DirectBufferPool {

    /** Slots per block (the default native buffer pool uses 16 chunks per block). */
    static final int BLOCK_SLOTS = 16;

    /** Largest contiguous run served from a block; larger requests get a dedicated buffer (native: 4 chunks). */
    static final int MAX_GROUP_SLOTS = 4;

    /* tryAcquire results besides a lease handle (handles are >= 0). */
    static final long EXHAUSTED = -1;
    static final long IMPOSSIBLE = -2;

    /** Share of {@code MaxDirectMemorySize} the pool's ceiling may use; the rest is headroom for other users. */
    private static final double DIRECT_MEMORY_FRACTION = 0.8;

    /** Handle tag for dedicated buffers; slot-run handles have it clear. */
    private static final long DEDICATED_TAG = 1L << 62;

    private final int partSize;
    private final int maxSlots;
    private final int numBlocks;
    /** Blocks allocated at construction and never trimmed (the floor). */
    private final int floorBlocks;
    /** {@code fixed()} pool: never allocates or frees after construction, so serves no dedicated buffers. */
    private final boolean fixed;
    /** auto() pool whose derived ceiling was reduced to fit the JVM direct memory limit. */
    private final boolean ceilingClamped;

    /** Byte budget shared by blocks and dedicated buffers: {@code maxSlots * partSize}. */
    private final long ceilingBytes;

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

        Dedicated(ByteBuffer buffer, long address) {
            this.buffer = buffer;
            this.address = address;
        }
    }

    /** Leased dedicated buffers by handle; freed on release. */
    private final Map<Long, Dedicated> leasedDedicated = new HashMap<>();
    private long nextDedicatedId;

    /** Set by {@link #close()}: acquires throw; released memory is freed instead of kept. */
    private volatile boolean closed;

    /**
     * Private: {@link S3Client} builds pools via {@link #fromOptions}.
     * {@code initialSlots} rounds down to whole blocks for the eager floor
     * (a final partial block counts when the floor reaches the ceiling).
     *
     * @throws IllegalArgumentException for invalid sizes
     */
    private S3DirectBufferPool(int partSize, int initialSlots, int maxSlots, boolean fixed,
                               boolean ceilingClamped) {
        if (partSize <= 0)       throw new IllegalArgumentException("partSize must be > 0");
        if (initialSlots < 0)    throw new IllegalArgumentException("initialSlots must be >= 0");
        if (maxSlots < 1)        throw new IllegalArgumentException("maxSlots must be >= 1");
        if (initialSlots > maxSlots) {
            throw new IllegalArgumentException(
                "initialSlots (" + initialSlots + ") must be <= maxSlots (" + maxSlots + ")");
        }
        // One block is one direct ByteBuffer, so BLOCK_SLOTS parts (or the
        // whole pool, if smaller) must fit in Integer.MAX_VALUE bytes.
        int slotsPerBlock = Math.min(BLOCK_SLOTS, maxSlots);
        if ((long) slotsPerBlock * partSize > Integer.MAX_VALUE) {
            int maxPartSize = Integer.MAX_VALUE / slotsPerBlock;
            throw new IllegalArgumentException(
                "partSize (" + partSize + ") is too large for the direct buffer pool: with this pool size, "
              + "partSize can be at most " + maxPartSize + " bytes");
        }

        this.partSize = partSize;
        this.maxSlots = maxSlots;
        this.numBlocks = (maxSlots + BLOCK_SLOTS - 1) / BLOCK_SLOTS;
        this.floorBlocks = initialSlots == maxSlots ? numBlocks : initialSlots / BLOCK_SLOTS;
        this.fixed = fixed;
        this.ceilingClamped = ceilingClamped;
        this.ceilingBytes = (long) maxSlots * partSize;
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
              + " block(s) (up to " + BLOCK_SLOTS + " x " + partSize + " bytes each). "
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
     * @throws IllegalArgumentException if the sizing yields less than one
     *                                  part, an explicit memoryLimitInBytes
     *                                  does not match a fixed or elastic
     *                                  ceiling, or partSize is too large for
     *                                  the pool
     * @throws IllegalStateException    if the ceiling does not fit in 80% of
     *                                  {@code -XX:MaxDirectMemorySize}
     */
    static S3DirectBufferPool fromOptions(S3DirectBufferPoolOptions poolOptions, S3ClientOptions clientOptions) {
        int partSize = resolvePartSize(clientOptions);
        int initialSlots;
        int maxSlots;
        boolean ceilingClamped = false;
        switch (poolOptions.getMode()) {
            case FIXED: {
                // Floor == ceiling == memoryLimitBytes rounded down to whole
                // parts: fully eager, never grows or trims.
                long bytes = poolOptions.getMemoryLimitBytes();
                requireAtLeastOnePart("memoryLimitBytes", bytes, partSize);
                checkMemoryLimitMatches(clientOptions, bytes / partSize * partSize, partSize);
                maxSlots = (int) Math.min(Integer.MAX_VALUE, bytes / partSize);
                initialSlots = maxSlots;
                break;
            }
            case ELASTIC: {
                // Ceiling and floor both round down to whole parts, so the
                // floor never exceeds minBytes (the constructor then keeps
                // only the whole blocks it covers, or the whole pool when the
                // floor reaches the ceiling). elastic() already rejected
                // minBytes < 0, maxBytes <= 0 and minBytes > maxBytes.
                long maxBytes = poolOptions.getMaxBytes();
                requireAtLeastOnePart("maxBytes", maxBytes, partSize);
                checkMemoryLimitMatches(clientOptions, maxBytes / partSize * partSize, partSize);
                maxSlots = (int) Math.min(Integer.MAX_VALUE, maxBytes / partSize);
                initialSlots = (int) Math.min(maxSlots, poolOptions.getMinBytes() / partSize);
                break;
            }
            case AUTO:
            default: {
                // Sized like the default native buffer pool. Floor: one block.
                // Ceiling: the client's memory limit, resolved in the native
                // client's order (see S3DirectBufferPoolOptions#auto()).
                long memoryLimitBytes = clientOptions.getMemoryLimitInBytes();
                if (memoryLimitBytes <= 0) {
                    memoryLimitBytes = parsePositiveEnvScaled("AWS_CRT_S3_MEMORY_LIMIT_IN_MB", 1024L * 1024L);
                }
                if (memoryLimitBytes <= 0) {
                    memoryLimitBytes = parsePositiveEnvScaled("AWS_CRT_S3_MEMORY_LIMIT_IN_GIB", 1024L * 1024L * 1024L);
                }
                if (memoryLimitBytes <= 0) {
                    // Derived, not set by the customer: shrink it to fit the
                    // JVM's direct memory limit instead of failing. Explicit
                    // sizes are never changed; they fail fast below.
                    memoryLimitBytes = S3Client.defaultMemoryLimitForThroughput(clientOptions.getThroughputTargetGbps());
                    long maxDirectMemory = getMaxDirectMemory();
                    long available = (long) (maxDirectMemory * DIRECT_MEMORY_FRACTION) / partSize * partSize;
                    if (maxDirectMemory > 0 && memoryLimitBytes > available && available >= partSize) {
                        // WARN: the pool is smaller than the client would normally
                        // use for this throughput, which can limit transfer speed.
                        long recommendedMiB = (long) Math.ceil(memoryLimitBytes / DIRECT_MEMORY_FRACTION / (1024 * 1024));
                        Log.log(Log.LogLevel.Warn, Log.LogSubject.JavaCrtS3,
                            "S3DirectBufferPool: the default ceiling for this throughput target is " + memoryLimitBytes
                          + " bytes, but only " + available + " bytes fit within 80% of MaxDirectMemorySize ("
                          + maxDirectMemory + " bytes), so the pool uses " + available + " bytes. This can limit "
                          + "transfer throughput. To use the full default, set -XX:MaxDirectMemorySize="
                          + recommendedMiB + "m or higher; or set S3ClientOptions.withMemoryLimitInBytes to choose "
                          + "the ceiling yourself.");
                        memoryLimitBytes = available;
                        ceilingClamped = true;
                    }
                }
                maxSlots = (int) Math.min(Integer.MAX_VALUE, memoryLimitBytes / partSize);
                if (maxSlots < 1) {
                    // Without this, the constructor's generic "maxSlots must be >= 1"
                    // hides the real cause (limit smaller than one part).
                    throw new IllegalArgumentException(
                        "resolved memory limit (" + memoryLimitBytes + " bytes) is smaller than one part ("
                      + partSize + " bytes). Raise the memory limit or reduce partSize");
                }
                initialSlots = Math.min(BLOCK_SLOTS, maxSlots);
                break;
            }
        }
        validateDirectMemoryCapacity((long) maxSlots * partSize);
        return new S3DirectBufferPool(partSize, initialSlots, maxSlots,
                                      poolOptions.getMode() == S3DirectBufferPoolOptions.Mode.FIXED,
                                      ceilingClamped);
    }

    /**
     * Size check that runs before {@link #checkMemoryLimitMatches}, so a size
     * below one part reports that cause rather than a mismatch with a
     * 0-byte ceiling.
     */
    private static void requireAtLeastOnePart(String name, long bytes, int partSize) {
        if (bytes < partSize) {
            throw new IllegalArgumentException(
                name + " (" + bytes + ") must be >= partSize (" + partSize + ")");
        }
    }

    /**
     * For fixed/elastic pools the pool ceiling IS the client's memory; an
     * explicit, different memoryLimitInBytes would give the native client
     * a different limit than the pool enforces. Refuse rather than override.
     * The ceiling is a whole number of parts, so a fixed() or elastic()
     * size that is not a multiple of partSize rounds down; the message says
     * so, because the caller may have passed the same number to both
     * options.
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
     * The client's part size, or the native client's 8 MiB default when
     * unset. Must match the native client's resolution; the native factory
     * fails client creation if it does not.
     */
    private static int resolvePartSize(S3ClientOptions clientOptions) {
        long partSize = clientOptions.getPartSize();
        if (partSize <= 0) {
            return 8 * 1024 * 1024;
        }
        if (partSize > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                "partSize (" + partSize + ") is too large for the direct buffer pool");
        }
        return (int) partSize;
    }

    /* ==================================================================== */
    /* Accessors + lifecycle                                                */
    /* ==================================================================== */

    /** @return the per-slot byte size (the client's resolved part size) */
    int partSize()       { return partSize; }
    /** @return the byte budget shared by blocks and dedicated buffers */
    long ceilingBytes()  { return ceilingBytes; }
    /** @return the largest reservation served from blocks, without a dedicated buffer */
    long maxGroupBytes() { return (long) Math.min(MAX_GROUP_SLOTS, Math.min(BLOCK_SLOTS, maxSlots)) * partSize; }
    /**
     * @return whether dedicated (larger than {@link #maxGroupBytes()}) buffers
     *         can be served; false only for fixed pools, which never allocate
     *         after construction
     */
    boolean servesOversize() { return !fixed; }
    /**
     * @return whether an auto() pool reduced its derived ceiling to fit the
     *         JVM direct memory limit; S3Client then passes the ceiling to
     *         the native client so both use the same limit
     */
    boolean ceilingClamped() { return ceilingClamped; }
    /** @return bytes currently backed (blocks plus dedicated buffers); for diagnostics and tests */
    long committedBytes() {
        synchronized (lock) { return committedBytes; }
    }

    /**
     * Marks the pool closed and immediately frees every unused block.
     * Called by {@link S3Client} when its shutdown completes (and on
     * client-construction failure). Memory still leased by unclosed
     * {@link S3BorrowedBuffer}s is freed when released. Idempotent.
     */
    void close() {
        synchronized (lock) {
            closed = true;
            for (int b = 0; b < numBlocks; b++) {
                if (blocks[b] != null && usedMask[b] == 0) {
                    unbackBlock(b);
                }
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

    /**
     * Non-blocking acquire of {@code size} bytes. MUST NOT block (runs on
     * native event-loop threads); on {@link #EXHAUSTED} native pends its
     * future.
     *
     * @return a lease handle; {@link #EXHAUSTED} when capacity is currently
     *         taken; or {@link #IMPOSSIBLE} when this pool can never serve
     *         the size (reason logged; native fails the reservation)
     * @throws IllegalStateException if the pool is closed
     */
    long tryAcquire(long size) {
        if (size <= 0) size = 1;
        long slotsNeeded = (size + partSize - 1) / partSize;
        synchronized (lock) {
            // Under the lock, so no acquire can back memory after close().
            if (closed) throw new IllegalStateException("pool is closed");
            if (slotsNeeded <= MAX_GROUP_SLOTS && slotsNeeded <= Math.min(BLOCK_SLOTS, maxSlots)) {
                return acquireRunLocked((int) slotsNeeded);
            }
            return acquireDedicatedLocked(size);
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
     * on a closed pool is freed); a dedicated buffer is freed. After this
     * returns the memory MAY be re-issued and overwritten; any outstanding
     * view of it is UNSAFE to read.
     */
    void release(long handle) {
        synchronized (lock) {
            if ((handle & DEDICATED_TAG) != 0) {
                Dedicated d = leasedDedicated.remove(handle);
                if (d == null) throw new IllegalStateException("release: dedicated lease not held");
                freeDedicated(d);
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
     * Frees every fully unused block above the floor. Scheduled by the
     * native client with the same idleness gating as the default native
     * buffer pool (5 seconds after the client goes idle, skipped if any
     * request is in flight at either point).
     * Dedicated buffers are never idle (they are freed on release), so trim
     * never sees them.
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
        int want = (1 << count) - 1;
        for (int b = 0; b < numBlocks; b++) {
            if (blocks[b] == null) {
                continue;
            }
            for (int start = 0; start + count <= blockSlots(b); start++) {
                if ((usedMask[b] & (want << start)) == 0) {
                    return leaseRun(b, start, count);
                }
            }
        }
        for (int b = 0; b < numBlocks; b++) {
            if (blocks[b] == null && blockSlots(b) >= count) {
                long bytes = (long) blockSlots(b) * partSize;
                if (committedBytes + bytes > ceilingBytes) {
                    continue;   // dedicated buffers hold the budget; a smaller (last) block may still fit
                }
                backBlock(b);
                return leaseRun(b, 0, count);
            }
        }
        return EXHAUSTED;
    }

    private long acquireDedicatedLocked(long size) {
        if (!servesOversize()) {
            Log.log(Log.LogLevel.Error, Log.LogSubject.JavaCrtS3,
                "S3DirectBufferPool: a " + size + "-byte buffer was requested, larger than the " + maxGroupBytes()
              + " bytes this pool serves from its blocks, and a fixed() pool never allocates a separate buffer. "
              + "Use S3DirectBufferPoolOptions.auto() or elastic(), or a larger partSize.");
            return IMPOSSIBLE;
        }
        if (size > ceilingBytes || size > Integer.MAX_VALUE) {
            Log.log(Log.LogLevel.Error, Log.LogSubject.JavaCrtS3,
                "S3DirectBufferPool: a " + size + "-byte buffer was requested, which exceeds the pool ceiling of "
              + ceilingBytes + " bytes. Raise the pool's memory limit or use a smaller part size.");
            return IMPOSSIBLE;
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
        long handle = DEDICATED_TAG | nextDedicatedId++;
        leasedDedicated.put(handle, new Dedicated(dbb, nativeGetDirectBufferAddress(dbb)));
        return handle;
    }

    private int blockSlots(int b) {
        return Math.min(BLOCK_SLOTS, maxSlots - b * BLOCK_SLOTS);
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
     *                          ({@code maxSlots x partSize})
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
        long availableForPool = (long) (maxDirectMemory * DIRECT_MEMORY_FRACTION);

        if (poolCapacityBytes > availableForPool) {
            long poolMiB = poolCapacityBytes / (1024 * 1024);
            long maxMiB = maxDirectMemory / (1024 * 1024);
            long recommendedMiB = (long) (poolCapacityBytes * 1.25 / (1024 * 1024));
            throw new IllegalStateException(
                "S3DirectBufferPool requires " + poolMiB + " MiB of direct memory, "
              + "but MaxDirectMemorySize is " + maxMiB + " MiB "
              + "(80% usable = " + (availableForPool / (1024 * 1024)) + " MiB). "
              + "Either set -XX:MaxDirectMemorySize=" + recommendedMiB + "m, "
              + "or lower the pool's ceiling: S3ClientOptions.withMemoryLimitInBytes with auto(), "
              + "or a smaller size passed to fixed() or elastic().");
        }
    }

    /** Returns the JVM's {@code MaxDirectMemorySize} via reflective probes, or -1 if it cannot be determined. */
    private static long getMaxDirectMemory() {
        // Java 8: sun.misc.VM.maxDirectMemory(). Removed in Java 9.
        try {
            Class<?> vmClass = Class.forName("sun.misc.VM");
            java.lang.reflect.Method method = vmClass.getDeclaredMethod("maxDirectMemory");
            return (Long) method.invoke(null);
        } catch (Exception ignored) {
            // Fall through to alternative.
        }

        // Java 9+: jdk.internal.misc.VM. Only reachable if the application
        // passes --add-exports java.base/jdk.internal.misc=ALL-UNNAMED.
        try {
            Class<?> vmClass = Class.forName("jdk.internal.misc.VM");
            java.lang.reflect.Method method = vmClass.getDeclaredMethod("maxDirectMemory");
            return (Long) method.invoke(null);
        } catch (Exception ignored) {
            // Cannot determine.
        }

        // Fallback (the usual path on Java 9+): check the runtime args for an
        // explicit -XX:MaxDirectMemorySize. Without one, HotSpot's default
        // limit is Runtime.maxMemory() (the -Xmx value). Accessed
        // reflectively because java.lang.management does not exist on
        // Android, where direct memory has no JVM limit and -1 is returned.
        try {
            Class<?> mgmtFactory = Class.forName("java.lang.management.ManagementFactory");
            Object runtimeMxBean = mgmtFactory.getMethod("getRuntimeMXBean").invoke(null);
            @SuppressWarnings("unchecked")
            java.util.List<String> inputArgs = (java.util.List<String>) Class
                .forName("java.lang.management.RuntimeMXBean")
                .getMethod("getInputArguments")
                .invoke(runtimeMxBean);
            // The JVM uses the last occurrence if the flag is given more than once.
            for (int i = inputArgs.size() - 1; i >= 0; i--) {
                String arg = inputArgs.get(i);
                if (arg.startsWith("-XX:MaxDirectMemorySize=")) {
                    String val = arg.substring("-XX:MaxDirectMemorySize=".length()).trim().toLowerCase();
                    long multiplier = 1;
                    if (val.endsWith("t")) {
                        multiplier = 1024L * 1024L * 1024L * 1024L;
                        val = val.substring(0, val.length() - 1);
                    } else if (val.endsWith("g")) {
                        multiplier = 1024L * 1024L * 1024L;
                        val = val.substring(0, val.length() - 1);
                    } else if (val.endsWith("m")) {
                        multiplier = 1024L * 1024L;
                        val = val.substring(0, val.length() - 1);
                    } else if (val.endsWith("k")) {
                        multiplier = 1024L;
                        val = val.substring(0, val.length() - 1);
                    }
                    long bytes = Long.parseLong(val) * multiplier;
                    // 0 means "use the default", which is Runtime.maxMemory().
                    return bytes > 0 ? bytes : Runtime.getRuntime().maxMemory();
                }
            }
            return Runtime.getRuntime().maxMemory();
        } catch (Exception ignored) {
            // Cannot determine.
        }

        return -1;
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
            // multiplyExact: silent overflow would wrap to a bogus limit (the
            // native client checks for overflow for the same reason).
            if (units > 0) return Math.multiplyExact(units, unitBytes);
        } catch (NumberFormatException | ArithmeticException ignored) {
            // fall through
        }
        return 0;
    }

    // Implemented in src/native/s3_java_buffer_pool.c via JNI.
    private static native long nativeGetDirectBufferAddress(ByteBuffer dbb);
}
