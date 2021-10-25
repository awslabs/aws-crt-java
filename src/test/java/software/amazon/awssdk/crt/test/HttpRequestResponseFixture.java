/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.test;

import org.junit.Assert;
import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.http.HttpClientConnection;
import software.amazon.awssdk.crt.http.HttpClientConnectionManager;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.http.HttpStreamResponseHandler;
import software.amazon.awssdk.crt.http.HttpStream;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class HttpRequestResponseFixture extends HttpClientTestFixture {

    protected final static Charset UTF8 = StandardCharsets.UTF_8;
    protected final String EMPTY_BODY = "";
    protected final static String TEST_DOC_LINE = "This is a sample to prove that http downloads and uploads work. It doesn't really matter what's in here, we mainly just need to verify the downloads and uploads work.";
    protected final static String TEST_DOC_SHA256 = "C7FDB5314B9742467B16BD5EA2F8012190B5E2C44A005F7984F89AAB58219534";

    protected class TestHttpResponse {
        int statusCode = -1;
        int blockType = -1;
        List<HttpHeader> headers = new ArrayList<>();
        ByteBuffer bodyBuffer = ByteBuffer.wrap(new byte[16 * 1024 * 1024]); // Allow up to 16 MB Responses
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
            for (HttpHeader h : headers) {
                builder.append("\nHeader[" + i + "]: " + h.toString());
            }

            builder.append("\nBody:\n");
            builder.append(getBody());

            return builder.toString();
        }
    }

    protected boolean shouldRetry(TestHttpResponse response) {
        // Retry if we couldn't connect or if we got 503 response
        if (response.onCompleteErrorCode != CRT.AWS_CRT_SUCCESS || response.statusCode == 503) {
            return true;
        }
        return false;
    }

    public static String byteArrayToHex(byte[] input) {
        StringBuilder output = new StringBuilder(input.length * 2);
        for (byte b : input) {
            output.append(String.format("%02X", b));
        }
        return output.toString();
    }

    public String calculateBodyHash(ByteBuffer bodyBuffer) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(bodyBuffer);
        return byteArrayToHex(digest.digest());
    }

    public TestHttpResponse getResponse(URI uri, HttpRequest request, byte[] chunkedData,
            HttpClientConnection.AwsHTTPProtocolVersion expectedVersion) throws Exception {
        boolean actuallyConnected = false;

        final CompletableFuture<Void> reqCompleted = new CompletableFuture<>();

        final TestHttpResponse response = new TestHttpResponse();

        CompletableFuture<Void> shutdownComplete = null;

        try (HttpClientConnectionManager connPool = createConnectionPoolManager(uri, expectedVersion)) {
            shutdownComplete = connPool.getShutdownCompleteFuture();
            try (HttpClientConnection conn = connPool.acquireConnection().get(60, TimeUnit.SECONDS)) {
                actuallyConnected = true;
                HttpStreamResponseHandler streamHandler = new HttpStreamResponseHandler() {
                    @Override
                    public void onResponseHeaders(HttpStream stream, int responseStatusCode, int blockType,
                            HttpHeader[] nextHeaders) {
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

                HttpStream stream = conn.makeRequest(request, streamHandler);
                stream.activate();

                if (chunkedData != null) {
                    stream.writeChunk(chunkedData, true).get(5, TimeUnit.SECONDS);
                }
                // Give the request up to 60 seconds to complete, otherwise throw a
                // TimeoutException
                reqCompleted.get(60, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Assert.assertTrue(actuallyConnected);

        shutdownComplete.get(60, TimeUnit.SECONDS);

        CrtResource.waitForNoResources();

        return response;

    }

}
