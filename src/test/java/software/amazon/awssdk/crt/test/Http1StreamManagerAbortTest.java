/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.test;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Test;
import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.http.Http1StreamManager;
import software.amazon.awssdk.crt.http.HttpClientConnectionManagerOptions;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpManagerMetrics;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.http.HttpStream;
import software.amazon.awssdk.crt.http.HttpStreamBase;
import software.amazon.awssdk.crt.http.HttpStreamBaseResponseHandler;
import software.amazon.awssdk.crt.http.HttpStreamManager;
import software.amazon.awssdk.crt.http.HttpStreamManagerOptions;
import software.amazon.awssdk.crt.http.HttpVersion;
import software.amazon.awssdk.crt.http.Http2StreamManagerOptions;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;

/**
 * Tests for {@link Http1StreamManager} connection release guarantees when streams
 * are cancelled or aborted externally (e.g., due to SDK timeout).
 */
public class Http1StreamManagerAbortTest extends HttpRequestResponseFixture {
    private final static String ENDPOINT = "https://d1cz66xoahf9cl.cloudfront.net";
    private final static String PATH = "/random_32_byte.data";
    private final static int MAX_CONNECTIONS = 3;

    private HttpStreamManager createStreamManager(URI uri, int maxConnections) {
        try (EventLoopGroup eventLoopGroup = new EventLoopGroup(1);
                HostResolver resolver = new HostResolver(eventLoopGroup);
                ClientBootstrap bootstrap = new ClientBootstrap(eventLoopGroup, resolver);
                SocketOptions sockOpts = new SocketOptions();
                TlsContextOptions tlsOpts = TlsContextOptions.createDefaultClient().withAlpnList("http/1.1");
                TlsContext tlsContext = createHttpClientTlsContext(tlsOpts)) {
            HttpClientConnectionManagerOptions h1Options = new HttpClientConnectionManagerOptions()
                    .withClientBootstrap(bootstrap)
                    .withSocketOptions(sockOpts)
                    .withTlsContext(tlsContext)
                    .withUri(uri)
                    .withMaxConnections(maxConnections);
            Http2StreamManagerOptions h2Options = new Http2StreamManagerOptions()
                    .withConnectionManagerOptions(h1Options);
            HttpStreamManagerOptions options = new HttpStreamManagerOptions()
                    .withHTTP1ConnectionManagerOptions(h1Options)
                    .withHTTP2StreamManagerOptions(h2Options)
                    .withExpectedProtocol(HttpVersion.HTTP_1_1);
            return HttpStreamManager.create(options);
        }
    }

    private HttpRequest createRequest(URI uri) {
        HttpHeader[] headers = new HttpHeader[] {
                new HttpHeader("host", uri.getHost()),
                new HttpHeader("content-length", "0")
        };
        return new HttpRequest("GET", PATH, headers, null);
    }

    /**
     * Verify that {@code abortStream()} on an activated stream releases the
     * connection slot back to the pool.
     *
     * <p>The handler returns 0 from onResponseBody to prevent the response window from
     * advancing, keeping the stream pinned in the "body in progress" state until we abort.
     */
    @Test
    public void testAbortStreamAfterActivateReleasesConnection() throws Exception {
        skipIfNetworkUnavailable();
        URI uri = new URI(ENDPOINT);

        try (HttpStreamManager streamManager = createStreamManager(uri, MAX_CONNECTIONS)) {
            HttpRequest request = createRequest(uri);

            final CompletableFuture<Void> headersReceived = new CompletableFuture<>();
            HttpStreamBaseResponseHandler handler = new HttpStreamBaseResponseHandler() {
                @Override
                public void onResponseHeaders(HttpStreamBase stream, int responseStatusCode, int blockType,
                        HttpHeader[] nextHeaders) {
                    headersReceived.complete(null);
                }

                @Override
                public int onResponseBody(HttpStreamBase stream, byte[] bodyBytesIn) {
                    return 0;
                }

                @Override
                public void onResponseComplete(HttpStreamBase stream, int errorCode) {
                }
            };

            HttpStreamBase stream = streamManager.acquireStream(request, handler).get(60, TimeUnit.SECONDS);
            headersReceived.get(60, TimeUnit.SECONDS);

            HttpManagerMetrics metrics = streamManager.getManagerMetrics();
            Assert.assertEquals("Should have 1 leased connection", 1, metrics.getLeasedConcurrency());

            streamManager.abortStream(stream);

            Thread.sleep(500);

            metrics = streamManager.getManagerMetrics();
            Assert.assertEquals("Leased concurrency should return to 0 after abort",
                    0, metrics.getLeasedConcurrency());
        }

        CrtResource.waitForNoResources();
    }

