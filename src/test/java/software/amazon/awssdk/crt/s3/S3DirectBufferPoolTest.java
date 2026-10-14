/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

/*
 * Deliberately in software.amazon.awssdk.crt.s3 rather than the usual
 * ...crt.test package: the pool's accessors (ceilingBytes, partSize,
 * ceilingClamped) and S3BorrowedBuffer's constructor are package-private, and
 * reaching them here avoids either widening production visibility or writing
 * reflection into the tests. CrtTestFixture is public with protected helpers,
 * so extending it across packages still works.
 */
package software.amazon.awssdk.crt.s3;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.auth.credentials.DefaultChainCredentialsProvider;
import software.amazon.awssdk.crt.auth.signing.AwsSigningConfig;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.test.CrtTestFixture;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tests for the S3 direct buffer pool and the borrowed buffers it hands out.
 *
 * <p>Split by what each test needs, because that decides where it runs:</p>
 * <ul>
 *   <li><b>Configuration and buffer-contract tests</b> need neither credentials
 *       nor network, so they run in every CI job on every platform we support.
 *       They cover the validation and lifetime rules that a pool configured for
 *       the happy path never exercises.</li>
 *   <li><b>Delivery tests</b> need real S3, so they gate on credentials the same
 *       way {@code S3ClientTest} does, and skip without them.</li>
 * </ul>
 */
public class S3DirectBufferPoolTest extends CrtTestFixture {

    private static final String BUCKET_NAME = System.getenv("CRT_S3_TEST_BUCKET_NAME") == null
            ? "aws-c-s3-test-bucket"
            : System.getenv("CRT_S3_TEST_BUCKET_NAME");
    private static final String REGION = "us-west-2";
    private static final String ENDPOINT = String.format("%s.s3.%s.amazonaws.com", BUCKET_NAME, REGION);
    private static final String PRE_EXIST_10MB_PATH = "/pre-existing-10MB";
    private static final long PRE_EXIST_10MB_SIZE = 10 * 1024 * 1024;

    private static final int PART_SIZE = 8 * 1024 * 1024;
    private static final long MIB = 1024 * 1024;

    public S3DirectBufferPoolTest() {
    }

    /* ==================================================================== */
    /* Group A: configuration and validation. No credentials, no network.   */
    /* ==================================================================== */

