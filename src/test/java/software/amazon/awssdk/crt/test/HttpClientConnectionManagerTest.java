package software.amazon.awssdk.crt.test;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.http.*;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsConnectionOptions;
import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.io.TlsContextOptions;

public class HttpClientConnectionManagerTest extends HttpClientTestFixture  {
    private final static Charset UTF8 = StandardCharsets.UTF_8;
    private final static int NUM_THREADS = 10;
    private final static int NUM_CONNECTIONS = 20;
    private final static int NUM_REQUESTS = 60;
    private final static int NUM_ITERATIONS = 10;
    private final static int GROWTH_PER_THREAD = 0; // expected VM footprint growth per thread
    private final static int EXPECTED_HTTP_STATUS = 200;
    private final static String endpoint = "https://aws-crt-test-stuff.s3.amazonaws.com";
    private final static String path = "/random_32_byte.data";
    private final String EMPTY_BODY = "";

    private HttpClientConnectionManager createConnectionManager(URI uri, int numThreads, int numConnections) {
        try (EventLoopGroup eventLoopGroup = new EventLoopGroup(1);
                HostResolver resolver = new HostResolver(eventLoopGroup);
                ClientBootstrap bootstrap = new ClientBootstrap(eventLoopGroup, resolver);
                SocketOptions sockOpts = new SocketOptions();
                TlsContext tlsContext = createHttpClientTlsContext();
                TlsConnectionOptions tlsConnectionOptions = new TlsConnectionOptions(tlsContext)) {
            tlsConnectionOptions.withServerName(endpoint);
            HttpClientConnectionManagerOptions options = new HttpClientConnectionManagerOptions();
            options.withClientBootstrap(bootstrap)
                    .withSocketOptions(sockOpts)
                    .withTlsConnectionOptions(tlsConnectionOptions)
                    .withUri(uri)
                    .withMaxConnections(numConnections);

            return HttpClientConnectionManager.create(options);
        }
    }

    private HttpRequest createHttpRequest(String method, String endpoint, String path, String requestBody) throws Exception{
        URI uri = new URI(endpoint);
        HttpHeader[] requestHeaders =
                new HttpHeader[]{
                        new HttpHeader("Host", uri.getHost()),
                        new HttpHeader("Content-Length", Integer.toString(requestBody.getBytes(UTF8).length))
                };
        HttpRequest request = new HttpRequest(method, path, requestHeaders, null);

        return request;
    }

    private void testParallelConnections(HttpClientConnectionManager connPool, HttpRequest request, int numThreads, int numRequests) {
        final AtomicInteger numRequestsMade = new AtomicInteger(0);
        final AtomicInteger numConnectionFailures = new AtomicInteger(0);
        final ConcurrentHashMap<Integer, Integer> reqIdToStatus = new ConcurrentHashMap<>();
        final AtomicInteger numErrorCode = new AtomicInteger(0);

        final ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
        List<CompletableFuture> requestCompleteFutures = new ArrayList<>();

        for (int i = 0; i < numRequests; i++) {

            Log.log(Log.LogLevel.Trace, Log.LogSubject.HttpConnectionManager, String.format("Starting request %d", i));
            CompletableFuture requestCompleteFuture = new CompletableFuture();
            requestCompleteFutures.add(requestCompleteFuture);

            threadPool.execute(() -> {
                // Request a connection from the connection pool
                connPool.acquireConnection()
                        // When the connection is acquired, submit a request on it
                        .whenComplete((conn, throwable) -> {
                            if (throwable != null) {
                                numConnectionFailures.incrementAndGet();
                                // this next line should be completely impossible to be valid.
                                // but this test AFAIK hasn't segfaulted and there's no way
                                // a connection setup has never failed. Conn should always be null though.
                                // commenting out for now.
                                //connPool.releaseConnection(conn);
                                requestCompleteFuture.completeExceptionally(throwable);
                            }

                            int requestId = numRequestsMade.incrementAndGet();
                            HttpStream stream = conn.makeRequest(request, new HttpStreamResponseHandler() {
                                @Override
                                public void onResponseHeaders(HttpStream stream, int responseStatusCode, int blockType,
                                        HttpHeader[] nextHeaders) {
                                    reqIdToStatus.put(requestId, responseStatusCode);
                                }

                                @Override
                                public void onResponseComplete(HttpStream stream, int errorCode) {
                                    if (errorCode != CRT.AWS_CRT_SUCCESS) {
                                        numErrorCode.incrementAndGet();
                                    }
                                    // When this Request is complete, release the conn back to the pool
                                    connPool.releaseConnection(conn);
                                    stream.close();
                                    requestCompleteFuture.complete(null);
                                }
                            });

                            if (stream != null) {
                                stream.activate();
                            }
                        });
            });
        }

        // Wait for all Requests to complete
        for (CompletableFuture f : requestCompleteFutures) {
            f.join();
        }

        final int requiredSuccesses = (int) Math.floor(numRequests * 0.95);
        final int allowedFailures = numRequests - requiredSuccesses;

        // Verify we got some Http Status Code for each Request
        Assert.assertTrue(reqIdToStatus.size() >= requiredSuccesses);
        // Verify that the failure counts aren't too high
        Assert.assertTrue(numErrorCode.get() <= allowedFailures);
        Assert.assertTrue(numConnectionFailures.get() <= allowedFailures);
    }

