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
import software.amazon.awssdk.crt.http.CrtHttpStreamHandler;
import software.amazon.awssdk.crt.http.HttpClientConnectionManager;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.http.HttpStream;
import software.amazon.awssdk.crt.io.ClientBootstrap;
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

    private HttpClientConnectionManager createConnectionPool(URI uri, int numThreads, int numConnections) {
        try(ClientBootstrap bootstrap = new ClientBootstrap(numThreads);
            SocketOptions sockOpts = new SocketOptions();
            TlsContext tlsContext =  new TlsContext()) {

            return new HttpClientConnectionManager(bootstrap, sockOpts, tlsContext, uri,
                HttpClientConnectionManager.DEFAULT_MAX_BUFFER_SIZE, HttpClientConnectionManager.DEFAULT_MAX_WINDOW_SIZE, numConnections);
        }
    }

    private HttpRequest createHttpRequest(String method, String endpoint, String path, String requestBody) throws Exception{
        URI uri = new URI(endpoint);
        HttpHeader[] requestHeaders =
                new HttpHeader[]{
                        new HttpHeader("Host", uri.getHost()),
                        new HttpHeader("Content-Length", Integer.toString(requestBody.getBytes(UTF8).length))
                };
        HttpRequest request = new HttpRequest(method, path, requestHeaders);

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
                            conn.makeRequest(request, new CrtHttpStreamHandler() {
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
        for (CompletableFuture f: requestCompleteFutures) {
            f.join();
        }

        // Verify we got some Http Status Code for each Request
        Assert.assertEquals(numRequests, reqIdToStatus.size());

        // Verify Status code is Http 200 for each Request
        for (Integer status : reqIdToStatus.values()) {
            Assert.assertEquals(EXPECTED_HTTP_STATUS, status.intValue());
        }
        Assert.assertTrue(numErrorCode.get() < numRequests * 0.02);
        Assert.assertTrue(numConnectionFailures.get() < numRequests * 0.02);
    }

    public void testParallelRequests(int numThreads, int numRequests) throws Exception {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);

        CrtResource.waitForNoResources();

        URI uri = new URI(endpoint);
        try (HttpClientConnectionManager connectionPool = createConnectionPool(uri, numThreads, NUM_CONNECTIONS)) {
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
        System.out.println("testSerialRequests START");
        testParallelRequestsWithLeakCheck(1, NUM_REQUESTS / NUM_THREADS);
        System.out.println("testSerialRequests END");
    }

    @Test
    public void testParallelRequests() throws Exception {
        System.out.println("testParallelRequests START");
        testParallelRequestsWithLeakCheck(2, (NUM_REQUESTS / NUM_THREADS) * 2);
        System.out.println("testParallelRequests END");
    }

    @Test
    public void testMaxParallelRequests() throws Exception {
        System.out.println("testMaxParallelRequests START");
        testParallelRequestsWithLeakCheck(NUM_THREADS, NUM_REQUESTS);
        System.out.println("testMaxParallelRequests END");
    }
}
