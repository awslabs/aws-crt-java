/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import software.amazon.awssdk.crt.*;
import software.amazon.awssdk.crt.auth.credentials.Credentials;
import software.amazon.awssdk.crt.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.crt.auth.signing.AwsSigner;
import software.amazon.awssdk.crt.auth.signing.AwsSigningConfig;
import software.amazon.awssdk.crt.cal.EccKeyPair;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.http.HttpRequestBodyStream;
import static software.amazon.awssdk.crt.utils.ByteBufferUtils.transferData;

public class SigningTest {

    public static String METHOD = "POST";
    private static byte[] TEST_ACCESS_KEY_ID = "AKIDEXAMPLE".getBytes();
    private static byte[] TEST_SECRET_ACCESS_KEY = "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY".getBytes();

    public SigningTest() {}

    private HttpRequest createSimpleRequest(String endpoint, String method, String path, String body) throws Exception {

        URI uri = new URI(endpoint);

        HttpHeader[] requestHeaders =
                new HttpHeader[]{
                    new HttpHeader("Host", uri.getHost()),
                    new HttpHeader("Content-Length", Integer.toString(body.getBytes(StandardCharsets.UTF_8).length))
                };

        final ByteBuffer bodyBytesIn = ByteBuffer.wrap(body.getBytes(StandardCharsets.UTF_8));
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

        return new HttpRequest(method, path, requestHeaders, bodyStream);
    }

    /* post-vanilla-query test case */
    private HttpRequest createSigv4TestSuiteRequest() throws Exception {

        HttpHeader[] requestHeaders =
                new HttpHeader[]{
                    new HttpHeader("Host", "example.amazonaws.com")
                };

        String method = "POST";
        String path = "/?Param1=value1";

        return new HttpRequest(method, path, requestHeaders, null);
    }

    private HttpRequest createUnsignableRequest(String method, String path) throws Exception {

        HttpHeader[] requestHeaders = new HttpHeader[]{
            new HttpHeader("Authorization", "bad")
        };

        return new HttpRequest(method, path, requestHeaders, null);
    }