    /**
     * Verify that direct {@code cancel()} + {@code close()} on an activated stream
     * still releases the connection slot (due to the AtomicBoolean guard and
     * onResponseComplete being triggered by cancel on an active stream).
     */
    @Test
    public void testDirectCancelCloseOnActivatedStreamReleasesConnection() throws Exception {
        skipIfNetworkUnavailable();
        URI uri = new URI(ENDPOINT);

        try (HttpStreamManager streamManager = createStreamManager(uri, MAX_CONNECTIONS)) {
            HttpRequest request = createRequest(uri);

            final CompletableFuture<Void> headersReceived = new CompletableFuture<>();
            HttpStreamBaseResponseHandler handler = new HttpStreamBaseResponseHandler() {
                @Override
                public void onResponseHeaders(HttpStreamBase stream, int responseStatusCode, int blockType,
                        HttpHeader[] nextHeaders) {
                    headersReceived.complete(null);
                }

                @Override
                public int onResponseBody(HttpStreamBase stream, byte[] bodyBytesIn) {
                    return 0;
                }

                @Override
                public void onResponseComplete(HttpStreamBase stream, int errorCode) {
                }
            };

            HttpStreamBase stream = streamManager.acquireStream(request, handler).get(60, TimeUnit.SECONDS);
            headersReceived.get(60, TimeUnit.SECONDS);

            HttpManagerMetrics metrics = streamManager.getManagerMetrics();
            Assert.assertEquals(1, metrics.getLeasedConcurrency());

            // Simulate what the SDK does on timeout: direct cancel + close
            stream.cancel();
            stream.close();

            Thread.sleep(500);

            metrics = streamManager.getManagerMetrics();
            Assert.assertEquals("Leased concurrency should return to 0 after direct cancel+close",
                    0, metrics.getLeasedConcurrency());
        }

        CrtResource.waitForNoResources();
    }

    /**
     * Verify that aborting all streams in a full pool does not permanently exhaust it.
     * After aborting N streams (where N = maxConcurrency), a new request must still succeed.
     */
    @Test
    public void testPoolRecoveryAfterMultipleAborts() throws Exception {
        skipIfNetworkUnavailable();
        URI uri = new URI(ENDPOINT);

        try (HttpStreamManager streamManager = createStreamManager(uri, MAX_CONNECTIONS)) {
            HttpRequest request = createRequest(uri);

            for (int i = 0; i < MAX_CONNECTIONS; i++) {
                final CompletableFuture<Void> headersReceived = new CompletableFuture<>();
                HttpStreamBaseResponseHandler handler = new HttpStreamBaseResponseHandler() {
                    @Override
                    public void onResponseHeaders(HttpStreamBase stream, int responseStatusCode, int blockType,
                            HttpHeader[] nextHeaders) {
                        headersReceived.complete(null);
                    }

                    @Override
                    public void onResponseComplete(HttpStreamBase stream, int errorCode) {
                    }
                };

                HttpStreamBase stream = streamManager.acquireStream(request, handler).get(60, TimeUnit.SECONDS);
                headersReceived.get(60, TimeUnit.SECONDS);
                streamManager.abortStream(stream);
            }

            Thread.sleep(1000);

            // Pool should still work: a new request must succeed
            final CompletableFuture<Void> reqCompleted = new CompletableFuture<>();
            final AtomicInteger responseStatus = new AtomicInteger(-1);

            HttpStreamBaseResponseHandler handler = new HttpStreamBaseResponseHandler() {
                @Override
                public void onResponseHeaders(HttpStreamBase stream, int responseStatusCode, int blockType,
                        HttpHeader[] nextHeaders) {
                    responseStatus.set(responseStatusCode);
                }

                @Override
                public void onResponseComplete(HttpStreamBase stream, int errorCode) {
                    reqCompleted.complete(null);
                    stream.close();
                }
            };

            streamManager.acquireStream(request, handler).get(10, TimeUnit.SECONDS);
            reqCompleted.get(10, TimeUnit.SECONDS);

            Assert.assertEquals("Pool should still serve requests after aborting all connections",
                    200, responseStatus.get());
        }

        CrtResource.waitForNoResources();
    }

