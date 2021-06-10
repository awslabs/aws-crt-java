package com.amazonaws.s3;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.amazonaws.s3.model.PutObjectOutput;
import org.mockito.ArgumentCaptor;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

import com.amazonaws.s3.model.GetObjectOutput;
import com.amazonaws.s3.model.GetObjectRequest;
import com.amazonaws.s3.model.PutObjectRequest;
import com.amazonaws.test.AwsClientTestFixture;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.auth.credentials.CredentialsProvider;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.io.StandardRetryOptions;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.s3.S3MetaRequestOptions;
import software.amazon.awssdk.crt.s3.S3Client;

public class S3NativeClientTest extends AwsClientTestFixture {
    private static final String BUCKET = System.getProperty("crt.test_s3_bucket", "aws-crt-canary-bucket");
    private static final String REGION = System.getProperty("crt.test_s3_region", "us-west-2");
    private static final String ALT_REGION = System.getProperty("crt.alt_test_s3_region", "us-east-1");
    private static final String GET_OBJECT_KEY = System.getProperty("crt.test_s3_get_object_key", "get_object_test_10MB.txt");
    private static final String PUT_OBJECT_KEY = System.getProperty("crt.test_s3_put_object_key", "upload_object_test.txt");

    @BeforeClass
    public static void haveAwsCredentials() {
        Assume.assumeTrue(areAwsCredentialsAvailable());
    }

