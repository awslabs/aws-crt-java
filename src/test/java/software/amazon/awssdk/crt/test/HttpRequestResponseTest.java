/*
 * Copyright 2010-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.awssdk.crt.test;

import static software.amazon.awssdk.crt.utils.ByteBufferUtils.transferData;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.http.HttpClientConnection;
import software.amazon.awssdk.crt.http.HttpClientConnectionManager;
import software.amazon.awssdk.crt.http.HttpClientConnectionManagerOptions;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.http.HttpRequestBodyStream;
import software.amazon.awssdk.crt.http.HttpStreamResponseHandler;
import software.amazon.awssdk.crt.http.HttpStream;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class HttpRequestResponseTest extends HttpClientTestFixture {
    private final static Charset UTF8 = StandardCharsets.UTF_8;
    private final String EMPTY_BODY = "";
    private final static String TEST_DOC_LINE = "This is a sample to prove that http downloads and uploads work. It doesn't really matter what's in here, we mainly just need to verify the downloads and uploads work.";
    private final static String TEST_DOC_SHA256 = "C7FDB5314B9742467B16BD5EA2F8012190B5E2C44A005F7984F89AAB58219534";

    private class TestHttpResponse {
        int statusCode = -1;
        int blockType = -1;
        List<HttpHeader> headers = new ArrayList<>();
        ByteBuffer bodyBuffer = ByteBuffer.wrap(new byte[16*1024*1024]); // Allow up to 16 MB Responses
        int onCompleteErrorCode = -1;

        public String getBody() {
            bodyBuffer.flip();
            return UTF8.decode(bodyBuffer).toString();
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Status: " + statusCode);
            int i = 0;
            for (HttpHeader h: headers) {
                builder.append("\nHeader[" + i + "]: " + h.toString());
            }

            builder.append("\nBody:\n");
            builder.append(getBody());

            return builder.toString();
        }
    }

    public static String byteArrayToHex(byte[] input) {
        StringBuilder output = new StringBuilder(input.length * 2);
        for (byte b: input) {
            output.append(String.format("%02X", b));
        }
        return output.toString();
    }

    private String calculateBodyHash(ByteBuffer bodyBuffer) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(bodyBuffer);
        return byteArrayToHex(digest.digest());
    }

    private HttpClientConnectionManager createConnectionPoolManager(URI uri) {
        try(EventLoopGroup eventLoopGroup = new EventLoopGroup(1);
            HostResolver resolver = new HostResolver(eventLoopGroup);
            ClientBootstrap bootstrap = new ClientBootstrap(eventLoopGroup, resolver);
            SocketOptions sockOpts = new SocketOptions();
            TlsContext tlsContext =  createHttpClientTlsContext()) {

            HttpClientConnectionManagerOptions options = new HttpClientConnectionManagerOptions()
                    .withClientBootstrap(bootstrap)
                    .withSocketOptions(sockOpts)
                    .withTlsContext(tlsContext)
                    .withUri(uri);

            return HttpClientConnectionManager.create(options);
        }
    }

    public TestHttpResponse getResponse(URI uri, HttpRequest request) throws Exception {
        boolean actuallyConnected = false;

        final CompletableFuture<Void> reqCompleted = new CompletableFuture<>();

        final TestHttpResponse response = new TestHttpResponse();

        CompletableFuture<Void> shutdownComplete = null;
        try (HttpClientConnectionManager connPool = createConnectionPoolManager(uri)) {
            shutdownComplete = connPool.getShutdownCompleteFuture();
            try (HttpClientConnection conn = connPool.acquireConnection().get(60, TimeUnit.SECONDS)) {
                actuallyConnected = true;
                HttpStreamResponseHandler streamHandler = new HttpStreamResponseHandler() {
                    @Override
                    public void onResponseHeaders(HttpStream stream, int responseStatusCode, int blockType, HttpHeader[] nextHeaders) {
                        response.statusCode = responseStatusCode;
                        Assert.assertEquals(responseStatusCode, stream.getResponseStatusCode());
                        response.headers.addAll(Arrays.asList(nextHeaders));
                    }

                    @Override
                    public void onResponseHeadersDone(HttpStream stream, int blockType) {
                        response.blockType = blockType;
                    }

                    @Override
                    public int onResponseBody(HttpStream stream, byte[] bodyBytesIn) {
                        response.bodyBuffer.put(bodyBytesIn);
                        int amountRead = bodyBytesIn.length;

                        // Slide the window open by the number of bytes just read
                        return amountRead;
                    }

                    @Override
                    public void onResponseComplete(HttpStream stream, int errorCode) {
                        response.onCompleteErrorCode = errorCode;
                        reqCompleted.complete(null);
                        stream.close();
                    }
                };

                conn.makeRequest(request, streamHandler).activate();
                // Give the request up to 60 seconds to complete, otherwise throw a TimeoutException
                reqCompleted.get(60, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Assert.assertTrue(actuallyConnected);

        shutdownComplete.get();

        return response;
    }

    private boolean shouldRetry(TestHttpResponse response) {
        // Retry if we couldn't connect or if we got 503 response
        if (response.onCompleteErrorCode != CRT.AWS_CRT_SUCCESS || response.statusCode == 503) {
            return true;
        }
        return false;
    }

    public TestHttpResponse testRequest(String method, String endpoint, String path, String requestBody, int expectedStatus) throws Exception {
        URI uri = new URI(endpoint);

        HttpHeader[] requestHeaders =
                new HttpHeader[]{
                    new HttpHeader("Host", uri.getHost()),
                    new HttpHeader("Content-Length", Integer.toString(requestBody.getBytes(UTF8).length))
                };

        final ByteBuffer bodyBytesIn = ByteBuffer.wrap(requestBody.getBytes(UTF8));
        HttpRequestBodyStream bodyStream = new HttpRequestBodyStream() {
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

        HttpRequest request = new HttpRequest(method, path, requestHeaders, bodyStream);

        TestHttpResponse response = null;
        int numAttempts = 0;
        do {

            request.getBodyStream().resetPosition();

            numAttempts++;
            response = null;
            try {
                response = getResponse(uri, request);
            } catch (Exception ex) {
                //do nothing just let it retry
            }

        } while ((response == null || shouldRetry(response)) && numAttempts < 3);

        Assert.assertNotEquals(-1, response.blockType);

        boolean hasContentLengthHeader = false;

        for (HttpHeader h: response.headers) {
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
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        testRequest("DELETE", "https://httpbin.org", "/delete", EMPTY_BODY, 200);
        testRequest("DELETE", "https://httpbin.org", "/get", EMPTY_BODY, 405);
        testRequest("DELETE", "https://httpbin.org", "/post", EMPTY_BODY, 405);
        testRequest("DELETE", "https://httpbin.org", "/put", EMPTY_BODY, 405);
    }

    @Test
    public void testHttpGet() throws Exception {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        testRequest("GET", "https://httpbin.org", "/delete", EMPTY_BODY, 405);
        testRequest("GET", "https://httpbin.org", "/get", EMPTY_BODY, 200);
        testRequest("GET", "https://httpbin.org", "/post", EMPTY_BODY, 405);
        testRequest("GET", "https://httpbin.org", "/put", EMPTY_BODY, 405);
    }

    @Test
    public void testHttpPost() throws Exception {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        testRequest("POST", "https://httpbin.org", "/delete", EMPTY_BODY, 405);
        testRequest("POST", "https://httpbin.org", "/get", EMPTY_BODY, 405);
        testRequest("POST", "https://httpbin.org", "/post", EMPTY_BODY, 200);
        testRequest("POST", "https://httpbin.org", "/put", EMPTY_BODY, 405);
    }

    @Test
    public void testHttpPut() throws Exception {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        testRequest("PUT", "https://httpbin.org", "/delete", EMPTY_BODY, 405);
        testRequest("PUT", "https://httpbin.org", "/get", EMPTY_BODY, 405);
        testRequest("PUT", "https://httpbin.org", "/post", EMPTY_BODY, 405);
        testRequest("PUT", "https://httpbin.org", "/put", EMPTY_BODY, 200);
    }

    @Test
    public void testHttpResponseStatusCodes() throws Exception {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        testRequest("GET", "https://httpbin.org", "/status/200", EMPTY_BODY, 200);
        testRequest("GET", "https://httpbin.org", "/status/300", EMPTY_BODY, 300);
        testRequest("GET", "https://httpbin.org", "/status/400", EMPTY_BODY, 400);
        testRequest("GET", "https://httpbin.org", "/status/500", EMPTY_BODY, 500);
    }

    @Test
    public void testHttpDownload() throws Exception {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        TestHttpResponse response = testRequest("GET", "https://aws-crt-test-stuff.s3.amazonaws.com", "/http_test_doc.txt", EMPTY_BODY, 200);

        ByteBuffer body = response.bodyBuffer;
        body.flip(); //Flip from Write mode to Read mode

        Assert.assertEquals(TEST_DOC_SHA256, calculateBodyHash(body));
    }

    /**
     * Removes trailing commas, and trims quote characters from a string.
     *
     * @param input
     * @return
     */
    private String extractValueFromJson(String input) {
        return input.trim()                     // Remove spaces from front and back
                    .replaceAll(",$", "")       // Remove comma if it's the last character
                    .replaceAll("^\"|\"$", ""); // Remove quotes from front and back
    }

    @Test
    public void testHttpUpload() throws Exception {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        String bodyToSend = TEST_DOC_LINE;
        TestHttpResponse response = testRequest("PUT", "https://httpbin.org", "/anything", bodyToSend, 200);

        // Get the Body bytes that were echoed back to us
        String body = response.getBody();

        /**
         * Example Json Response Body from httpbin.org:
         *
         * {
         *   "args": {},
         *   "data": "This is a sample to prove that http downloads and uploads work. It doesn't really matter what's in here, we mainly just need to verify the downloads and uploads work.",
         *   "files": {},
         *   "form": {},
         *   "headers": {
         *     "Content-Length": "166",
         *     "Host": "httpbin.org"
         *   },
         *   "json": null,
         *   "method": "PUT",
         *   "origin": "1.2.3.4, 5.6.7.8",
         *   "url": "https://httpbin.org/anything"
         * }
         *
         */

        String echoedBody = null;
        for (String line: body.split("\n")) {
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
    public void testHttpRequestUnActivated() throws Exception {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);

        URI uri = new URI("https://httpbin.org");

        HttpHeader[] requestHeaders =
                new HttpHeader[]{
                        new HttpHeader("Host", uri.getHost())
                };

        HttpRequest request = new HttpRequest("GET", "/get", requestHeaders, null);

        CompletableFuture<Void> shutdownComplete = null;
        try (HttpClientConnectionManager connPool = createConnectionPoolManager(uri)) {
            shutdownComplete = connPool.getShutdownCompleteFuture();
            try (HttpClientConnection conn = connPool.acquireConnection().get(60, TimeUnit.SECONDS)) {
                HttpStreamResponseHandler streamHandler = new HttpStreamResponseHandler() {
                    @Override
                    public void onResponseHeaders(HttpStream stream, int responseStatusCode, int blockType, HttpHeader[] nextHeaders) {
                        // do nothing
                    }

                    @Override
                    public void onResponseHeadersDone(HttpStream stream, int blockType) {
                        // do nothing
                    }

                    @Override
                    public int onResponseBody(HttpStream stream, byte[] bodyBytesIn) {
                        //do nothing
                        return bodyBytesIn.length;
                    }

                    @Override
                    public void onResponseComplete(HttpStream stream, int errorCode) {
                        //do nothing.
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
    }
}
