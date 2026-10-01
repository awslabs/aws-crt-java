/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.s3;

import java.nio.ByteBuffer;

/**
 * Sizing for the Java-owned direct buffer pool that an {@link S3Client}
 * uses for all of its part buffers, in place of the default native buffer
 * pool. Pass one
 * to {@link S3ClientOptions#withDirectBufferPoolOptions(S3DirectBufferPoolOptions)}.
 *
 * <h2>Opt-in</h2>
 * Without this option the client uses the default native buffer pool
 * and the historical {@code byte[]}-delivery path is unchanged. With it,
 * part buffers come from {@link ByteBuffer#allocateDirect
 * direct ByteBuffers}: JVM-visible (counted against
 * {@code -XX:MaxDirectMemorySize}), hard-capped, and trimmed when idle.
 * The cap applies to memory the pool is using. On JVMs where direct
 * memory cannot be freed on demand, memory the pool has released is
 * returned only after a GC cycle, so actual direct memory can briefly
 * exceed the cap (still bounded by {@code -XX:MaxDirectMemorySize}).
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
 *   <li>{@link #elastic(long, long)}. Caller-chosen floor and ceiling.</li>
 *   <li>{@link #fixed(long)}. Floor equals ceiling: never grows or trims.</li>
 * </ul>
 * Every factory checks at client construction that the ceiling fits
 * within 80% of {@code -XX:MaxDirectMemorySize}, and fails client
 * construction otherwise. The ceiling is also handed to the client as its
 * memory limit unless {@link S3ClientOptions#withMemoryLimitInBytes} is set
 * (for fixed and elastic pools an explicit, different limit fails client
 * construction).
 *
 * <h2>Delivery contract</h2>
 * Enabling the pool does NOT change the behavior of
 * {@link S3MetaRequestResponseHandler#onResponseBody(ByteBuffer, long, long)}:
 * it continues to receive a heap {@code byte[]}-backed {@link ByteBuffer}
 * that is safe to retain indefinitely. Zero-copy delivery is available
 * ONLY by overriding
 * {@link S3MetaRequestResponseHandler#onResponseBody(S3BorrowedBuffer, long, long)}.
 * The two overloads are mutually exclusive per request, chosen when the
 * request is made: if the handler overrides the borrowed-buffer overload
 * (and the pool is enabled), every body chunk goes to it and the
 * {@code ByteBuffer} overload is never called by the client. Without the
 * pool, the borrowed-buffer override is ignored and the {@code ByteBuffer}
 * overload receives every chunk.
 *
 * <h2>Uploads</h2>
 * Multipart uploads take each part's buffer from this pool and hold it
 * until the part completes (including retries); single-part uploads stream
 * from their source and do not use the pool. Uploads and downloads share
 * the same ceiling and wait queue, so a large upload can make downloads
 * wait and vice versa. Uploads gain the same memory visibility and cap,
 * but there is no zero-copy upload path: {@link S3BorrowedBuffer} applies
 * to downloads only. Limits on upload part size are described under
 * Parts larger than one slot.
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
 * <p>Instances are immutable and may be reused across clients; each client
 * builds its own pool from them.</p>
 */
public final class S3DirectBufferPoolOptions {

    /** How {@link S3DirectBufferPool} derives floor and ceiling. */
    enum Mode { AUTO, FIXED, ELASTIC }

    private final Mode mode;
    private final long memoryLimitBytes;   // FIXED only
    private final long minBytes;           // ELASTIC only
    private final long maxBytes;           // ELASTIC only

    private S3DirectBufferPoolOptions(Mode mode, long memoryLimitBytes, long minBytes, long maxBytes) {
        this.mode = mode;
        this.memoryLimitBytes = memoryLimitBytes;
        this.minBytes = minBytes;
        this.maxBytes = maxBytes;
    }

