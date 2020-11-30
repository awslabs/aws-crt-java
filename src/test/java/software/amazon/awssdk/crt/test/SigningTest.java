/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import software.amazon.awssdk.crt.*;
import software.amazon.awssdk.crt.auth.credentials.Credentials;
import software.amazon.awssdk.crt.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.crt.auth.signing.AwsSigner;
import software.amazon.awssdk.crt.auth.signing.AwsSigningConfig;
import software.amazon.awssdk.crt.auth.signing.AwsSigningResult;
import software.amazon.awssdk.crt.auth.signing.AwsSigningUtils;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.http.HttpRequestBodyStream;
import static software.amazon.awssdk.crt.utils.ByteBufferUtils.transferData;

public class SigningTest extends CrtTestFixture {

    private static String METHOD = "POST";
    private static byte[] TEST_ACCESS_KEY_ID = "AKIDEXAMPLE".getBytes(StandardCharsets.UTF_8);
    private static byte[] TEST_SECRET_ACCESS_KEY = "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY".getBytes(StandardCharsets.UTF_8);

    private static SimpleDateFormat DATE_FORMAT = dateFormat();

    private static SimpleDateFormat dateFormat() {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format;
    }

    public SigningTest() {}

    private HttpRequestBodyStream makeBodyStreamFromString(String body) {
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

        return bodyStream;
    }

    private HttpRequest createSimpleRequest(String endpoint, String method, String path, String body) throws Exception {

        URI uri = new URI(endpoint);

        HttpHeader[] requestHeaders =
                new HttpHeader[]{
                    new HttpHeader("Host", uri.getHost()),
                    new HttpHeader("Content-Length", Integer.toString(body.getBytes(StandardCharsets.UTF_8).length))
                };

        return new HttpRequest(method, path, requestHeaders, makeBodyStreamFromString(body));
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

    private HttpRequest createBadBodyStreamRequest(String method, String path) throws Exception {

        HttpHeader[] requestHeaders = new HttpHeader[]{
                new HttpHeader("Something", "else")
        };

        HttpRequestBodyStream badBodyStream = new HttpRequestBodyStream() {
            @Override
            public boolean sendRequestBody(ByteBuffer bodyBytesOut) {
                throw new RuntimeException("Doh");
            }

            @Override
            public boolean resetPosition() {
                return true;
            }
        };

        return new HttpRequest(method, path, requestHeaders, badBodyStream);
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
                config.setSignatureType(AwsSigningConfig.AwsSignatureType.HTTP_REQUEST_VIA_HEADERS);
                config.setRegion("us-east-1");
                config.setService("service");
                config.setTime(System.currentTimeMillis());
                config.setCredentialsProvider(provider);
                config.setShouldSignHeader(filterParam);
                config.setUseDoubleUriEncode(true);
                config.setShouldNormalizeUriPath(true);
                config.setSignedBodyValue(AwsSigningConfig.AwsSignedBodyValue.EMPTY_SHA256);

                CompletableFuture<HttpRequest> result = AwsSigner.signRequest(request, config);
                HttpRequest signedRequest = result.get();
                assertNotNull(signedRequest);

                assertTrue(hasHeader(signedRequest, "X-Amz-Date"));
                assertTrue(hasHeader(signedRequest, "Authorization"));
            }
        }
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
                config.setSignatureType(AwsSigningConfig.AwsSignatureType.HTTP_REQUEST_VIA_QUERY_PARAMS);
                config.setRegion("us-east-1");
                config.setService("service");
                config.setTime(DATE_FORMAT.parse("2015-08-30T12:36:00Z").getTime());
                config.setCredentialsProvider(provider);
                config.setUseDoubleUriEncode(true);
                config.setShouldNormalizeUriPath(true);
                config.setSignedBodyValue(AwsSigningConfig.AwsSignedBodyValue.EMPTY_SHA256);
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
                config.setSignatureType(AwsSigningConfig.AwsSignatureType.HTTP_REQUEST_VIA_HEADERS);
                config.setRegion("us-east-1");
                config.setService("service");
                config.setTime(DATE_FORMAT.parse("2015-08-30T12:36:00Z").getTime());
                config.setCredentialsProvider(provider);
                config.setUseDoubleUriEncode(true);
                config.setShouldNormalizeUriPath(true);
                config.setSignedBodyValue(AwsSigningConfig.AwsSignedBodyValue.EMPTY_SHA256);

