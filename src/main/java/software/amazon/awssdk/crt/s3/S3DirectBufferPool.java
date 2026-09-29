/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.s3;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import software.amazon.awssdk.crt.Log;

/**
 * Internal. A Java-owned pool of {@link ByteBuffer#allocateDirect direct
 * ByteBuffer} slots used as the destination memory for {@code aws-c-s3}
 * response bodies. Customers configure it through
 * {@link S3DirectBufferPoolOptions}; {@link S3Client} creates one pool per
 * client at construction ({@link #fromOptions}) and closes it when its
 * shutdown completes. The pool is never shared between clients: each
 * client's native pool state keeps its own pending-reserve list, and a
 * slot released by one client could never wake a reservation pended by
 * another.
 *
 * <p>Each slot holds one part, so slot size is the client's resolved part
 * size. The native factory re-checks equality at client creation as a
 * guard against Java and native part-size resolution diverging.</p>
 *
 * <p>Thread safety: all methods are safe for concurrent invocation.
 * {@code tryAcquireSlot} and {@code releaseSlot} are non-blocking
 * (they run on aws-c-s3 event-loop threads); exhaustion backpressure
 * is handled natively.</p>
 *
 * <p>Lifetime: slots leased by unclosed {@link S3BorrowedBuffer}s outlive
 * both the client and {@link #close()}; each is freed when its buffer is
 * closed (or recovered by the buffer's GC fallback).</p>
 */
final class S3DirectBufferPool {

    /**
     * Direct buffers, sized to maxSlots. [0, nextGrowthIndex) are backed;
     * the rest are null until lazy growth (or nulled again by trim).
     * WARNING: must outlive every native read, anchored by the
     * native pool state's JNI global ref on this object.
     */
    private final ByteBuffer[] slots;

    /**
     * Cached native addresses, one per allocated slot; 0L when
     * unallocated/trimmed. Direct memory is not relocated by GC.
     */
    private final long[] slotAddresses;

    /** Index queue of free slots; acquire = take, release = offer. */
    private final BlockingQueue<Integer> freeIndices;

    private final int partSize;
    private final int initialSlots;
    private final int maxSlots;

    /**
     * The next slot index to back during lazy growth; equivalently, the
     * high-water mark of slot indices ever backed (eager + lazy-grown).
     * Monotonically increases up to {@code maxSlots} and never shrinks,
     * even on trim: trim unbacks memory but leaves the index in
     * circulation, so decrementing this cursor would let growth re-issue
     * an index still in {@code freeIndices}, a double-lease.
     * Guarded by {@code growthLock} on writes.
     */
    private int nextGrowthIndex;

    /**
     * Guards all writes to {@code slots[]} and {@code slotAddresses[]}.
     * Serializes lazy growth, re-backing of trimmed slots, {@link #trim()},
     * the {@link #close()} sweep, and the closed-pool branch of
     * {@link #releaseSlot(int)}.
     */
    private final Object growthLock = new Object();

    /**
     * Set by {@link #close()}. Once set, acquires throw, {@link #trim()}
     * skips, and {@link #releaseSlot(int)} frees slots instead of
     * re-queueing them.
     */
    private volatile boolean closed;