    /**
     * Recommended default. Sizes the pool the same way aws-c-s3 sizes its
     * default buffer pool, so no tuning is needed.
     *
     * <p>The ceiling is the most memory the pool will use. It is taken from
     * the sources below, in priority order: the first one that is set is
     * used, and the rest are ignored.</p>
     * <ol>
     *   <li>{@link S3ClientOptions#withMemoryLimitInBytes}</li>
     *   <li>the {@code AWS_CRT_S3_MEMORY_LIMIT_IN_MB} environment variable</li>
     *   <li>the {@code AWS_CRT_S3_MEMORY_LIMIT_IN_GIB} environment variable</li>
     *   <li>aws-c-s3's default for the client's throughput target</li>
     * </ol>
     *
     * <p>One block (16 parts, or fewer if the ceiling is smaller) is
     * allocated up front and kept when the pool trims; it may be freed to
     * make room for a buffer larger than 4 parts. Above that, the pool grows
     * on demand and shrinks when idle. Each time it grows it briefly delays
     * other requests on the client; see {@link #elastic(long, long)} for
     * details.</p>
     *
     * @return options for an automatically sized pool
     */
    public static S3DirectBufferPoolOptions auto() {
        return new S3DirectBufferPoolOptions(Mode.AUTO, 0, 0, 0);
    }

    /**
     * Fixed-size pool: all of its memory is allocated when the client is
     * created, and it never grows or shrinks. Use it when you need a hard,
     * predictable memory budget, for example in a tight container.
     *
     * <ul>
     *   <li><b>Size:</b> {@code memoryLimitBytes / partSize} parts, rounded
     *       down to whole parts.</li>
     *   <li><b>When full:</b> new requests wait until memory frees up.</li>
     *   <li><b>Largest buffer:</b> 4 parts. A request that needs more fails,
     *       with the reason logged (see the class doc).</li>
     *   <li><b>No allocation pauses:</b> memory is never allocated or freed
     *       after the client is created.</li>
     * </ul>
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
     * Pool with a floor and ceiling you choose, in bytes. Use it to size the
     * pool yourself while still letting it grow and shrink with demand.
     *
     * <ul>
     *   <li><b>Floor:</b> {@code minBytes}, rounded up to whole parts and then
     *       to whole 16-part blocks (never above the ceiling), is allocated
     *       when the client is created and kept when the pool trims. It may be
     *       freed to make room for a buffer larger than 4 parts.</li>
     *   <li><b>Ceiling:</b> {@code maxBytes}, rounded down to whole parts; the
     *       pool never uses more than that.</li>
     *   <li><b>In between:</b> the pool grows on demand one block at a time,
     *       and frees unused blocks above the floor once the client goes
     *       idle.</li>
     * </ul>
     *
     * <p><b>Note: growth cost.</b> Each time the pool grows it allocates and
     * zero-fills one 16-part block (128 MiB with 8 MiB parts), which takes
     * several milliseconds and briefly delays other requests on the client.
     * A sudden burst can grow several blocks in quick succession, and the
     * same happens again after the pool has shrunk while idle. If your
     * workload can't tolerate these pauses, use {@link #fixed(long)}.</p>
     *
     * @param minBytes memory allocated at client construction and kept
     *                 through trim ({@code >= 0}; 0 means no warm floor)
     * @param maxBytes most memory the pool will use ({@code >= minBytes};
     *                 must be at least one part, checked when the client is
     *                 created)
     * @return options for an elastic pool
     * @throws IllegalArgumentException if {@code minBytes < 0},
     *                                  {@code maxBytes <= 0}, or
     *                                  {@code minBytes > maxBytes}
     */
    public static S3DirectBufferPoolOptions elastic(long minBytes, long maxBytes) {
        if (minBytes < 0) {
            throw new IllegalArgumentException("minBytes must be >= 0");
        }
        if (maxBytes <= 0) {
            throw new IllegalArgumentException("maxBytes must be > 0");
        }
        if (minBytes > maxBytes) {
            throw new IllegalArgumentException(
                "minBytes (" + minBytes + ") must be <= maxBytes (" + maxBytes + ")");
        }
        return new S3DirectBufferPoolOptions(Mode.ELASTIC, 0, minBytes, maxBytes);
    }

    Mode getMode()               { return mode; }
    long getMemoryLimitBytes()   { return memoryLimitBytes; }
    long getMinBytes()           { return minBytes; }
    long getMaxBytes()           { return maxBytes; }
}
