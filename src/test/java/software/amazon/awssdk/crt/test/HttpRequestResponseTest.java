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

import org.junit.Assert;
import org.junit.Test;
import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.http.CrtHttpStreamHandler;
import software.amazon.awssdk.crt.http.HttpConnection;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.http.HttpStream;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContext;

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

public class HttpRequestResponseTest {
    private final static Charset UTF8 = StandardCharsets.UTF_8;
    private final String EMPTY_BODY = "";
    private final static String TEST_DOC_LINE = "This is a sample to prove that http downloads and uploads work. It doesn't really matter what's in here, we mainly just need to verify the downloads and uploads work.";
    private final static int TEST_DOC_NUM_LINES = 86401;
    private final static String TEST_DOC_SHA256 = "C7FDB5314B9742467B16BD5EA2F8012190B5E2C44A005F7984F89AAB58219534";

    private class TestHttpResponse {
        int statusCode = -1;
        boolean hasBody = false;
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

    private void transferData(ByteBuffer in, ByteBuffer out) {
        int amtToTransfer = Math.min(in.remaining(), out.remaining());

        if (amtToTransfer > 0) {
            out.put(in.array(), in.arrayOffset() + in.position(), amtToTransfer);
            in.position(in.position() + amtToTransfer);
        }
    }

    public static String byteArrayToHex(byte[] input) {
        StringBuilder output = new StringBuilder(input.length * 2);
        for(byte b: input)
            output.append(String.format("%02X", b));
        return output.toString();
    }

    private String calculateBodyHash(ByteBuffer bodyBuffer) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(bodyBuffer);
        return byteArrayToHex(digest.digest());
    }

    public TestHttpResponse getResponse(URI uri, HttpRequest request, String reqBody) throws Exception {
        boolean actuallyConnected = false;

        ClientBootstrap bootstrap = new ClientBootstrap(1);
        SocketOptions sockOpts = new SocketOptions();
        TlsContext tlsContext =  new TlsContext();

        final ByteBuffer bodyBytesIn = ByteBuffer.wrap(reqBody.getBytes(UTF8));
        final CompletableFuture<Void> reqCompleted = new CompletableFuture<>();

        final TestHttpResponse response = new TestHttpResponse();

        HttpConnection conn = null;
        HttpStream stream = null;
        try {
            conn = HttpConnection.createConnection(uri, bootstrap, sockOpts, tlsContext).get();
            actuallyConnected = true;
            CrtHttpStreamHandler streamHandler = new CrtHttpStreamHandler() {
                @Override
                public void onResponseHeaders(HttpStream stream, int responseStatusCode, HttpHeader[] nextHeaders) {
                    response.statusCode = responseStatusCode;
                    response.headers.addAll(Arrays.asList(nextHeaders));
                }

                @Override
                public void onResponseHeadersDone(HttpStream stream, boolean hasBody) {
                    response.hasBody = hasBody;
                }

                @Override
                public int onResponseBody(HttpStream stream, ByteBuffer bodyBytesIn) {
                    int start = bodyBytesIn.position();
                    response.bodyBuffer.put(bodyBytesIn);
                    int amountRead = bodyBytesIn.position() - start;

                    // Slide the window open by the number of bytes just read
                    return amountRead;
                }

                @Override
                public void onResponseComplete(HttpStream stream, int errorCode) {
                    response.onCompleteErrorCode = errorCode;
                    reqCompleted.complete(null);
                }

                @Override
                public boolean sendRequestBody(HttpStream stream, ByteBuffer bodyBytesOut) {
                    transferData(bodyBytesIn, bodyBytesOut);

                    return bodyBytesIn.remaining() == 0;
                }
            };

            stream = conn.makeRequest(request, streamHandler);
            Assert.assertNotNull(stream);
            // Give the request up to 60 seconds to complete, otherwise throw a TimeoutException
            reqCompleted.get(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (stream != null) {
                stream.close();
            }
            if (conn != null) {
                conn.close();
            }

            tlsContext.close();
            sockOpts.close();
            bootstrap.close();
        }

        Assert.assertTrue(actuallyConnected);
        Assert.assertEquals(0, CrtResource.getAllocatedNativeResourceCount());

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
        HttpRequest request = new HttpRequest(method, path, requestHeaders);

        TestHttpResponse response = null;
        int numAttempts = 0;
        do {
            numAttempts++;
            response = getResponse(uri, request, requestBody);
        } while (shouldRetry(response) && numAttempts < 3);

        Assert.assertEquals("Expected and Actual Status Codes don't match", expectedStatus, response.statusCode);

        return response;
    }

    @Test
    public void testHttpDelete() throws Exception {
        testRequest("DELETE", "https://httpbin.org", "/delete", EMPTY_BODY, 200);
        testRequest("DELETE", "https://httpbin.org", "/get", EMPTY_BODY, 405);
        testRequest("DELETE", "https://httpbin.org", "/post", EMPTY_BODY, 405);
        testRequest("DELETE", "https://httpbin.org", "/put", EMPTY_BODY, 405);
    }

    @Test
    public void testHttpGet() throws Exception {
        testRequest("GET", "https://httpbin.org", "/delete", EMPTY_BODY, 405);
        testRequest("GET", "https://httpbin.org", "/get", EMPTY_BODY, 200);
        testRequest("GET", "https://httpbin.org", "/post", EMPTY_BODY, 405);
        testRequest("GET", "https://httpbin.org", "/put", EMPTY_BODY, 405);
    }

    @Test
    public void testHttpPost() throws Exception {
        testRequest("POST", "https://httpbin.org", "/delete", EMPTY_BODY, 405);
        testRequest("POST", "https://httpbin.org", "/get", EMPTY_BODY, 405);
        testRequest("POST", "https://httpbin.org", "/post", EMPTY_BODY, 200);
        testRequest("POST", "https://httpbin.org", "/put", EMPTY_BODY, 405);
    }

    @Test
    public void testHttpPut() throws Exception {
        testRequest("PUT", "https://httpbin.org", "/delete", EMPTY_BODY, 405);
        testRequest("PUT", "https://httpbin.org", "/get", EMPTY_BODY, 405);
        testRequest("PUT", "https://httpbin.org", "/post", EMPTY_BODY, 405);
        testRequest("PUT", "https://httpbin.org", "/put", EMPTY_BODY, 200);
    }

    @Test
    public void testHttpResponseStatusCodes() throws Exception {
        testRequest("GET", "https://httpbin.org", "/status/200", EMPTY_BODY, 200);
        testRequest("GET", "https://httpbin.org", "/status/300", EMPTY_BODY, 300);
        testRequest("GET", "https://httpbin.org", "/status/400", EMPTY_BODY, 400);
        testRequest("GET", "https://httpbin.org", "/status/500", EMPTY_BODY, 500);
    }

    @Test
    public void testHttpDownload() throws Exception {
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

}
