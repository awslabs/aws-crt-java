/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.test;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Test;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.http.Http1StreamManager;
import software.amazon.awssdk.crt.http.HttpClientConnectionManagerOptions;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.http.HttpStream;
import software.amazon.awssdk.crt.http.HttpStreamBase;
import software.amazon.awssdk.crt.http.HttpStreamBaseResponseHandler;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;

/**
 * Verifies that all stream callbacks run on the same event-loop thread.
 * This invariant is what makes it safe to call activate() before completing
 * the future in {@link Http1StreamManager} — since the connection-acquired
 * callback and all stream callbacks share the same single-threaded event loop,
 * no stream callback can fire until the connection-acquired callback returns.
 */
public class Http1StreamManagerThreadAffinityTest extends HttpRequestResponseFixture {
    private final static String ENDPOINT = "https://d1cz66xoahf9cl.cloudfront.net";
    private final static String PATH = "/random_32_byte.data";

    /**
     * Verify that onResponseHeaders, onResponseBody, and onResponseComplete all run
     * on the same thread. Since the connection manager guarantees the acquire callback
     * also runs on this thread (see connection_manager.c:461-470), this proves all
     * operations happen on a single event-loop thread with no interleaving possible.
     */
    @Test
    public void testAllStreamCallbacksRunOnSameThread() throws Exception {
        skipIfNetworkUnavailable();
        URI uri = new URI(ENDPOINT);

        try (EventLoopGroup eventLoopGroup = new EventLoopGroup(0);
                HostResolver resolver = new HostResolver(eventLoopGroup);
                ClientBootstrap bootstrap = new ClientBootstrap(eventLoopGroup, resolver);
                SocketOptions sockOpts = new SocketOptions();
                TlsContextOptions tlsOpts = TlsContextOptions.createDefaultClient().withAlpnList("http/1.1");
                TlsContext tlsContext = createHttpClientTlsContext(tlsOpts);
                Http1StreamManager streamManager = Http1StreamManager.create(
                        new HttpClientConnectionManagerOptions()
                                .withClientBootstrap(bootstrap)
                                .withSocketOptions(sockOpts)
                                .withTlsContext(tlsContext)
                                .withUri(uri)
                                .withMaxConnections(1))) {

            HttpHeader[] headers = new HttpHeader[] {
                    new HttpHeader("host", uri.getHost()),
                    new HttpHeader("content-length", "0")
            };
            HttpRequest request = new HttpRequest("GET", PATH, headers, null);

            final AtomicReference<String> acquiredThread = new AtomicReference<>();
            final AtomicReference<String> headersThread = new AtomicReference<>();
            final AtomicReference<String> bodyThread = new AtomicReference<>();
            final AtomicReference<String> completeThread = new AtomicReference<>();
            final CompletableFuture<Void> done = new CompletableFuture<>();

            HttpStreamBaseResponseHandler handler = new HttpStreamBaseResponseHandler() {
                @Override
                public void onResponseHeaders(HttpStreamBase stream, int responseStatusCode, int blockType,
                        HttpHeader[] nextHeaders) {
                    headersThread.set(Thread.currentThread().getName());
                }

                @Override
                public int onResponseBody(HttpStreamBase stream, byte[] bodyBytesIn) {
                    bodyThread.set(Thread.currentThread().getName());
                    return bodyBytesIn.length;
                }

                @Override
                public void onResponseComplete(HttpStreamBase stream, int errorCode) {
                    completeThread.set(Thread.currentThread().getName());
                    done.complete(null);
                    stream.close();
                }
            };

            // The future is completed from the event-loop thread inside Http1StreamManager's
            // whenComplete callback. A chained whenComplete runs inline on the completing
            // thread, so this captures the connection-acquired event-loop thread name.
            streamManager.acquireStream(request, handler).whenComplete((stream, ex) -> {
                acquiredThread.set(Thread.currentThread().getName());
            });

            done.get(60, TimeUnit.SECONDS);

            // Verify all callbacks ran on the same event-loop thread as connection-acquired
            Assert.assertNotNull("acquireStream future should have completed", acquiredThread.get());
            Assert.assertNotNull("onResponseHeaders should have been called", headersThread.get());
            Assert.assertNotNull("onResponseComplete should have been called", completeThread.get());

            Assert.assertEquals(
                    "onResponseHeaders must run on same thread as connection-acquired",
                    acquiredThread.get(), headersThread.get());

            if (bodyThread.get() != null) {
                Assert.assertEquals(
                        "onResponseBody must run on same thread as connection-acquired",
                        acquiredThread.get(), bodyThread.get());
            }

            Assert.assertEquals(
                    "onResponseComplete must run on same thread as connection-acquired",
                    acquiredThread.get(), completeThread.get());

            // Verify it's NOT the test's main thread (confirming it's an event-loop thread)
            Assert.assertNotEquals(
                    "Stream callbacks must not run on the caller's thread",
                    Thread.currentThread().getName(), completeThread.get());
        }

        CrtResource.waitForNoResources();
    }
}