    /**
     * Private: {@link S3Client} builds pools via {@link #fromOptions}.
     *
     * @throws IllegalArgumentException for invalid sizes
     */
    private S3DirectBufferPool(int partSize, int initialSlots, int maxSlots) {
        if (partSize <= 0)       throw new IllegalArgumentException("partSize must be > 0");
        if (initialSlots < 0)    throw new IllegalArgumentException("initialSlots must be >= 0");
        if (maxSlots < 1)        throw new IllegalArgumentException("maxSlots must be >= 1");
        if (initialSlots > maxSlots) {
            throw new IllegalArgumentException(
                "initialSlots (" + initialSlots + ") must be <= maxSlots (" + maxSlots + ")");
        }

        this.partSize      = partSize;
        this.initialSlots  = initialSlots;
        this.maxSlots      = maxSlots;
        this.slots         = new ByteBuffer[maxSlots];
        this.slotAddresses = new long[maxSlots];
        this.freeIndices   = new LinkedBlockingQueue<>(maxSlots);
        this.nextGrowthIndex = 0;

        // Eagerly pre-allocate the first `initialSlots` direct buffers.
        // Remaining slots are allocated on demand in tryAcquireSlot().
        // On partial OOM: null allocated refs so GC can reclaim off-heap
        // memory promptly, log, and rethrow.
        try {
            for (int i = 0; i < initialSlots; i++) {
                // slots[i] holds the buffer, keeping its off-heap memory
                // (and the cached address below) alive.
                ByteBuffer dbb = ByteBuffer.allocateDirect(partSize);
                slots[i] = dbb;

                // Cache the native address so JNI avoids a call per part.
                slotAddresses[i] = nativeGetDirectBufferAddress(dbb);

                freeIndices.add(i);
                nextGrowthIndex++;
            }
        } catch (OutOfMemoryError e) {
            Log.log(Log.LogLevel.Warn, Log.LogSubject.JavaCrtS3,
                "S3DirectBufferPool: OutOfMemoryError during eager allocation "
              + "at slot " + nextGrowthIndex + " of " + initialSlots
              + " (partSize=" + partSize + " bytes). "
              + "Releasing partial allocation. Consider raising "
              + "-XX:MaxDirectMemorySize or reducing pool size.");
            // Null refs so Cleaner can reclaim off-heap memory promptly.
            for (int j = 0; j < nextGrowthIndex; j++) {
                slots[j] = null;
                slotAddresses[j] = 0L;
            }
            freeIndices.clear();
            nextGrowthIndex = 0;
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
                return createFixed(poolOptions.getMemoryLimitBytes(), partSize);
            case ELASTIC:
                return createElastic(poolOptions.getInitialSlots(), poolOptions.getMaxSlots(), partSize);
            case AUTO:
            default:
                return createAuto(clientOptions, partSize);
        }
    }

    /**
     * The client's part size, or aws-c-s3's 8 MiB default when unset.
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
     * 8 slots, or the ceiling if smaller.
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
        int initialSlots = Math.min(8, maxSlots);  // small warm floor
        validateDirectMemoryCapacity((long) maxSlots * partSize);
        return new S3DirectBufferPool(partSize, initialSlots, maxSlots);
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
        return new S3DirectBufferPool(partSize, slotCount, slotCount);
    }

    /** Caller-chosen floor and ceiling (validated by the options factory and the constructor). */
    private static S3DirectBufferPool createElastic(int initialSlots, int maxSlots, int partSize) {
        validateDirectMemoryCapacity((long) maxSlots * partSize);
        return new S3DirectBufferPool(partSize, initialSlots, maxSlots);
    }

    /* ==================================================================== */
    /* Accessors + lifecycle                                                */
    /* ==================================================================== */

    /** @return the per-slot byte size (the client's resolved part size) */
    int partSize()       { return partSize; }
    /** @return the pool ceiling, the maximum number of slots the pool may grow to */
    int maxSlots()       { return maxSlots; }
    /**
     * Peak number of slots ever backed (eager + lazy-grown). For
     * diagnostics and tests. Some of these slots may currently be
     * trimmed (unbacked); they are lazily re-backed on next acquire.
     *
     * @return the peak number of slots ever allocated
     */
    int peakAllocatedSlots() {
        synchronized (growthLock) { return nextGrowthIndex; }
    }

    /**
     * Marks the pool closed and immediately frees every UNUSED slot's
     * memory. Called by {@link S3Client} when its shutdown completes (and
     * on client-construction failure), when no meta request can acquire
     * a slot any more.
     *
     * <p>Slots still leased by unclosed {@link S3BorrowedBuffer}s are NOT
     * freed here (that would be a use-after-free under the holder); each
     * one is freed when its buffer is closed, including one released
     * concurrently with this call (see {@link #releaseSlot}). Idempotent.</p>
     */
    void close() {
        closed = true;
        freeQueuedSlots();
    }

