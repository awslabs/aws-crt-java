/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.s3;

import java.nio.ByteBuffer;

/**
 * Settings for an optional direct buffer pool that holds an
 * {@link S3Client}'s transfer memory in Java direct memory. To turn the
 * pool on, pass these options to
 * {@link S3ClientOptions#withDirectBufferPoolOptions(S3DirectBufferPoolOptions)}
 * when you create the client.
 *
 * <p>The client splits each upload and download into parts and holds the
 * parts in flight in memory. By default that memory is native memory the
 * JVM cannot see or limit. With the pool, the memory comes from
 * {@link ByteBuffer#allocateDirect direct ByteBuffers} instead: it counts
 * against {@code -XX:MaxDirectMemorySize}, never grows past a limit you
 * control, and (except with {@link #fixed(long)}) is given back when the
 * client is idle.</p>
 *
 * <h2>Opt-in</h2>
 * The pool is off unless you set these options. Without them the client
 * behaves exactly as before. With them, existing response handlers keep
 * working unchanged; reading response data without a copy is a separate,
 * optional step (see Delivery).
 *
 * <h2>Example</h2>
 * <pre>{@code
 * S3ClientOptions options = new S3ClientOptions()
 *     .withRegion("us-west-2")
 *     .withClientBootstrap(bootstrap)
 *     .withCredentialsProvider(credentialsProvider)
 *     .withDirectBufferPoolOptions(S3DirectBufferPoolOptions.auto());
 * S3Client client = new S3Client(options);
 * }</pre>
 *
 * <h2>Choosing a factory</h2>
 * <ul>
 *   <li>{@link #auto()}: recommended. Sized automatically; grows with
 *       demand and shrinks when idle.</li>
 *   <li>{@link #fixed(long)}: a fixed amount of memory, allocated up front.
 *       Use it when you need a hard, predictable memory budget.</li>
 *   <li>{@link #elastic(long, long)}: grows and shrinks between a minimum
 *       and maximum you choose.</li>
 * </ul>
 *
 * <h2>Sizing</h2>
 * Every pool has a floor and a ceiling:
 * <ul>
 *   <li><b>Floor:</b> memory allocated when the client is created and kept
 *       even while the client is idle.</li>
 *   <li><b>Ceiling:</b> the most memory the pool will ever use. When it is
 *       reached, new requests wait until memory is freed.</li>
 * </ul>
 * Memory is counted in parts ({@link S3ClientOptions#withPartSize}, 8 MiB
 * by default), so byte sizes you pass are rounded to whole parts. Between
 * the floor and the ceiling, the pool grows as needed and gives memory
 * back when idle.
 *
 * <p>How {@link S3ClientOptions#withMemoryLimitInBytes} works with the
 * pool depends on the factory:</p>
 * <ul>
 *   <li>{@link #auto()}: it sets the ceiling. Use it to cap how much memory
 *       the pool can use.</li>
 *   <li>{@link #fixed(long)} and {@link #elastic(long, long)}: the size you
 *       pass to the factory is the ceiling, so you don't need to set
 *       {@link S3ClientOptions#withMemoryLimitInBytes}. If it is set, it is
 *       an upper bound: the pool's ceiling (the factory size rounded down
 *       to whole parts) must not exceed it, or creating the client fails
 *       and the error message gives the values to use.</li>
 * </ul>
 *
 * <p><b>JVM direct memory limit:</b> the pool's memory counts against
 * {@code -XX:MaxDirectMemorySize}, which by default equals the maximum
 * heap size ({@code -Xmx}). The ceiling must fit within 80% of that limit.
 * If you chose the ceiling (with {@link #fixed(long)},
 * {@link #elastic(long, long)}, {@code withMemoryLimitInBytes} or an
 * environment variable) and it doesn't fit, creating the client throws;
 * raise {@code -XX:MaxDirectMemorySize} or choose a smaller ceiling. A
 * default ceiling picked by {@link #auto()} is reduced to fit instead.
 * An explicit {@code -XX:MaxDirectMemorySize=0} allows no direct memory,
 * so creating the client always throws.</p>
 *
 * <h2>Delivery</h2>
 * Handlers that override
 * {@link S3MetaRequestResponseHandler#onResponseBody(S3BorrowedBuffer, long, long)}
 * receive each chunk straight from pool memory, with no copy into a heap
 * {@code byte[]}. The client calls exactly one {@code onResponseBody}
 * overload per request, and calls the borrowed-buffer one only when the
 * pool is enabled. A GetObject that sets a part number is the exception:
 * its body is copied once into separate memory outside the pool (see
 * {@link S3BorrowedBuffer}).
 *
 * <h2>Uploads</h2>
 * Multipart uploads hold each part in this pool until the part completes,
 * including retries; single-part uploads don't use it. Uploads and
 * downloads share the same ceiling, so a large upload can make downloads
 * wait, and the other way round. Uploads get the same memory visibility
 * and limit, but no copy is avoided: {@link S3BorrowedBuffer} is for
 * downloads only. Limits on upload part size are listed under Large
 * parts.
 *
 * <h2>Lifetime</h2>
 * The client creates the pool and owns it; each client has its own. When
 * the client shuts down, the pool frees its unused memory immediately.
 * Borrowed buffers keep their memory until they are closed, even after
 * shutdown; see {@link S3BorrowedBuffer}.
 *
 * <h2>Large parts</h2>
 * Usually there is nothing to do: the client uses larger parts on its own
 * when it needs them, for example for uploads too big for S3's
 * 10,000-part limit at the configured part size. These limits apply:
 * <ul>
 *   <li>If you set {@link S3ClientOptions#withPartSize partSize}
 *       explicitly, it is never raised: an upload that would need larger
 *       parts is rejected when the request is made, with the part size to
 *       use.</li>
 *   <li>An upload part can be at most half the ceiling, and never more
 *       than 5 GiB; an upload that needs larger parts is rejected when the
 *       request is made.</li>
 *   <li>{@link S3ClientOptions#withPartSize partSize} must be smaller than
 *       128 MiB (128 MiB itself is rejected), or creating the client
 *       fails. Pools whose ceiling is under 16 parts allow somewhat larger
 *       parts; the error message gives the exact limit.</li>
 * </ul>
 * {@link #fixed(long)} pools have a further limit on very large uploads;
 * see that method.
 * A request that can never fit in the pool fails, with the reason logged.
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
     * Recommended default. Sizes the pool automatically, so no tuning is
     * needed.
     *
     * <p>The ceiling is the most memory the pool will use. It is taken from
     * the sources below, in priority order: the first one that is set is
     * used, and the rest are ignored.</p>
     * <ol>
     *   <li>{@link S3ClientOptions#withMemoryLimitInBytes}</li>
     *   <li>the {@code AWS_CRT_S3_MEMORY_LIMIT_IN_MB} environment variable
     *       (in MiB)</li>
     *   <li>the {@code AWS_CRT_S3_MEMORY_LIMIT_IN_GIB} environment variable</li>
     *   <li>a default based on
     *       {@link S3ClientOptions#withThroughputTargetGbps} (if unset,
     *       detected from the EC2 instance type, so the ceiling can differ
     *       between instances). If this default doesn't fit within 80% of
     *       the JVM direct memory limit, it is reduced to fit and a warning
     *       is logged, since a smaller pool can limit throughput; raise
     *       {@code -XX:MaxDirectMemorySize} to use the full default.</li>
     * </ol>
     *
     * <p>The floor is 16 parts (128 MiB with the default part size), or the
     * whole pool if the ceiling is smaller. Above that, the pool grows on
     * demand and shrinks when idle. Each time it grows it briefly delays
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
     *   <li><b>Size:</b> {@code memoryLimitBytes}, rounded down to whole
     *       parts ({@link S3ClientOptions#withPartSize}, 8 MiB by
     *       default).</li>
     *   <li><b>When full:</b> new requests wait until memory frees up.</li>
     *   <li><b>Very large uploads:</b> each upload part must fit in 4 parts'
     *       worth of memory. Only uploads big enough that S3's 10,000-part
     *       limit forces a larger part size are affected (over about 312 GiB
     *       with the default 8 MiB part size); they are rejected when the
     *       request is made, with the reason. Downloads are not affected.</li>
     *   <li><b>No allocation pauses:</b> memory is never allocated or freed
     *       after the client is created.</li>
     * </ul>
     *
     * @param memoryLimitBytes total off-heap budget for the pool; must be
     *                         at least one part, checked when the client
     *                         is created
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
     *   <li><b>Floor:</b> memory allocated when the client is created and
     *       kept while idle. The pool allocates 16 parts at a time, so this
     *       is {@code minBytes} rounded down to a multiple of 16 parts (or
     *       the whole pool, if {@code minBytes} reaches the ceiling); it is
     *       never more than you asked for.</li>
     *   <li><b>Ceiling:</b> {@code maxBytes}, rounded down to whole parts; the
     *       pool never uses more than that.</li>
     *   <li><b>In between:</b> the pool grows on demand 16 parts at a time,
     *       and gives memory above the floor back once the client goes
     *       idle.</li>
     * </ul>
     *
     * <p><b>Note: growth cost.</b> Each time the pool grows it allocates and
     * zero-fills 16 parts of memory (128 MiB with 8 MiB parts), which takes
     * several milliseconds and briefly delays other requests on the client.
     * A sudden burst can grow the pool several times in quick succession,
     * and the same happens again after the pool has shrunk while idle. If
     * your workload can't tolerate these pauses, use {@link #fixed(long)}.</p>
     *
     * @param minBytes memory allocated when the client is created and kept
     *                 while idle ({@code >= 0}; 0 allocates nothing up
     *                 front)
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