    @Test
    public void testGetObject() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);

        try (final EventLoopGroup elGroup = new EventLoopGroup(9);
                final HostResolver resolver = new HostResolver(elGroup, 128);
                final ClientBootstrap clientBootstrap = new ClientBootstrap(elGroup, resolver);
                final CredentialsProvider provider = getTestCredentialsProvider()) {
            final S3NativeClient nativeClient = new S3NativeClient(REGION, clientBootstrap, provider, 64_000_000l,
                    100.);
            final long length[] = { 0 };
            nativeClient.getObject(GetObjectRequest.builder().bucket(BUCKET).key(GET_OBJECT_KEY).build(),
                    new ResponseDataConsumer<GetObjectOutput>() {

                        @Override
                        public void onResponse(GetObjectOutput response) {
                            assertNotNull(response);
                        }

                        @Override
                        public void onResponseData(ByteBuffer bodyBytesIn) {
                            length[0] += bodyBytesIn.remaining();
                        }

                        @Override
                        public void onFinished() {
                        }

                        @Override
                        public void onException(final CrtRuntimeException e) {
                        }
                    }).join();
        }
    }

    @Test
    public void testPutObject() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);

        try (final EventLoopGroup elGroup = new EventLoopGroup(9);
                final HostResolver resolver = new HostResolver(elGroup, 128);
                final ClientBootstrap clientBootstrap = new ClientBootstrap(elGroup, resolver);
                final CredentialsProvider provider = getTestCredentialsProvider()) {
            final S3NativeClient nativeClient = new S3NativeClient(REGION, clientBootstrap, provider, 64_000_000l,
                    100.);
            final long contentLength = 1024l;
            final long lengthWritten[] = { 0 };
            nativeClient.putObject(
                    PutObjectRequest.builder().bucket(BUCKET).key(PUT_OBJECT_KEY).contentLength(contentLength).build(),
                    buffer -> {
                        while (buffer.hasRemaining()) {
                            buffer.put((byte) 42);
                            ++lengthWritten[0];
                        }

                        return lengthWritten[0] == contentLength;
                    }).join();

        }
    }

    private class CancelTestData<T>
    {
        public int ExpectedPartCount;
        public int PartCount;
        public CompletableFuture<T> ResultFuture;
    }

    @Test
    public void testGetObjectCancel() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);

        try (final EventLoopGroup elGroup = new EventLoopGroup(9);
                final HostResolver resolver = new HostResolver(elGroup, 128);
                final ClientBootstrap clientBootstrap = new ClientBootstrap(elGroup, resolver);
                final CredentialsProvider provider = getTestCredentialsProvider()) {

            Exception exceptionResult = null;

            final S3NativeClient nativeClient = new S3NativeClient(REGION, clientBootstrap, provider, 64_000_000l,
                    100.);

            final CancelTestData<GetObjectOutput> testData = new CancelTestData<GetObjectOutput>();
            testData.ExpectedPartCount = 1;

            try {
                testData.ResultFuture = nativeClient.getObject(
                        GetObjectRequest.builder().bucket(BUCKET).key(GET_OBJECT_KEY).build(),
                        new ResponseDataConsumer<GetObjectOutput>() {

                            @Override
                            public void onResponse(GetObjectOutput response) {
                                assertNotNull(response);
                            }

                            @Override
                            public void onResponseData(ByteBuffer bodyBytesIn) {
                                ++testData.PartCount;

                                if (testData.PartCount == testData.ExpectedPartCount) {
                                    testData.ResultFuture.cancel(true);
                                }
                            }

                            @Override
                            public void onFinished() {
                            }

                            @Override
                            public void onException(final CrtRuntimeException e) {
                            }
                        });
                testData.ResultFuture.join();
            } catch (Exception e) {
                exceptionResult = e;
            } finally {
                Assume.assumeTrue(exceptionResult != null);
                Assume.assumeTrue(exceptionResult instanceof CancellationException);
                Assume.assumeTrue(testData.ExpectedPartCount == testData.ExpectedPartCount);
            }
        }
    }

    @Test
    public void testPutObjectCancel() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);

        try (final EventLoopGroup elGroup = new EventLoopGroup(9);
                final HostResolver resolver = new HostResolver(elGroup, 128);
                final ClientBootstrap clientBootstrap = new ClientBootstrap(elGroup, resolver);
                final CredentialsProvider provider = getTestCredentialsProvider()) {

            Exception exceptionResult = null;

            final S3NativeClient nativeClient = new S3NativeClient(REGION, clientBootstrap, provider, 64_000_000l,
                    100.);
            final long contentLength = 1 * 1024l * 1024l * 1024l;
            final long lengthWritten[] = { 0 };

            final CancelTestData<PutObjectOutput> testData = new CancelTestData<PutObjectOutput>();
            testData.ExpectedPartCount = 2;

            try {
                testData.ResultFuture = nativeClient.putObject(PutObjectRequest.builder()
                        .bucket(BUCKET).key(PUT_OBJECT_KEY).contentLength(contentLength).build(), buffer -> {
                            while (buffer.hasRemaining()) {
                                buffer.put((byte) 42);
                                ++lengthWritten[0];
                            }

                            ++testData.PartCount;

                            if (testData.PartCount == testData.ExpectedPartCount) {
                                testData.ResultFuture.cancel(true);
                            }

                            return lengthWritten[0] == contentLength;
                        });

                testData.ResultFuture.join();
            } catch (Exception e) {
                exceptionResult = e;
            } finally {
                Assume.assumeTrue(exceptionResult != null);
                Assume.assumeTrue(exceptionResult instanceof CancellationException);
                Assume.assumeTrue(testData.ExpectedPartCount == testData.ExpectedPartCount);
            }
        }
    }


    @Test
    public void testRetryOptions() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);

        try (final EventLoopGroup elGroup = new EventLoopGroup(9);
                final HostResolver resolver = new HostResolver(elGroup, 128);
                final ClientBootstrap clientBootstrap = new ClientBootstrap(elGroup, resolver);
                final CredentialsProvider provider = getTestCredentialsProvider()) {

            StandardRetryOptions standardRetryOptions = new StandardRetryOptions.StandardRetryOptionsBuilder().build();

            final S3NativeClient nativeClient = new S3NativeClient(REGION, clientBootstrap, provider, 64_000_000l,
                    100., 0, standardRetryOptions);

            Assume.assumeTrue(nativeClient != null);
        }
    }

    @Test
    public void testGetObjectWrongRegion() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);

        try (final EventLoopGroup elGroup = new EventLoopGroup(9);
                final HostResolver resolver = new HostResolver(elGroup, 128);
                final ClientBootstrap clientBootstrap = new ClientBootstrap(elGroup, resolver);
                final CredentialsProvider provider = getTestCredentialsProvider()) {

            final S3NativeClient nativeClient = new S3NativeClient(ALT_REGION, clientBootstrap, provider, 64_000_000l,
                    100.);

            try {
                nativeClient.getObject(GetObjectRequest.builder().bucket(BUCKET).key(GET_OBJECT_KEY).build(),
                        new ResponseDataConsumer<GetObjectOutput>() {

                            @Override
                            public void onResponse(GetObjectOutput response) {
                                assertNotNull(response);
                            }

                            @Override
                            public void onResponseData(ByteBuffer bodyBytesIn) {
                            }

                            @Override
                            public void onFinished() {
                            }

                            @Override
                            public void onException(final CrtRuntimeException e) {
                            }
                        }).join();
            } catch(Exception e) {
                System.out.println(e.toString());
            }
        }
    }

    private void validateCustomHeaders(List<HttpHeader> generatedHeaders, HttpHeader[] customHeaders) {
        if (customHeaders == null || customHeaders.length == 0) {
            return;
        }

        for (HttpHeader customHeader : customHeaders) {
            int numTimesFound = 0;

            for (HttpHeader generatedHeader : generatedHeaders) {
                if (generatedHeader.getName().equals(customHeader.getName())) {
                    assertEquals(generatedHeader.getValue(), customHeader.getValue());
                    ++numTimesFound;
                }
            }

            assertEquals(1, numTimesFound);
        }
    }

    /*
     * Interface for an anonymous function to generate a specific type of request
     * (ie: PutObject, GetObject, etc.) with the given headers.
     */
    interface CustomHeadersTestLambda {
        void run(final S3NativeClient s3NativeClient, HttpHeader[] customHeaders);
    }

    /*
     * Runs the given test lambda for custom headers, passing it the appropriate
     * arguments, then validating the output.
     */
    private void customHeadersTestCase(CustomHeadersTestLambda customHeadersLambda, HttpHeader[] customHeaders) {
        final S3Client mockInternalClient = mock(S3Client.class);
        final S3NativeClient nativeClient = new S3NativeClient(REGION, mockInternalClient);

        customHeadersLambda.run(nativeClient, customHeaders);

        ArgumentCaptor<S3MetaRequestOptions> optionsArgument = ArgumentCaptor.forClass(S3MetaRequestOptions.class);
        verify(mockInternalClient).makeMetaRequest(optionsArgument.capture());
        List<S3MetaRequestOptions> options = optionsArgument.getAllValues();

        assertEquals(options.size(), 1);

        S3MetaRequestOptions getObjectOptions = options.get(0);
        validateCustomHeaders(getObjectOptions.getHttpRequest().getHeaders(), customHeaders);
    }

    /*
     * Using the customHeadersTestCase function, executes a series of test cases
     * using the given test lambda.
     */
    private void testCustomHeaders(CustomHeadersTestLambda customHeadersLambda) {
        customHeadersTestCase(customHeadersLambda, null);
        customHeadersTestCase(customHeadersLambda, new HttpHeader[] {});

        HttpHeader[] customHeaders = new HttpHeader[] { new HttpHeader("Host", "test_host"),
                new HttpHeader("CustomHeader", "CustomHeaderValue"), };

        customHeadersTestCase(customHeadersLambda, customHeaders);
    }

    @Test
    public void testGetObjectCustomHeaders() {
        testCustomHeaders((nativeClient, customHeaders) -> nativeClient.getObject(
                GetObjectRequest.builder().bucket(BUCKET).key(GET_OBJECT_KEY).customHeaders(customHeaders).build(),
                null));
    }

    @Test
    public void testPutObjectCustomHeaders() {
        testCustomHeaders((nativeClient, customHeaders) -> nativeClient.putObject(PutObjectRequest.builder()
                .bucket(BUCKET).key(PUT_OBJECT_KEY).contentLength(0L).customHeaders(customHeaders).build(), null));
    }

    /*
     * Interface for an anonymous function to generate a specific type of request
     * (ie: PutObject, GetObject, etc.) with the given values.
     */
    interface CustomQueryParametersTestLambda {
        void run(final S3NativeClient s3NativeClient, String key, String customQueryParameters);
    }

    /*
     * Runs the given test lambda for custom query parameters, passing it the
     * appropriate arguments, then validating the output.
     */
    public void customQueryParametersTestCase(CustomQueryParametersTestLambda customQueryParametersTestLambda,
            String key, String customQueryParameters) {
        final S3Client mockInternalClient = mock(S3Client.class);
        final S3NativeClient nativeClient = new S3NativeClient(REGION, mockInternalClient);

        customQueryParametersTestLambda.run(nativeClient, key, customQueryParameters);

        ArgumentCaptor<S3MetaRequestOptions> optionsArgument = ArgumentCaptor.forClass(S3MetaRequestOptions.class);
        verify(mockInternalClient).makeMetaRequest(optionsArgument.capture());
        List<S3MetaRequestOptions> optionsList = optionsArgument.getAllValues();

        assertEquals(optionsList.size(), 1);
        S3MetaRequestOptions options = optionsList.get(0);

        HttpRequest httpRequest = options.getHttpRequest();

        if (customQueryParameters == null || customQueryParameters.trim().equals("")) {
            assertTrue(httpRequest.getEncodedPath().equals("/" + key));
        } else {
            assertTrue(httpRequest.getEncodedPath().equals("/" + key + "?" + customQueryParameters));
        }
    }

    /*
     * Using the customQueryParametersTestCase function, executes a series of tests
     * using the given test lambda.
     */
    private void testCustomQueryParameters(CustomQueryParametersTestLambda customQueryParametersTestLambda) {
        String key = "test_key";

        customQueryParametersTestCase(customQueryParametersTestLambda, key, null);
        customQueryParametersTestCase(customQueryParametersTestLambda, key, "");

        String param1Name = "param1";
        String param1Value = "value1";
        String param2Name = "param2";
        String param2Value = "value2";

        String customQueryParameters = param1Name + "=" + param1Value + "&" + param2Name + "=" + param2Value;

        customQueryParametersTestCase(customQueryParametersTestLambda, key, customQueryParameters);
    }

    @Test
    public void testGetObjectCustomQueryParameters() {
        testCustomQueryParameters((nativeClient, key, customQueryParameters) -> nativeClient.getObject(
                GetObjectRequest.builder().bucket(BUCKET).key(key).customQueryParameters(customQueryParameters).build(),
                null));
    }

    @Test
    public void testPutObjectCustomQueryParameters() {
        testCustomQueryParameters(
                (nativeClient, key,
                        customQueryParameters) -> nativeClient.putObject(PutObjectRequest.builder().bucket(BUCKET)
                                .key(key).contentLength(0L).customQueryParameters(customQueryParameters).build(),
                                null));
    }
}
