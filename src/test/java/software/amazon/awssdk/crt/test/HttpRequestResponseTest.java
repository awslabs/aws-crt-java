/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import static software.amazon.awssdk.crt.utils.ByteBufferUtils.transferData;

import org.junit.Assert;
import org.junit.Test;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.http.HttpClientConnection;
import software.amazon.awssdk.crt.http.HttpClientConnectionManager;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.http.HttpRequestBodyStream;
import software.amazon.awssdk.crt.http.HttpStreamResponseHandler;
import software.amazon.awssdk.crt.http.HttpStream;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class HttpRequestResponseTest extends HttpRequestResponseFixture {

    public TestHttpResponse testRequest(String method, String endpoint, String path, String requestBody,
            boolean useChunkedEncoding, int expectedStatus) throws Exception {
        URI uri = new URI(endpoint);

        HttpHeader[] requestHeaders = null;

        if (!useChunkedEncoding) {
            requestHeaders = new HttpHeader[] { new HttpHeader("Host", uri.getHost()),
                    new HttpHeader("Content-Length", Integer.toString(requestBody.getBytes(UTF8).length)) };
        } else {
            requestHeaders = new HttpHeader[] { new HttpHeader("Host", uri.getHost()),
                    new HttpHeader("Transfer-Encoding", "chunked") };
        }

        HttpRequestBodyStream bodyStream = null;

        if (!useChunkedEncoding) {
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
        }

        HttpRequest request = new HttpRequest(method, path, requestHeaders, bodyStream);

        TestHttpResponse response = null;
        int numAttempts = 0;
        do {

            if (request.getBodyStream() != null) {
                request.getBodyStream().resetPosition();
            }

            numAttempts++;
            response = null;
            try {
                if (useChunkedEncoding) {
                    response = getResponse(uri, request, requestBody.getBytes(UTF8),
                            HttpClientConnection.ProtocolVersion.HTTP_1_1);
                } else {
                    response = getResponse(uri, request, null, HttpClientConnection.ProtocolVersion.HTTP_1_1);
                }
            } catch (Exception ex) {
                // do nothing just let it retry
            }

        } while ((response == null || shouldRetry(response)) && numAttempts < 3);

        Assert.assertNotEquals(-1, response.blockType);

        boolean hasContentLengthHeader = false;

        for (HttpHeader h : response.headers) {
            if (h.getName().equals("Content-Length")) {
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
    public void testHttpDelete() throws Exception {
        skipIfNetworkUnavailable();
        testRequest("DELETE", "https://httpbin.org", "/delete", EMPTY_BODY, false, 200);
        testRequest("DELETE", "https://httpbin.org", "/get", EMPTY_BODY, false, 405);
        testRequest("DELETE", "https://httpbin.org", "/post", EMPTY_BODY, false, 405);
        testRequest("DELETE", "https://httpbin.org", "/put", EMPTY_BODY, false, 405);
    }

    @Test
    public void testHttpGet() throws Exception {
        skipIfNetworkUnavailable();
        testRequest("GET", "https://httpbin.org", "/delete", EMPTY_BODY, false, 405);
        testRequest("GET", "https://httpbin.org", "/get", EMPTY_BODY, false, 200);
        testRequest("GET", "https://httpbin.org", "/post", EMPTY_BODY, false, 405);
        testRequest("GET", "https://httpbin.org", "/put", EMPTY_BODY, false, 405);
    }

    @Test
    public void testHttpPost() throws Exception {
        skipIfNetworkUnavailable();
        testRequest("POST", "https://httpbin.org", "/delete", EMPTY_BODY, false, 405);
        testRequest("POST", "https://httpbin.org", "/get", EMPTY_BODY, false, 405);
        testRequest("POST", "https://httpbin.org", "/post", EMPTY_BODY, false, 200);
        testRequest("POST", "https://httpbin.org", "/put", EMPTY_BODY, false, 405);
    }

    @Test
    public void testHttpPut() throws Exception {
        skipIfNetworkUnavailable();
        testRequest("PUT", "https://httpbin.org", "/delete", EMPTY_BODY, false, 405);
        testRequest("PUT", "https://httpbin.org", "/get", EMPTY_BODY, false, 405);
        testRequest("PUT", "https://httpbin.org", "/post", EMPTY_BODY, false, 405);
        testRequest("PUT", "https://httpbin.org", "/put", EMPTY_BODY, false, 200);
    }

    @Test
    public void testHttpResponseStatusCodes() throws Exception {
        skipIfNetworkUnavailable();
        testRequest("GET", "https://httpbin.org", "/status/200", EMPTY_BODY, false, 200);
        testRequest("GET", "https://httpbin.org", "/status/300", EMPTY_BODY, false, 300);
        testRequest("GET", "https://httpbin.org", "/status/400", EMPTY_BODY, false, 400);
        testRequest("GET", "https://httpbin.org", "/status/500", EMPTY_BODY, false, 500);
    }

    @Test
    public void testHttpDownload() throws Exception {
        skipIfNetworkUnavailable();
        TestHttpResponse response = testRequest("GET", "https://aws-crt-test-stuff.s3.amazonaws.com",
                "/http_test_doc.txt", EMPTY_BODY, false, 200);

        ByteBuffer body = response.bodyBuffer;
        body.flip(); // Flip from Write mode to Read mode

        Assert.assertEquals(TEST_DOC_SHA256, calculateBodyHash(body));
    }

    /**
     * Removes trailing commas, and trims quote characters from a string.
     *
     * @param input
     * @return
     */
    private String extractValueFromJson(String input) {
        return input.trim() // Remove spaces from front and back
                .replaceAll(",$", "") // Remove comma if it's the last character
                .replaceAll("^\"|\"$", ""); // Remove quotes from front and back
    }

    private void testHttpUpload(boolean chunked) throws Exception {
        skipIfNetworkUnavailable();
        String bodyToSend = TEST_DOC_LINE;
        TestHttpResponse response = testRequest("PUT", "https://httpbin.org", "/anything", bodyToSend, chunked, 200);

        // Get the Body bytes that were echoed back to us
        String body = response.getBody();

        /**
         * Example Json Response Body from httpbin.org:
         *
         * {
         * "args": {},
         * "data": "This is a sample to prove that http downloads and
         * uploads work. It doesn't really matter what's in here, we mainly just need to
         * verify the downloads and uploads work.",
         * "files": {},
         * "form": {},
         * "headers": {
         * "Content-Length": "166",
         * "Host": "httpbin.org"
         * },
         * "json": null,
         * "method": "PUT",
         * "origin": "1.2.3.4, 5.6.7.8",
         * "url": "https://httpbin.org/anything"
         * }
         *
         */

        String echoedBody = null;
        for (String line : body.split("\n")) {
            String[] keyAndValue = line.split(":", 2);

            // Found JSON Key/Value Pair
            if (keyAndValue.length == 2) {
                String key = extractValueFromJson(keyAndValue[0]);
                String val = extractValueFromJson(keyAndValue[1]);

                // Found Echoed Body
                if (key.equals("data")) {
                    echoedBody = extractValueFromJson(val);
                }
            }
        }

        Assert.assertNotNull("Response Body did not contain \"data\" JSON key:\n" + body, echoedBody);
        Assert.assertEquals(bodyToSend, echoedBody);
    }

    @Test
    public void testHttpUpload() throws Exception {
        skipIfNetworkUnavailable();
        testHttpUpload(false);
    }

    @Test
    public void testHttpUploadChunked() throws Exception {
        skipIfNetworkUnavailable();
        testHttpUpload(true);
    }

    @Test
    public void testHttpRequestUnActivated() throws Exception {
        skipIfNetworkUnavailable();

        URI uri = new URI("https://httpbin.org");

        HttpHeader[] requestHeaders = new HttpHeader[] { new HttpHeader("Host", uri.getHost()) };

        HttpRequest request = new HttpRequest("GET", "/get", requestHeaders, null);

        CompletableFuture<Void> shutdownComplete = null;
        try (HttpClientConnectionManager connPool = createConnectionPoolManager(uri,
                HttpClientConnection.ProtocolVersion.HTTP_1_1)) {
            shutdownComplete = connPool.getShutdownCompleteFuture();
            try (HttpClientConnection conn = connPool.acquireConnection().get(60, TimeUnit.SECONDS)) {
                HttpStreamResponseHandler streamHandler = new HttpStreamResponseHandler() {
                    @Override
                    public void onResponseHeaders(HttpStream stream, int responseStatusCode, int blockType,
                            HttpHeader[] nextHeaders) {
                        // do nothing
                    }

                    @Override
                    public void onResponseHeadersDone(HttpStream stream, int blockType) {
                        // do nothing
                    }

                    @Override
                    public int onResponseBody(HttpStream stream, byte[] bodyBytesIn) {
                        // do nothing
                        return bodyBytesIn.length;
                    }

                    @Override
                    public void onResponseComplete(HttpStream stream, int errorCode) {
                        // do nothing.
                    }
                };

                HttpStream stream = conn.makeRequest(request, streamHandler);
                stream.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (shutdownComplete != null) {
            shutdownComplete.get();
        }

        CrtResource.waitForNoResources();
    }

    @Test
    public void testMarshallJniUtf8Path() throws Exception {
        HttpRequest request = new HttpRequest("GET", "/?ሴ=bar");
        request.marshalForJni();
    }
}