    /**
     * Verify that {@code abortStream(null)} is a safe no-op — covers the case where
     * the stream was never acquired (e.g., the request timed out waiting for a connection).
     */
    @Test
    public void testAbortStreamWithNullIsNoOp() throws Exception {
        skipIfNetworkUnavailable();
        URI uri = new URI(ENDPOINT);

        try (HttpStreamManager streamManager = createStreamManager(uri, MAX_CONNECTIONS)) {
            // Should not throw
            streamManager.abortStream(null);

            HttpManagerMetrics metrics = streamManager.getManagerMetrics();
            Assert.assertEquals(0, metrics.getLeasedConcurrency());
        }

        CrtResource.waitForNoResources();
    }

    /**
     * Verify that calling {@code abortStream()} multiple times on the same stream
     * is idempotent and does not double-release the connection.
     */
    @Test
    public void testAbortStreamIsIdempotent() throws Exception {
        skipIfNetworkUnavailable();
        URI uri = new URI(ENDPOINT);

        try (HttpStreamManager streamManager = createStreamManager(uri, MAX_CONNECTIONS)) {
            HttpRequest request = createRequest(uri);

            final CompletableFuture<Void> headersReceived = new CompletableFuture<>();
            HttpStreamBaseResponseHandler handler = new HttpStreamBaseResponseHandler() {
                @Override
                public void onResponseHeaders(HttpStreamBase stream, int responseStatusCode, int blockType,
                        HttpHeader[] nextHeaders) {
                    headersReceived.complete(null);
                }

                @Override
                public int onResponseBody(HttpStreamBase stream, byte[] bodyBytesIn) {
                    return 0;
                }

                @Override
                public void onResponseComplete(HttpStreamBase stream, int errorCode) {
                }
            };

            HttpStreamBase stream = streamManager.acquireStream(request, handler).get(60, TimeUnit.SECONDS);
            headersReceived.get(60, TimeUnit.SECONDS);

            // Abort multiple times — should not throw or cause issues
            streamManager.abortStream(stream);
            streamManager.abortStream(stream);
            streamManager.abortStream(stream);

            Thread.sleep(500);

            HttpManagerMetrics metrics = streamManager.getManagerMetrics();
            Assert.assertEquals("Should have 0 leased after idempotent aborts",
                    0, metrics.getLeasedConcurrency());

            // Pool should still function
            final CompletableFuture<Void> reqCompleted = new CompletableFuture<>();
            final AtomicInteger responseStatus = new AtomicInteger(-1);

            HttpStreamBaseResponseHandler handler2 = new HttpStreamBaseResponseHandler() {
                @Override
                public void onResponseHeaders(HttpStreamBase stream2, int responseStatusCode, int blockType,
                        HttpHeader[] nextHeaders) {
                    responseStatus.set(responseStatusCode);
                }

                @Override
                public void onResponseComplete(HttpStreamBase stream2, int errorCode) {
                    reqCompleted.complete(null);
                    stream2.close();
                }
            };

            streamManager.acquireStream(request, handler2).get(10, TimeUnit.SECONDS);
            reqCompleted.get(10, TimeUnit.SECONDS);
            Assert.assertEquals(200, responseStatus.get());
        }

        CrtResource.waitForNoResources();
    }