    private boolean hasHeader(HttpRequest request, String name) {
        for (HttpHeader header : request.getHeaders()) {
            if (header.getName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasHeaderWithValue(HttpRequest request, String name, String value) {
        for (HttpHeader header : request.getHeaders()) {
            if (header.getName().equals(name) && header.getValue().equals(value)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasHeaderWithValuePrefix(HttpRequest request, String name, String valuePrefix) {
        for (HttpHeader header : request.getHeaders()) {
            if (header.getName().equals(name) && header.getValue().startsWith(valuePrefix)) {
                return true;
            }
        }

        return false;
    }

    @Test
    public void testSigningSuccess() throws Exception {
        try (StaticCredentialsProvider provider = new StaticCredentialsProvider.StaticCredentialsProviderBuilder()
            .withAccessKeyId(TEST_ACCESS_KEY_ID)
            .withSecretAccessKey(TEST_SECRET_ACCESS_KEY)
            .build();) {

            HttpRequest request = createSimpleRequest("https://www.example.com", "POST", "/derp", "<body>Hello</body>");

            Predicate<String> filterParam = param -> !param.equals("bad-param");

            try (AwsSigningConfig config = new AwsSigningConfig()) {
                config.setAlgorithm(AwsSigningConfig.AwsSigningAlgorithm.SIGV4);
                config.setTransform(AwsSigningConfig.AwsRequestSigningTransform.HEADER);
                config.setRegion("us-east-1");
                config.setService("service");
                config.setTime(Instant.now());
                config.setCredentialsProvider(provider);
                config.setShouldSignParameter(filterParam);
                config.setUseDoubleUriEncode(true);
                config.setShouldNormalizeUriPath(true);
                config.setSignBody(AwsSigningConfig.AwsBodySigningConfigType.AWS_BODY_SIGNING_OFF);

                CompletableFuture<HttpRequest> result = AwsSigner.signRequest(request, config);
                HttpRequest signedRequest = result.get();
                assertNotNull(signedRequest);

                assertTrue(hasHeader(signedRequest, "X-Amz-Date"));
                assertTrue(hasHeader(signedRequest, "Authorization"));
            }
        }

        CrtResource.waitForNoResources();
    }

    @Test
    public void testQuerySigningSuccess() throws Exception {
        try (StaticCredentialsProvider provider = new StaticCredentialsProvider.StaticCredentialsProviderBuilder()
            .withAccessKeyId(TEST_ACCESS_KEY_ID)
            .withSecretAccessKey(TEST_SECRET_ACCESS_KEY)
            .build();) {

            HttpRequest request = createSigv4TestSuiteRequest();

            try (AwsSigningConfig config = new AwsSigningConfig()) {
                config.setAlgorithm(AwsSigningConfig.AwsSigningAlgorithm.SIGV4);
                config.setTransform(AwsSigningConfig.AwsRequestSigningTransform.QUERY_PARAM);
                config.setRegion("us-east-1");
                config.setService("service");
                config.setTime(Instant.parse("2015-08-30T12:36:00Z"));
                config.setCredentialsProvider(provider);
                config.setUseDoubleUriEncode(true);
                config.setShouldNormalizeUriPath(true);
                config.setSignBody(AwsSigningConfig.AwsBodySigningConfigType.AWS_BODY_SIGNING_OFF);
                config.setExpirationInSeconds(60);

                CompletableFuture<HttpRequest> result = AwsSigner.signRequest(request, config);
                HttpRequest signedRequest = result.get();
                assertNotNull(signedRequest);

                String path = signedRequest.getEncodedPath();

                assertTrue(path.contains("X-Amz-Signature="));
                assertTrue(path.contains("X-Amz-SignedHeaders=host"));
                assertTrue(path.contains("X-Amz-Credential=AKIDEXAMPLE%2F20150830%2F"));
                assertTrue(path.contains("X-Amz-Algorithm=AWS4-HMAC-SHA256"));
                assertTrue(path.contains("X-Amz-Expires=60"));
            }
        }

        CrtResource.waitForNoResources();
    }

    @Test
    public void testSigningBasicSigv4Test() throws Exception {
        try (StaticCredentialsProvider provider = new StaticCredentialsProvider.StaticCredentialsProviderBuilder()
            .withAccessKeyId(TEST_ACCESS_KEY_ID)
            .withSecretAccessKey(TEST_SECRET_ACCESS_KEY)
            .build();) {

            HttpRequest request = createSigv4TestSuiteRequest();

            try (AwsSigningConfig config = new AwsSigningConfig()) {
                config.setAlgorithm(AwsSigningConfig.AwsSigningAlgorithm.SIGV4);
                config.setTransform(AwsSigningConfig.AwsRequestSigningTransform.HEADER);
                config.setRegion("us-east-1");
                config.setService("service");
                config.setTime(Instant.parse("2015-08-30T12:36:00Z"));
                config.setCredentialsProvider(provider);
                config.setUseDoubleUriEncode(true);
                config.setShouldNormalizeUriPath(true);
                config.setSignBody(AwsSigningConfig.AwsBodySigningConfigType.AWS_BODY_SIGNING_OFF);

                CompletableFuture<HttpRequest> result = AwsSigner.signRequest(request, config);
                HttpRequest signedRequest = result.get();
                assertNotNull(signedRequest);

                assertTrue(hasHeaderWithValue(signedRequest, "X-Amz-Date", "20150830T123600Z"));

                // expected authorization value for post-vanilla-query test
                assertTrue(hasHeaderWithValue(signedRequest, "Authorization", "AWS4-HMAC-SHA256 Credential=AKIDEXAMPLE/20150830/us-east-1/service/aws4_request, SignedHeaders=host;x-amz-date, Signature=28038455d6de14eafc1f9222cf5aa6f1a96197d7deb8263271d420d138af7f11"));
            }
        }

        CrtResource.waitForNoResources();
    }

    @Test
    public void testSigv4aHeader() throws Exception {
        Credentials credentials = new Credentials(TEST_ACCESS_KEY_ID, TEST_SECRET_ACCESS_KEY, null);
        HttpRequest request = createSigv4TestSuiteRequest();

        try (AwsSigningConfig config = new AwsSigningConfig()) {
            config.setAlgorithm(AwsSigningConfig.AwsSigningAlgorithm.SIGV4_ASYMMETRIC);
            config.setTransform(AwsSigningConfig.AwsRequestSigningTransform.HEADER);
            config.setRegion("us-east-1");
            config.setService("service");
            config.setTime(Instant.parse("2015-08-30T12:36:00Z"));
            config.setCredentials(credentials);
            config.setUseDoubleUriEncode(true);
            config.setShouldNormalizeUriPath(true);
            config.setSignBody(AwsSigningConfig.AwsBodySigningConfigType.AWS_BODY_SIGNING_OFF);

            CompletableFuture<HttpRequest> result = AwsSigner.signRequest(request, config);
            HttpRequest signedRequest = result.get();
            assertNotNull(signedRequest);

            assertTrue(hasHeaderWithValue(signedRequest, "X-Amz-Date", "20150830T123600Z"));

            // expected authorization prefix
            assertTrue(hasHeaderWithValuePrefix(signedRequest, "Authorization", "AWS4-ECDSA-P256-SHA256 Credential=AKIDEXAMPLE/20150830/service/aws4_request, SignedHeaders=host;x-amz-date;x-amz-region-set, Signature="));
        }

        CrtResource.waitForNoResources();
    }

    @Test
    public void testSigv4aQuery() throws Exception {
        HttpRequest request = createSigv4TestSuiteRequest();
        Credentials credentials = new Credentials(TEST_ACCESS_KEY_ID, TEST_SECRET_ACCESS_KEY, null);

        try (EccKeyPair keyPair = EccKeyPair.newDeriveFromCredentials(credentials, EccKeyPair.AwsEccCurve.AWS_ECDSA_P256);
             AwsSigningConfig config = new AwsSigningConfig()) {
            config.setAlgorithm(AwsSigningConfig.AwsSigningAlgorithm.SIGV4_ASYMMETRIC);
            config.setTransform(AwsSigningConfig.AwsRequestSigningTransform.QUERY_PARAM);
            config.setRegion("us-east-1");
            config.setService("service");
            config.setTime(Instant.parse("2015-08-30T12:36:00Z"));
            config.setCredentials(credentials);
            config.setEccKeyPair(keyPair);
            config.setUseDoubleUriEncode(true);
            config.setShouldNormalizeUriPath(true);
            config.setSignBody(AwsSigningConfig.AwsBodySigningConfigType.AWS_BODY_SIGNING_OFF);
            config.setExpirationInSeconds(60);

            CompletableFuture<HttpRequest> result = AwsSigner.signRequest(request, config);
            HttpRequest signedRequest = result.get();
            assertNotNull(signedRequest);

            String path = signedRequest.getEncodedPath();

            assertTrue(path.contains("X-Amz-Signature="));
            assertTrue(path.contains("X-Amz-SignedHeaders=host"));
            assertTrue(path.contains("X-Amz-Credential=AKIDEXAMPLE%2F20150830%2Fservice%2Faws4_request"));
            assertTrue(path.contains("X-Amz-Algorithm=AWS4-ECDSA-P256-SHA256"));
            assertTrue(path.contains("X-Amz-Expires=60"));
            assertTrue(path.contains("X-Amz-Region-Set=us-east-1"));
        }

        CrtResource.waitForNoResources();
    }

    @Test(expected = CrtRuntimeException.class)
    public void testSigningFailureBadRequest() throws Exception {
        try (StaticCredentialsProvider provider = new StaticCredentialsProvider.StaticCredentialsProviderBuilder()
            .withAccessKeyId(TEST_ACCESS_KEY_ID)
            .withSecretAccessKey(TEST_SECRET_ACCESS_KEY)
            .build();) {

            // request is missing Host header
            HttpRequest request = createUnsignableRequest("POST", "/bad");

            try (AwsSigningConfig config = new AwsSigningConfig()) {
                config.setAlgorithm(AwsSigningConfig.AwsSigningAlgorithm.SIGV4);
                config.setTransform(AwsSigningConfig.AwsRequestSigningTransform.HEADER);
                config.setRegion("us-east-1");
                config.setService("service");
                config.setTime(Instant.now());
                config.setCredentialsProvider(provider);
                config.setUseDoubleUriEncode(true);
                config.setShouldNormalizeUriPath(true);
                config.setSignBody(AwsSigningConfig.AwsBodySigningConfigType.AWS_BODY_SIGNING_OFF);

                CompletableFuture<HttpRequest> result = AwsSigner.signRequest(request, config);
                result.get();
            }
        } catch (Exception e) {
            Throwable cause = e.getCause();
            assertTrue(cause != null);
            assertTrue(cause.getClass() == CrtRuntimeException.class);
            CrtRuntimeException crt = (CrtRuntimeException) cause;
            assertTrue(crt.errorName.equals("AWS_AUTH_SIGNING_ILLEGAL_REQUEST_HEADER"));
            throw crt;
        } finally {
            CrtResource.waitForNoResources();
        }
    }
};
