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
 * Slots above the floor are allocated on demand and freed again by trim
 * once the client goes idle (no requests in flight for 5 seconds). The
 * factories differ only in how the floor and ceiling are chosen:
 * <ul>
 *   <li>{@link #auto()}. Recommended default.</li>
 *   <li>{@link #elastic(int, int)}. Caller-chosen floor and ceiling.</li>
 *   <li>{@link #fixed(long)}. Floor equals ceiling: never grows or trims.</li>
 * </ul>
 * Every factory checks at client construction that the ceiling fits
 * within 80% of {@code -XX:MaxDirectMemorySize}, and fails client
 * construction otherwise.
 *
 * <h2>Lifetime</h2>
 * The client creates the pool at construction and owns it: one pool per
 * client, never shared. When the client's shutdown completes, the pool
 * frees every unused slot. Slots held by unclosed {@link S3BorrowedBuffer}s
 * stay valid and are freed as each buffer is closed. Client shutdown does
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
     * default for the client's throughput target. 8 slots (fewer if the
     * ceiling is smaller) are pre-allocated and kept through trim.
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
     * slots. Never grows or trims, so nothing is ever allocated on the
     * event loop. A demand spike beyond capacity waits natively until
     * slots free up. For tight container-memory budgets.
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
     * Pool with {@code initialSlots} pre-allocated and kept through trim,
     * growing on demand up to {@code maxSlots}. Memory tracks demand
     * between {@code initialSlots × partSize} and {@code maxSlots × partSize}.
     *
     * <p><b>Warning: cold-start cost.</b> Each growth runs
     * {@code ByteBuffer.allocateDirect(partSize)} synchronously on an
     * aws-c-s3 event-loop thread (~50-100 us for 8 MiB parts). A sudden
     * burst can trigger {@code maxSlots - initialSlots} growths in quick
     * succession. For event-loop-sensitive workloads use {@link #fixed(long)},
     * or pass {@code initialSlots == maxSlots}.</p>
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