    public void testParallelRequests(int numThreads, int numRequests) throws Exception {
        skipIfNetworkUnavailable();

        URI uri = new URI(endpoint);

        try (HttpClientConnectionManager connectionPool = createConnectionManager(uri, numThreads, NUM_CONNECTIONS)) {
            HttpRequest request = createHttpRequest("GET", endpoint, path, EMPTY_BODY);
            testParallelConnections(connectionPool, request, 1, numRequests);
        }

        CrtResource.logNativeResources();
        CrtResource.waitForNoResources();
    }

    public void testParallelRequestsWithLeakCheck(int numThreads, int numRequests) throws Exception {
        skipIfNetworkUnavailable();
        Callable<Void> fn = () -> {
            testParallelRequests(numThreads, numRequests);
            return null;
        };

        // Dalvik is SUPER STOCHASTIC about when it frees JVM memory, it has no observable correlation
        // to when System.gc() is called. Therefore, we cannot reliably sample it, so we don't bother.
        // If we have a leak, we should have it on all platforms, and we'll catch it elsewhere.
        if (CRT.getOSIdentifier() != "android") {
            int fixedGrowth = CrtMemoryLeakDetector.expectedFixedGrowth();
            fixedGrowth += (numThreads * GROWTH_PER_THREAD);
            // On Mac, JVM seems to expand by about 4K no matter how careful we are. With the workload
            // we're running, 8K worth of growth (an additional 4K for an increased healthy margin)
            // in the JVM only is acceptable.
            fixedGrowth = Math.max(fixedGrowth, 8192);
            CrtMemoryLeakDetector.leakCheck(NUM_ITERATIONS, fixedGrowth, fn);
        }
    }

    @Test
    public void testSerialRequests() throws Exception {
        testParallelRequestsWithLeakCheck(1, NUM_REQUESTS / NUM_THREADS);
    }