    /**
     * Frees every slot currently in the free queue. Safe: a queued index is
     * by definition unleased (no native ticket caches its address), and
     * polling under growthLock partitions each index to exactly one party.
     * A racing acquirer that wins a poll keeps a valid slot we never see.
     * Leased slots are not in the queue; they are freed in releaseSlot()
     * when their borrowed buffers close (closed == true branch). No
     * re-offer: only called once the pool is closed for good.
     */
    private void freeQueuedSlots() {
        synchronized (growthLock) {
            Integer idx;
            while ((idx = freeIndices.poll()) != null) {
                int i = idx;
                ByteBuffer dbb = slots[i];
                if (dbb == null) {
                    continue;   // already trimmed
                }
                slots[i] = null;
                slotAddresses[i] = 0L;
                DirectBufferCleaner.free(dbb);
            }
        }
    }

    /* ==================================================================== */
    /* Package-private JNI back-call surface                                */
    /* ==================================================================== */

    /*
     * Invoked FROM s3_java_buffer_pool.c via JNI. Signatures must remain
     * stable; the method IDs are cached in java_class_ids.c.
     */

    /**
     * Non-blocking acquire. Returns a free slot index if one is
     * immediately available, OR grows a new slot if the pool has
     * not yet reached {@code maxSlots}. Returns {@code -1} if the
     * pool is fully allocated AND every slot is currently leased.
     *
     * <p><b>CRITICAL:</b> MUST NOT block. Runs on aws-c-s3 event-loop
     * threads; blocking stalls all I/O on that loop. On -1 the native
     * side pends its future ({@code s_java_pool_reserve}).</p>
     *
     * <h3>Three stages</h3>
     * <ol>
     *   <li>Fast path: poll a free index (re-backing it if trim freed it).</li>
     *   <li>Lazy growth: allocateDirect under {@code growthLock} while below
     *       {@code maxSlots}.</li>
     *   <li>Exhausted: return -1; native side pends its future.</li>
     * </ol>
     */
    int tryAcquireSlot() {
        if (closed) throw new IllegalStateException("pool is closed");

        // Fast path: existing free slot in the queue.
        Integer idx = freeIndices.poll();
        if (idx != null) {
            // Re-back the slot if trim() freed it. The outer check is a
            // racy fast-path read; the re-check under growthLock (the only
            // writer to slots[i]) is definitive, and trim's null write is
            // published to us via the queue's lock (drain-and-reoffer).
            if (slots[idx] == null) {
                synchronized (growthLock) {
                    if (slots[idx] == null) {
                        ByteBuffer dbb;
                        try {
                            dbb = ByteBuffer.allocateDirect(partSize);
                        } catch (OutOfMemoryError e) {
                            // Return the index to the queue so it
                            // isn't lost. On next attempt the same
                            // race applies but the reallocation may
                            // succeed after some other DBB is
                            // released elsewhere in the JVM.
                            freeIndices.offer(idx);
                            Log.log(Log.LogLevel.Warn, Log.LogSubject.JavaCrtS3,
                                "S3DirectBufferPool: OutOfMemoryError re-allocating "
                              + "trimmed slot " + idx + " (partSize=" + partSize + " bytes). "
                              + "Consider raising -XX:MaxDirectMemorySize.");
                            throw e;
                        }
                        slots[idx] = dbb;
                        slotAddresses[idx] = nativeGetDirectBufferAddress(dbb);
                    }
                }
            }
            return idx;
        }

        // Slow path: try to grow under lock. Still non-blocking.
        synchronized (growthLock) {
            // Under lock: only grow if we haven't hit the ceiling.
            // A concurrent grower may have raised nextGrowthIndex to
            // maxSlots between the fast-path poll and this lock.
            if (nextGrowthIndex < maxSlots) {
                int newIdx = nextGrowthIndex;
                // OutOfMemoryError propagates (the customer must size
                // -XX:MaxDirectMemorySize for maxSlots, see
                // S3DirectBufferPoolOptions.elastic); WARN first for operator context.
                ByteBuffer dbb;
                try {
                    dbb = ByteBuffer.allocateDirect(partSize);
                } catch (OutOfMemoryError e) {
                    Log.log(Log.LogLevel.Warn, Log.LogSubject.JavaCrtS3,
                        "S3DirectBufferPool: OutOfMemoryError during lazy growth "
                      + "at slot " + newIdx + " of " + maxSlots
                      + " (partSize=" + partSize + " bytes). "
                      + "Consider raising -XX:MaxDirectMemorySize or reducing maxSlots.");
                    throw e;
                }
                slots[newIdx] = dbb;
                slotAddresses[newIdx] = nativeGetDirectBufferAddress(dbb);
                nextGrowthIndex++;
                return newIdx;
            }
        }

        // Pool fully allocated AND all slots leased. Return sentinel.
        // The native side MUST pend its future on the C-side
        // pending_reserves list. NEVER block this thread.
        return -1;
    }

