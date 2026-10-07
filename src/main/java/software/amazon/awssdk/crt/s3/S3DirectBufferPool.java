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
 * All mutable block and dedicated-buffer state lives in the {@link Synced}
 * holder ({@code synced}), and every read or write of it happens inside
 * {@code synchronized (synced)}; helpers that require that are suffixed
 * {@code Locked}. The immutable configuration fields,
 * {@link #setNativePoolState}, and the volatile {@code closed} fast-path
 * checks do not need it. Native callers (reserve, ticket release, trim,
 * request finish) additionally hold the native pool state's
 * {@code pending_lock} around their JNI calls, so acquires and releases are
 * serialized and pending reservations cannot be stranded. Lock order:
 * native pending_lock, then {@code synced}. Nothing here calls into native
 * code that takes pending_lock.
 *
 * <p>The native wait queue is strict FIFO for anything that consumes
 * capacity, so a large waiting request holds back smaller ones behind it.
 * That is deadlock-free because only leases in use hold budget, and with
 * in-order part reservation those always complete.</p>
 *
 * <p>Growth backs a whole block with {@code allocateDirect} (which
 * zero-fills) on the reserving thread, usually a native event-loop
 * thread, while holding both locks, so other reserves and releases wait
 * for it.</p>
 *
 * <p>Lifetime: leases held by unclosed {@link S3BorrowedBuffer}s outlive
 * both the client and {@link #close()}; their memory is freed when the last
 * lease on it is released (or recovered by the buffer's GC fallback).</p>
 */
final class S3DirectBufferPool {

    /**
     * Part size used when {@link S3ClientOptions#withPartSize} is unset. Must
     * equal the native client's default ({@code g_default_part_size_fallback},
     * 8 MiB, in aws-c-s3); the native factory fails client creation if the
     * two ever differ.
     */
    static final int DEFAULT_PART_SIZE = (int) (8 * SizeUnits.MIB);

    /** Slots per block (the default native buffer pool uses 16 chunks per block). */
    static final int BLOCK_SLOTS = 16;

    /**
     * Largest contiguous run served from a block; larger requests get a
     * dedicated buffer (native: 4 chunks).
     */
    static final int MAX_GROUP_SLOTS = 4;

    /* tryAcquire results besides a lease handle (handles are >= 0). */
    static final long EXHAUSTED = -1;
    static final long IMPOSSIBLE = -2;

    /**
     * Share of {@code MaxDirectMemorySize} the pool's ceiling may use; the
     * rest is headroom for other users.
     */
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

    /** Largest slot run served from a block: {@value #MAX_GROUP_SLOTS}, or fewer in a smaller pool. */
    private final int maxGroupSlots;

    /**
     * Native pool state pointer, published by s3ClientNew once the native
     * client exists; 0 before then. S3Client passes it on each meta request
     * so the request's finish callback can drain the pool's wait queue.
     */
    private volatile long nativePoolState;

    /** One dedicated buffer. */
    private static final class Dedicated {
        final ByteBuffer buffer;
        final long address;

        Dedicated(ByteBuffer buffer, long address) {
            this.buffer = buffer;
            this.address = address;
        }
    }

    /** Mutable pool state. The holder is also the lock (see Concurrency in the class doc). */
    private static final class Synced {
        /**
         * Backing buffer per block; null until first needed (or after trim).
         * WARNING: must outlive every native read of a lease into it; anchored
         * by the native pool state's JNI global ref on the pool.
         */
        final ByteBuffer[] blocks;
        /** Cached native base address per block; 0 when unbacked. */
        final long[] blockAddresses;
        /** Per-block occupancy bitmask (bit i = slot i leased). */
        final int[] usedMask;
        /** Backed block bytes plus every dedicated buffer; never exceeds ceilingBytes. */
        long committedBytes;
        /** Leased dedicated buffers by handle; freed on release. */
        final Map<Long, Dedicated> leasedDedicated = new HashMap<>();
        long nextDedicatedId;

        Synced(int numBlocks) {
            blocks = new ByteBuffer[numBlocks];
            blockAddresses = new long[numBlocks];
            usedMask = new int[numBlocks];
        }
    }

    private final Synced synced;

    /**
     * Set by {@link #close()} while holding {@code synced}: acquires throw;
     * released memory is freed instead of kept. Volatile so the fast-path
     * reads outside the lock see it.
     */
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
        this.maxGroupSlots = Math.min(MAX_GROUP_SLOTS, maxSlots);
        this.synced = new Synced(numBlocks);

        // Eager floor. On partial OOM: free the blocks already allocated
        // right away (direct memory is scarce at exactly this point, so do
        // not leave them for GC), log, and rethrow. The pool is not yet
        // shared; the monitor is taken only to keep the Synced rule uniform.
        synchronized (synced) {
            try {
                for (int b = 0; b < floorBlocks; b++) {
                    backBlockLocked(b);
                }
            } catch (OutOfMemoryError e) {
                Log.log(Log.LogLevel.Warn, Log.LogSubject.JavaCrtS3,
                    "S3DirectBufferPool: OutOfMemoryError during eager allocation of " + floorBlocks
                  + " block(s) (up to " + BLOCK_SLOTS + " x " + partSize + " bytes each). "
                  + "Consider raising -XX:MaxDirectMemorySize or reducing pool size.");
                freeUnusedBlocksLocked(0);  // nothing is leased yet, so this frees every backed block
                throw e;
            }
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
     *                                  part, memoryLimitInBytes is negative
     *                                  or below a fixed or elastic ceiling,
     *                                  or partSize is negative or too large
     *                                  for the pool
     * @throws IllegalStateException    if the ceiling does not fit in 80% of
     *                                  {@code -XX:MaxDirectMemorySize}
     */
    static S3DirectBufferPool fromOptions(S3DirectBufferPoolOptions poolOptions, S3ClientOptions clientOptions) {
        int partSize = resolvePartSize(clientOptions);
        // From here on memoryLimitInBytes is 0 (unset) or positive. A negative
        // value would read as unset here but reach the native client as a
        // huge unsigned limit, so the two would size transfers differently.
        long explicitLimit = clientOptions.getMemoryLimitInBytes();
        if (explicitLimit < 0) {
            throw new IllegalArgumentException(
                "S3ClientOptions.memoryLimitInBytes (" + explicitLimit + ") must be > 0, or 0 to leave it unset, "
              + "when a direct buffer pool is enabled");
        }
        int initialSlots;
        int maxSlots;
        boolean ceilingClamped = false;
        switch (poolOptions.getMode()) {
            case FIXED: {
                // Floor == ceiling == memoryLimitBytes rounded down to whole
                // parts: fully eager, never grows or trims.
                long bytes = poolOptions.getMemoryLimitBytes();
                requireAtLeastOnePart("memoryLimitBytes", bytes, partSize);
                checkMemoryLimitCoversCeiling(clientOptions, bytes / partSize * partSize, partSize);
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
                checkMemoryLimitCoversCeiling(clientOptions, maxBytes / partSize * partSize, partSize);
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
                    memoryLimitBytes = parsePositiveEnvScaled("AWS_CRT_S3_MEMORY_LIMIT_IN_MB", SizeUnits.MIB);
                }
                if (memoryLimitBytes <= 0) {
                    memoryLimitBytes = parsePositiveEnvScaled("AWS_CRT_S3_MEMORY_LIMIT_IN_GIB", SizeUnits.GIB);
                }
                if (memoryLimitBytes <= 0) {
                    // Derived, not set by the customer: shrink it to fit the
                    // JVM's direct memory limit instead of failing. Explicit
                    // sizes are never changed; they fail fast below.
                    memoryLimitBytes = S3Client.defaultMemoryLimitForThroughput(clientOptions.getThroughputTargetGbps());
                    long maxDirectMemory = DirectMemoryLimit.BYTES;
                    long available = (long) (maxDirectMemory * DIRECT_MEMORY_FRACTION) / partSize * partSize;
                    if (maxDirectMemory > 0 && memoryLimitBytes > available && available >= partSize) {
                        // WARN: the pool is smaller than the client would normally
                        // use for this throughput, which can limit transfer speed.
                        long recommendedMiB = recommendedMaxDirectMemoryMiB(memoryLimitBytes);
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
     * Size check that runs before {@link #checkMemoryLimitCoversCeiling}, so a
     * size below one part reports that cause rather than a conflict with a
     * 0-byte ceiling.
     */
    private static void requireAtLeastOnePart(String name, long bytes, int partSize) {
        if (bytes < partSize) {
            throw new IllegalArgumentException(
                name + " (" + bytes + ") must be >= partSize (" + partSize + ")");
        }
    }

    /**
     * For fixed/elastic pools the pool ceiling is the client's memory. An
     * explicit memoryLimitInBytes is an upper bound: a ceiling at or below
     * it is within the limit, and {@link S3Client} then gives the native
     * client the ceiling, so its range sizing and part-size checks match
     * the memory that exists. A ceiling above it would use more than the
     * customer allowed, so refuse rather than shrink an explicit pool size.
     * The ceiling is a whole number of parts, so a fixed() or elastic()
     * size that is not a multiple of partSize rounds down; the message says
     * so, because the caller may have passed the same number to both
     * options.
     */
    private static void checkMemoryLimitCoversCeiling(S3ClientOptions clientOptions, long ceilingBytes,
                                                      int partSize) {
        long explicit = clientOptions.getMemoryLimitInBytes();
        if (explicit <= 0) {
            return;
        }
        if (explicit < ceilingBytes) {
            throw new IllegalArgumentException(
                "The direct buffer pool's ceiling (" + ceilingBytes + " bytes) exceeds "
              + "S3ClientOptions.memoryLimitInBytes (" + explicit + "). The ceiling is a whole number of parts ("
              + partSize + " bytes each), rounded down from the pool size. Raise memoryLimitInBytes to at least "
              + ceilingBytes + ", leave it unset (the pool ceiling is used), or pass a smaller size to the pool.");
        }
        if (explicit > ceilingBytes) {
            Log.log(Log.LogLevel.Debug, Log.LogSubject.JavaCrtS3,
                "S3DirectBufferPool: memoryLimitInBytes is " + explicit + " bytes; the client uses the pool's "
              + "ceiling of " + ceilingBytes + " bytes as its memory limit.");
        }
    }

    /**
     * The client's part size, or {@link #DEFAULT_PART_SIZE} when unset (0).
     * Must match the native client's resolution; the native factory fails
     * client creation if it does not. A negative value would
     * read as unset here but reach the native client as a huge unsigned part
     * size, so it is rejected with the actual cause instead.
     */
    private static int resolvePartSize(S3ClientOptions clientOptions) {
        long partSize = clientOptions.getPartSize();
        if (partSize < 0) {
            throw new IllegalArgumentException(
                "S3ClientOptions.partSize (" + partSize + ") must be > 0, or 0 to leave it unset, "
              + "when a direct buffer pool is enabled");
        }
        if (partSize == 0) {
            return DEFAULT_PART_SIZE;
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
    long maxGroupBytes() { return (long) maxGroupSlots * partSize; }

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

    /** @return the native pool state pointer, 0 until the native client is created */
    long nativePoolState() { return nativePoolState; }

    /** Called once by s3ClientNew after the native client (and its pool) is created. */
    void setNativePoolState(long state) { nativePoolState = state; }

    /**
     * Marks the pool closed and immediately frees every unused block.
     * Called by {@link S3Client} when its shutdown completes (and on
     * client-construction failure). Memory still leased by unclosed
     * {@link S3BorrowedBuffer}s is freed when released. Idempotent.
     */
    void close() {
        synchronized (synced) {
            closed = true;
            freeUnusedBlocksLocked(0);
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
     * @return a lease handle, {@link #EXHAUSTED}, or {@link #IMPOSSIBLE}
     * @throws IllegalStateException if the pool is closed
     */
    long tryAcquire(long size) {
        if (size <= 0) size = 1;
        long slotsNeeded = (size + partSize - 1) / partSize;
        synchronized (synced) {
            // Under the monitor, so no acquire can back memory after close().
            if (closed) throw new IllegalStateException("pool is closed");
            if (slotsNeeded <= maxGroupSlots) {
                return acquireRunLocked((int) slotsNeeded);
            }
            return acquireDedicatedLocked(size);
        }
    }

    long leaseAddress(long handle) {
        synchronized (synced) {
            if ((handle & DEDICATED_TAG) != 0) {
                Dedicated d = synced.leasedDedicated.get(handle);
                if (d == null) throw new IllegalStateException("leaseAddress: dedicated lease not held");
                return d.address;
            }
            int b = runBlock(handle);
            int mask = runMask(handle);
            if ((synced.usedMask[b] & mask) != mask) throw new IllegalStateException("leaseAddress: slot run not leased");
            return synced.blockAddresses[b] + (long) runStart(handle) * partSize;
        }
    }

    /** Returns a lease. Its memory may be reused immediately; any view of it is then unsafe to read. */
    void release(long handle) {
        synchronized (synced) {
            if ((handle & DEDICATED_TAG) != 0) {
                Dedicated d = synced.leasedDedicated.remove(handle);
                if (d == null) throw new IllegalStateException("release: dedicated lease not held");
                synced.committedBytes -= d.buffer.capacity();
                DirectBufferCleaner.free(d.buffer);
                return;
            }
            int b = runBlock(handle);
            int mask = runMask(handle);
            if ((synced.usedMask[b] & mask) != mask) throw new IllegalStateException("release: slot run not leased");
            synced.usedMask[b] &= ~mask;
            if (closed && synced.usedMask[b] == 0 && synced.blocks[b] != null) {
                unbackBlockLocked(b);
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
        synchronized (synced) {
            freeUnusedBlocksLocked(floorBlocks);
        }
    }

    /* ==================================================================== */
    /* Allocation internals (caller holds the synced monitor)               */
    /* ==================================================================== */

    /**
     * First fit: a free run in a backed block, else back a new block within
     * the ceiling (freeing an idle short last block if that is what makes one
     * fit).
     */
    private long acquireRunLocked(int count) {
        int want = (1 << count) - 1;
        for (int b = 0; b < numBlocks; b++) {
            if (synced.blocks[b] == null) {
                continue;
            }
            for (int start = 0; start + count <= blockSlots(b); start++) {
                if ((synced.usedMask[b] & (want << start)) == 0) {
                    return leaseRunLocked(b, start, count);
                }
            }
        }
        // Only the last block can be shorter than BLOCK_SLOTS. If it is
        // allocated, unused and too short for this run, its bytes may be all
        // that keeps a new block from fitting; free it only when that makes
        // one fit (like acquireDedicatedLocked's reclaim), so EXHAUSTED means
        // growth is impossible, not just blocked by an idle block.
        int last = numBlocks - 1;
        long shortIdleBytes = synced.blocks[last] != null && synced.usedMask[last] == 0 && blockSlots(last) < count
            ? synced.blocks[last].capacity() : 0;
        for (int b = 0; b < numBlocks; b++) {
            if (synced.blocks[b] == null && blockSlots(b) >= count) {
                long bytes = (long) blockSlots(b) * partSize;
                if (synced.committedBytes + bytes > ceilingBytes) {
                    if (synced.committedBytes - shortIdleBytes + bytes > ceilingBytes) {
                        continue;   // dedicated buffers hold the budget; a smaller (last) block may still fit
                    }
                    unbackBlockLocked(last);
                    shortIdleBytes = 0;
                }
                backBlockLocked(b);
                return leaseRunLocked(b, 0, count);
            }
        }
        return EXHAUSTED;
    }

    /** Allocates a dedicated buffer, freeing idle blocks only when that makes it fit. */
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
        long needed = synced.committedBytes + size - ceilingBytes;  // bytes to reclaim; <= 0 means it fits now
        if (needed > 0) {
            // Free blocks only if that makes the buffer fit: freeing them and
            // still returning EXHAUSTED would only force blocks to be backed
            // (and zero-filled) again later.
            long reclaimable = 0;
            for (int b = 0; b < numBlocks; b++) {
                if (synced.blocks[b] != null && synced.usedMask[b] == 0) {
                    reclaimable += synced.blocks[b].capacity();
                }
            }
            if (reclaimable < needed) {
                return EXHAUSTED;
            }
            // Make room by freeing fully unused blocks (highest first, floor included).
            for (int b = numBlocks - 1; b >= 0 && synced.committedBytes + size > ceilingBytes; b--) {
                if (synced.blocks[b] != null && synced.usedMask[b] == 0) {
                    unbackBlockLocked(b);
                }
            }
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
        synced.committedBytes += dbb.capacity();
        long handle = DEDICATED_TAG | synced.nextDedicatedId++;
        synced.leasedDedicated.put(handle, new Dedicated(dbb, nativeGetDirectBufferAddress(dbb)));
        return handle;
    }

    private int blockSlots(int b) {
        return Math.min(BLOCK_SLOTS, maxSlots - b * BLOCK_SLOTS);
    }

    /**
     * Marks a run of {@code count} slots from {@code start} in block {@code b}
     * as leased and returns its handle: {@code b} in bits 16 and up,
     * {@code start} in bits 8-15, {@code count} in bits 0-7.
     */
    private long leaseRunLocked(int b, int start, int count) {
        synced.usedMask[b] |= ((1 << count) - 1) << start;
        return ((long) b << 16) | ((long) start << 8) | count;
    }

    /** Block index of a slot-run handle. */
    private static int runBlock(long handle) { return (int) (handle >>> 16); }

    /** Index of the run's first slot within its block. */
    private static int runStart(long handle) { return (int) ((handle >>> 8) & 0xFF); }
    
    /**
     * Bits the run occupies in its block's {@code usedMask}: {@code count}
     * bits starting at the run's first slot.
     */
    private static int runMask(long handle) {
        int count = (int) (handle & 0xFF);
        return ((1 << count) - 1) << runStart(handle);
    }

    private void backBlockLocked(int b) {
        ByteBuffer dbb = ByteBuffer.allocateDirect(blockSlots(b) * partSize);
        synced.blocks[b] = dbb;
        synced.blockAddresses[b] = nativeGetDirectBufferAddress(dbb);
        synced.committedBytes += dbb.capacity();
    }

    /** Frees every backed block from {@code fromBlock} up that has no slot leased. */
    private void freeUnusedBlocksLocked(int fromBlock) {
        for (int b = fromBlock; b < numBlocks; b++) {
            if (synced.blocks[b] != null && synced.usedMask[b] == 0) {
                unbackBlockLocked(b);
            }
        }
    }

    /** Frees a fully unused block. Null before free so no stale address is ever handed out. */
    private void unbackBlockLocked(int b) {
        ByteBuffer dbb = synced.blocks[b];
        synced.blocks[b] = null;
        synced.blockAddresses[b] = 0L;
        synced.committedBytes -= dbb.capacity();
        DirectBufferCleaner.free(dbb);
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
     *                               {@code MaxDirectMemorySize}, including
     *                               an explicit {@code MaxDirectMemorySize=0}
     */
    private static void validateDirectMemoryCapacity(long poolCapacityBytes) {
        long maxDirectMemory = DirectMemoryLimit.BYTES;
        // Logged here, not in DirectMemoryLimit's initializer: a failure during
        // class initialization would break that class for the life of the JVM.
        Log.log(Log.LogLevel.Debug, Log.LogSubject.JavaCrtS3,
            "S3DirectBufferPool: MaxDirectMemorySize = " + maxDirectMemory + " bytes (from "
          + DirectMemoryLimit.SOURCE + ")");
        if (maxDirectMemory == 0) {
            // An explicit -XX:MaxDirectMemorySize=0 means no direct memory at
            // all (leaving the flag off is the JVM default), so every
            // allocation would fail with OutOfMemoryError.
            long recommendedMiB = recommendedMaxDirectMemoryMiB(poolCapacityBytes);
            throw new IllegalStateException(
                "S3DirectBufferPool requires " + mibRoundedUp(poolCapacityBytes) + " MiB of direct memory, "
              + "but -XX:MaxDirectMemorySize=0 allows none. Remove the flag to use the JVM default, "
              + "or set -XX:MaxDirectMemorySize=" + recommendedMiB + "m or higher.");
        }
        if (maxDirectMemory < 0) {
            long poolMiB = mibRoundedUp(poolCapacityBytes);
            if (isAndroid()) {
                // Android has no MaxDirectMemorySize: direct buffers come from
                // native memory, bounded only by the device and the app's
                // memory limits, so there is nothing to check against.
                Log.log(Log.LogLevel.Warn, Log.LogSubject.JavaCrtS3,
                    "S3DirectBufferPool: Android has no JVM direct memory limit, so the pool's " + poolMiB
                  + " MiB ceiling is not checked; it comes from native memory. Size the pool for the device "
                  + "(S3DirectBufferPoolOptions.fixed or elastic, or S3ClientOptions.withMemoryLimitInBytes).");
                return;
            }
            // Unable to determine the limit (non-HotSpot JVM or reflective
            // access denied). Log a warning but don't block construction.
            Log.log(Log.LogLevel.Warn, Log.LogSubject.JavaCrtS3,
                "S3DirectBufferPool: unable to determine MaxDirectMemorySize. "
              + "Pool requires " + poolMiB + " MiB of direct memory. "
              + "Ensure -XX:MaxDirectMemorySize is set appropriately.");
            return;
        }

        // Reserve 20% of MaxDirectMemorySize for other direct buffer users.
        long availableForPool = (long) (maxDirectMemory * DIRECT_MEMORY_FRACTION);

        if (poolCapacityBytes > availableForPool) {
            long poolMiB = mibRoundedUp(poolCapacityBytes);
            long maxMiB = maxDirectMemory / SizeUnits.MIB;
            long recommendedMiB = recommendedMaxDirectMemoryMiB(poolCapacityBytes);
            throw new IllegalStateException(
                "S3DirectBufferPool requires " + poolMiB + " MiB of direct memory, "
              + "but MaxDirectMemorySize is " + maxMiB + " MiB "
              + "(80% usable = " + (availableForPool / SizeUnits.MIB) + " MiB). "
              + "Either set -XX:MaxDirectMemorySize=" + recommendedMiB + "m, "
              + "or lower the pool's ceiling: S3ClientOptions.withMemoryLimitInBytes with auto(), "
              + "or a smaller size passed to fixed() or elastic().");
        }
    }

    /** Android's runtimes (Dalvik and ART) both report {@code java.vm.name} as "Dalvik". */
    private static boolean isAndroid() {
        try {
            return "Dalvik".equals(System.getProperty("java.vm.name"));
        } catch (SecurityException e) {
            return false;
        }
    }

    /**
     * Whole MiB, rounded up, for messages about memory a pool needs. Limits
     * are shown rounded down, so a pool that does not fit never prints a
     * requirement equal to or below the limit.
     */
    private static long mibRoundedUp(long bytes) {
        return (bytes + SizeUnits.MIB - 1) / SizeUnits.MIB;
    }

    /**
     * Smallest {@code -XX:MaxDirectMemorySize}, in whole MiB, whose 80% share
     * fits {@code poolBytes}. Rounds up, so following it always passes
     * {@link #validateDirectMemoryCapacity}.
     */
    private static long recommendedMaxDirectMemoryMiB(long poolBytes) {
        return (long) Math.ceil(poolBytes / DIRECT_MEMORY_FRACTION / SizeUnits.MIB);
    }

    /**
     * The JVM's {@code MaxDirectMemorySize}. A lazy holder: the limit is fixed
     * for the life of the JVM, so it is probed on first use and cached.
     */
    private static final class DirectMemoryLimit {
        /**
         * Why the HotSpotDiagnosticMXBean probe failed on a HotSpot-based
         * JVM, or null. Set during probe().
         */
        private static String hotSpotFailure;
        /** Which probe answered, for the DEBUG log. Set by probe(). */
        private static String source;

        /**
         * The limit in bytes, or -1 if it cannot be determined. 0 means an
         * explicit {@code -XX:MaxDirectMemorySize=0} (no direct memory), not
         * "unset".
         */
        static final long BYTES = probe();
        /** Which probe answered (plus any HotSpot fallback note), for the DEBUG log. */
        static final String SOURCE = source;

        /**
         * Tries each source in order, most exact first, and returns the first
         * answer. Every probe is reflective, and any failure (class missing,
         * module not exported, security manager, unknown option) falls through
         * to the next one.
         */
        private static long probe() {
            if (isAndroid()) {
                source = "Android, which has no JVM limit";
                return -1;  // none of the probes exist there
            }
            // Java 8: sun.misc.VM. Java 9+, only with --add-exports:
            // jdk.internal.misc.VM. Both are exact, as enforced by allocateDirect.
            for (String vmClass : new String[] {"sun.misc.VM", "jdk.internal.misc.VM"}) {
                long bytes = fromVmClass(vmClass);
                if (bytes >= 0) {
                    source = vmClass;
                    return bytes;
                }
            }
            long bytes = fromHotSpotDiagnostic();  // HotSpot Java 9+: the usual path
            if (bytes >= 0) {
                source = "HotSpotDiagnosticMXBean";
                return bytes;
            }
            bytes = fromInputArguments();  // other JVMs
            source = (bytes >= 0 ? "JVM input arguments" : "no probe succeeded")
                + (hotSpotFailure == null ? "" : "; HotSpotDiagnosticMXBean failed with " + hotSpotFailure
                    + ", so a value set in an options file (-XX:Flags) is missed");
            return bytes;
        }

        /**
         * HotSpot-based JVMs (Oracle, and OpenJDK builds such as Corretto and
         * Temurin) are expected to have the bean.
         */
        private static boolean isHotSpotBased() {
            try {
                String vm = String.valueOf(System.getProperty("java.vm.name"));
                return vm.contains("HotSpot") || vm.contains("OpenJDK");
            } catch (SecurityException e) {
                return false;
            }
        }

        /** {@code maxDirectMemory()} on a JDK-internal VM class, or -1. */
        private static long fromVmClass(String className) {
            try {
                Object value = Class.forName(className).getDeclaredMethod("maxDirectMemory").invoke(null);
                return (Long) value;
            } catch (Exception | LinkageError e) {
                return -1;
            }
        }

        /**
         * The flag as HotSpot resolved it, from every source (command line,
         * JAVA_TOOL_OPTIONS, JDK_JAVA_OPTIONS, an options file), or -1. An
         * unset flag has origin DEFAULT and value 0; HotSpot then uses
         * {@code Runtime.maxMemory()}. Reflective because
         * {@code com.sun.management} is HotSpot-specific and
         * {@code java.lang.management} does not exist on Android.
         */
        private static long fromHotSpotDiagnostic() {
            try {
                Class<?> beanInterface = Class.forName("com.sun.management.HotSpotDiagnosticMXBean");
                Object bean = Class.forName("java.lang.management.ManagementFactory")
                    .getMethod("getPlatformMXBean", Class.class)
                    .invoke(null, beanInterface);
                if (bean == null) {
                    if (isHotSpotBased()) {
                        hotSpotFailure = "no platform HotSpotDiagnosticMXBean";
                    }
                    return -1;
                }
                Object option = beanInterface.getMethod("getVMOption", String.class)
                    .invoke(bean, "MaxDirectMemorySize");
                Class<?> optionClass = Class.forName("com.sun.management.VMOption");
                String origin = String.valueOf(optionClass.getMethod("getOrigin").invoke(option));
                if ("DEFAULT".equals(origin)) {
                    return Runtime.getRuntime().maxMemory();
                }
                // An explicit 0 means no direct memory, as the JVM reads it.
                return Long.parseLong(String.valueOf(optionClass.getMethod("getValue").invoke(option)));
            } catch (Exception | LinkageError e) {
                if (isHotSpotBased()) {
                    hotSpotFailure = e.getClass().getName();
                }
                return -1;
            }
        }

        /**
         * Parses an explicit {@code -XX:MaxDirectMemorySize} from the JVM's
         * input arguments, else {@code Runtime.maxMemory()} (HotSpot's
         * default), or -1. Misses a value set in an options file, which is
         * why it runs last.
         */
        private static long fromInputArguments() {
            try {
                Object runtimeMxBean = Class.forName("java.lang.management.ManagementFactory")
                    .getMethod("getRuntimeMXBean").invoke(null);
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
                            multiplier = 1024L * SizeUnits.GIB;
                            val = val.substring(0, val.length() - 1);
                        } else if (val.endsWith("g")) {
                            multiplier = SizeUnits.GIB;
                            val = val.substring(0, val.length() - 1);
                        } else if (val.endsWith("m")) {
                            multiplier = SizeUnits.MIB;
                            val = val.substring(0, val.length() - 1);
                        } else if (val.endsWith("k")) {
                            multiplier = SizeUnits.KIB;
                            val = val.substring(0, val.length() - 1);
                        }
                        // An explicit 0 means no direct memory, as the JVM reads it.
                        // multiplyExact: an absurd value overflows to -1 (unknown)
                        // instead of wrapping to a bogus limit.
                        return Math.multiplyExact(Long.parseLong(val), multiplier);
                    }
                }
                return Runtime.getRuntime().maxMemory();
            } catch (Exception | LinkageError e) {
                return -1;
            }
        }
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
