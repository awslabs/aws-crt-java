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
 * A Java-owned pool of pre-allocated {@link ByteBuffer#allocateDirect
 * direct ByteBuffer} slots used as the destination memory for
 * {@code aws-c-s3} response bodies on the CRT-based S3 client.
 *
 * <h2>Opt-in</h2>
 * Attach a pool to a {@link S3Client} via
 * {@link S3ClientOptions#withDirectByteBufferPool(S3DirectBufferPool)}.
 * Without that call, the client uses the default native buffer pool
 * and the historical {@code byte[]}-delivery path is unchanged.
 *
 * <h2>Lifetime contract (read this carefully)</h2>
 * When this pool is active, the {@link ByteBuffer} delivered to
 * {@link S3MetaRequestResponseHandler#onResponseBody(ByteBuffer, long, long)}
 * is a <em>slice</em> of a pooled direct buffer. The slice's bytes are
 * valid <strong>only during the call</strong>. After the SDK / consumer
 * acknowledges receipt (via the read-window backpressure signal), the
 * pool may recycle the slot and overwrite the bytes with content from
 * a subsequent request.
 *
 * <p>If your handler needs to retain the bytes past the call, copy them
 * out explicitly inside the call:</p>
 * <pre>
 *   public int onResponseBody(ByteBuffer buf, long start, long end) {
 *       byte[] copy = new byte[buf.remaining()];
 *       buf.get(copy);                  // explicit copy out
 *       myStash.add(copy);              // safe to retain indefinitely
 *       return 0;
 *   }
 * </pre>
 *
 * <h2>Sizing</h2>
 * The pool supports three sizing modes:
 *
 * <h3>Auto-scaled default</h3>
 * {@code S3DirectBufferPool.create(clientOptions)} — reads the
 * client's {@code throughputTargetGbps} and sizes the pool using the
 * same auto-scaling formula as {@code aws_s3_default_buffer_pool}
 * (2-24 GiB). Reads the {@code AWS_CRT_S3_MEMORY_LIMIT_IN_GIB} env
 * var if set. Preserves the current native pool's default footprint
 * with JVM visibility added. Recommended when the operator does not
 * have specific sizing requirements.
 *
 * <h3>Fixed</h3>
 * {@code S3DirectBufferPool.createFixed(memoryLimitBytes, partSize)} — all
 * slots are pre-allocated at construction. Memory budget is fixed at
 * {@code slotCount × partSize} and never changes. Hard-capped: no
 * forced-buffer overrun; a spike beyond capacity throws
 * {@code OutOfMemoryError: Direct buffer memory} at
 * {@code tryAcquireSlot}. Suggested values: {@code partSize = 8 MiB},
 * {@code slotCount = 32} (256 MiB total). Recommended for workloads
 * with predictable throughput and tight container-memory budgets.
 *
 * <h3>Elastic (lazy growth)</h3>
 * {@code S3DirectBufferPool.createElastic(initialSlots, maxSlots, partSize)}
 * — {@code initialSlots} are pre-allocated; further slots are allocated
 * lazily on first use up to {@code maxSlots}. Memory budget tracks
 * actual demand, bounded above by {@code maxSlots × partSize}. Mirrors
 * the {@code aws-c-s3} default pool's lazy-allocation-within-ceiling
 * semantics. Recommended for workloads with variable throughput.
 *
 * <p><b>IMPORTANT:</b> when sizing {@code -XX:MaxDirectMemorySize},
 * always provision against {@code maxSlots × partSize} (the ceiling),
 * not {@code initialSlots × partSize} (the steady-state floor).
 * Otherwise growth under spike will throw
 * {@code OutOfMemoryError: Direct buffer memory}.</p>
 *
 * <p>First-use latency cost of growing a slot under load is ~50–100 µs
 * (one {@code ByteBuffer.allocateDirect(partSize)} call plus address
 * caching). This cost is paid only on the {@code tryAcquireSlot()} call
 * that triggers growth, and happens under the growth lock.</p>
 *
 * <h2>Thread safety</h2>
 * All package-private methods are safe for concurrent invocation.
 * {@code tryAcquireSlot} and {@code releaseSlot} are non-blocking —
 * pool-exhaustion backpressure is implemented on the native side via
 * a pending-reserve-future queue, NOT by blocking the caller (which
 * runs on an aws-c-s3 event-loop thread).
 */
public final class S3DirectBufferPool implements AutoCloseable {

    /**
     * Strong references to the pre-allocated direct buffers.
     *
     * <p>The array is sized to {@code maxSlots} at construction.
     * Indices {@code [0, allocatedSlots)} hold live
     * {@link ByteBuffer#allocateDirect direct buffers}; indices
     * {@code [allocatedSlots, maxSlots)} hold {@code null} until
     * lazy growth fills them.</p>
     *
     * <p><b>WARNING:</b> these references MUST outlive every native
     * read that the pool has handed out. If the array is cleared
     * or the pool is GC'd while the receive thread is mid-write,
     * we get a use-after-free at the JNI boundary. The lifetime
     * is anchored by the {@link S3Client} that holds a reference
     * to this pool for its entire lifetime.</p>
     */
    private final ByteBuffer[] slots;

    /**
     * Cached native addresses (one per allocated slot). Populated
     * lazily alongside {@code slots[i]}. Entries for unallocated
     * indices are {@code 0L} (never read; guarded by
     * {@code allocatedSlots}).
     *
     * <p>Addresses are stable for each buffer's lifetime — direct
     * buffers are off-heap and NOT relocated by GC, unlike heap
     * {@code byte[]} which can move under compacting collectors.</p>
     */
    private final long[] slotAddresses;

    /** Index queue of free slots; acquire = take, release = offer. */
    private final BlockingQueue<Integer> freeIndices;

    private final int partSize;
    private final int initialSlots;
    private final int maxSlots;

    /**
     * Number of slots currently allocated (eager + lazy-grown).
     * Monotonically increases up to {@code maxSlots}; never shrinks.
     * Guarded by {@code growthLock} on writes.
     */
    private int allocatedSlots;

    /** Held only on the lazy-growth slow path of {@code tryAcquireSlot()}. */
    private final Object growthLock = new Object();

    private volatile boolean closed;

    /**
     * Private — use {@link #createFixed(long, int)} or
     * {@link #createElastic(int, int, int)}.
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
        this.allocatedSlots = 0;

        // Eagerly pre-allocate the first `initialSlots` direct buffers.
        // Remaining slots (up to maxSlots) are allocated on demand in
        // tryAcquireSlot() under growthLock.
        //
        // If allocateDirect throws OutOfMemoryError partway through, we
        // catch it, release strong references to any DBBs we did
        // allocate (so GC + Cleaner can reclaim their off-heap memory
        // promptly), log a diagnostic, and rethrow so the caller sees
        // the OOM.
        try {
            for (int i = 0; i < initialSlots; i++) {
                // ByteBuffer.allocateDirect returns off-heap memory managed
                // by the JVM's internal Bits accounting. The address is
                // stable; the underlying memory is reclaimed only via the
                // Cleaner attached to the buffer when it becomes unreachable.
                // Keeping the buffer in `slots[i]` is what prevents that.
                ByteBuffer dbb = ByteBuffer.allocateDirect(partSize);
                slots[i] = dbb;

                // Cache the native address. JNI will use this to construct
                // aws_byte_buf without crossing back into Java per chunk.
                slotAddresses[i] = nativeGetDirectBufferAddress(dbb);

                freeIndices.add(i);
                allocatedSlots++;
            }
        } catch (OutOfMemoryError e) {
            Log.log(Log.LogLevel.Warn, Log.LogSubject.JavaCrtS3,
                "S3DirectBufferPool: OutOfMemoryError during eager allocation "
              + "at slot " + allocatedSlots + " of " + initialSlots
              + " (partSize=" + partSize + " bytes). "
              + "Releasing partial allocation. Consider raising "
              + "-XX:MaxDirectMemorySize or reducing pool size.");
            // Null out our strong references so the Cleaner can reclaim
            // the DBBs' off-heap memory as soon as GC runs, rather than
            // waiting for this (about-to-be-thrown) constructor's `this`
            // to become unreachable via stack unwind.
            for (int j = 0; j < allocatedSlots; j++) {
                slots[j] = null;
                slotAddresses[j] = 0L;
            }
            freeIndices.clear();
            allocatedSlots = 0;
            throw e;
        }
    }

    /**
     * Auto-scaled default: reads {@code clientOptions.throughputTargetGbps}
     * and sizes the pool using the same formula as
     * {@code aws_s3_default_buffer_pool}:
     * <ul>
     *   <li>&lt; 25 Gbps target -> 2 GiB</li>
     *   <li>25-75 Gbps -> 4 GiB</li>
     *   <li>75-100 Gbps -> 8 GiB</li>
     *   <li>100-200 Gbps -> 16 GiB</li>
     *   <li>&ge; 200 Gbps -> 24 GiB</li>
     * </ul>
     *
     * <p>If the {@code AWS_CRT_S3_MEMORY_LIMIT_IN_GIB} environment
     * variable is set, its value overrides the auto-scaled result.</p>
     *
     * <p>Elastic-mode growth semantics apply (initial slots pre-allocated,
     * lazy growth up to the ceiling). This preserves the current native
     * pool's behavior — same memory footprint, same growth-under-spike
     * pattern — while adding JVM visibility.</p>
     *
     * @param clientOptions the {@code S3ClientOptions} whose
     *                      {@code throughputTargetGbps} drives sizing
     */
    public static S3DirectBufferPool create(S3ClientOptions clientOptions) {
        long memoryLimitBytes = resolveAutoScaledMemoryLimit(clientOptions);
        int partSize = 8 * 1024 * 1024;  // 8 MiB, matches aws-c-s3 default
        int maxSlots = (int) (memoryLimitBytes / partSize);
        int initialSlots = Math.min(8, maxSlots);  // small warm floor
        return new S3DirectBufferPool(partSize, initialSlots, maxSlots);
    }

    /**
     * Fixed-size pool: pre-allocate {@code memoryLimitBytes / partSize}
     * slots up-front; no lazy growth. Equivalent to
     * {@code createElastic(slotCount, slotCount, partSize)}.
     *
     * @param memoryLimitBytes total off-heap memory budget for the pool
     * @param partSize         per-slot size in bytes
     */
    public static S3DirectBufferPool createFixed(long memoryLimitBytes, int partSize) {
        if (memoryLimitBytes < partSize) {
            throw new IllegalArgumentException(
                "memoryLimitBytes (" + memoryLimitBytes
              + ") must be >= partSize (" + partSize + ")");
        }
        int slotCount = (int) (memoryLimitBytes / partSize);
        return new S3DirectBufferPool(partSize, slotCount, slotCount);
    }

    /**
     * Elastic pool: pre-allocate {@code initialSlots} up-front; grow
     * lazily up to {@code maxSlots} on demand from {@code tryAcquireSlot}.
     * Memory tracks actual workload between
     * {@code initialSlots × partSize} (steady-state floor) and
     * {@code maxSlots × partSize} (ceiling).
     *
     * <p>This mode mirrors the {@code aws-c-s3} default pool's
     * lazy-allocation-within-ceiling semantics. Use when workload
     * throughput is variable and you want memory consumption to
     * track demand.</p>
     *
     * <p>{@code -XX:MaxDirectMemorySize} must accommodate
     * {@code maxSlots × partSize × 1.5} (pool ceiling + 50% headroom
     * for other DBB users in the application). Otherwise growth
     * under spike throws {@code OutOfMemoryError: Direct buffer memory}.</p>
     *
     * <p><b>Warning: Cold-start cost.</b> Each growth runs
     * {@code ByteBuffer.allocateDirect(partSize)} synchronously on
     * the calling thread (typically an aws-c-s3 event-loop thread).
     * Per-growth cost is ~50-100 us for {@code partSize = 8 MiB}.
     * In the worst case, a sudden burst can trigger
     * {@code maxSlots - initialSlots} growths in rapid succession,
     * accumulating to ~ms of event-loop work before reaching
     * steady state. <b>For event-loop-sensitive workloads, prefer
     * {@link #createFixed} (fully eager), or pass
     * {@code initialSlots == maxSlots} here to get the same eager
     * behavior</b>.</p>
     *
     * @param initialSlots number of slots to pre-allocate at construction (>= 0)
     * @param maxSlots     pool ceiling; tryAcquireSlot grows up to this on demand (>= 1, >= initialSlots)
     * @param partSize     per-slot size in bytes (> 0)
     */
    public static S3DirectBufferPool createElastic(int initialSlots, int maxSlots, int partSize) {
        return new S3DirectBufferPool(partSize, initialSlots, maxSlots);
    }

    // -----------------------------------------------------------------
    //  Package-private JNI back-call surface.
    //  These methods are invoked FROM s3_java_buffer_pool.c via JNI.
    //  They must remain stable signature-wise; the method IDs are
    //  cached in java_class_ids.c.
    // -----------------------------------------------------------------

    /**
     * Non-blocking acquire. Returns a free slot index if one is
     * immediately available, OR grows a new slot if the pool has
     * not yet reached {@code maxSlots}. Returns {@code -1} if the
     * pool is fully allocated AND every slot is currently leased.
     *
     * <p><b>CRITICAL:</b> this method MUST NOT block. It is called
     * from {@code s_java_pool_reserve} which runs on the
     * aws-c-s3 client's event-loop thread. Blocking that thread
     * would stall socket I/O, TLS state-machine progress, and
     * task scheduling for every other request sharing the same
     * event loop. When this method returns -1, the native side
     * routes the reserve future onto the pool's pending list —
     * see {@code s_java_pool_reserve} in {@code s3_java_buffer_pool.c}
     * for the async backpressure path.</p>
     *
     * <h3>Three-stage selection (all non-blocking)</h3>
     * <ol>
     *   <li><b>Fast path:</b> {@link BlockingQueue#poll()} for an
     *       already-free index. ~10 ns. The common steady-state
     *       outcome once {@code initialSlots} has warmed.</li>
     *   <li><b>Lazy growth:</b> if the queue is empty AND
     *       {@code allocatedSlots < maxSlots}, allocate a new direct
     *       buffer under {@code growthLock} and return its index
     *       directly (not via the queue). Cost: one
     *       {@code ByteBuffer.allocateDirect(partSize)} call
     *       (~50-100 us for 8 MiB).</li>
     *   <li><b>Exhausted:</b> if the pool is fully allocated AND
     *       all slots are leased, return {@code -1}. The caller
     *       (native pool reserve) pends its future on the C-side
     *       pending list, which is drained from the ticket-destroy
     *       path. The event loop returns immediately.</li>
     * </ol>
     *
     * <p>For fixed-size pools (constructed via {@link #createFixed}
     * or {@link #createElastic} with {@code initialSlots == maxSlots}),
     * the lazy-growth stage never fires — behavior is identical to a
     * pure eager pool with non-blocking try-acquire.</p>
     */
    int tryAcquireSlot() {
        if (closed) throw new IllegalStateException("pool is closed");

        // Fast path: existing free slot in the queue.
        Integer idx = freeIndices.poll();
        if (idx != null) return idx;

        // Slow path: try to grow under lock. Still non-blocking.
        synchronized (growthLock) {
            // Under lock: only grow if we haven't hit the ceiling.
            // A concurrent grower may have raised allocatedSlots to
            // maxSlots between the fast-path poll and this lock.
            if (allocatedSlots < maxSlots) {
                int newIdx = allocatedSlots;
                // ByteBuffer.allocateDirect may throw
                // OutOfMemoryError: Direct buffer memory if
                // MaxDirectMemorySize is exceeded. Propagating up is
                // the right behavior — the customer must size the
                // JVM direct memory ceiling for maxSlots, not
                // initialSlots. See createElastic Javadoc. Log a
                // WARN first so operators see the pool sizing
                // context in server logs (symmetric with the
                // constructor's eager-allocation OOM path).
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
                allocatedSlots++;
                return newIdx;
            }
        }

        // Pool fully allocated AND all slots leased. Return sentinel.
        // The native side MUST pend its future on the C-side
        // pending_reserves list — NEVER block this thread.
        return -1;
    }

    /**
     * Return a slot to the free pool.
     *
     * <p>Called by the native ticket's {@code release} vtable function
     * when {@code aws_s3_buffer_ticket_release} fires. After this
     * returns, the slot's memory MAY be handed out to a subsequent
     * {@code tryAcquireSlot()} call and its bytes overwritten — any
     * outstanding Java reference to a slice of this slot is now
     * UNSAFE to read.</p>
     */
    void releaseSlot(int slotIndex) {
        validateIndex(slotIndex);
        // Defensive check: the JNI caller should only release indices
        // that were returned by tryAcquireSlot(). Guards the
        // [allocatedSlots, maxSlots) gap during lazy growth — a bug
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
    }

    /*
     * NOTE: There is no Java-side `sliceView` method.
     *
     * The JNI body callback (Layer 3) constructs the ByteBuffer
     * view delivered to the user directly in C via
     * `NewDirectByteBuffer(env, slot_addr + offset, length)`,
     * avoiding a JNI->Java round-trip per delivered part.
     *
     * The resulting ByteBuffer has the same semantics as a
     * `slots[slotIndex].duplicate().position(off).limit(off+len)`
     * — it shares the slot's underlying memory with no Cleaner
     * attached (the slot's parent DBB owns the memory).
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

    /** Returns the per-slot byte size (matches aws-c-s3's part_size config). */
    public int partSize()       { return partSize; }
    /** Returns the pool ceiling — the maximum number of slots the pool may grow to. */
    public int maxSlots()       { return maxSlots; }
    /** Returns the number of slots pre-allocated at construction. */
    public int initialSlots()   { return initialSlots; }
    /**
     * Returns the number of slots currently allocated (eager +
     * lazy-grown). For diagnostics.
     *
     * <p>Synchronizes only on {@code growthLock} — the same lock
     * that guards writes to {@code allocatedSlots}. Avoids
     * acquiring {@code this}'s monitor to prevent any potential
     * lock-ordering inversion if other methods on this class
     * are ever marked {@code synchronized}.</p>
     */
    public int allocatedSlots() {
        synchronized (growthLock) { return allocatedSlots; }
    }

    @Override
    public void close() {
        closed = true;
        // No native free required: DirectByteBuffers are reclaimed by
        // the JVM Cleaner once the strong references in `slots` are
        // dropped (i.e. when this pool itself is GC'd). Both eagerly
        // pre-allocated slots and lazily grown slots are tracked the
        // same way in the `slots` array.
    }

    private void validateIndex(int idx) {
        // Allow any index up to maxSlots, since lazy-grown slots
        // occupy indices in [initialSlots, maxSlots). Only indices
        // currently in [0, allocatedSlots) point at live buffers,
        // but enforcement happens elsewhere — JNI only ever passes
        // back indices it received from tryAcquireSlot, which by
        // construction are < allocatedSlots at that moment.
        if (idx < 0 || idx >= maxSlots) {
            throw new IllegalArgumentException("invalid slot index: " + idx);
        }
    }

    /**
     * Resolves the auto-scaled memory limit based on the client's
     * throughputTargetGbps setting, matching the native pool's formula.
     */
    private static long resolveAutoScaledMemoryLimit(S3ClientOptions clientOptions) {
        // Check environment variable override first.
        String envOverride = System.getenv("AWS_CRT_S3_MEMORY_LIMIT_IN_GIB");
        if (envOverride != null && !envOverride.isEmpty()) {
            try {
                long gib = Long.parseLong(envOverride.trim());
                if (gib > 0) {
                    return gib * 1024L * 1024L * 1024L;
                }
            } catch (NumberFormatException ignored) {
                // Fall through to auto-scaling.
            }
        }

        // Auto-scale based on throughputTargetGbps.
        double gbps = clientOptions.getThroughputTargetGbps();
        long gib;
        if (gbps >= 200.0) {
            gib = 24;
        } else if (gbps >= 100.0) {
            gib = 16;
        } else if (gbps >= 75.0) {
            gib = 8;
        } else if (gbps >= 25.0) {
            gib = 4;
        } else {
            gib = 2;
        }
        return gib * 1024L * 1024L * 1024L;
    }

    // Implemented in src/native/s3_java_buffer_pool.c via JNI.
    private static native long nativeGetDirectBufferAddress(ByteBuffer dbb);

    /* javadoc on the package-private members is intentionally rich:
     * the JNI side cannot call private methods, so these signatures
     * are effectively a contract. Any change here must be coordinated
     * with java_class_ids.c. */
}