                CompletableFuture<HttpRequest> result = AwsSigner.signRequest(request, config);
                HttpRequest signedRequest = result.get();
                assertNotNull(signedRequest);

                assertTrue(hasHeaderWithValue(signedRequest, "X-Amz-Date", "20150830T123600Z"));

                // expected authorization value for post-vanilla-query test
                assertTrue(hasHeaderWithValue(signedRequest, "Authorization", "AWS4-HMAC-SHA256 Credential=AKIDEXAMPLE/20150830/us-east-1/service/aws4_request, SignedHeaders=host;x-amz-date, Signature=28038455d6de14eafc1f9222cf5aa6f1a96197d7deb8263271d420d138af7f11"));
            }
        }
    }

    @Test
    public void testSigv4aHeader() throws Exception {
        Credentials credentials = new Credentials(TEST_ACCESS_KEY_ID, TEST_SECRET_ACCESS_KEY, null);
        HttpRequest request = createSigv4TestSuiteRequest();

        try (AwsSigningConfig config = new AwsSigningConfig()) {
            config.setAlgorithm(AwsSigningConfig.AwsSigningAlgorithm.SIGV4_ASYMMETRIC);
            config.setSignatureType(AwsSigningConfig.AwsSignatureType.HTTP_REQUEST_VIA_HEADERS);
            config.setRegion("us-east-1");
            config.setService("service");
            config.setTime(DATE_FORMAT.parse("2015-08-30T12:36:00Z").getTime());
            config.setCredentials(credentials);
            config.setUseDoubleUriEncode(true);
            config.setShouldNormalizeUriPath(true);
            config.setSignedBodyValue(AwsSigningConfig.AwsSignedBodyValue.EMPTY_SHA256);

            CompletableFuture<HttpRequest> result = AwsSigner.signRequest(request, config);
            HttpRequest signedRequest = result.get();
            assertNotNull(signedRequest);

            assertTrue(hasHeaderWithValue(signedRequest, "X-Amz-Date", "20150830T123600Z"));

            // expected authorization prefix
            assertTrue(hasHeaderWithValuePrefix(signedRequest, "Authorization", "AWS4-ECDSA-P256-SHA256 Credential=AKIDEXAMPLE/20150830/service/aws4_request, SignedHeaders=host;x-amz-date;x-amz-region-set, Signature="));
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
                config.setSignatureType(AwsSigningConfig.AwsSignatureType.HTTP_REQUEST_VIA_HEADERS);
                config.setRegion("us-east-1");
                config.setService("service");
                config.setTime(System.currentTimeMillis());
                config.setCredentialsProvider(provider);
                config.setUseDoubleUriEncode(true);
                config.setShouldNormalizeUriPath(true);
                config.setSignedBodyValue(AwsSigningConfig.AwsSignedBodyValue.EMPTY_SHA256);

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
        }
    }

    @Test(expected = CrtRuntimeException.class)
    public void testSigningFailureBodyStreamException() throws Exception {
        try (StaticCredentialsProvider provider = new StaticCredentialsProvider.StaticCredentialsProviderBuilder()
                .withAccessKeyId(TEST_ACCESS_KEY_ID)
                .withSecretAccessKey(TEST_SECRET_ACCESS_KEY)
                .build();) {

            // request is missing Host header
            HttpRequest request = createBadBodyStreamRequest("POST", "/bad");

            try (AwsSigningConfig config = new AwsSigningConfig()) {
                config.setAlgorithm(AwsSigningConfig.AwsSigningAlgorithm.SIGV4);
                config.setSignatureType(AwsSigningConfig.AwsSignatureType.HTTP_REQUEST_VIA_HEADERS);
                config.setRegion("us-east-1");
                config.setService("service");
                config.setTime(System.currentTimeMillis());
                config.setCredentialsProvider(provider);
                config.setUseDoubleUriEncode(true);
                config.setShouldNormalizeUriPath(true);
                config.setSignedBodyHeader(AwsSigningConfig.AwsSignedBodyHeaderType.X_AMZ_CONTENT_SHA256);

                CompletableFuture<HttpRequest> result = AwsSigner.signRequest(request, config);
                result.get();
            }
        } catch (Exception e) {
            Throwable cause = e.getCause();
            assertTrue(cause != null);
            assertTrue(cause.getClass() == CrtRuntimeException.class);
            CrtRuntimeException crt = (CrtRuntimeException) cause;
            assertTrue(crt.errorName.equals("AWS_IO_STREAM_READ_FAILED"));
            throw crt;
        }
    }

    /*
     * Chunked encoding signing based on https://docs.aws.amazon.com/AmazonS3/latest/API/sigv4-streaming.html
     */
    private static String CHUNKED_ACCESS_KEY_ID = "AKIAIOSFODNN7EXAMPLE";
    private static String CHUNKED_SECRET_ACCESS_KEY = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";
    private static String CHUNKED_SIGV4A_TEST_ECC_PUB_X = "18b7d04643359f6ec270dcbab8dce6d169d66ddc9778c75cfb08dfdb701637ab";
    private static String CHUNKED_SIGV4A_TEST_ECC_PUB_Y = "fa36b35e4fe67e3112261d2e17a956ef85b06e44712d2850bcd3c2161e9993f2";
    private static String CHUNKED_TEST_REGION= "us-east-1";
    private static String CHUNKED_TEST_SERVICE = "s3";
    private static String CHUNKED_TEST_SIGNING_TIME = "2013-05-24T00:00:00Z";
    private static int CHUNK1_SIZE = 65536;
    private static int CHUNK2_SIZE = 1024;

    private Credentials createChunkedTestCredentials() {
        return new Credentials(CHUNKED_ACCESS_KEY_ID.getBytes(), CHUNKED_SECRET_ACCESS_KEY.getBytes(), null);
    }

    private AwsSigningConfig createChunkedRequestSigningConfig() throws Exception {

        AwsSigningConfig config = new AwsSigningConfig();
        config.setAlgorithm(AwsSigningConfig.AwsSigningAlgorithm.SIGV4);
        config.setSignatureType(AwsSigningConfig.AwsSignatureType.HTTP_REQUEST_VIA_HEADERS);
        config.setRegion(CHUNKED_TEST_REGION);
        config.setService(CHUNKED_TEST_SERVICE);
        config.setTime(DATE_FORMAT.parse(CHUNKED_TEST_SIGNING_TIME).getTime());

        config.setUseDoubleUriEncode(false);
        config.setShouldNormalizeUriPath(true);

        config.setSignedBodyHeader(AwsSigningConfig.AwsSignedBodyHeaderType.X_AMZ_CONTENT_SHA256);
        config.setSignedBodyValue(AwsSigningConfig.AwsSignedBodyValue.STREAMING_AWS4_HMAC_SHA256_PAYLOAD);

        config.setCredentials(createChunkedTestCredentials());

        return config;
    }

    private AwsSigningConfig createChunkSigningConfig() throws Exception {
        AwsSigningConfig config = new AwsSigningConfig();
        config.setAlgorithm(AwsSigningConfig.AwsSigningAlgorithm.SIGV4);
        config.setSignatureType(AwsSigningConfig.AwsSignatureType.HTTP_REQUEST_CHUNK);
        config.setRegion(CHUNKED_TEST_REGION);
        config.setService(CHUNKED_TEST_SERVICE);
        config.setTime(DATE_FORMAT.parse(CHUNKED_TEST_SIGNING_TIME).getTime());

        config.setUseDoubleUriEncode(false);
        config.setShouldNormalizeUriPath(true);
        config.setSignedBodyHeader(AwsSigningConfig.AwsSignedBodyHeaderType.NONE);
        config.setCredentials(createChunkedTestCredentials());

        return config;
    }

    private HttpRequest createChunkedTestRequest() throws Exception {

        URI uri = new URI("https://s3.amazonaws.com/examplebucket/chunkObject.txt");

        HttpHeader[] requestHeaders =
                new HttpHeader[]{
                        new HttpHeader("Host", uri.getHost()),
                        new HttpHeader("x-amz-storage-class", "REDUCED_REDUNDANCY"),
                        new HttpHeader("Content-Encoding", "aws-chunked"),
                        new HttpHeader("x-amz-decoded-content-length", "66560"),
                        new HttpHeader("Content-Length", "66824")
                };

        return new HttpRequest("PUT", uri.getPath(), requestHeaders, null);
    }

    private HttpRequestBodyStream createChunk1Stream() {
        StringBuilder chunkBody = new StringBuilder();
        for (int i = 0; i < CHUNK1_SIZE; ++i) {
            chunkBody.append('a');
        }

        return makeBodyStreamFromString(chunkBody.toString());
    }

    private HttpRequestBodyStream createChunk2Stream() {
        StringBuilder chunkBody = new StringBuilder();
        for (int i = 0; i < CHUNK2_SIZE; ++i) {
            chunkBody.append('a');
        }

        return makeBodyStreamFromString(chunkBody.toString());
    }

    private static String EXPECTED_CHUNK_REQUEST_AUTHORIZATION_HEADER =
            "AWS4-HMAC-SHA256 Credential=AKIAIOSFODNN7EXAMPLE/20130524/us-east-1/s3/aws4_request, " +
            "SignedHeaders=content-encoding;content-length;host;x-amz-content-sha256;x-amz-date;x-amz-decoded-content-length;x-" +
            "amz-storage-class, Signature=4f232c4386841ef735655705268965c44a0e4690baa4adea153f7db9fa80a0a9";

    private static byte[] EXPECTED_REQUEST_SIGNATURE = "4f232c4386841ef735655705268965c44a0e4690baa4adea153f7db9fa80a0a9".getBytes(StandardCharsets.UTF_8);
    private static byte[] EXPECTED_FIRST_CHUNK_SIGNATURE = "ad80c730a21e5b8d04586a2213dd63b9a0e99e0e2307b0ade35a65485a288648".getBytes(StandardCharsets.UTF_8);
    private static byte[] EXPECTED_SECOND_CHUNK_SIGNATURE = "0055627c9e194cb4542bae2aa5492e3c1575bbb81b612b7d234b86a503ef5497".getBytes(StandardCharsets.UTF_8);
    private static byte[] EXPECTED_FINAL_CHUNK_SIGNATURE = "b6c6ea8a5354eaf15b3cb7646744f4275b71ea724fed81ceb9323e279d449df9".getBytes(StandardCharsets.UTF_8);

    @Test
    public void testChunkedSigv4Signing() throws Exception {

        HttpRequest request = createChunkedTestRequest();


        CompletableFuture<HttpRequest> result = AwsSigner.signRequest(request, createChunkedRequestSigningConfig());
        HttpRequest signedRequest = result.get();
        assertNotNull(signedRequest);

        assertTrue(hasHeaderWithValue(signedRequest, "Authorization", EXPECTED_CHUNK_REQUEST_AUTHORIZATION_HEADER));

        /* If the authorization header is equal then certainly we can assume the signature value */
        byte[] signature = EXPECTED_REQUEST_SIGNATURE;

        HttpRequestBodyStream chunk1 = createChunk1Stream();
        CompletableFuture<byte[]> chunk1Result = AwsSigner.signChunk(chunk1, signature, createChunkSigningConfig());

        signature = chunk1Result.get();
        assertTrue(Arrays.equals(signature, EXPECTED_FIRST_CHUNK_SIGNATURE));

        HttpRequestBodyStream chunk2 = createChunk2Stream();
        CompletableFuture<byte[]> chunk2Result = AwsSigner.signChunk(chunk2, signature, createChunkSigningConfig());

        signature = chunk2Result.get();
        assertTrue(Arrays.equals(signature, EXPECTED_SECOND_CHUNK_SIGNATURE));

        CompletableFuture<byte[]> finalChunkResult = AwsSigner.signChunk(null, signature, createChunkSigningConfig());
        signature = finalChunkResult.get();
        assertTrue(Arrays.equals(signature, EXPECTED_FINAL_CHUNK_SIGNATURE));
    }

    private static String CHUNKED_SIGV4A_CANONICAL_REQUEST = "PUT\n" +
            "/examplebucket/chunkObject.txt\n" +
            "\n" +
            "content-encoding:aws-chunked\n" +
            "content-length:66824\n" +
            "host:s3.amazonaws.com\n" +
            "x-amz-content-sha256:STREAMING-AWS4-ECDSA-P256-SHA256-PAYLOAD\n" +
            "x-amz-date:20130524T000000Z\n" +
            "x-amz-decoded-content-length:66560\n" +
            "x-amz-region-set:us-east-1\n" +
            "x-amz-storage-class:REDUCED_REDUNDANCY\n" +
            "\n" +
            "content-encoding;content-length;host;x-amz-content-sha256;x-amz-date;x-amz-decoded-content-length;x-amz-region-set;x-amz-storage-class\n" +
            "STREAMING-AWS4-ECDSA-P256-SHA256-PAYLOAD";

    private static String CHUNK_STS_PRE_SIGNATURE = "AWS4-ECDSA-P256-SHA256-PAYLOAD\n" +
        "20130524T000000Z\n" +
        "20130524/s3/aws4_request\n";

    private static String CHUNK1_STS_POST_SIGNATURE = "\ne3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855\n" +
        "bf718b6f653bebc184e1479f1935b8da974d701b893afcf49e701f3e2f9f9c5a";

    private static String CHUNK2_STS_POST_SIGNATURE = "\ne3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855\n" +
        "2edc986847e209b4016e141a6dc8716d3207350f416969382d431539bf292e4a";

    private static String CHUNK3_STS_POST_SIGNATURE = "\ne3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855\n" +
        "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    private byte[] buildChunkStringToSign(byte[] previousSignature, String stsPostSignature) {
        StringBuilder stsBuilder = new StringBuilder();

        stsBuilder.append(CHUNK_STS_PRE_SIGNATURE);
        String signature = new String(previousSignature, StandardCharsets.UTF_8);
        int paddingIndex = signature.indexOf('*');
        if (paddingIndex != -1) {
            signature = signature.substring(0, paddingIndex);
        }
        stsBuilder.append(signature);
        stsBuilder.append(stsPostSignature);

        return stsBuilder.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Test
    public void testChunkedSigv4aSigning() throws Exception {

        HttpRequest request = createChunkedTestRequest();
        AwsSigningConfig chunkedRequestSigningConfig = createChunkedRequestSigningConfig();
        chunkedRequestSigningConfig.setAlgorithm(AwsSigningConfig.AwsSigningAlgorithm.SIGV4_ASYMMETRIC);
        chunkedRequestSigningConfig.setSignedBodyValue(AwsSigningConfig.AwsSignedBodyValue.STREAMING_AWS4_ECDSA_P256_SHA256_PAYLOAD);

        CompletableFuture<AwsSigningResult> result = AwsSigner.sign(request, chunkedRequestSigningConfig);
        HttpRequest signedRequest = result.get().getSignedRequest();
        assertNotNull(signedRequest);

        byte[] requestSignature = result.get().getSignature();
        assertTrue(AwsSigningUtils.verifySigv4aEcdsaSignature(request, CHUNKED_SIGV4A_CANONICAL_REQUEST, chunkedRequestSigningConfig, requestSignature, CHUNKED_SIGV4A_TEST_ECC_PUB_X, CHUNKED_SIGV4A_TEST_ECC_PUB_Y));

        HttpRequestBodyStream chunk1 = createChunk1Stream();
        AwsSigningConfig chunkSigningConfig = createChunkSigningConfig();
        chunkSigningConfig.setAlgorithm(AwsSigningConfig.AwsSigningAlgorithm.SIGV4_ASYMMETRIC);

        CompletableFuture<byte[]> chunk1Result = AwsSigner.signChunk(chunk1, requestSignature, chunkSigningConfig);

        byte[] chunk1StringToSign = buildChunkStringToSign(requestSignature, CHUNK1_STS_POST_SIGNATURE);
        assertTrue(AwsSigningUtils.verifyRawSha256EcdsaSignature(chunk1StringToSign, chunk1Result.get(), CHUNKED_SIGV4A_TEST_ECC_PUB_X, CHUNKED_SIGV4A_TEST_ECC_PUB_Y));

        HttpRequestBodyStream chunk2 = createChunk2Stream();
        CompletableFuture<byte[]> chunk2Result = AwsSigner.signChunk(chunk2, chunk1Result.get(), chunkSigningConfig);

        byte[] chunk2StringToSign = buildChunkStringToSign(chunk1Result.get(), CHUNK2_STS_POST_SIGNATURE);
        assertTrue(AwsSigningUtils.verifyRawSha256EcdsaSignature(chunk2StringToSign, chunk2Result.get(), CHUNKED_SIGV4A_TEST_ECC_PUB_X, CHUNKED_SIGV4A_TEST_ECC_PUB_Y));

        CompletableFuture<byte[]> chunk3Result = AwsSigner.signChunk(null, chunk2Result.get(), chunkSigningConfig);

        byte[] chunk3StringToSign = buildChunkStringToSign(chunk2Result.get(), CHUNK3_STS_POST_SIGNATURE);
        assertTrue(AwsSigningUtils.verifyRawSha256EcdsaSignature(chunk3StringToSign, chunk3Result.get(), CHUNKED_SIGV4A_TEST_ECC_PUB_X, CHUNKED_SIGV4A_TEST_ECC_PUB_Y));
    }
};