    /*
     * A ceiling this large cannot fit within 80% of any plausible
     * -XX:MaxDirectMemorySize, so the test doesn't need to know the actual
     * limit to know the check must reject it.
     */
    @Test
    public void testFixedCeilingBeyondDirectMemoryIsRejected() {
        S3ClientOptions options = baseOptions()
                .withDirectBufferPoolOptions(S3DirectBufferPoolOptions.fixed(1024L * 1024 * MIB)); // 1 PiB

        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> S3DirectBufferPool.fromOptions(
                        options.getDirectBufferPoolOptions(), options));
        // The message has to tell the customer which flag to raise, or they
        // cannot act on it.
        assertTrue("error should name MaxDirectMemorySize, was: " + ex.getMessage(),
                ex.getMessage().contains("MaxDirectMemorySize"));
    }

    /*
     * The client's memory limit and the pool's ceiling are one budget, not two.
     * A ceiling above the limit must fail at construction rather than letting
     * the pool exceed a limit the customer set.
     */
    @Test
    public void testCeilingAboveClientMemoryLimitIsRejected() {
        S3ClientOptions options = baseOptions()
                .withMemoryLimitInBytes(64 * MIB)
                .withDirectBufferPoolOptions(S3DirectBufferPoolOptions.fixed(256 * MIB));

        assertThrows(IllegalArgumentException.class,
                () -> S3DirectBufferPool.fromOptions(options.getDirectBufferPoolOptions(), options));
    }

    @Test
    public void testNegativeSizesAreRejected() {
        S3ClientOptions negativeLimit = baseOptions()
                .withMemoryLimitInBytes(-1)
                .withDirectBufferPoolOptions(S3DirectBufferPoolOptions.auto());
        assertThrows(IllegalArgumentException.class,
                () -> S3DirectBufferPool.fromOptions(negativeLimit.getDirectBufferPoolOptions(), negativeLimit));

        S3ClientOptions negativePart = new S3ClientOptions()
                .withRegion(REGION)
                .withPartSize(-1)
                .withDirectBufferPoolOptions(S3DirectBufferPoolOptions.auto());
        assertThrows(IllegalArgumentException.class,
                () -> S3DirectBufferPool.fromOptions(negativePart.getDirectBufferPoolOptions(), negativePart));

        // The factories themselves reject nonsense before a client is involved.
        assertThrows(IllegalArgumentException.class, () -> S3DirectBufferPoolOptions.fixed(-1));
        assertThrows(IllegalArgumentException.class, () -> S3DirectBufferPoolOptions.elastic(-1, 256 * MIB));
    }

    @Test
    public void testElasticFloorAboveCeilingIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> S3DirectBufferPoolOptions.elastic(256 * MIB, 64 * MIB));
    }

    /* A ceiling that cannot hold even one part is unusable, so it must fail
     * rather than produce a pool that can never serve a request. */
    @Test
    public void testCeilingSmallerThanOnePartIsRejected() {
        S3ClientOptions options = baseOptions()
                .withDirectBufferPoolOptions(S3DirectBufferPoolOptions.fixed(PART_SIZE / 2));

        assertThrows(IllegalArgumentException.class,
                () -> S3DirectBufferPool.fromOptions(options.getDirectBufferPoolOptions(), options));
    }

    /*
     * The pool allocates whole parts, so a ceiling between two multiples has to
     * round down. Rounding up would let the pool exceed a limit the customer set.
     */
    @Test
    public void testCeilingRoundsDownToWholeParts() {
        long requested = (3 * (long) PART_SIZE) + (PART_SIZE / 3); // 3 and a bit parts
        S3ClientOptions options = baseOptions()
                .withDirectBufferPoolOptions(S3DirectBufferPoolOptions.fixed(requested));

        S3DirectBufferPool pool =
                S3DirectBufferPool.fromOptions(options.getDirectBufferPoolOptions(), options);
        try {
            assertEquals(PART_SIZE, pool.partSize());
            assertEquals(3L * PART_SIZE, pool.ceilingBytes());
            assertTrue("rounded ceiling must not exceed the requested size",
                    pool.ceilingBytes() <= requested);
        } finally {
            pool.close();
        }
    }

    /*
     * auto() derives its ceiling from the throughput target and, unlike an
     * explicit size, reduces it to fit the JVM's direct memory rather than
     * failing. Asserted as a biconditional against the derived default so the
     * test holds on any machine without needing to read the actual limit:
     * either the default fit and nothing was clamped, or it didn't and the
     * ceiling came down.
     */
    @Test
    public void testAutoClampsCeilingRatherThanFailing() {
        double throughputGbps = 400.0; // high enough to derive a large default
        long derivedDefault = S3Client.defaultMemoryLimitForThroughput(throughputGbps);
        Assume.assumeTrue("native default sizing unavailable", derivedDefault > 0);

        S3ClientOptions options = new S3ClientOptions()
                .withRegion(REGION)
                .withPartSize(PART_SIZE)
                .withThroughputTargetGbps(throughputGbps)
                .withDirectBufferPoolOptions(S3DirectBufferPoolOptions.auto());

        S3DirectBufferPool pool =
                S3DirectBufferPool.fromOptions(options.getDirectBufferPoolOptions(), options);
        try {
            assertTrue("ceiling must never exceed the derived default",
                    pool.ceilingBytes() <= derivedDefault);
            assertEquals("ceilingClamped must say exactly whether the ceiling was reduced",
                    pool.ceilingBytes() < derivedDefault, pool.ceilingClamped());
        } finally {
            pool.close();
        }
    }

    /*
     * An explicit -XX:MaxDirectMemorySize=0 allows no direct memory at all, so a
     * pool cannot be created. Only meaningful in a JVM started with that flag,
     * so it skips elsewhere rather than silently passing.
     */
    @Test
    public void testZeroDirectMemoryLimitIsRejected() {
        Assume.assumeTrue("needs a JVM started with -XX:MaxDirectMemorySize=0",
                "0".equals(System.getProperty("aws.crt.test.expect_zero_direct_memory")));

        S3ClientOptions options = baseOptions()
                .withDirectBufferPoolOptions(S3DirectBufferPoolOptions.auto());
        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> S3DirectBufferPool.fromOptions(
                        options.getDirectBufferPoolOptions(), options));
        assertTrue("error should explain that no direct memory is allowed, was: " + ex.getMessage(),
                ex.getMessage().contains("MaxDirectMemorySize"));
    }

    /* ==================================================================== */
    /* Group B: borrowed buffer contract. No credentials, no network.        */
    /*                                                                      */
    /* A ticket pointer of 0 means the native release is a no-op (see the    */
    /* guard in nativeReleaseTicket), so a buffer over plain direct memory   */
    /* exercises the Java-side lifetime rules without an S3 transfer.        */
    /* ==================================================================== */

    @Test
    public void testCloseIsIdempotent() {
        S3BorrowedBuffer buffer = newDetachedBuffer(16);
        buffer.close();
        buffer.close(); // documented as safe to call twice
    }

    @Test
    public void testToByteArrayIsAtMostOnce() {
        byte[] expected = {1, 2, 3, 4};
        S3BorrowedBuffer buffer = newDetachedBuffer(expected);

        assertArrayEquals(expected, buffer.toByteArray());
        // toByteArray releases as it copies, so a second call has nothing to
        // read. Unlike close(), it is not idempotent.
        assertThrows(IllegalStateException.class, buffer::toByteArray);
    }

    @Test
    public void testAsByteBufferAfterCloseThrows() {
        S3BorrowedBuffer buffer = newDetachedBuffer(16);
        assertNotNull(buffer.asByteBuffer());
        buffer.close();
        // Returning a view of released pool memory would hand out bytes that
        // belong to another request.
        assertThrows(IllegalStateException.class, buffer::asByteBuffer);
    }

    @Test
    public void testAsByteBufferExposesTheBodyBytes() {
        byte[] expected = {9, 8, 7};
        S3BorrowedBuffer buffer = newDetachedBuffer(expected);
        try {
            ByteBuffer view = buffer.asByteBuffer();
            assertEquals(expected.length, view.remaining());
            byte[] seen = new byte[view.remaining()];
            view.duplicate().get(seen);
            assertArrayEquals(expected, seen);
        } finally {
            buffer.close();
        }
    }

    /*
     * A handler that only implements the heap callback must still receive the
     * body when the borrowed callback is invoked, otherwise overriding neither
     * method would silently drop data. This is the fallback path in the
     * interface's default onResponseBodyBorrowed.
     */
    @Test
    public void testDefaultBorrowedCallbackFallsBackToHeapCallback() {
        byte[] body = {5, 6, 7, 8};
        List<byte[]> heapDeliveries = new ArrayList<>();

        S3MetaRequestResponseHandler heapOnlyHandler = new S3MetaRequestResponseHandler() {
            @Override
            public int onResponseBody(ByteBuffer bodyBytesIn, long rangeStart, long rangeEnd) {
                byte[] copy = new byte[bodyBytesIn.remaining()];
                bodyBytesIn.get(copy);
                heapDeliveries.add(copy);
                return copy.length;
            }
        };

        S3BorrowedBuffer buffer = newDetachedBuffer(body);
        int credited = heapOnlyHandler.onResponseBodyBorrowed(buffer, 0, body.length);

        assertEquals("fallback should deliver exactly once", 1, heapDeliveries.size());
        assertArrayEquals(body, heapDeliveries.get(0));
        assertEquals("window credit should pass through unchanged", body.length, credited);
    }

    /* ==================================================================== */
    /* Group C: delivery against real S3. Skips without credentials.        */
    /* ==================================================================== */

    /*
     * The borrowed path is only taken when the client has a pool AND the handler
     * overrides onResponseBodyBorrowed. A regression in that routing makes the
     * whole feature silently inert rather than broken, which is why all three
     * combinations are asserted.
     */
    @Test
    public void testPoolAndBorrowedHandlerTakeBorrowedPath() throws Exception {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());

        RecordingHandler handler = new RecordingHandler();
        downloadPreExisting10MB(poolOptions(256 * MIB), handler);

        assertEquals("expected the borrowed callback only", 0, handler.heapCallbacks.get());
        assertTrue("expected at least one borrowed delivery", handler.borrowedCallbacks.get() > 0);
        assertEquals(PRE_EXIST_10MB_SIZE, handler.bytesReceived.get());
    }

    @Test
    public void testPoolWithHeapOnlyHandlerTakesHeapPath() throws Exception {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());

        HeapOnlyRecordingHandler handler = new HeapOnlyRecordingHandler();
        downloadPreExisting10MB(poolOptions(256 * MIB), handler);

        assertTrue("expected heap deliveries", handler.heapCallbacks.get() > 0);
        assertEquals(PRE_EXIST_10MB_SIZE, handler.bytesReceived.get());
    }

    @Test
    public void testBorrowedHandlerWithoutPoolTakesHeapPath() throws Exception {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());

        RecordingHandler handler = new RecordingHandler();
        downloadPreExisting10MB(null /* no pool */, handler);

        assertEquals("without a pool there is nothing to borrow", 0, handler.borrowedCallbacks.get());
        assertTrue("expected the heap fallback to deliver", handler.heapCallbacks.get() > 0);
        assertEquals(PRE_EXIST_10MB_SIZE, handler.bytesReceived.get());
    }

    /*
     * The bytes a borrowed buffer exposes must match the object exactly, and
     * arrive in order with no gaps or overlaps. A lifetime bug in the pool shows
     * up here as corrupted or duplicated ranges rather than as an error.
     */
    @Test
    public void testBorrowedBytesMatchObjectAndArriveInOrder() throws Exception {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());

        RecordingHandler handler = new RecordingHandler();
        handler.verifyContiguousRanges = true;
        downloadPreExisting10MB(poolOptions(256 * MIB), handler);

        assertEquals(PRE_EXIST_10MB_SIZE, handler.bytesReceived.get());
        assertEquals("ranges must be contiguous from zero", null, handler.rangeError);
    }

    /*
     * DEFAULT meta requests never reserve from the pool, so their body arrives
     * through a separately owned copy with a NULL ticket. That path had a
     * use-after-free: the body was freed before Java read it.
     */
    @Test
    public void testDefaultMetaRequestTypeDeliversCorrectBytes() throws Exception {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());

        RecordingHandler handler = new RecordingHandler();
        handler.verifyContiguousRanges = true;
        downloadPreExisting10MB(poolOptions(256 * MIB), handler,
                S3MetaRequestOptions.MetaRequestType.DEFAULT);

        assertEquals(PRE_EXIST_10MB_SIZE, handler.bytesReceived.get());
        assertEquals("owned-copy path must deliver contiguous bytes", null, handler.rangeError);
    }

    /*
     * The case the pool's hard ceiling exists for: more concurrent demand than
     * the pool can satisfy at once. Reservations have to queue and then be
     * served as buffers come back, with every request still completing and no
     * request losing or duplicating bytes. A ceiling of two parts against eight
     * concurrent downloads guarantees the queue is used.
     */
    @Test
    public void testPoolSmallerThanDemandStillCompletesEveryRequest() throws Exception {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());

        final int concurrency = 8;
        try (EventLoopGroup elg = new EventLoopGroup(0, 4);
                HostResolver hostResolver = new HostResolver(elg);
                ClientBootstrap bootstrap = new ClientBootstrap(elg, hostResolver);
                DefaultChainCredentialsProvider credentials =
                        new DefaultChainCredentialsProvider.DefaultChainCredentialsProviderBuilder()
                                .withClientBootstrap(bootstrap).build();
                AwsSigningConfig signingConfig =
                        AwsSigningConfig.getDefaultS3SigningConfig(REGION, credentials)) {

            S3ClientOptions options = new S3ClientOptions()
                    .withRegion(REGION)
                    .withPartSize(PART_SIZE)
                    .withClientBootstrap(bootstrap)
                    .withSigningConfig(signingConfig)
                    .withDirectBufferPoolOptions(S3DirectBufferPoolOptions.fixed(2L * PART_SIZE));

            try (S3Client client = new S3Client(options)) {
                List<RecordingHandler> handlers = new ArrayList<>();
                List<S3MetaRequest> requests = new ArrayList<>();
                try {
                    for (int i = 0; i < concurrency; ++i) {
                        RecordingHandler handler = new RecordingHandler();
                        handler.verifyContiguousRanges = true;
                        handlers.add(handler);
                        requests.add(client.makeMetaRequest(getObjectOptions(handler,
                                S3MetaRequestOptions.MetaRequestType.GET_OBJECT)));
                    }
                    for (RecordingHandler handler : handlers) {
                        handler.finished.get(5, TimeUnit.MINUTES);
                    }
                } finally {
                    for (S3MetaRequest request : requests) {
                        request.close();
                    }
                }

                for (int i = 0; i < concurrency; ++i) {
                    RecordingHandler handler = handlers.get(i);
                    assertEquals("request " + i + " error code", 0, handler.errorCode.get());
                    assertEquals("request " + i + " byte count",
                            PRE_EXIST_10MB_SIZE, handler.bytesReceived.get());
                    assertEquals("request " + i + " ranges", null, handler.rangeError);
                }
            }
        }
    }

    /*
     * Cancelling a request while others are waiting on the pool used to leave
     * the waiters stalled: the cancelled reservation's memory came back without
     * the pool being told to retry the queue. The survivors must still finish.
     */
    @Test
    public void testCancelWithPendingReservationsDoesNotStallSurvivors() throws Exception {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());

        final int concurrency = 6;
        try (EventLoopGroup elg = new EventLoopGroup(0, 4);
                HostResolver hostResolver = new HostResolver(elg);
                ClientBootstrap bootstrap = new ClientBootstrap(elg, hostResolver);
                DefaultChainCredentialsProvider credentials =
                        new DefaultChainCredentialsProvider.DefaultChainCredentialsProviderBuilder()
                                .withClientBootstrap(bootstrap).build();
                AwsSigningConfig signingConfig =
                        AwsSigningConfig.getDefaultS3SigningConfig(REGION, credentials)) {

            S3ClientOptions options = new S3ClientOptions()
                    .withRegion(REGION)
                    .withPartSize(PART_SIZE)
                    .withClientBootstrap(bootstrap)
                    .withSigningConfig(signingConfig)
                    .withDirectBufferPoolOptions(S3DirectBufferPoolOptions.fixed(2L * PART_SIZE));

            try (S3Client client = new S3Client(options)) {
                List<RecordingHandler> handlers = new ArrayList<>();
                List<S3MetaRequest> requests = new ArrayList<>();
                try {
                    for (int i = 0; i < concurrency; ++i) {
                        RecordingHandler handler = new RecordingHandler();
                        handlers.add(handler);
                        requests.add(client.makeMetaRequest(getObjectOptions(handler,
                                S3MetaRequestOptions.MetaRequestType.GET_OBJECT)));
                    }

                    // Cancel half while the pool is certainly contended.
                    for (int i = 0; i < concurrency; i += 2) {
                        requests.get(i).cancel();
                    }

                    // Every request must reach a terminal state: the cancelled
                    // ones with an error, the rest completing normally. A stall
                    // shows up as this timing out.
                    for (int i = 0; i < concurrency; ++i) {
                        handlers.get(i).finished.get(5, TimeUnit.MINUTES);
                    }
                } finally {
                    for (S3MetaRequest request : requests) {
                        request.close();
                    }
                }

                for (int i = 1; i < concurrency; i += 2) {
                    RecordingHandler handler = handlers.get(i);
                    assertEquals("survivor " + i + " should have completed",
                            0, handler.errorCode.get());
                    assertEquals("survivor " + i + " byte count",
                            PRE_EXIST_10MB_SIZE, handler.bytesReceived.get());
                }
            }
        }
    }

    /* ==================================================================== */
    /* Helpers                                                              */
    /* ==================================================================== */

    /** Options with everything the pool's sizing needs and nothing else. */
    private static S3ClientOptions baseOptions() {
        return new S3ClientOptions().withRegion(REGION).withPartSize(PART_SIZE);
    }

    private static S3DirectBufferPoolOptions poolOptions(long ceilingBytes) {
        return S3DirectBufferPoolOptions.fixed(ceilingBytes);
    }

    /** A buffer over plain direct memory with no pool ticket behind it. */
    private static S3BorrowedBuffer newDetachedBuffer(int size) {
        return new S3BorrowedBuffer(0L, ByteBuffer.allocateDirect(size));
    }

    private static S3BorrowedBuffer newDetachedBuffer(byte[] contents) {
        ByteBuffer direct = ByteBuffer.allocateDirect(contents.length);
        direct.put(contents);
        direct.flip();
        return new S3BorrowedBuffer(0L, direct);
    }

    private static S3MetaRequestOptions getObjectOptions(
            S3MetaRequestResponseHandler handler,
            S3MetaRequestOptions.MetaRequestType type) {

        HttpHeader[] headers = {new HttpHeader("Host", ENDPOINT)};
        HttpRequest request = new HttpRequest("GET", PRE_EXIST_10MB_PATH, headers, null);
        return new S3MetaRequestOptions()
                .withMetaRequestType(type)
                .withHttpRequest(request)
                .withResponseHandler(handler);
    }

    private void downloadPreExisting10MB(S3DirectBufferPoolOptions pool,
            S3MetaRequestResponseHandler handler) throws Exception {
        downloadPreExisting10MB(pool, handler, S3MetaRequestOptions.MetaRequestType.GET_OBJECT);
    }

    private void downloadPreExisting10MB(S3DirectBufferPoolOptions pool,
            S3MetaRequestResponseHandler handler,
            S3MetaRequestOptions.MetaRequestType type) throws Exception {

        try (EventLoopGroup elg = new EventLoopGroup(0, 1);
                HostResolver hostResolver = new HostResolver(elg);
                ClientBootstrap bootstrap = new ClientBootstrap(elg, hostResolver);
                DefaultChainCredentialsProvider credentials =
                        new DefaultChainCredentialsProvider.DefaultChainCredentialsProviderBuilder()
                                .withClientBootstrap(bootstrap).build();
                AwsSigningConfig signingConfig =
                        AwsSigningConfig.getDefaultS3SigningConfig(REGION, credentials)) {

            S3ClientOptions options = new S3ClientOptions()
                    .withRegion(REGION)
                    .withPartSize(PART_SIZE)
                    .withClientBootstrap(bootstrap)
                    .withSigningConfig(signingConfig);
            if (pool != null) {
                options.withDirectBufferPoolOptions(pool);
            }

            try (S3Client client = new S3Client(options)) {
                S3MetaRequest request = client.makeMetaRequest(getObjectOptions(handler, type));
                try {
                    if (handler instanceof RecordingHandler) {
                        ((RecordingHandler) handler).finished.get(5, TimeUnit.MINUTES);
                        assertEquals("download failed", 0,
                                ((RecordingHandler) handler).errorCode.get());
                    } else {
                        ((HeapOnlyRecordingHandler) handler).finished.get(5, TimeUnit.MINUTES);
                        assertEquals("download failed", 0,
                                ((HeapOnlyRecordingHandler) handler).errorCode.get());
                    }
                } finally {
                    request.close();
                }
            }
        }
    }

    /**
     * Records which body callback the client chose, counts bytes, and optionally
     * checks that ranges arrive contiguously from zero. Overrides both body
     * methods so the client's routing decision is observable rather than assumed.
     */
    private static final class RecordingHandler implements S3MetaRequestResponseHandler {
        final AtomicInteger borrowedCallbacks = new AtomicInteger();
        final AtomicInteger heapCallbacks = new AtomicInteger();
        final AtomicLong bytesReceived = new AtomicLong();
        final AtomicInteger errorCode = new AtomicInteger();
        final CompletableFuture<Void> finished = new CompletableFuture<>();

        volatile boolean verifyContiguousRanges = false;
        volatile String rangeError = null;
        private long nextExpectedOffset = 0;

        @Override
        public int onResponseBodyBorrowed(S3BorrowedBuffer buffer, long rangeStart, long rangeEnd) {
            borrowedCallbacks.incrementAndGet();
            int length = (int) (rangeEnd - rangeStart);
            try {
                // Read through the view so a released or recycled buffer shows
                // up as wrong bytes rather than going unnoticed.
                ByteBuffer view = buffer.asByteBuffer();
                if (view.remaining() != length) {
                    recordRangeError("view length " + view.remaining() + " != range length " + length);
                }
                checkRange(rangeStart, rangeEnd);
                bytesReceived.addAndGet(length);
            } finally {
                buffer.close();
            }
            return length;
        }

        @Override
        public int onResponseBody(ByteBuffer bodyBytesIn, long rangeStart, long rangeEnd) {
            heapCallbacks.incrementAndGet();
            int length = bodyBytesIn.remaining();
            checkRange(rangeStart, rangeEnd);
            bytesReceived.addAndGet(length);
            return length;
        }

        @Override
        public void onFinished(S3FinishedResponseContext context) {
            errorCode.set(context.getErrorCode());
            finished.complete(null);
        }

        private synchronized void checkRange(long rangeStart, long rangeEnd) {
            if (!verifyContiguousRanges) {
                return;
            }
            if (rangeStart != nextExpectedOffset) {
                recordRangeError("expected offset " + nextExpectedOffset + " but got " + rangeStart);
            }
            nextExpectedOffset = rangeEnd;
        }

        private void recordRangeError(String message) {
            if (rangeError == null) {
                rangeError = message;
            }
        }
    }

    /** Overrides only the heap callback, so the client must not borrow for it. */
    private static final class HeapOnlyRecordingHandler implements S3MetaRequestResponseHandler {
        final AtomicInteger heapCallbacks = new AtomicInteger();
        final AtomicLong bytesReceived = new AtomicLong();
        final AtomicInteger errorCode = new AtomicInteger();
        final CompletableFuture<Void> finished = new CompletableFuture<>();

        @Override
        public int onResponseBody(ByteBuffer bodyBytesIn, long rangeStart, long rangeEnd) {
            heapCallbacks.incrementAndGet();
            int length = bodyBytesIn.remaining();
            bytesReceived.addAndGet(length);
            return length;
        }

        @Override
        public void onFinished(S3FinishedResponseContext context) {
            errorCode.set(context.getErrorCode());
            finished.complete(null);
        }
    }
}
