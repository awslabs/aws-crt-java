/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.s3;

import java.nio.ByteBuffer;

/**
 * Sizing for the Java-owned direct buffer pool that an {@link S3Client}
 * uses as the destination memory for download response bodies. Pass one
 * to {@link S3ClientOptions#withDirectBufferPoolOptions(S3DirectBufferPoolOptions)}.
 *
 * <h2>Opt-in</h2>
 * Without this option the client uses the default native buffer pool
 * and the historical {@code byte[]}-delivery path is unchanged. With it,
 * download staging memory comes from {@link ByteBuffer#allocateDirect
 * direct ByteBuffers}: JVM-visible (counted against
 * {@code -XX:MaxDirectMemorySize}), hard-capped, and trimmed when idle.
 * The cap applies to memory the pool is using. On JVMs where direct
 * memory cannot be freed on demand, memory the pool has released is
 * returned only after a GC cycle, so actual direct memory can briefly
 * exceed the cap (still bounded by {@code -XX:MaxDirectMemorySize}).
 *
 * <h2>Delivery contract</h2>
 * Enabling the pool does NOT change the behavior of
 * {@link S3MetaRequestResponseHandler#onResponseBody(ByteBuffer, long, long)}:
 * it continues to receive a heap {@code byte[]}-backed {@link ByteBuffer}
 * that is safe to retain indefinitely. Zero-copy delivery is available
 * ONLY by overriding
 * {@link S3MetaRequestResponseHandler#onResponseBody(S3BorrowedBuffer, long, long)}.
 *
 * <h2>Sizing</h2>
 * Every pool has a warm floor of pre-allocated slots and a ceiling; each
 * slot holds one part, so slot size is always the client's part size.
 * Like aws-c-s3's default pool, memory is allocated in blocks of 16
 * contiguous slots (the floor rounds up to whole blocks). Blocks above the
 * floor are allocated on demand and freed again by trim once the client
 * goes idle (no requests in flight for 5 seconds). The
 * factories differ only in how the floor and ceiling are chosen:
 * <ul>
 *   <li>{@link #auto()}. Recommended default.</li>
 *   <li>{@link #elastic(int, int)}. Caller-chosen floor and ceiling.</li>
 *   <li>{@link #fixed(long)}. Floor equals ceiling: never grows or trims.</li>
 * </ul>
 * Every factory checks at client construction that the ceiling fits
 * within 80% of {@code -XX:MaxDirectMemorySize}, and fails client
 * construction otherwise. The ceiling is also handed to the client as its
 * memory limit unless {@link S3ClientOptions#withMemoryLimitInBytes} is set
 * (for fixed and elastic pools an explicit, different limit fails client
 * construction).
 *
 * <h2>Parts larger than one slot</h2>
 * As with the default native pool, the client may need buffers larger
 * than its part size: uploads whose part size aws-c-s3 raises to stay
 * within S3's 10,000-part limit, resumed uploads with a larger part size,
 * and downloads with aws-c-s3's automatic range sizing (used only when no
 * part size is set). Up to 4 parts are served from contiguous free slots
 * in an existing block, with no new allocation. Larger buffers are
 * dedicated to the request that needs them: allocated within the same
 * ceiling, reused only by that request, and freed when it finishes. Each
 * such request pays its own allocation (which zero-fills the memory);
 * the default native pool instead shares same-size blocks across
 * requests. With aws-c-s3's default sizing, automatic download ranges fit
 * in 4 slots; a large explicit memory limit with few connections can
 * produce larger ranges. To make room for a dedicated buffer the pool
 * frees fully unused blocks, floor included; freed floor blocks are
 * allocated again on demand. Only a pool that can grow allocates
 * dedicated buffers. A request that can never fit fails with the reason
 * logged. Waiting requests are served strictly in order, so a large
 * request waiting for memory also holds back smaller requests queued
 * behind it. The pool never silently changes what the
 * customer configured; a multipart upload fails up front with the reason
 * when it would need a part size larger than an explicitly set
 * {@link S3ClientOptions#withPartSize partSize}, or larger than half the
 * pool ceiling (capped at 5 GiB). A fixed pool does not grow, so it keeps
 * downloads at its part size and fails uploads that need more than 4
 * parts' worth.
 *
 * <h2>Lifetime</h2>
 * The client creates the pool at construction and owns it: one pool per
 * client, never shared. When the client's shutdown completes, the pool
 * frees all unused memory. Memory held by unclosed {@link S3BorrowedBuffer}s
 * stays valid and is freed as each buffer is closed; until then, an
 * unclosed buffer keeps its whole 16-slot block allocated. Client shutdown does
 * not close borrowed buffers: the customer MUST close each one. An unclosed
 * buffer is only recovered by the GC fallback, after an unbounded delay,
 * and is reported as a leak.
 *
 * <p>Instances are immutable and may be reused across clients; each client
 * builds its own pool from them.</p>
 */
