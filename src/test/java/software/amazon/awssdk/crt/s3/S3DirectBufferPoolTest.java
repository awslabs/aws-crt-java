/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

/*
 * Deliberately in software.amazon.awssdk.crt.s3 rather than the usual
 * ...crt.test package: the pool's accessors and allocation surface
 * (ceilingBytes, committedBytes, partSize, ceilingClamped, tryAcquire,
 * release, trim) and S3BorrowedBuffer's constructor are package-private, and
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
 *   <li><b>Configuration, allocation, buffer-contract and routing tests</b>
 *       need neither credentials nor network, so they run in every CI job on
 *       every platform we support. They cover the validation, growth, trim,
 *       lifetime and delivery-path rules that a pool configured for the happy
 *       path never exercises.</li>
 *   <li><b>End-to-end delivery tests</b> need real S3, so they gate on
 *       credentials the same way {@code S3ClientTest} does, and skip without
 *       them. They confirm native honours the routing decision.</li>
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
     * pool cannot be created.
     *
     * The gate detects that JVM rather than trusting a property somebody has to
     * remember to set: a one-byte direct allocation fails if and only if the flag
     * is in force. The previous property-based gate named a property no job or
     * profile ever set, so this test always skipped while appearing to cover the
     * path.
     *
     * NOTE: it still cannot run in this project today. pom.xml sets forkCount=0,
     * so tests share the Maven JVM, and Maven's own launcher needs direct buffers
     * to start -- MAVEN_OPTS=-XX:MaxDirectMemorySize=0 kills Maven before any test
     * runs. Covering this path requires a forked test JVM.
     */
    @Test
    public void testZeroDirectMemoryLimitIsRejected() {
        Assume.assumeTrue("needs a JVM started with -XX:MaxDirectMemorySize=0",
                directMemoryIsForbidden());

        S3ClientOptions options = baseOptions()
                .withDirectBufferPoolOptions(S3DirectBufferPoolOptions.auto());
        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> S3DirectBufferPool.fromOptions(
                        options.getDirectBufferPoolOptions(), options));
        assertTrue("error should explain that no direct memory is allowed, was: " + ex.getMessage(),
                ex.getMessage().contains("MaxDirectMemorySize"));
    }

    /* ==================================================================== */
    /* Group A2: allocation, growth and trim. No credentials, no network.   */
    /*                                                                     */
    /* tryAcquire/release/trim/close are the pool's whole allocation        */
    /* surface and are called only from native, so a transfer is the only   */
    /* other way to reach them -- which makes their coverage credential-    */
    /* gated and indirect. Driving them here asserts the ceiling, growth    */
    /* and trim rules directly. A small part size keeps each block at       */
    /* BLOCK_BYTES so these run anywhere, whatever the JVM's direct memory  */
    /* limit.                                                               */
    /* ==================================================================== */

    /** 64 KiB: well under a real part, so the whole test pool is a few MiB. */
    private static final int SMALL_PART = 64 * 1024;
    /** One block of SMALL_PART slots -- the granularity the pool allocates in. */
    private static final long BLOCK_BYTES = (long) S3DirectBufferPool.BLOCK_SLOTS * SMALL_PART;

    /*
     * Growth is per block, not per part, and stops dead at the ceiling. The
     * per-block jump matters to a customer sizing a pool: committing one part
     * commits a whole block's worth of memory.
     */
    @Test
    public void testElasticPoolGrowsBlockByBlockToTheCeilingThenRefuses() {
        S3DirectBufferPool pool = elasticPool(0, 2 * BLOCK_BYTES);
        try {
            assertEquals(2 * BLOCK_BYTES, pool.ceilingBytes());
            assertEquals("a pool with no floor commits nothing until something is acquired",
                    0, pool.committedBytes());

            List<Long> leases = new ArrayList<>();
            leases.add(acquireOnePart(pool));
            assertEquals("the first acquire backs a whole block, not one part",
                    BLOCK_BYTES, pool.committedBytes());

            // Fill the rest of block 0. Still one block: growth is driven by
            // running out of slots, not by the number of leases.
            while (leases.size() < S3DirectBufferPool.BLOCK_SLOTS) {
                leases.add(acquireOnePart(pool));
            }
            assertEquals(BLOCK_BYTES, pool.committedBytes());

            // Block 0 is full, so this one has to grow the pool.
            leases.add(acquireOnePart(pool));
            assertEquals("a full block must grow the pool, not fail",
                    2 * BLOCK_BYTES, pool.committedBytes());

            while (leases.size() < 2 * S3DirectBufferPool.BLOCK_SLOTS) {
                leases.add(acquireOnePart(pool));
            }
            assertEquals(2 * BLOCK_BYTES, pool.committedBytes());

            // At the ceiling: refuse rather than exceed it. Native pends its
            // future on EXHAUSTED and retries when memory frees.
            assertEquals("acquiring past the ceiling must report EXHAUSTED",
                    S3DirectBufferPool.EXHAUSTED, pool.tryAcquire(SMALL_PART));
            assertEquals("a refused acquire must not commit anything",
                    2 * BLOCK_BYTES, pool.committedBytes());

            // A release makes room without changing what is committed: the
            // block stays backed and its slot is reused.
            pool.release(leases.remove(leases.size() - 1));
            leases.add(acquireOnePart(pool));
            assertEquals(2 * BLOCK_BYTES, pool.committedBytes());

            for (long lease : leases) {
                pool.release(lease);
            }
        } finally {
            pool.close();
        }
        // close() frees every unbacked block, including the ones growth added.
        assertEquals("close must give all pool memory back", 0, pool.committedBytes());
    }

    /*
     * trim() is what gives idle memory back, and it has two bounds that are
     * easy to get wrong in opposite directions: it must not free a block that
     * still has a lease (use-after-free), and it must not free below the floor
     * (the floor is the memory the customer asked to keep allocated).
     */
    @Test
    public void testTrimFreesAboveTheFloorButNotLeasedBlocksOrTheFloor() {
        S3DirectBufferPool pool = elasticPool(BLOCK_BYTES, 2 * BLOCK_BYTES);
        try {
            assertEquals("the floor is allocated when the pool is created",
                    BLOCK_BYTES, pool.committedBytes());

            List<Long> floorLeases = new ArrayList<>();
            while (floorLeases.size() < S3DirectBufferPool.BLOCK_SLOTS) {
                floorLeases.add(acquireOnePart(pool));
            }
            long aboveFloor = acquireOnePart(pool); // grows past the floor
            assertEquals(2 * BLOCK_BYTES, pool.committedBytes());

            pool.trim();
            assertEquals("trim must not free a block with a lease outstanding",
                    2 * BLOCK_BYTES, pool.committedBytes());

            pool.release(aboveFloor);
            pool.trim();
            assertEquals("trim must free an unused block above the floor",
                    BLOCK_BYTES, pool.committedBytes());

            // Completely idle now, so this is the case where trim could wrongly
            // free the floor as well.
            for (long lease : floorLeases) {
                pool.release(lease);
            }
            pool.trim();
            assertEquals("the floor is kept even when the pool is completely idle",
                    BLOCK_BYTES, pool.committedBytes());
        } finally {
            pool.close();
        }
        // Only shutdown releases the floor.
        assertEquals("close must free the floor too", 0, pool.committedBytes());
    }

    /**
     * An elastic pool over {@link #SMALL_PART} slots, built without a client
     * so no credentials or native client are involved.
     */
    private static S3DirectBufferPool elasticPool(long floorBytes, long ceilingBytes) {
        S3ClientOptions options = new S3ClientOptions()
                .withRegion(REGION)
                .withPartSize(SMALL_PART)
                .withDirectBufferPoolOptions(S3DirectBufferPoolOptions.elastic(floorBytes, ceilingBytes));
        return S3DirectBufferPool.fromOptions(options.getDirectBufferPoolOptions(), options);
    }

    /** One part, asserting it was actually leased (handles are {@code >= 0}). */
    private static long acquireOnePart(S3DirectBufferPool pool) {
        long handle = pool.tryAcquire(SMALL_PART);
        assertTrue("expected a lease handle, got " + handle, handle >= 0);
        return handle;
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
        // toByteArray hands the buffer back as it copies, so a second call has
        // nothing to read. Unlike close(), it is not idempotent. (This buffer has
        // no pool ticket, so only the Java state machine is under test here; the
        // pool-release half of toByteArray needs a real pooled buffer.)
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
     * Borrowed delivery requires BOTH a pool on the client and a handler that
     * implements S3BorrowedBufferDelivery; all four combinations are asserted
     * here, on the adapter that makes the decision.
     *
     * This asserts the decision rather than the delivered bytes, which is the
     * only way to see it: both paths deliver identical bytes by design, so a
     * routing regression makes the feature silently inert rather than broken.
     * It needs no credentials and no network, so unlike the end-to-end tests
     * below it runs in every CI job on every platform we support -- which is
     * where a platform-specific routing bug would show up.
     */
    @Test
    public void testBorrowedDeliveryRequiresBothPoolAndHandlerOptIn() {
        S3MetaRequestResponseHandler heapOnly = new HeapOnlyRecordingHandler();
        S3MetaRequestResponseHandler borrowed = new RecordingHandler();

        assertTrue("pool + opted-in handler must borrow",
                new S3MetaRequestResponseHandlerNativeAdapter(borrowed, true).usesBorrowedDelivery());
        assertFalse("a handler that did not opt in must not borrow",
                new S3MetaRequestResponseHandlerNativeAdapter(heapOnly, true).usesBorrowedDelivery());
        assertFalse("without a pool there is nothing to borrow",
                new S3MetaRequestResponseHandlerNativeAdapter(borrowed, false).usesBorrowedDelivery());
        assertFalse("neither condition met",
                new S3MetaRequestResponseHandlerNativeAdapter(heapOnly, false).usesBorrowedDelivery());
    }

    /* ==================================================================== */
    /* Group C: delivery against real S3. Skips without credentials.        */
    /* ==================================================================== */

    /*
     * End-to-end confirmation that native honours the routing decision the
     * adapter made: the borrowed path is only taken when the client has a pool
     * AND the handler implements S3BorrowedBufferDelivery.
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
        assertTrue("expected heap deliveries instead", handler.heapCallbacks.get() > 0);
        assertEquals(PRE_EXIST_10MB_SIZE, handler.bytesReceived.get());
    }

    /*
     * The bytes a borrowed buffer exposes must match the object exactly, and
     * arrive in order with no gaps or overlaps.
     *
     * The content assertion compares a digest of the borrowed delivery against a
     * digest of the same object fetched over the heap path, rather than against a
     * hardcoded pattern: it needs no knowledge of what the fixture contains, and
     * it fails on exactly the bug a buffer pool makes possible -- a slot handed
     * back while it still holds another request's bytes. Length and offset checks
     * cannot see that, because corruption preserves both.
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
        assertTrue("must have been delivered over the borrowed path",
                handler.borrowedCallbacks.get() > 0);
        assertEquals("the borrowed path must not fall back to heap delivery",
                0, handler.heapCallbacks.get());
        assertEquals("borrowed bytes must equal the heap path's bytes",
                heapBaselineDigest(), handler.contentDigest());
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
        // Without these two, a regression that routed DEFAULT through the heap
        // path would still pass: the handler counts bytes on both callbacks.
        assertTrue("owned-copy delivery must still use the borrowed callback",
                handler.borrowedCallbacks.get() > 0);
        assertEquals("owned-copy path must not fall back to heap delivery",
                0, handler.heapCallbacks.get());
        assertEquals("the owned copy must carry the object's bytes unchanged",
                heapBaselineDigest(), handler.contentDigest());
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

    /**
     * Hex SHA-256 of the 10 MiB fixture as delivered over the HEAP path with no
     * pool attached -- the trusted baseline for content comparisons.
     *
     * Cached for the class: the fixture is immutable, so one extra download
     * serves every test that needs it.
     */
    private static volatile String heapBaseline = null;

    private String heapBaselineDigest() throws Exception {
        String cached = heapBaseline;
        if (cached != null) {
            return cached;
        }
        HeapOnlyRecordingHandler baseline = new HeapOnlyRecordingHandler();
        downloadPreExisting10MB(null /* no pool: plain heap delivery */, baseline);
        assertEquals("baseline download must be complete before it is trusted",
                PRE_EXIST_10MB_SIZE, baseline.bytesReceived.get());
        cached = baseline.contentDigest();
        heapBaseline = cached;
        return cached;
    }

    /**
     * True only in a JVM that allows no direct memory at all, which is what
     * -XX:MaxDirectMemorySize=0 produces. Probing beats trusting a property:
     * the answer cannot drift out of sync with how the JVM was launched.
     */
    private static boolean directMemoryIsForbidden() {
        try {
            ByteBuffer probe = ByteBuffer.allocateDirect(1);
            return probe.capacity() != 1;
        } catch (OutOfMemoryError expectedWhenForbidden) {
            return true;
        }
    }

    private static MessageDigest newSha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            // Every JRE ships SHA-256; a missing one is an environment fault.
            throw new AssertionError("SHA-256 unavailable", e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
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
                // Required for MetaRequestType.DEFAULT: aws-c-s3 rejects a default
                // meta request with no operation name before it sends anything.
                // Ignored by the other types, so it is set unconditionally.
                .withOperationName("GetObject")
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
    private static final class RecordingHandler
            implements S3MetaRequestResponseHandler, S3BorrowedBufferDelivery {
        final AtomicInteger borrowedCallbacks = new AtomicInteger();
        final AtomicInteger heapCallbacks = new AtomicInteger();
        final AtomicLong bytesReceived = new AtomicLong();
        final AtomicInteger errorCode = new AtomicInteger();
        final CompletableFuture<Void> finished = new CompletableFuture<>();

        volatile boolean verifyContiguousRanges = false;
        volatile String rangeError = null;
        private long nextExpectedOffset = 0;
        private final MessageDigest digest = newSha256();

        @Override
        public int onResponseBodyBorrowed(S3BorrowedBuffer buffer, long rangeStart, long rangeEnd) {
            borrowedCallbacks.incrementAndGet();
            int length = (int) (rangeEnd - rangeStart);
            try {
                // Every byte goes through the digest. Length and offset checks alone
                // cannot see a pool slot handed back while it still holds another
                // request's bytes, which is the failure mode a buffer pool makes
                // possible; comparing the digest to the heap path's can.
                ByteBuffer view = buffer.asByteBuffer();
                if (view.remaining() != length) {
                    recordRangeError("view length " + view.remaining() + " != range length " + length);
                }
                checkRange(rangeStart, rangeEnd);
                updateDigest(view);
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
            updateDigest(bodyBytesIn);
            bytesReceived.addAndGet(length);
            return length;
        }

        /** Hex SHA-256 of every delivered byte, in delivery order. */
        String contentDigest() {
            synchronized (this) {
                return toHex(digest.digest());
            }
        }

        private synchronized void updateDigest(ByteBuffer src) {
            digest.update(src.duplicate());
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

    /**
     * Overrides only the heap callback, so the client must not borrow for it.
     * Also digests its bytes, which makes it the trusted baseline the borrowed
     * path is compared against.
     */
    private static final class HeapOnlyRecordingHandler implements S3MetaRequestResponseHandler {
        final AtomicInteger heapCallbacks = new AtomicInteger();
        final AtomicLong bytesReceived = new AtomicLong();
        final AtomicInteger errorCode = new AtomicInteger();
        final CompletableFuture<Void> finished = new CompletableFuture<>();
        private final MessageDigest digest = newSha256();

        @Override
        public int onResponseBody(ByteBuffer bodyBytesIn, long rangeStart, long rangeEnd) {
            heapCallbacks.incrementAndGet();
            int length = bodyBytesIn.remaining();
            synchronized (this) {
                digest.update(bodyBytesIn.duplicate());
            }
            bytesReceived.addAndGet(length);
            return length;
        }

        /** Hex SHA-256 of every delivered byte, in delivery order. */
        String contentDigest() {
            synchronized (this) {
                return toHex(digest.digest());
            }
        }

        @Override
        public void onFinished(S3FinishedResponseContext context) {
            errorCode.set(context.getErrorCode());
            finished.complete(null);
        }
    }
}
