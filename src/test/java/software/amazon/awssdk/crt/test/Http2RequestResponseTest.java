/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import static software.amazon.awssdk.crt.utils.ByteBufferUtils.transferData;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import software.amazon.awssdk.crt.http.HttpClientConnection;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.http.HttpRequestBodyStream;

import java.net.URI;
import java.nio.ByteBuffer;

public class Http2RequestResponseTest extends HttpRequestResponseFixture {

    public TestHttpResponse testHttp2Request(String method, String endpoint, String path, String requestBody,
            int expectedStatus) throws Exception {
        URI uri = new URI(endpoint);

        HttpHeader[] requestHeaders = null;

        /* TODO: Http2 headers and request */
        requestHeaders = new HttpHeader[] { new HttpHeader("host", uri.getHost()),
                new HttpHeader("content-length", Integer.toString(requestBody.getBytes(UTF8).length)) };

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
                response = getResponse(uri, request, null, HttpClientConnection.AwsHTTPProtocolVersion.HTTP_2);

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
    public void testHttpGet() throws Exception {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        testHttp2Request("GET", "https://httpbin.org", "/delete", EMPTY_BODY, 405);
        testHttp2Request("GET", "https://httpbin.org", "/get", EMPTY_BODY, 200);
        testHttp2Request("GET", "https://httpbin.org", "/post", EMPTY_BODY, 405);
        testHttp2Request("GET", "https://httpbin.org", "/put", EMPTY_BODY, 405);
    }

    @Test
    public void testHttpPost() throws Exception {
        skipIfNetworkUnavailable();
        testHttp2Request("POST", "https://httpbin.org", "/delete", EMPTY_BODY, 405);
        testHttp2Request("POST", "https://httpbin.org", "/get", EMPTY_BODY, 405);
        testHttp2Request("POST", "https://httpbin.org", "/post", EMPTY_BODY, 200);
        testHttp2Request("POST", "https://httpbin.org", "/put", EMPTY_BODY, 405);
    }

    @Test
    public void testHttpPut() throws Exception {
        skipIfNetworkUnavailable();
        testHttp2Request("PUT", "https://httpbin.org", "/delete", EMPTY_BODY, 405);
        testHttp2Request("PUT", "https://httpbin.org", "/get", EMPTY_BODY, 405);
        testHttp2Request("PUT", "https://httpbin.org", "/post", EMPTY_BODY, 405);
        testHttp2Request("PUT", "https://httpbin.org", "/put", EMPTY_BODY, 200);
    }

    @Test
    public void testHttpResponseStatusCodes() throws Exception {
        skipIfNetworkUnavailable();
        testHttp2Request("GET", "https://httpbin.org", "/status/200", EMPTY_BODY, 200);
        testHttp2Request("GET", "https://httpbin.org", "/status/300", EMPTY_BODY, 300);
        testHttp2Request("GET", "https://httpbin.org", "/status/400", EMPTY_BODY, 400);
        testHttp2Request("GET", "https://httpbin.org", "/status/500", EMPTY_BODY, 500);
    }

    @Test
    public void testHttpDownload() throws Exception {
        skipIfNetworkUnavailable();
        /* cloudfront uses HTTP/2 */
        TestHttpResponse response = testHttp2Request("GET", "https://d1cz66xoahf9cl.cloudfront.net/",
                "/http_test_doc.txt", EMPTY_BODY, 200);

        ByteBuffer body = response.bodyBuffer;
        body.flip(); // Flip from Write mode to Read mode

        Assert.assertEquals(TEST_DOC_SHA256, calculateBodyHash(body));
    }
}
