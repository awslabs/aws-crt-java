package software.amazon.awssdk.crt.test;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;
import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.http.CrtHttpStreamHandler;
import software.amazon.awssdk.crt.http.HttpConnectionPoolManager;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.http.HttpStream;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.Log;

public class HttpConnectionManagerTest {
    private final static Charset UTF8 = StandardCharsets.UTF_8;
    private final static int NUM_THREADS = 10;
    private final static int NUM_CONNECTIONS = 20;
    private final static int NUM_REQUESTS = 100;
    private final static int EXPECTED_HTTP_STATUS = 200;
    private final static String endpoint = "https://aws-crt-test-stuff.s3.amazonaws.com";
    private final static String path = "/random_32_byte.data";
    private final String EMPTY_BODY = "";

    private HttpConnectionPoolManager createConnectionPool(URI uri, int numThreads, int numConnections) {
        try(ClientBootstrap bootstrap = new ClientBootstrap(numThreads);
            SocketOptions sockOpts = new SocketOptions();
            TlsContext tlsContext =  new TlsContext()) {

            return new HttpConnectionPoolManager(bootstrap, sockOpts, tlsContext, uri,
                HttpConnectionPoolManager.DEFAULT_MAX_BUFFER_SIZE, HttpConnectionPoolManager.DEFAULT_MAX_WINDOW_SIZE, numConnections);
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

    private void testParallelConnections(HttpConnectionPoolManager connPool, HttpRequest request, int numRequests) {
        final AtomicInteger numRequestsMade = new AtomicInteger(0);
        final AtomicInteger numConnectionFailures = new AtomicInteger(0);
        final ConcurrentHashMap<Integer, Integer> reqIdToStatus = new ConcurrentHashMap<>();
        final AtomicInteger numErrorCode = new AtomicInteger(0);

        List<CompletableFuture> requestCompleteFutures = new ArrayList<>();

        for (int i = 0; i < numRequests; i++) {

            Log.log(Log.LogLevel.Trace, String.format("Starting request %d", i));

            CompletableFuture requestCompleteFuture = new CompletableFuture();
            requestCompleteFutures.add(requestCompleteFuture);

            // Request a connection from the connection pool
            connPool.acquireConnection()
                // When the connection is acquired, submit a request on it
                .whenComplete((conn, throwable) -> {
                    if (throwable != null) {
                        numConnectionFailures.incrementAndGet();
                        connPool.releaseConnection(conn);
                        requestCompleteFuture.completeExceptionally(throwable);
                    }
                    Log.log(Log.LogLevel.Trace, "Acquired Connection");
                    int requestId = numRequestsMade.incrementAndGet();
                    conn.makeRequest(request, new CrtHttpStreamHandler() {
                        @Override
                        public void onResponseHeaders(HttpStream stream, int responseStatusCode, HttpHeader[] nextHeaders) {
                            reqIdToStatus.put(requestId, responseStatusCode);
                        }

                        @Override
                        public void onResponseComplete(HttpStream stream, int errorCode) {
                            Log.log(Log.LogLevel.Trace, "OnResponseComplete");
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

        }

        Log.log(Log.LogLevel.Trace, "Waiting on requests");

        // Wait for all Requests to complete
        for (CompletableFuture f: requestCompleteFutures) {
            f.join();
        }

        Log.log(Log.LogLevel.Trace, "All requests done");

        // Verify we got some Http Status Code for each Request
        Assert.assertEquals(numRequests, reqIdToStatus.size());

        // Verify Status code is Http 200 for each Request
        for (Integer status : reqIdToStatus.values()) {
            Assert.assertEquals(EXPECTED_HTTP_STATUS, status.intValue());
        }
        Assert.assertEquals(0, numErrorCode.get());
        Assert.assertEquals(0, numConnectionFailures.get());
    }

    @Test
    public void testParallelRequests() throws Exception {
        CrtResource.waitForNoResources();

        Log.log(Log.LogLevel.Trace, "BeginTest");

        URI uri = new URI(endpoint);

        try (HttpConnectionPoolManager connectionPool = createConnectionPool(uri, NUM_THREADS, NUM_CONNECTIONS)) {
            HttpRequest request = createHttpRequest("GET", endpoint, path, EMPTY_BODY);
            testParallelConnections(connectionPool, request, NUM_REQUESTS);
        }

        Log.log(Log.LogLevel.Trace, "EndTest");
        CrtResource.logNativeResources();

        CrtResource.waitForNoResources();
    }

}