public final class S3DirectBufferPoolOptions {

    /** How {@link S3DirectBufferPool} derives floor and ceiling. */
    enum Mode { AUTO, FIXED, ELASTIC }

    private final Mode mode;
    private final long memoryLimitBytes;   // FIXED only
    private final int initialSlots;        // ELASTIC only
    private final int maxSlots;            // ELASTIC only

    private S3DirectBufferPoolOptions(Mode mode, long memoryLimitBytes, int initialSlots, int maxSlots) {
        this.mode = mode;
        this.memoryLimitBytes = memoryLimitBytes;
        this.initialSlots = initialSlots;
        this.maxSlots = maxSlots;
    }

    /**
     * Sized like aws-c-s3's default buffer pool. The ceiling is the
     * client's memory limit, resolved in aws-c-s3's order:
     * {@link S3ClientOptions#withMemoryLimitInBytes} if set, else the
     * {@code AWS_CRT_S3_MEMORY_LIMIT_IN_MB} / {@code _IN_GIB} environment
     * variable ({@code _IN_MB} wins when both are set), else aws-c-s3's
     * default for the client's throughput target. One block (16 slots,
     * fewer if the ceiling is smaller) is pre-allocated and kept through
     * trim (it may be reclaimed for a dedicated buffer).
     *
     * <p>Growth above the floor allocates on an aws-c-s3 event-loop
     * thread; see {@link #elastic(int, int)} for the cost.</p>
     *
     * @return options for an automatically sized pool
     */
    public static S3DirectBufferPoolOptions auto() {
        return new S3DirectBufferPoolOptions(Mode.AUTO, 0, 0, 0);
    }

    /**
     * Fully pre-allocated pool of {@code memoryLimitBytes / partSize}
     * slots (rounded down to whole parts). Never grows or trims, so
     * nothing is ever allocated on the event loop. A demand spike beyond
     * capacity waits natively until slots free up. Serves buffers of up to
     * 4 parts from contiguous slots but no larger (see the class doc). For
     * tight container-memory budgets.
     *
     * @param memoryLimitBytes total off-heap budget for the pool; must be
     *                         at least one part
     * @return options for a fixed pool
     * @throws IllegalArgumentException if {@code memoryLimitBytes <= 0}
     */
    public static S3DirectBufferPoolOptions fixed(long memoryLimitBytes) {
        if (memoryLimitBytes <= 0) {
            throw new IllegalArgumentException("memoryLimitBytes must be > 0");
        }
        return new S3DirectBufferPoolOptions(Mode.FIXED, memoryLimitBytes, 0, 0);
    }

    /**
     * Pool with {@code initialSlots} (rounded up to whole 16-slot blocks)
     * pre-allocated and kept through trim (reclaimable for a dedicated
     * buffer), growing on demand in blocks up to {@code maxSlots}. Memory tracks demand
     * between {@code initialSlots × partSize} and {@code maxSlots × partSize}.
     *
     * <p><b>Warning: cold-start cost.</b> Each growth allocates one 16-slot
     * block with {@code ByteBuffer.allocateDirect}, which zero-fills it,
     * synchronously on an aws-c-s3 event-loop thread and under the pool's
     * lock (several milliseconds for a 128 MiB block of 8 MiB parts). A
     * sudden burst can grow several blocks in quick succession. For
     * event-loop-sensitive workloads use {@link #fixed(long)}, or pass
     * {@code initialSlots == maxSlots}.</p>
     *
     * @param initialSlots slots pre-allocated at client construction ({@code >= 0})
     * @param maxSlots     ceiling ({@code >= 1}, {@code >= initialSlots})
     * @return options for an elastic pool
     * @throws IllegalArgumentException for invalid slot counts
     */
    public static S3DirectBufferPoolOptions elastic(int initialSlots, int maxSlots) {
        if (initialSlots < 0) {
            throw new IllegalArgumentException("initialSlots must be >= 0");
        }
        if (maxSlots < 1) {
            throw new IllegalArgumentException("maxSlots must be >= 1");
        }
        if (initialSlots > maxSlots) {
            throw new IllegalArgumentException(
                "initialSlots (" + initialSlots + ") must be <= maxSlots (" + maxSlots + ")");
        }
        return new S3DirectBufferPoolOptions(Mode.ELASTIC, 0, initialSlots, maxSlots);
    }

    Mode getMode()               { return mode; }
    long getMemoryLimitBytes()   { return memoryLimitBytes; }
    int getInitialSlots()        { return initialSlots; }
    int getMaxSlots()            { return maxSlots; }
}
