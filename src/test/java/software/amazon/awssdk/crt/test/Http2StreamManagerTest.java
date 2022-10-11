/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.test;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
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
import software.amazon.awssdk.crt.io.TlsContextOptions;
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

    private Http2StreamManager createStreamManager(URI uri, int numConnections, int maxStreams) {

        try (EventLoopGroup eventLoopGroup = new EventLoopGroup(1);
                HostResolver resolver = new HostResolver(eventLoopGroup);
                ClientBootstrap bootstrap = new ClientBootstrap(eventLoopGroup, resolver);
                SocketOptions sockOpts = new SocketOptions();
                TlsContextOptions tlsOpts = TlsContextOptions.createDefaultClient().withAlpnList("h2");
                TlsContext tlsContext = createHttpClientTlsContext(tlsOpts)) {
            Http2StreamManagerOptions options = new Http2StreamManagerOptions();
            if (maxStreams != 0) {
                options.withMaxConcurrentStreamsPerConnection(maxStreams)
                        .withIdealConcurrentStreamsPerConnection(maxStreams);
            }
            HttpClientConnectionManagerOptions connectionManagerOptions = new HttpClientConnectionManagerOptions();
            connectionManagerOptions.withClientBootstrap(bootstrap)
                    .withSocketOptions(sockOpts)
                    .withTlsContext(tlsContext)
                    .withUri(uri)
                    .withMaxConnections(numConnections);
            options.withConnectionManagerOptions(connectionManagerOptions);

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

                streamManager.acquireStream(request, new HttpStreamBaseResponseHandler() {
                    @Override
                    public void onResponseHeaders(HttpStreamBase stream, int responseStatusCode, int blockType,
                            HttpHeader[] nextHeaders) {
                        reqIdToStatus.put(requestId, responseStatusCode);
                    }

                    @Override
                    public void onResponseComplete(HttpStreamBase stream, int errorCode) {
                        if (errorCode != CRT.AWS_CRT_SUCCESS
                                || stream.getResponseStatusCode() != EXPECTED_HTTP_STATUS) {
                            Log.log(Log.LogLevel.Error, Log.LogSubject.HttpConnectionManager,
                                    String.format("Response completed with error: error_code=%s, response status=%d",
                                            CRT.awsErrorName(errorCode), stream.getResponseStatusCode()));
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
        // Verify that the failure counts aren't too high
        Assert.assertTrue(numErrorCode.get() <= allowedFailures);
        Assert.assertTrue(numStreamsFailures.get() <= allowedFailures);
    }

    public void testParallelRequests(int numThreads, int numRequests) throws Exception {
        skipIfNetworkUnavailable();

        URI uri = new URI(endpoint);

        try (Http2StreamManager streamManager = createStreamManager(uri, NUM_CONNECTIONS, 0)) {
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
        skipIfNetworkUnavailable();
        URI uri = new URI(endpoint);
        try (Http2StreamManager streamManager = createStreamManager(uri, NUM_CONNECTIONS, 0)) {
        }

        CrtResource.logNativeResources();
        CrtResource.waitForNoResources();
    }

    @Test
    public void testSerialRequests() throws Exception {
        skipIfNetworkUnavailable();
        testParallelRequestsWithLeakCheck(1, NUM_REQUESTS / NUM_THREADS);
    }

    @Test
    public void testMaxParallelRequests() throws Exception {
        skipIfNetworkUnavailable();
        testParallelRequestsWithLeakCheck(NUM_THREADS, NUM_REQUESTS);
    }

    @Test
    public void testStreamManagerMetrics() throws Exception {
        skipIfNetworkUnavailable();
        URI uri = new URI(endpoint);
        int maxStreams = 3;

        HttpStreamBaseResponseHandler nullHandler = new HttpStreamBaseResponseHandler() {
            @Override
            public void onResponseHeaders(HttpStreamBase stream, int responseStatusCode, int blockType, HttpHeader[] nextHeaders) {
                //do nothing, we're not going to make a request.
            }

            @Override
            public void onResponseComplete(HttpStreamBase stream, int errorCode) {
                //do nothing, we're not going to make a request.
            }
        };

        Http2Request request = createHttp2Request("GET", endpoint, path, EMPTY_BODY);

        try (Http2StreamManager streamManager = createStreamManager(uri, 1, maxStreams)) {
            HttpManagerMetrics metrics = streamManager.getManagerMetrics();
            Assert.assertNotNull(metrics);
            Assert.assertEquals(0, metrics.getAvailableConcurrency());
            Assert.assertEquals(0, metrics.getLeasedConcurrency());
            Assert.assertEquals(0, metrics.getPendingConcurrencyAcquires());

            List<Http2Stream> receivedStreams = new ArrayList<>();
            int giveUpCtr = 99;

            while(receivedStreams.size() < maxStreams && giveUpCtr-- > 0) {

                CompletableFuture<Http2Stream> streamFuture = streamManager.acquireStream(request, nullHandler);
                try {
                    Http2Stream stream = streamFuture.get(3, TimeUnit.SECONDS);
                    receivedStreams.add(stream);
                } catch (CrtRuntimeException ignored) {
                }
            }

            if (giveUpCtr < 0) {
                Assert.fail("test streams were not acquired. Most likely you don't have a network connection.");
            }

            metrics = streamManager.getManagerMetrics();
            // case pool of 3, 3 vended connections, none in flight.
            Assert.assertEquals(maxStreams, metrics.getLeasedConcurrency());
            Assert.assertEquals(0, metrics.getPendingConcurrencyAcquires());
            Assert.assertEquals(0, metrics.getAvailableConcurrency());

            // case acquire 1, pool of 3, 3 vended, thus 1 in flight
            CompletableFuture<Http2Stream> streamFuture = streamManager.acquireStream(request, nullHandler);
            metrics = streamManager.getManagerMetrics();
            Assert.assertEquals(1, metrics.getPendingConcurrencyAcquires());
            // should still be 0
            Assert.assertEquals(0, metrics.getAvailableConcurrency());

            Http2Stream stream = streamFuture.get();
            stream.close();

            for (Http2Stream h2Stream: receivedStreams) {
                h2Stream.close();
            }
        }

        CrtResource.logNativeResources();
        CrtResource.waitForNoResources();
    }
}
