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
import software.amazon.awssdk.crt.http.Http2StreamManager;
import software.amazon.awssdk.crt.http.Http2Request;
import software.amazon.awssdk.crt.http.Http2Stream;
import software.amazon.awssdk.crt.http.Http2StreamManagerOptions;
import software.amazon.awssdk.crt.http.HttpClientConnection;
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

public class Http2StreamManagerTest extends HttpClientTestFixture {
    private final static Charset UTF8 = StandardCharsets.UTF_8;
    private final static int NUM_THREADS = 10;
    private final static int NUM_CONNECTIONS = 20;
    private final static int NUM_REQUESTS = 60;
    private final static int NUM_ITERATIONS = 10;
    private final static int GROWTH_PER_THREAD = 0; // expected VM footprint growth per thread
    private final static int EXPECTED_HTTP_STATUS = 200;
    private final static String endpoint = "https://d1cz66xoahf9cl.cloudfront.net/"; // Use cloudfront for HTTP/2
    private final static String path = "/random_32_byte.data";
    private final String EMPTY_BODY = "";

    private Http2StreamManager createStreamManager(URI uri, int numConnections) {
        try (EventLoopGroup eventLoopGroup = new EventLoopGroup(1);
                HostResolver resolver = new HostResolver(eventLoopGroup);
                ClientBootstrap bootstrap = new ClientBootstrap(eventLoopGroup, resolver);
                SocketOptions sockOpts = new SocketOptions();
                TlsContext tlsContext = createHttpClientTlsContext()) {

            Http2StreamManagerOptions options = new Http2StreamManagerOptions();
            options.withClientBootstrap(bootstrap)
                    .withSocketOptions(sockOpts)
                    .withTlsContext(tlsContext)
                    .withUri(uri)
                    .withMaxConnections(numConnections);

            return Http2StreamManager.create(options);
        }
    }

    private Http2Request createHttp2Request(String method, String endpoint, String path, String requestBody)
            throws Exception {
        URI uri = new URI(endpoint);
        HttpHeader[] requestHeaders = new HttpHeader[] {
                new HttpHeader(":method", method),
                new HttpHeader(":path", path),
                new HttpHeader(":scheme", uri.getScheme()),
                new HttpHeader(":authority", uri.getHost()),
                new HttpHeader("content-length", Integer.toString(requestBody.getBytes(UTF8).length))
        };
        Http2Request request = new Http2Request(requestHeaders, null);

        return request;
    }

    private void testParallelStreams(Http2StreamManager streamManager, Http2Request request, int numThreads,
            int numRequests) {
        final AtomicInteger numRequestsMade = new AtomicInteger(0);
        final AtomicInteger numStreamsFailures = new AtomicInteger(0);
        final ConcurrentHashMap<Integer, Integer> reqIdToStatus = new ConcurrentHashMap<>();
        final AtomicInteger numErrorCode = new AtomicInteger(0);

        final ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
        List<CompletableFuture<Void>> requestCompleteFutures = new ArrayList<>();

        for (int i = 0; i < numRequests; i++) {

            Log.log(Log.LogLevel.Trace, Log.LogSubject.HttpConnectionManager, String.format("Starting request %d", i));
            CompletableFuture<Void> requestCompleteFuture = new CompletableFuture<Void>();
            requestCompleteFutures.add(requestCompleteFuture);

            threadPool.execute(() -> {
                // Request a connection from the connection pool
                int requestId = numRequestsMade.incrementAndGet();
                streamManager.acquireStream(request, new HttpStreamResponseHandler() {
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
                        stream.close();
                        requestCompleteFuture.complete(null);
                    }
                }).whenComplete((stream, throwable) -> {
                    if (throwable != null) {
                        numStreamsFailures.incrementAndGet();
                        requestCompleteFuture.completeExceptionally(throwable);
                    }
                });
            });
        }

        // Wait for all Requests to complete
        for (CompletableFuture<Void> f : requestCompleteFutures) {
            f.join();
        }

        final int requiredSuccesses = (int) Math.floor(numRequests * 0.95);
        final int allowedFailures = numRequests - requiredSuccesses;

        // Verify we got some Http Status Code for each Request
        Assert.assertTrue(reqIdToStatus.size() >= requiredSuccesses);
        // Verify that the failure counts aren't too high ?????
        Assert.assertTrue(numErrorCode.get() <= allowedFailures);
        Assert.assertTrue(numStreamsFailures.get() <= allowedFailures);
    }

    public void testParallelRequests(int numThreads, int numRequests) throws Exception {
        skipIfNetworkUnavailable();

        URI uri = new URI(endpoint);

        try (Http2StreamManager streamManager = createStreamManager(uri, NUM_CONNECTIONS)) {
            Http2Request request = createHttp2Request("GET", endpoint, path, EMPTY_BODY);
            testParallelStreams(streamManager, request, 1, numRequests);
        }

        CrtResource.logNativeResources();
        CrtResource.waitForNoResources();
    }

    public void testParallelRequestsWithLeakCheck(int numThreads, int numRequests) throws Exception {
        skipIfNetworkUnavailable();
        Callable<Void> fn = () -> {
            testParallelRequests(numThreads, numRequests);
            Thread.sleep(2000); // wait for async shutdowns to complete
            return null;
        };

        // Dalvik is SUPER STOCHASTIC about when it frees JVM memory, it has no
        // observable correlation
        // to when System.gc() is called. Therefore, we cannot reliably sample it, so we
        // don't bother.
        // If we have a leak, we should have it on all platforms, and we'll catch it
        // elsewhere.
        if (CRT.getOSIdentifier() != "android") {
            int fixedGrowth = CrtMemoryLeakDetector.expectedFixedGrowth();
            fixedGrowth += (numThreads * GROWTH_PER_THREAD);
            // On Mac, JVM seems to expand by about 4K no matter how careful we are. With
            // the workload
            // we're running, 8K worth of growth (an additional 4K for an increased healthy
            // margin)
            // in the JVM only is acceptable.
            fixedGrowth = Math.max(fixedGrowth, 8192);
            CrtMemoryLeakDetector.leakCheck(NUM_ITERATIONS, fixedGrowth, fn);
        }
    }

    @Test
    public void testSanitizer() throws Exception {
        Log.initLoggingToStderr(Log.LogLevel.Trace);
        URI uri = new URI(endpoint);
        try (Http2StreamManager streamManager = createStreamManager(uri, NUM_CONNECTIONS)) {
        }

        CrtResource.logNativeResources();
        CrtResource.waitForNoResources();
    }

    @Test
    public void testSerialRequests() throws Exception {
        Log.initLoggingToStderr(Log.LogLevel.Trace);
        testParallelRequestsWithLeakCheck(1, NUM_REQUESTS / NUM_THREADS);
    }

    @Test
    public void testMaxParallelRequests() throws Exception {
        testParallelRequestsWithLeakCheck(NUM_THREADS, NUM_REQUESTS);
    }
}