    /**
     * Test that the counters for the connection manager are correct (at least when used serially).
     */
    @Test
    public void testConnectionCounters() throws Exception {
        skipIfNetworkUnavailable();

        URI uri = new URI(endpoint);

        int maxConns = 3;

        try (HttpClientConnectionManager connectionPool = createConnectionManager(uri, 1, maxConns)) {
            Assert.assertEquals(maxConns, connectionPool.getMaxConnections());
            HttpManagerMetrics metrics = connectionPool.getManagerMetrics();
            Assert.assertNotNull(metrics);
            Assert.assertEquals(0, metrics.getAvailableConcurrency());
            Assert.assertEquals(0, metrics.getLeasedConcurrency());
            Assert.assertEquals(0, metrics.getPendingConcurrencyAcquires());

            List<HttpClientConnection> receivedClientConnections = new ArrayList<>();
            int giveUpCtr = 99;

            while(receivedClientConnections.size() < maxConns && giveUpCtr-- > 0) {
                CompletableFuture<HttpClientConnection> connectionAcquire = connectionPool.acquireConnection();
                try {
                    HttpClientConnection connection = connectionAcquire.get(3, TimeUnit.SECONDS);
                    receivedClientConnections.add(connection);
                } catch (CrtRuntimeException ignored) {
                }
            }

            if (giveUpCtr < 0) {
                Assert.fail("test connections where not acquired. Most likely you don't have a network connection.");
            }

            metrics = connectionPool.getManagerMetrics();
            // case pool of 3, 3 vended connections, none in flight.
            Assert.assertEquals(maxConns, metrics.getLeasedConcurrency());
            Assert.assertEquals(0, metrics.getPendingConcurrencyAcquires());
            Assert.assertEquals(0, metrics.getAvailableConcurrency());

            // case acquire 1, pool of 3, 3 vended, thus 1 in flight
            CompletableFuture<HttpClientConnection> connectionAcquire = connectionPool.acquireConnection();
            metrics = connectionPool.getManagerMetrics();
            Assert.assertEquals(1, metrics.getPendingConcurrencyAcquires());
            // should still be 0
            Assert.assertEquals(0, metrics.getAvailableConcurrency());

            // case release one, pool of 3, 3 vended, 1 in flight, 0 available. When we return 1, the other should be
            // able to complete. Pending should go back to 0.
            HttpClientConnection conn = receivedClientConnections.remove(0);
            connectionPool.releaseConnection(conn);

            conn = connectionAcquire.get(3, TimeUnit.SECONDS);

            metrics = connectionPool.getManagerMetrics();
            Assert.assertEquals(3, metrics.getLeasedConcurrency());
            Assert.assertEquals(0, metrics.getAvailableConcurrency());
            Assert.assertEquals(0, metrics.getPendingConcurrencyAcquires());

            connectionPool.releaseConnection(conn);
            metrics = connectionPool.getManagerMetrics();
            Assert.assertEquals(2, metrics.getLeasedConcurrency());
            Assert.assertEquals(1, metrics.getAvailableConcurrency());
            Assert.assertEquals(0, metrics.getPendingConcurrencyAcquires());

            conn = receivedClientConnections.remove(0);
            connectionPool.releaseConnection(conn);
            metrics = connectionPool.getManagerMetrics();
            Assert.assertEquals(1, metrics.getLeasedConcurrency());
            Assert.assertEquals(2, metrics.getAvailableConcurrency());
            Assert.assertEquals(0, metrics.getPendingConcurrencyAcquires());

            conn = receivedClientConnections.remove(0);
            connectionPool.releaseConnection(conn);
            metrics = connectionPool.getManagerMetrics();
            Assert.assertEquals(0, metrics.getLeasedConcurrency());
            Assert.assertEquals(3, metrics.getAvailableConcurrency());
            Assert.assertEquals(0, metrics.getPendingConcurrencyAcquires());
        }

        CrtResource.logNativeResources();
        CrtResource.waitForNoResources();
    }

    @Test
    public void testMaxParallelRequests() throws Exception {
        testParallelRequestsWithLeakCheck(NUM_THREADS, NUM_REQUESTS);
    }

    @Test
    public void testPendingAcquisitionsDuringShutdown() throws Exception {
        skipIfNetworkUnavailable();
        HttpClientConnection firstConnection = null;
        CompletableFuture<HttpClientConnection> firstAcquisition;
        CompletableFuture<HttpClientConnection> secondAcquisition;

        try (HttpClientConnectionManager connectionPool = createConnectionManager(new URI(endpoint), 1, 1)) {

            firstAcquisition = connectionPool.acquireConnection();
            secondAcquisition = connectionPool.acquireConnection();

            firstConnection = firstAcquisition.get();
        }

        firstConnection.close();
    }

    @Test
    public void testCancelAcquire() throws Exception {
        // related: https://github.com/awslabs/aws-sdk-kotlin/issues/511
        skipIfNetworkUnavailable();

        try (HttpClientConnectionManager connectionPool = createConnectionManager(new URI(endpoint), 1, 1)) {
            CompletableFuture<HttpClientConnection> firstAcquisition = connectionPool.acquireConnection();
            CompletableFuture<HttpClientConnection> secondAcquisition = connectionPool.acquireConnection();
            CompletableFuture<HttpClientConnection> thirdAcquisition = connectionPool.acquireConnection();

            HttpClientConnection firstConnection = firstAcquisition.get();

            // cancel acquisition and abandon it
            secondAcquisition.cancel(false);

            // return the first conn to the pool, future acquisitions should succeed
            firstConnection.close();

            // should succeed, will timeout if the second acquisition doesn't return the unused/abandoned conn to the pool
            HttpClientConnection conn = thirdAcquisition.get(500, TimeUnit.MILLISECONDS);
            conn.close();
        }
    }
}
