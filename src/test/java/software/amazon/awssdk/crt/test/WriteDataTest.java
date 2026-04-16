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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class WriteDataTest extends HttpRequestResponseFixture {
    private final static String HOST = "https://localhost:3443";

    @Test
    public void testHttp2WriteData() throws Exception {
        skipIfAndroid();
        skipIfLocalhostUnavailable();

        URI uri = new URI(HOST);
        byte[] payload = "hello from writeData".getBytes(StandardCharsets.UTF_8);

        HttpHeader[] headers = new HttpHeader[]{
            new HttpHeader(":method", "PUT"),
            new HttpHeader(":path", "/echo"),
            new HttpHeader(":scheme", uri.getScheme()),
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
        String body = response.getBody();
        Assert.assertEquals("hello from writeData", body);

        shutdownComplete.get(60, TimeUnit.SECONDS);
        CrtResource.waitForNoResources();
    }

    @Test
    public void testHttp2WriteDataEndStreamOnly() throws Exception {
        skipIfAndroid();
        skipIfLocalhostUnavailable();

        URI uri = new URI(HOST);

        HttpHeader[] headers = new HttpHeader[]{
            new HttpHeader(":method", "GET"),
            new HttpHeader(":path", "/echo"),
            new HttpHeader(":scheme", uri.getScheme()),
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

        shutdownComplete.get(60, TimeUnit.SECONDS);
        CrtResource.waitForNoResources();
    }
}
