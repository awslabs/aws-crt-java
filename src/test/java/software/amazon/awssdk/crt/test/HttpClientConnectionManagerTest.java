package software.amazon.awssdk.crt.test;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.http.HttpClientConnectionManager;
import software.amazon.awssdk.crt.http.HttpClientConnectionManagerOptions;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpProxyOptions;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.http.HttpStreamResponseHandler;
import software.amazon.awssdk.crt.http.HttpStream;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.Log;

public class HttpClientConnectionManagerTest {
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

    static final String PROXY_HOST = System.getProperty("proxyhost");
    static final String PROXY_PORT = System.getProperty("proxyport");

    private HttpClientConnectionManager createConnectionManager(URI uri, int numThreads, int numConnections) {
        return createConnectionManager(uri, numThreads, numConnections, null, 0);
    }

    private HttpClientConnectionManager createConnectionManager(URI uri, int numThreads, int numConnections, String proxyHost, int proxyPort) {
        try (EventLoopGroup eventLoopGroup = new EventLoopGroup(1);
            HostResolver resolver = new HostResolver(eventLoopGroup);
            ClientBootstrap bootstrap = new ClientBootstrap(eventLoopGroup, resolver);
            SocketOptions sockOpts = new SocketOptions();
            TlsContext tlsContext =  new TlsContext()) {

            HttpProxyOptions proxyOptions = null;
            if (proxyHost != null) {
                proxyOptions = new HttpProxyOptions();
                proxyOptions.setHost(proxyHost);
                proxyOptions.setPort(proxyPort);
            }

            HttpClientConnectionManagerOptions options = new HttpClientConnectionManagerOptions();
            options.withClientBootstrap(bootstrap)
                .withSocketOptions(sockOpts)
                .withTlsContext(tlsContext)
                .withUri(uri)
                .withMaxConnections(numConnections)
                .withProxyOptions(proxyOptions);

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
                                connPool.releaseConnection(conn);
                                requestCompleteFuture.completeExceptionally(throwable);
                            }

                            int requestId = numRequestsMade.incrementAndGet();
                            conn.makeRequest(request, new HttpStreamResponseHandler() {
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

        // Verify Status code is Http 200 for each Request
        for (Integer status : reqIdToStatus.values()) {
            Assert.assertEquals(EXPECTED_HTTP_STATUS, status.intValue());
        }
        Assert.assertTrue(numErrorCode.get() <= allowedFailures);
        Assert.assertTrue(numConnectionFailures.get() <= allowedFailures);
    }

    public void testParallelRequests(int numThreads, int numRequests) throws Exception {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);

        CrtResource.waitForNoResources();

        URI uri = new URI(endpoint);

        try (HttpClientConnectionManager connectionPool = createConnectionManager(uri, numThreads, NUM_CONNECTIONS)) {
            HttpRequest request = createHttpRequest("GET", endpoint, path, EMPTY_BODY);
            testParallelConnections(connectionPool, request, 1, numRequests);
        }

        CrtResource.logNativeResources();
        CrtResource.waitForNoResources();
    }

    public void testParallelRequestsWithLeakCheck(int numThreads, int numRequests) throws Exception {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        Callable<Void> fn = () -> {
            testParallelRequests(numThreads, numRequests);
            Thread.sleep(2000); // wait for async shutdowns to complete
            return null;
        };

        
        int fixedGrowth = CrtMemoryLeakDetector.expectedFixedGrowth();
        CrtMemoryLeakDetector.leakCheck(NUM_ITERATIONS, fixedGrowth + (numThreads * GROWTH_PER_THREAD), fn);
    }

    @Test
    public void testSerialRequests() throws Exception {
        testParallelRequestsWithLeakCheck(1, NUM_REQUESTS / NUM_THREADS);
    }

    @Test
    public void testParallelRequests() throws Exception {
        testParallelRequestsWithLeakCheck(2, (NUM_REQUESTS / NUM_THREADS) * 2);
    }

    @Test
    public void testMaxParallelRequests() throws Exception {
        testParallelRequestsWithLeakCheck(NUM_THREADS, NUM_REQUESTS);
    }

    @Test
    public void testParallelRequestsWithLocalProxy() throws Exception {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);

        String proxyHost = PROXY_HOST;
        int proxyPort = 0;
        if (PROXY_PORT != null) {
            proxyPort = Integer.parseInt(PROXY_PORT);
        }

        Assume.assumeTrue(proxyHost != null && proxyHost.length() > 0 && proxyPort > 0);

        CrtResource.waitForNoResources();

        URI uri = new URI(endpoint);

        try (HttpClientConnectionManager connectionPool = createConnectionManager(uri, NUM_THREADS, NUM_CONNECTIONS, proxyHost, proxyPort)) {
            HttpRequest request = createHttpRequest("GET", endpoint, path, EMPTY_BODY);

            testParallelConnections(connectionPool, request, 1, NUM_REQUESTS);
        }

        CrtResource.waitForNoResources();
    }
}
