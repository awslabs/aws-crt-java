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
import software.amazon.awssdk.crt.http.HttpVersion;
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
    private final static String HOST = "https://localhost:8082";
    private final int MAX_TEST_RETRIES = 5;
    private final int TEST_RETRY_SLEEP_MILLIS = 2000;

    public TestHttpResponse testRequest(String method, String endpoint, String path, String requestBody,
            boolean useChunkedEncoding, int expectedStatus) throws Exception {
//        if(endpoint.equals(HOST)) {
//            // postman-echo.com in now requires TLS1.3,
//            // but our Mac implementation doesn't support TLS1.3 yet.
//            // The work has been planned to Dec. 2025 to support TLS1.3,
//            // so disable the test for now. And reenable it afterward
//            skipIfMac();
//        }
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
                            HttpVersion.HTTP_1_1);
                } else {
                    response = getResponse(uri, request, null, HttpVersion.HTTP_1_1);
                }
            } catch (Exception ex) {
                // do nothing just let it retry
            }

        } while ((response == null || shouldRetry(response)) && numAttempts < 3);

        Assert.assertNotEquals(-1, response.blockType);


        if (response.statusCode < 500) { // if the server errored, not our fault
            Assert.assertEquals("Expected and Actual Status Codes don't match", expectedStatus, response.statusCode);
        }
        if (response.statusCode == 200) {
            boolean hasContentLengthHeader = false;
            for (HttpHeader h : response.headers) {
                if (h.getName().toLowerCase().equals("content-length")) {
                    hasContentLengthHeader = true;
                }
            }
            Assert.assertTrue("Expected to have content-length header that is missing.", hasContentLengthHeader);
        }

        return response;
    }

    @Test
    public void testHttpEcho() throws Exception {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        String bodyToSend = "test echo body";
        TestHttpResponse response = testRequest("POST", HOST, "/echo", bodyToSend, false, 200);

        String echoedBody = response.getBody();
        String expectedJson = "{\n    \"data\": \"" + bodyToSend + "\"\n}";
        Assert.assertEquals(expectedJson, echoedBody);
    }

    @Test
    public void testHttpDownload() throws Exception {
        skipIfAndroid();
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
        skipIfAndroid();
        skipIfNetworkUnavailable();
        String bodyToSend = TEST_DOC_LINE;
        TestHttpResponse response = testRequest("PUT", HOST, "/put", bodyToSend, chunked, 200);

        // Get the Body bytes that were echoed back to us
        String body = response.getBody();

        /**
         * Example Json Response Body from postman-echo.com:
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
         * "Host": "postman-echo.com"
         * },
         * "json": null,
         * "method": "PUT",
         * "origin": "1.2.3.4, 5.6.7.8",
         * }
         *
         */

        // The response is a JSON object, extract the "data" field using proper JSON parsing
        String echoedBody = null;

        // Find the "data" field in the JSON response
        int dataIndex = body.indexOf("\"data\"");
        if (dataIndex != -1) {
            // Find the colon after "data"
            int colonIndex = body.indexOf(':', dataIndex);
            if (colonIndex != -1) {
                // Find the value after the colon, which should be a quoted string
                int valueStartIndex = body.indexOf('"', colonIndex);
                if (valueStartIndex != -1) {
                    // Find the end of the quoted string
                    int valueEndIndex = body.indexOf('"', valueStartIndex + 1);
                    while (valueEndIndex > 0 && body.charAt(valueEndIndex - 1) == '\\') {
                        // This quote is escaped, find the next one
                        valueEndIndex = body.indexOf('"', valueEndIndex + 1);
                    }

                    if (valueEndIndex != -1) {
                        // Extract the value between the quotes
                        echoedBody = body.substring(valueStartIndex + 1, valueEndIndex);
                    }
                }
            }
        }

        Assert.assertNotNull("Response Body did not contain \"data\" JSON key:\n" + body, echoedBody);
        Assert.assertEquals(bodyToSend, echoedBody);
    }

    @Test
    public void testHttpUpload() throws Exception {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        testHttpUpload(false);
    }

    @Test
    public void testHttpUploadChunked() throws Exception {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        testHttpUpload(true);
    }

    private void doHttpRequestUnActivatedTest() {
        // postman-echo.com in now requires TLS1.3,
        // but our Mac implementation doesn't support TLS1.3 yet.
        // The work has been planned to Dec. 2025 to support TLS1.3,
        // so disable the test for now. And reenable it afterward
        skipIfMac();
        try {
            URI uri = new URI(HOST);

            HttpHeader[] requestHeaders = new HttpHeader[]{new HttpHeader("Host", uri.getHost())};

            HttpRequest request = new HttpRequest("GET", "/get", requestHeaders, null);

            CompletableFuture<Void> shutdownComplete = null;
            try (HttpClientConnectionManager connPool = createConnectionPoolManager(uri,
                    HttpVersion.HTTP_1_1)) {
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
            }

            if (shutdownComplete != null) {
                shutdownComplete.get();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testHttpRequestUnActivated() throws Exception {
        skipIfAndroid();
        skipIfNetworkUnavailable();

        TestUtils.doRetryableTest(this::doHttpRequestUnActivatedTest, TestUtils::isRetryableTimeout, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    @Test
    public void testMarshallJniUtf8Path() throws Exception {
        skipIfAndroid();
        HttpRequest request = new HttpRequest("GET", "/?ሴ=bar");
        request.marshalForJni();
    }
}