    /**
     * Verify that aborting a stream that has already completed normally is safe.
     * The connection was already released by {@code onResponseComplete} — aborting
     * after the fact should be a no-op (no double-release).
     */
    @Test
    public void testAbortStreamAfterNormalCompletion() throws Exception {
        skipIfNetworkUnavailable();
        URI uri = new URI(ENDPOINT);

        try (HttpStreamManager streamManager = createStreamManager(uri, MAX_CONNECTIONS)) {
            HttpRequest request = createRequest(uri);

            final CompletableFuture<Void> reqCompleted = new CompletableFuture<>();
            final AtomicReference<HttpStreamBase> streamRef = new AtomicReference<>();

            HttpStreamBaseResponseHandler handler = new HttpStreamBaseResponseHandler() {
                @Override
                public void onResponseHeaders(HttpStreamBase stream, int responseStatusCode, int blockType,
                        HttpHeader[] nextHeaders) {
                }

                @Override
                public int onResponseBody(HttpStreamBase stream, byte[] bodyBytesIn) {
                    return bodyBytesIn.length;
                }

                @Override
                public void onResponseComplete(HttpStreamBase stream, int errorCode) {
                    streamRef.set(stream);
                    reqCompleted.complete(null);
                    stream.close();
                }
            };

            streamManager.acquireStream(request, handler).get(60, TimeUnit.SECONDS);
            reqCompleted.get(60, TimeUnit.SECONDS);

            Thread.sleep(200);
            HttpManagerMetrics metrics = streamManager.getManagerMetrics();
            Assert.assertEquals(0, metrics.getLeasedConcurrency());

            // Abort after completion should be a no-op (stream already closed/null)
            streamManager.abortStream(streamRef.get());

            metrics = streamManager.getManagerMetrics();
            Assert.assertEquals(0, metrics.getLeasedConcurrency());
        }

        CrtResource.waitForNoResources();
    }

    /**
     * Stress test: rapidly acquire and abort streams to verify no connection leaks
     * under repeated abort pressure.
     */
    @Test
    public void testRepeatedAbortDoesNotLeakConnections() throws Exception {
        skipIfNetworkUnavailable();
        URI uri = new URI(ENDPOINT);

        final int numAbortCycles = 10;

        try (HttpStreamManager streamManager = createStreamManager(uri, MAX_CONNECTIONS)) {
            HttpRequest request = createRequest(uri);

            for (int i = 0; i < numAbortCycles; i++) {
                final CompletableFuture<Void> headersReceived = new CompletableFuture<>();
                HttpStreamBaseResponseHandler handler = new HttpStreamBaseResponseHandler() {
                    @Override
                    public void onResponseHeaders(HttpStreamBase stream, int responseStatusCode, int blockType,
                            HttpHeader[] nextHeaders) {
                        headersReceived.complete(null);
                    }

                    @Override
                    public void onResponseComplete(HttpStreamBase stream, int errorCode) {
                    }
                };

                HttpStreamBase stream = streamManager.acquireStream(request, handler).get(30, TimeUnit.SECONDS);
                headersReceived.get(30, TimeUnit.SECONDS);
                streamManager.abortStream(stream);

                Thread.sleep(100);
            }

            Thread.sleep(500);
            HttpManagerMetrics metrics = streamManager.getManagerMetrics();
            Assert.assertEquals("All connections should be released after " + numAbortCycles + " abort cycles",
                    0, metrics.getLeasedConcurrency());

            // Final verification: pool can serve a normal request
            final CompletableFuture<Void> reqCompleted = new CompletableFuture<>();
            final AtomicInteger responseStatus = new AtomicInteger(-1);

            HttpStreamBaseResponseHandler finalHandler = new HttpStreamBaseResponseHandler() {
                @Override
                public void onResponseHeaders(HttpStreamBase stream, int responseStatusCode, int blockType,
                        HttpHeader[] nextHeaders) {
                    responseStatus.set(responseStatusCode);
                }

                @Override
                public void onResponseComplete(HttpStreamBase stream, int errorCode) {
                    reqCompleted.complete(null);
                    stream.close();
                }
            };

            streamManager.acquireStream(request, finalHandler).get(10, TimeUnit.SECONDS);
            reqCompleted.get(10, TimeUnit.SECONDS);
            Assert.assertEquals(200, responseStatus.get());
        }

        CrtResource.waitForNoResources();
    }
}