    /**
     * Release direct memory backing every currently-free slot with
     * index {@code >= initialSlots}, matching the native pool's
     * {@code aws_s3_default_buffer_pool_trim} timing and semantics
     * as closely as JVM idioms allow.
     *
     * <p>Invoked from the native {@code s_java_pool_trim} vtable
     * function, which is scheduled by aws-c-s3's client with the
     * same idleness gating as the native pool (5-second delay,
     * skipped if {@code num_requests_in_flight > 0} at either
     * schedule or execution time). See
     * {@code s_s3_client_schedule_buffer_pool_trim_synced} in
     * {@code aws-c-s3/source/s3_client.c}.</p>
     *
     * <h3>Mechanism</h3>
     * For each currently-free slot at index {@code >= initialSlots}:
     * <ol>
     *   <li>Nulls {@code slots[idx]} and {@code slotAddresses[idx]}
     *       under {@code growthLock}, so a concurrent
     *       {@code tryAcquireSlot} sees a consistent null state and
     *       falls into the reallocation path.</li>
     *   <li>Invokes {@link DirectBufferCleaner#free} to release native
     *       memory synchronously (see {@link DirectBufferCleaner});
     *       otherwise direct-memory accounting lags until GC and
     *       re-warm can hit {@code OutOfMemoryError}.</li>
     *   <li>Leaves the slot index in {@code freeIndices}.
     *       {@code tryAcquireSlot} detects the null {@code slots[i]}
     *       and reallocates under {@code growthLock} on next re-use.</li>
     * </ol>
     *
     * <p>Slots below {@code initialSlots} are never trimmed (warm floor).
     * Leased slots (not in {@code freeIndices}) are never trimmed
     * regardless of index.</p>
     *
     * <h3>Concurrency: drain-and-reoffer</h3>
     * <p>Trim {@code poll()}s every index out, frees candidates, and
     * {@code offer()}s all back. Queue removal is atomic, so trim and
     * concurrent acquirers can never hold the same index, and the queue's
     * lock publishes the {@code slots[i] = null} write. In-place iteration
     * has neither guarantee (weakly consistent iterator). Today trim and
     * the only Java-reachable reserve path are also serialized on the
     * client's process-work event loop, but aws-c-s3's async-write reserve
     * path runs on the caller's thread. Do NOT weaken this back to
     * in-place iteration.</p>
     */
    void trim() {
        if (closed) return;

        // growthLock serializes against tryAcquireSlot's slot writes.
        // A concurrent releaseSlot (e.g. S3BorrowedBuffer.close) only
        // offers indices; those just miss this pass.
        synchronized (growthLock) {
            // Drain the whole queue. Drained indices are invisible to
            // concurrent acquirers for the duration of this pass.
            java.util.ArrayList<Integer> drained = new java.util.ArrayList<>();
            Integer idx;
            while ((idx = freeIndices.poll()) != null) {
                drained.add(idx);
            }

            for (Integer boxed : drained) {
                int i = boxed;
                if (i < initialSlots) {
                    continue;   // preserve the warm floor
                }
                ByteBuffer dbb = slots[i];
                if (dbb == null) {
                    continue;   // already trimmed on a prior cycle
                }

                // Null BEFORE freeing; published to future acquirers via
                // the re-offer below.
                slots[i] = null;
                slotAddresses[i] = 0L;

                // Synchronous native-memory release (see DirectBufferCleaner).
                // Safe: the index is out of the queue, so no acquirer races us.
                DirectBufferCleaner.free(dbb);
            }

            // Re-offer every drained index; trim leaves queue contents
            // unchanged. Cannot fail (capacity == maxSlots), but check anyway.
            for (Integer boxed : drained) {
                if (!freeIndices.offer(boxed)) {
                    // Cannot happen: capacity == maxSlots and every
                    // index is unique. Log rather than throw. Trim is
                    // fire-and-forget and must never kill the caller.
                    Log.log(Log.LogLevel.Error, Log.LogSubject.JavaCrtS3,
                        "S3DirectBufferPool.trim: failed to re-offer slot index "
                      + boxed + " to the free queue. Slot is lost from circulation");
                }
            }
        }
    }

