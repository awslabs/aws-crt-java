/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.http.HttpClientConnection;
import software.amazon.awssdk.crt.http.HttpVersion;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.Http2Request;
import software.amazon.awssdk.crt.http.HttpStreamBase;
import software.amazon.awssdk.crt.http.Http2Stream;
import software.amazon.awssdk.crt.http.HttpClientConnectionManager;
import software.amazon.awssdk.crt.http.Http2ClientConnection;
import software.amazon.awssdk.crt.http.HttpStreamBaseResponseHandler;
import software.amazon.awssdk.crt.http.Http2ClientConnection.Http2ErrorCode;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.http.HttpRequestBodyStream;
import static software.amazon.awssdk.crt.utils.ByteBufferUtils.transferData;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Http2RequestResponseTest extends HttpRequestResponseFixture {
    private final static String HOST = "https://postman-echo.com";
    private final static HttpVersion EXPECTED_VERSION = HttpVersion.HTTP_2;

    private Http2Request getHttp2Request(String method, String endpoint, String path, String requestBody)
            throws Exception {
        URI uri = new URI(endpoint);

        HttpHeader[] requestHeaders = null;

        requestHeaders = new HttpHeader[] { new HttpHeader(":method", method), new HttpHeader(":path", path),
                new HttpHeader(":scheme", uri.getScheme()), new HttpHeader(":authority", uri.getHost()),
                new HttpHeader("content-length", Integer.toString(requestBody.getBytes(UTF8).length)), };

        HttpRequestBodyStream bodyStream = null;

        final ByteBuffer bodyBytesIn = ByteBuffer.wrap(requestBody.getBytes(UTF8));

        bodyStream = new HttpRequestBodyStream() {
            @Override
            public boolean sendRequestBody(ByteBuffer bodyBytesOut) {
                transferData(bodyBytesIn, bodyBytesOut);

                return bodyBytesIn.remaining() == 0;
            }

            @Override
            public boolean resetPosition() {
                bodyBytesIn.position(0);

                return true;
            }
        };

        Http2Request request = new Http2Request(requestHeaders, bodyStream);
        return request;
    }

    public TestHttpResponse testHttp2Request(String method, String endpoint, String path, String requestBody,
            int expectedStatus) throws Exception {
        URI uri = new URI(endpoint);
        Http2Request request = getHttp2Request(method, endpoint, path, requestBody);

        TestHttpResponse response = null;
        int numAttempts = 0;
        do {

            if (request.getBodyStream() != null) {
                request.getBodyStream().resetPosition();
            }

            numAttempts++;
            response = null;
            try {
                response = getResponse(uri, request, null, EXPECTED_VERSION);

            } catch (Exception ex) {
                // do nothing just let it retry
            }

        } while ((response == null || shouldRetry(response)) && numAttempts < 3);

        Assert.assertNotEquals(-1, response.blockType);

        boolean hasContentLengthHeader = false;

        /* The first header of response has to be ":status" for HTTP/2 response */
        response.headers.get(0).getName().equals(":status");
        for (HttpHeader h : response.headers) {
            if (h.getName().equals("content-length")) {
                hasContentLengthHeader = true;
            }
        }

        Assert.assertTrue(hasContentLengthHeader);
        if (response.statusCode < 500) { // if the server errored, not our fault
            Assert.assertEquals("Expected and Actual Status Codes don't match", expectedStatus, response.statusCode);
        }

        return response;
    }

    @Test
    public void testHttp2Get() throws Exception {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        testHttp2Request("GET", HOST, "/delete", EMPTY_BODY, 404);
        testHttp2Request("GET", HOST, "/get", EMPTY_BODY, 200);
        testHttp2Request("GET", HOST, "/post", EMPTY_BODY, 404);
        testHttp2Request("GET", HOST, "/put", EMPTY_BODY, 404);
    }

    @Test
    public void testHttp2Post() throws Exception {
        skipIfNetworkUnavailable();
        testHttp2Request("POST", HOST, "/delete", EMPTY_BODY, 404);
        testHttp2Request("POST", HOST, "/get", EMPTY_BODY, 404);
        testHttp2Request("POST", HOST, "/post", EMPTY_BODY, 200);
        testHttp2Request("POST", HOST, "/put", EMPTY_BODY, 404);
    }

    @Test
    public void testHttp2Put() throws Exception {
        skipIfNetworkUnavailable();
        testHttp2Request("PUT", HOST, "/delete", EMPTY_BODY, 404);
        testHttp2Request("PUT", HOST, "/get", EMPTY_BODY, 404);
        testHttp2Request("PUT", HOST, "/post", EMPTY_BODY, 404);
        testHttp2Request("PUT", HOST, "/put", EMPTY_BODY, 200);
    }

    @Test
    public void testHttp2ResponseStatusCodes() throws Exception {
        skipIfNetworkUnavailable();
        testHttp2Request("GET", HOST, "/status/200", EMPTY_BODY, 200);
        testHttp2Request("GET", HOST, "/status/300", EMPTY_BODY, 300);
        testHttp2Request("GET", HOST, "/status/400", EMPTY_BODY, 400);
        testHttp2Request("GET", HOST, "/status/500", EMPTY_BODY, 500);
    }

    @Test
    public void testHttp2Download() throws Exception {
        skipIfNetworkUnavailable();
        /* cloudfront uses HTTP/2 */
        TestHttpResponse response = testHttp2Request("GET", "https://d1cz66xoahf9cl.cloudfront.net/",
                "/http_test_doc.txt", EMPTY_BODY, 200);

        ByteBuffer body = response.bodyBuffer;
        body.flip(); // Flip from Write mode to Read mode

        Assert.assertEquals(TEST_DOC_SHA256, calculateBodyHash(body));
    }

    @Test
    public void testHttp2ResetStream() throws Exception {
        /*
         * Test that the binding works not the actual functionality. C part has the test
         * for functionality
         */
        skipIfNetworkUnavailable();

        CompletableFuture<Void> shutdownComplete = null;
        boolean actuallyConnected = false;
        URI uri = new URI(HOST);

        try (HttpClientConnectionManager connPool = createConnectionPoolManager(uri, EXPECTED_VERSION)) {
            shutdownComplete = connPool.getShutdownCompleteFuture();
            try (Http2ClientConnection conn = (Http2ClientConnection) connPool.acquireConnection().get(60,
                    TimeUnit.SECONDS);) {
                actuallyConnected = true;
                CompletableFuture<Void> streamComplete = new CompletableFuture<>();
                Assert.assertTrue(conn.getVersion() == EXPECTED_VERSION);
                HttpStreamBaseResponseHandler streamHandler = new HttpStreamBaseResponseHandler() {
                    @Override
                    public void onResponseHeaders(HttpStreamBase stream, int responseStatusCode, int blockType,
                            HttpHeader[] nextHeaders) {
                        Http2Stream h2Stream = (Http2Stream) stream;
                        h2Stream.resetStream(Http2ErrorCode.INTERNAL_ERROR);
                    }

                    @Override
                    public void onResponseComplete(HttpStreamBase stream, int errorCode) {
                        stream.close();
                        if (errorCode != 0) {
                            streamComplete.completeExceptionally(new CrtRuntimeException(errorCode));
                        } else {
                            streamComplete.complete(null);
                        }
                    }
                };
                Http2Request request = getHttp2Request("GET", HOST, "/get", EMPTY_BODY);
                try (Http2Stream h2Stream = conn.makeRequest(request, streamHandler)) {
                    h2Stream.activate();
                    streamComplete.get();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Assert.assertTrue(actuallyConnected);

        shutdownComplete.get(60, TimeUnit.SECONDS);

        CrtResource.waitForNoResources();
    }

}
