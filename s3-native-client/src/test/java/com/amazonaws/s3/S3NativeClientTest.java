package com.amazonaws.s3;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

import java.nio.ByteBuffer;
import java.util.List;

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
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.s3.*;

public class S3NativeClientTest extends AwsClientTestFixture {
    private static final String BUCKET = System.getProperty("crt.test_s3_bucket", "<bucket>>");
    private static final String REGION = System.getProperty("crt.test_s3_region", "us-east-1");
    private static final String GET_OBJECT_KEY = System.getProperty("crt.test_s3_get_object_key", "file.download");
    private static final String PUT_OBJECT_KEY = System.getProperty("crt.test_s3_put_object_key", "file.upload");

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
                        buffer.flip();
                        return lengthWritten[0] == contentLength;
                    }).join();

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

    interface CustomHeadersTestLambda {
        void run(final S3NativeClient s3NativeClient, HttpHeader[] customHeaders);
    }

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

    interface CustomQueryParametersTestLambda {
        void run(final S3NativeClient s3NativeClient, String customQueryParameters);
    }

    public void customQueryParametersTestCase(CustomQueryParametersTestLambda customQueryParametersTestLambda,
            String customQueryParameters) {
        final S3Client mockInternalClient = mock(S3Client.class);
        final S3NativeClient nativeClient = new S3NativeClient(REGION, mockInternalClient);

        customQueryParametersTestLambda.run(nativeClient, customQueryParameters);

        ArgumentCaptor<S3MetaRequestOptions> optionsArgument = ArgumentCaptor.forClass(S3MetaRequestOptions.class);
        verify(mockInternalClient).makeMetaRequest(optionsArgument.capture());
        List<S3MetaRequestOptions> optionsList = optionsArgument.getAllValues();

        assertEquals(optionsList.size(), 1);
        S3MetaRequestOptions options = optionsList.get(0);

        HttpRequest httpRequest = options.getHttpRequest();

        if (customQueryParameters == null || customQueryParameters.trim().equals("")) {
            assertFalse(httpRequest.getEncodedPath().endsWith("?"));
            assertFalse(httpRequest.getEncodedPath().endsWith("&"));
            assertFalse(httpRequest.getEncodedPath().endsWith(" "));
        } else {
            String encodedPath = httpRequest.getEncodedPath();
            assertTrue(encodedPath.endsWith("?" + customQueryParameters));

            int numInstances = 0;
            int currentPosition = 0;

            while (currentPosition != -1) {
                int newPosition = encodedPath.indexOf(customQueryParameters, currentPosition);

                if (newPosition == -1) {
                    currentPosition = -1;
                } else {
                    ++numInstances;
                    currentPosition = newPosition + customQueryParameters.length();
                }
            }

            assertEquals(1, numInstances);
        }
    }

    private void testCustomQueryParameters(CustomQueryParametersTestLambda customQueryParametersTestLambda) {
        customQueryParametersTestCase(customQueryParametersTestLambda, null);
        customQueryParametersTestCase(customQueryParametersTestLambda, "");
        customQueryParametersTestCase(customQueryParametersTestLambda, "  ");

        String customQueryParameters = "param1=value1&param2=value2";
        customQueryParametersTestCase(customQueryParametersTestLambda, customQueryParameters);
    }

    @Test
    public void testGetObjectCustomQueryParameters() {
        testCustomQueryParameters((nativeClient, customQueryParameters) -> nativeClient.getObject(GetObjectRequest
                .builder().bucket(BUCKET).key(GET_OBJECT_KEY).customQueryParameters(customQueryParameters).build(),
                null));
    }

    @Test
    public void testPutObjectCustomQueryParameters() {
        testCustomQueryParameters((nativeClient,
                customQueryParameters) -> nativeClient.putObject(PutObjectRequest.builder().bucket(BUCKET)
                        .key(PUT_OBJECT_KEY).contentLength(0L).customQueryParameters(customQueryParameters).build(),
                        null));
    }
}
