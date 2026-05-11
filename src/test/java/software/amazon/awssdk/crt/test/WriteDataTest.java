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
        byte[] payload = "hello from writeData".getBytes(StandardCharsets.UTF_8);

        HttpHeader[] headers = new HttpHeader[]{
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
                    stream.writeData(payload, true).get(5, TimeUnit.SECONDS);
                    reqCompleted.get(60, TimeUnit.SECONDS);
                }
            }
        }

        Assert.assertEquals(CRT.AWS_CRT_SUCCESS, response.onCompleteErrorCode);
        Assert.assertEquals(200, response.statusCode);
        // /echo returns JSON: {"body": "<sent>", "bytes": <len>}
        String body = response.getBody();
        Assert.assertTrue("Response should contain sent body: " + body,
                body.contains("\"body\": \"hello from writeData\""));
        Assert.assertTrue("Response should contain byte count: " + body,
                body.contains("\"bytes\": " + payload.length));

        shutdownComplete.get(60, TimeUnit.SECONDS);
        CrtResource.waitForNoResources();
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
        byte[] payload = "hello from writeData h1".getBytes(StandardCharsets.UTF_8);

        HttpHeader[] headers = new HttpHeader[]{
            new HttpHeader("Host", uri.getHost()),
            new HttpHeader("Content-Length", Integer.toString(payload.length)),
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
                    stream.writeData(payload, true).get(5, TimeUnit.SECONDS);
                    reqCompleted.get(60, TimeUnit.SECONDS);
                }
            }
        }

        Assert.assertEquals(CRT.AWS_CRT_SUCCESS, response.onCompleteErrorCode);
        Assert.assertEquals(200, response.statusCode);
        // H1 /echo returns JSON: {"data": "<sent>"}
        String body = response.getBody();
        Assert.assertTrue("Response should contain sent data: " + body,
                body.contains("\"data\": \"hello from writeData h1\""));

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