    /**
     * Return a slot to the free pool, or free it immediately when the
     * pool is closed.
     *
     * <p>After this returns, the slot MAY be re-issued to a subsequent
     * {@code tryAcquireSlot()} call and its bytes overwritten. Any
     * outstanding view is UNSAFE to read. When the pool is closed,
     * the slot is freed instead of queued for reuse that can never
     * happen.</p>
     */
    void releaseSlot(int slotIndex) {
        validateIndex(slotIndex);
        if (closed) {
            // Late release: a borrowed buffer outlived pool.close(). The
            // native ticket has fully released, so nothing references the
            // slot's address anymore. Free it now instead of queueing it
            // for reuse that can never happen. Null-tolerant (idempotent).
            synchronized (growthLock) {
                ByteBuffer dbb = slots[slotIndex];
                if (dbb != null) {
                    slots[slotIndex] = null;
                    slotAddresses[slotIndex] = 0L;
                    DirectBufferCleaner.free(dbb);
                }
            }
            return;
        }
        // Defensive check: the JNI caller should only release indices
        // that were returned by tryAcquireSlot(). Guards the
        // [nextGrowthIndex, maxSlots) gap during lazy growth. A bug
        // here would otherwise be a silent NPE deep in the call.
        ByteBuffer slot = slots[slotIndex];
        if (slot == null) {
            throw new IllegalStateException(
                "releaseSlot(" + slotIndex + "): slot has not been allocated");
        }
        // clear() resets position=0 and limit=capacity; the bytes are
        // NOT zeroed (waste of cycles since they will be overwritten).
        slot.clear();
        freeIndices.offer(slotIndex);
        // Close race: if close() ran between the closed check above and the
        // offer, its sweep may have drained the queue before our index
        // arrived. A sweep that missed our index read the queue's volatile
        // count before our offer updated it, and close() set `closed` before
        // sweeping, so this volatile re-read is guaranteed to see true. Sweep
        // again so the slot is freed instead of stranded in a closed pool.
        if (closed) {
            freeQueuedSlots();
        }
    }

    /*
     * No Java-side sliceView: views are built in C with
     * NewDirectByteBuffer over the slot address, avoiding a JNI
     * round-trip per part. They share slot memory and have no
     * Cleaner (the slot's buffer owns the memory).
     */

    long slotAddress(int slotIndex) {
        validateIndex(slotIndex);
        // Defensive check: silent 0L would flow to native as a NULL
        // pointer and crash on memcpy far from the source of the bug.
        // The JNI caller should only query addresses for indices
        // returned by tryAcquireSlot().
        long addr = slotAddresses[slotIndex];
        if (addr == 0L) {
            throw new IllegalStateException(
                "slotAddress(" + slotIndex + "): slot has not been allocated");
        }
        return addr;
    }

    /* ==================================================================== */
    /* Internal helpers                                                     */
    /* ==================================================================== */

    private void validateIndex(int idx) {
        // Accept [0, maxSlots). JNI only passes indices it got from tryAcquireSlot.
        if (idx < 0 || idx >= maxSlots) {
            throw new IllegalArgumentException("invalid slot index: " + idx);
        }
    }

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
              + "S3DirectBufferPoolOptions.elastic(initialSlots, maxSlots) "
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
