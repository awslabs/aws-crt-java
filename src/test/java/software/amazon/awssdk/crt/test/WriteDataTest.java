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
    @Test(expected = IllegalStateException.class)
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

        CompletableFuture<Void> reqCompleted = new CompletableFuture<>();
        TestHttpResponse response = new TestHttpResponse();

        CompletableFuture<Void> shutdownComplete;
        try (HttpClientConnectionManager connPool = createConnectionPoolManager(uri, HttpVersion.HTTP_1_1)) {
            shutdownComplete = connPool.getShutdownCompleteFuture();
            try (HttpClientConnection conn = connPool.acquireConnection().get(60, TimeUnit.SECONDS)) {

                HttpStreamResponseHandler streamHandler = new HttpStreamResponseHandler() {
                    @Override
                    public void onResponseHeaders(HttpStream stream, int responseStatusCode, int blockType,
                            HttpHeader[] nextHeaders) {
                        response.statusCode = responseStatusCode;
                        response.headers.addAll(Arrays.asList(nextHeaders));
                    }

                    @Override
                    public int onResponseBody(HttpStream stream, byte[] bodyBytesIn) {
                        response.bodyBuffer.put(bodyBytesIn);
                        return bodyBytesIn.length;
                    }

                    @Override
                    public void onResponseComplete(HttpStream stream, int errorCode) {
                        response.onCompleteErrorCode = errorCode;
                        reqCompleted.complete(null);
                    }
                };

                HttpStream stream = conn.makeRequest(request, streamHandler, true);
                stream.activate();

                stream.writeData("hello".getBytes(StandardCharsets.UTF_8), true, null);

                reqCompleted.get(60, TimeUnit.SECONDS);
                stream.close();
            }
        }

        shutdownComplete.get(60, TimeUnit.SECONDS);
        CrtResource.waitForNoResources();
    }
}
