/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.test;

import org.junit.Assert;
import org.junit.Test;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.http.*;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class WriteDataTest extends HttpRequestResponseFixture {
    private final static String HOST = "https://localhost";
    private final static int H1_TLS_PORT = 8082;
    private final static int H2_TLS_PORT = 3443;

    /**
     * Build an {@link HttpStreamManager} suitable for the localhost mock server
     * (self-signed cert -> verify peer disabled). Mirrors the setup used by
     * the stream-manager and connection-pool tests for localhost.
     */
    private HttpStreamManager createLocalhostStreamManager(URI uri, HttpVersion expectedVersion) {
        try (EventLoopGroup eventLoopGroup = new EventLoopGroup(1);
                HostResolver resolver = new HostResolver(eventLoopGroup);
                ClientBootstrap bootstrap = new ClientBootstrap(eventLoopGroup, resolver);
                SocketOptions sockOpts = new SocketOptions();
                TlsContextOptions tlsOpts = (expectedVersion == HttpVersion.HTTP_2
                        ? TlsContextOptions.createDefaultClient().withAlpnList("h2")
                        : TlsContextOptions.createDefaultClient().withAlpnList("http/1.1"))
                        .withVerifyPeer(false);
                TlsContext tlsContext = createHttpClientTlsContext(tlsOpts)) {
            HttpClientConnectionManagerOptions h1Options = new HttpClientConnectionManagerOptions()
                    .withClientBootstrap(bootstrap)
                    .withSocketOptions(sockOpts)
                    .withTlsContext(tlsContext)
                    .withUri(uri)
                    .withMaxConnections(1);
            Http2StreamManagerOptions h2Options = new Http2StreamManagerOptions()
                    .withConnectionManagerOptions(h1Options);
            HttpStreamManagerOptions options = new HttpStreamManagerOptions()
                    .withHTTP1ConnectionManagerOptions(h1Options)
                    .withHTTP2StreamManagerOptions(h2Options)
                    .withExpectedProtocol(expectedVersion);
            return HttpStreamManager.create(options);
        }
    }

    @Test
    public void testHttp2WriteData() throws Exception {
        skipIfAndroid();
        skipIfLocalhostUnavailable();

        URI uri = new URI(HOST + ":" + H2_TLS_PORT);
        String expectedBody = "hello from writeData";
        int expectedLen = expectedBody.getBytes(StandardCharsets.UTF_8).length;

        HttpHeader[] headers = new HttpHeader[]{
            new HttpHeader(":method", "PUT"),
            new HttpHeader(":path", "/echo"),
            new HttpHeader(":scheme", "https"),
            new HttpHeader(":authority", uri.getHost()),
            new HttpHeader("content-length", Integer.toString(expectedLen)),
        };
        Http2Request request = new Http2Request(headers, null);

        CompletableFuture<Void> reqCompleted = new CompletableFuture<>();
        TestHttpResponse response = new TestHttpResponse();

        CompletableFuture<Void> shutdownComplete;
        try (HttpClientConnectionManager connPool = createConnectionPoolManager(uri, HttpVersion.HTTP_2)) {
            shutdownComplete = connPool.getShutdownCompleteFuture();
            try (Http2ClientConnection conn = (Http2ClientConnection) connPool.acquireConnection()
                    .get(60, TimeUnit.SECONDS)) {

                HttpStreamBaseResponseHandler streamHandler = new HttpStreamBaseResponseHandler() {
                    @Override
                    public void onResponseHeaders(HttpStreamBase stream, int responseStatusCode, int blockType,
                            HttpHeader[] nextHeaders) {
                        response.statusCode = responseStatusCode;
                        response.headers.addAll(Arrays.asList(nextHeaders));
                    }

                    @Override
                    public int onResponseBody(HttpStreamBase stream, byte[] bodyBytesIn) {
                        response.bodyBuffer.put(bodyBytesIn);
                        return bodyBytesIn.length;
                    }

                    @Override
                    public void onResponseComplete(HttpStreamBase stream, int errorCode) {
                        response.onCompleteErrorCode = errorCode;
                        reqCompleted.complete(null);
                    }
                };

                try (Http2Stream stream = conn.makeRequest(request, streamHandler, true)) {
                    stream.activate();

                    /*
                     * Issue the write from a helper that:
                     *   1) allocates the byte[] locally, so once the helper returns the
                     *      caller's stack has no reference to it,
                     *   2) captures only a WeakReference to the array in its inner callback
                     *      (not the byte[] itself) so the lambda doesn't accidentally become
                     *      a Java-side GC root,
                     *   3) performs the "is the native GlobalRef still holding the array?"
                     *      assertion INSIDE the write-completion callback -- which is
                     *      guaranteed by the native code to run BEFORE native cleanup
                     *      (Release + DeleteGlobalRef) in s_write_data_complete.
                     *
                     * If NewGlobalRef on callback_data were missing or dropped early, the
                     * array would be unreachable from Java-land at the moment the callback
                     * fires, System.gc() inside the callback would clear the WeakReference,
                     * and the helper would complete the returned future exceptionally.
                     *
                     * Caveat: GetByteArrayElements may pin the array, and on some JVMs a
                     * pin can also keep the object GC-reachable. A pass here therefore
                     * proves "native side is keeping the array alive somehow" rather than
                     * strictly "GlobalRef is what is keeping it alive". Combined with the
                     * echo-body-matches assertion below (which would fail on any data
                     * corruption from premature release), this covers the interesting cases.
                     */
                    CompletableFuture<Void> writeFuture = issueWriteAndAssertReachable(stream, expectedBody);
                    writeFuture.get(5, TimeUnit.SECONDS);
                    reqCompleted.get(60, TimeUnit.SECONDS);
                }
            }
        }

        Assert.assertEquals(CRT.AWS_CRT_SUCCESS, response.onCompleteErrorCode);
        Assert.assertEquals(200, response.statusCode);
        // /echo returns JSON: {"body": "<sent>", "bytes": <len>}
        String body = response.getBody();
        Assert.assertTrue("Response should contain sent body intact: " + body,
                body.contains("\"body\": \"" + expectedBody + "\""));
        Assert.assertTrue("Response should contain byte count: " + body,
                body.contains("\"bytes\": " + expectedLen));

        shutdownComplete.get(60, TimeUnit.SECONDS);
        CrtResource.waitForNoResources();
    }

    /**
     * Allocates the payload locally and issues writeData. Inside the write-completion
     * callback (which runs on the event-loop thread BEFORE native cleanup -- see
     * s_write_data_complete in http_request_response.c), forces GC and asserts that a
     * WeakReference to the payload is still live. A pass means the native side is keeping
     * the array reachable from the JVM's point of view for the duration of the async write,
     * which is what the NewGlobalRef stored on callback_data is there to guarantee.
     *
     * The lambda captures {@code weak} and {@code future}, never {@code payload} directly,
     * so the lambda itself does not become a Java-side strong root for the array. Once this
     * method returns, the local {@code payload} is out of scope and the only thing
     * reachable from Java-land is whatever the native layer holds.
     */
    private CompletableFuture<Void> issueWriteAndAssertReachable(HttpStreamBase stream, String body) {
        byte[] payload = body.getBytes(StandardCharsets.UTF_8);
        WeakReference<byte[]> weak = new WeakReference<>(payload);
        CompletableFuture<Void> future = new CompletableFuture<>();

        stream.writeData(payload, true, (errorCode) -> {
            try {
                /* We're inside CallVoidMethod on the event-loop thread; native cleanup has
                 * not yet run, so the native GlobalRef is still holding the array. Trigger
                 * a collection attempt and verify the WeakReference is still live. */
                System.gc();
                if (weak.get() == null) {
                    future.completeExceptionally(new AssertionError(
                            "byte[] was reclaimed while native should still hold a GlobalRef on it "
                                    + "(NewGlobalRef on callback_data missing or dropped early?)"));
                    return;
                }
                if (errorCode != 0) {
                    future.completeExceptionally(new RuntimeException(
                            "writeData failed with errorCode=" + errorCode));
                } else {
                    future.complete(null);
                }
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });

        return future;
    }

    @Test
    public void testHttp2WriteDataEndStreamOnly() throws Exception {
        skipIfAndroid();
        skipIfLocalhostUnavailable();

        URI uri = new URI(HOST + ":" + H2_TLS_PORT);

        HttpHeader[] headers = new HttpHeader[]{
            new HttpHeader(":method", "GET"),
            new HttpHeader(":path", "/echo"),
            new HttpHeader(":scheme", "https"),
            new HttpHeader(":authority", uri.getHost()),
        };
        Http2Request request = new Http2Request(headers, null);

        CompletableFuture<Void> reqCompleted = new CompletableFuture<>();
        TestHttpResponse response = new TestHttpResponse();

        CompletableFuture<Void> shutdownComplete;
        try (HttpClientConnectionManager connPool = createConnectionPoolManager(uri, HttpVersion.HTTP_2)) {
            shutdownComplete = connPool.getShutdownCompleteFuture();
            try (Http2ClientConnection conn = (Http2ClientConnection) connPool.acquireConnection()
                    .get(60, TimeUnit.SECONDS)) {

                HttpStreamBaseResponseHandler streamHandler = new HttpStreamBaseResponseHandler() {
                    @Override
                    public void onResponseHeaders(HttpStreamBase stream, int responseStatusCode, int blockType,
                            HttpHeader[] nextHeaders) {
                        response.statusCode = responseStatusCode;
                        response.headers.addAll(Arrays.asList(nextHeaders));
                    }

                    @Override
                    public int onResponseBody(HttpStreamBase stream, byte[] bodyBytesIn) {
                        response.bodyBuffer.put(bodyBytesIn);
                        return bodyBytesIn.length;
                    }

                    @Override
                    public void onResponseComplete(HttpStreamBase stream, int errorCode) {
                        response.onCompleteErrorCode = errorCode;
                        reqCompleted.complete(null);
                    }
                };

                // Use manual writes but send null data with endStream=true (zero-byte body)
                try (Http2Stream stream = conn.makeRequest(request, streamHandler, true)) {
                    stream.activate();
                    stream.writeData(null, true).get(5, TimeUnit.SECONDS);
                    reqCompleted.get(60, TimeUnit.SECONDS);
                }
            }
        }

        Assert.assertEquals(CRT.AWS_CRT_SUCCESS, response.onCompleteErrorCode);
        Assert.assertEquals(200, response.statusCode);
        // /echo returns JSON: {"body": "", "bytes": 0}
        String body = response.getBody();
        Assert.assertTrue("Response should contain zero bytes: " + body,
                body.contains("\"bytes\": 0"));

        shutdownComplete.get(60, TimeUnit.SECONDS);
        CrtResource.waitForNoResources();
    }

    @Test
    public void testHttp1WriteData() throws Exception {
        skipIfAndroid();
        skipIfLocalhostUnavailable();

        URI uri = new URI(HOST + ":" + H1_TLS_PORT);
        String expectedBody = "hello from writeData h1";
        int expectedLen = expectedBody.getBytes(StandardCharsets.UTF_8).length;

        HttpHeader[] headers = new HttpHeader[]{
            new HttpHeader("Host", uri.getHost()),
            new HttpHeader("Content-Length", Integer.toString(expectedLen)),
        };
        HttpRequest request = new HttpRequest("PUT", "/echo", headers, null);

        CompletableFuture<Void> reqCompleted = new CompletableFuture<>();
        TestHttpResponse response = new TestHttpResponse();

        CompletableFuture<Void> shutdownComplete;
        try (HttpClientConnectionManager connPool = createConnectionPoolManager(uri, HttpVersion.HTTP_1_1)) {
            shutdownComplete = connPool.getShutdownCompleteFuture();
            try (HttpClientConnection conn = connPool.acquireConnection().get(60, TimeUnit.SECONDS)) {

                HttpStreamBaseResponseHandler streamHandler = new HttpStreamBaseResponseHandler() {
                    @Override
                    public void onResponseHeaders(HttpStreamBase stream, int responseStatusCode, int blockType,
                            HttpHeader[] nextHeaders) {
                        response.statusCode = responseStatusCode;
                        response.headers.addAll(Arrays.asList(nextHeaders));
                    }

                    @Override
                    public int onResponseBody(HttpStreamBase stream, byte[] bodyBytesIn) {
                        response.bodyBuffer.put(bodyBytesIn);
                        return bodyBytesIn.length;
                    }

                    @Override
                    public void onResponseComplete(HttpStreamBase stream, int errorCode) {
                        response.onCompleteErrorCode = errorCode;
                        reqCompleted.complete(null);
                    }
                };

                // Use the unified makeRequest with useManualDataWrites=true
                try (HttpStreamBase stream = conn.makeRequest(request, streamHandler, true)) {
                    stream.activate();

                    /*
                     * Same weak-ref-inside-write-callback assertion as testHttp2WriteData --
                     * proves that the H1 writeData path also keeps the byte[] reachable
                     * from the JVM for the duration of the async write. Helper is shared
                     * (takes HttpStreamBase), so this exercises the exact same JNI code path
                     * in http_request_response.c (httpStreamBaseWriteData) as H2.
                     */
                    CompletableFuture<Void> writeFuture = issueWriteAndAssertReachable(stream, expectedBody);
                    writeFuture.get(5, TimeUnit.SECONDS);
                    reqCompleted.get(60, TimeUnit.SECONDS);
                }
            }
        }

        Assert.assertEquals(CRT.AWS_CRT_SUCCESS, response.onCompleteErrorCode);
        Assert.assertEquals(200, response.statusCode);
        // H1 /echo returns JSON: {"data": "<sent>"}
        String body = response.getBody();
        Assert.assertTrue("Response should contain sent data: " + body,
                body.contains("\"data\": \"" + expectedBody + "\""));

        shutdownComplete.get(60, TimeUnit.SECONDS);
        CrtResource.waitForNoResources();
    }

    @Test
    public void testHttp1WriteDataEndStreamOnly() throws Exception {
        skipIfAndroid();
        skipIfLocalhostUnavailable();

        URI uri = new URI(HOST + ":" + H1_TLS_PORT);

        HttpHeader[] headers = new HttpHeader[]{
            new HttpHeader("Host", uri.getHost()),
            new HttpHeader("Content-Length", "0"),
        };
        HttpRequest request = new HttpRequest("GET", "/echo", headers, null);

        CompletableFuture<Void> reqCompleted = new CompletableFuture<>();
        TestHttpResponse response = new TestHttpResponse();

        CompletableFuture<Void> shutdownComplete;
        try (HttpClientConnectionManager connPool = createConnectionPoolManager(uri, HttpVersion.HTTP_1_1)) {
            shutdownComplete = connPool.getShutdownCompleteFuture();
            try (HttpClientConnection conn = connPool.acquireConnection().get(60, TimeUnit.SECONDS)) {

                HttpStreamBaseResponseHandler streamHandler = new HttpStreamBaseResponseHandler() {
                    @Override
                    public void onResponseHeaders(HttpStreamBase stream, int responseStatusCode, int blockType,
                            HttpHeader[] nextHeaders) {
                        response.statusCode = responseStatusCode;
                        response.headers.addAll(Arrays.asList(nextHeaders));
                    }

                    @Override
                    public int onResponseBody(HttpStreamBase stream, byte[] bodyBytesIn) {
                        response.bodyBuffer.put(bodyBytesIn);
                        return bodyBytesIn.length;
                    }

                    @Override
                    public void onResponseComplete(HttpStreamBase stream, int errorCode) {
                        response.onCompleteErrorCode = errorCode;
                        reqCompleted.complete(null);
                    }
                };

                try (HttpStreamBase stream = conn.makeRequest(request, streamHandler, true)) {
                    stream.activate();
                    stream.writeData(null, true).get(5, TimeUnit.SECONDS);
                    reqCompleted.get(60, TimeUnit.SECONDS);
                }
            }
        }

        Assert.assertEquals(CRT.AWS_CRT_SUCCESS, response.onCompleteErrorCode);
        Assert.assertEquals(200, response.statusCode);
        // H1 /echo returns JSON: {"data": ""}
        String body = response.getBody();
        Assert.assertTrue("Response should contain empty data: " + body,
                body.contains("\"data\": \"\""));

        shutdownComplete.get(60, TimeUnit.SECONDS);
        CrtResource.waitForNoResources();
    }

    /**
     * Tests that makeRequest throws IllegalStateException when called with both
     * a body stream and useManualDataWrites=true on an HTTP/1.1 connection.
     */
    @Test
    public void testHttp1MakeRequestWithBodyStreamAndManualWrites() throws Exception {
        skipIfAndroid();
        skipIfLocalhostUnavailable();

        URI uri = new URI(HOST + ":" + H1_TLS_PORT);

        HttpRequestBodyStream bodyStream = new HttpRequestBodyStream() {
            @Override
            public boolean sendRequestBody(ByteBuffer bodyBytesOut) {
                return true;
            }
        };

        HttpHeader[] headers = new HttpHeader[]{
            new HttpHeader("Host", uri.getHost()),
        };
        // Create request WITH body stream AND useManualDataWrites=true
        HttpRequest request = new HttpRequest("PUT", "/echo", headers, bodyStream);

        try (HttpClientConnectionManager connPool = createConnectionPoolManager(uri, HttpVersion.HTTP_1_1)) {
            try (HttpClientConnection conn = connPool.acquireConnection().get(60, TimeUnit.SECONDS)) {

                HttpStreamResponseHandler streamHandler = new HttpStreamResponseHandler() {
                    @Override
                    public void onResponseHeaders(HttpStream stream, int responseStatusCode, int blockType,
                            HttpHeader[] nextHeaders) {}
                    @Override
                    public int onResponseBody(HttpStream stream, byte[] bodyBytesIn) { return bodyBytesIn.length; }
                    @Override
                    public void onResponseComplete(HttpStream stream, int errorCode) {}
                };

                try {
                    conn.makeRequest(request, streamHandler, true);
                    Assert.fail("Expected IllegalStateException from makeRequest");
                } catch (IllegalStateException e) {
                    Assert.assertTrue(e.getMessage().contains("manual data writes"));
                }
            }
        }
    }

    /**
     * Smoke test: stream acquired from {@link HttpStreamManager} for HTTP/2
     * correctly threads {@code useManualDataWrites=true} through to the stream
     * and allows a simple "hello world" body to be sent via writeData().
     */
    @Test
    public void testHttp2StreamManagerWriteData() throws Exception {
        skipIfAndroid();
        skipIfLocalhostUnavailable();

        URI uri = new URI(HOST + ":" + H2_TLS_PORT);
        byte[] payload = "hello world".getBytes(StandardCharsets.UTF_8);

        HttpHeader[] headers = new HttpHeader[] {
            new HttpHeader(":method", "PUT"),
            new HttpHeader(":path", "/echo"),
            new HttpHeader(":scheme", "https"),
            new HttpHeader(":authority", uri.getHost()),
            new HttpHeader("content-length", Integer.toString(payload.length)),
        };
        Http2Request request = new Http2Request(headers, null);

        CompletableFuture<Void> reqCompleted = new CompletableFuture<>();
        TestHttpResponse response = new TestHttpResponse();

        CompletableFuture<Void> shutdownComplete;
        try (HttpStreamManager streamManager = createLocalhostStreamManager(uri, HttpVersion.HTTP_2)) {
            shutdownComplete = streamManager.getShutdownCompleteFuture();

            HttpStreamBaseResponseHandler streamHandler = new HttpStreamBaseResponseHandler() {
                @Override
                public void onResponseHeaders(HttpStreamBase stream, int responseStatusCode, int blockType,
                        HttpHeader[] nextHeaders) {
                    response.statusCode = responseStatusCode;
                    response.headers.addAll(Arrays.asList(nextHeaders));
                }

                @Override
                public int onResponseBody(HttpStreamBase stream, byte[] bodyBytesIn) {
                    response.bodyBuffer.put(bodyBytesIn);
                    return bodyBytesIn.length;
                }

                @Override
                public void onResponseComplete(HttpStreamBase stream, int errorCode) {
                    response.onCompleteErrorCode = errorCode;
                    reqCompleted.complete(null);
                }
            };

            try (HttpStreamBase stream = streamManager.acquireStream(request, streamHandler, true)
                    .get(60, TimeUnit.SECONDS)) {
                stream.writeData(payload, true).get(5, TimeUnit.SECONDS);
                reqCompleted.get(60, TimeUnit.SECONDS);
            }
        }

        Assert.assertEquals(CRT.AWS_CRT_SUCCESS, response.onCompleteErrorCode);
        Assert.assertEquals(200, response.statusCode);
        String body = response.getBody();
        Assert.assertTrue("Response should contain sent body: " + body,
                body.contains("\"body\": \"hello world\""));

        shutdownComplete.get(60, TimeUnit.SECONDS);
        CrtResource.waitForNoResources();
    }

    /**
     * Smoke test: stream acquired from {@link HttpStreamManager} for HTTP/1.1
     * correctly threads {@code useManualDataWrites=true} through to the stream
     * and allows a simple "hello world" body to be sent via writeData().
     */
    @Test
    public void testHttp1StreamManagerWriteData() throws Exception {
        skipIfAndroid();
        skipIfLocalhostUnavailable();

        URI uri = new URI(HOST + ":" + H1_TLS_PORT);
        byte[] payload = "hello world".getBytes(StandardCharsets.UTF_8);

        HttpHeader[] headers = new HttpHeader[] {
            new HttpHeader("Host", uri.getHost()),
            new HttpHeader("Content-Length", Integer.toString(payload.length)),
        };
        HttpRequest request = new HttpRequest("PUT", "/echo", headers, null);

        CompletableFuture<Void> reqCompleted = new CompletableFuture<>();
        TestHttpResponse response = new TestHttpResponse();

        CompletableFuture<Void> shutdownComplete;
        try (HttpStreamManager streamManager = createLocalhostStreamManager(uri, HttpVersion.HTTP_1_1)) {
            shutdownComplete = streamManager.getShutdownCompleteFuture();

            HttpStreamBaseResponseHandler streamHandler = new HttpStreamBaseResponseHandler() {
                @Override
                public void onResponseHeaders(HttpStreamBase stream, int responseStatusCode, int blockType,
                        HttpHeader[] nextHeaders) {
                    response.statusCode = responseStatusCode;
                    response.headers.addAll(Arrays.asList(nextHeaders));
                }

                @Override
                public int onResponseBody(HttpStreamBase stream, byte[] bodyBytesIn) {
                    response.bodyBuffer.put(bodyBytesIn);
                    return bodyBytesIn.length;
                }

                @Override
                public void onResponseComplete(HttpStreamBase stream, int errorCode) {
                    response.onCompleteErrorCode = errorCode;
                    reqCompleted.complete(null);
                }
            };

            try (HttpStreamBase stream = streamManager.acquireStream(request, streamHandler, true)
                    .get(60, TimeUnit.SECONDS)) {
                stream.writeData(payload, true).get(5, TimeUnit.SECONDS);
                reqCompleted.get(60, TimeUnit.SECONDS);
            }
        }

        Assert.assertEquals(CRT.AWS_CRT_SUCCESS, response.onCompleteErrorCode);
        Assert.assertEquals(200, response.statusCode);
        String body = response.getBody();
        Assert.assertTrue("Response should contain sent data: " + body,
                body.contains("\"data\": \"hello world\""));

        shutdownComplete.get(60, TimeUnit.SECONDS);
        CrtResource.waitForNoResources();
    }
}
