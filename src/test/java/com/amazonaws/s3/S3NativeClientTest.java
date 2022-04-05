/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package com.amazonaws.s3;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentCaptor;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.amazonaws.s3.model.DeleteObjectOutput;
import com.amazonaws.s3.model.DeleteObjectRequest;
import com.amazonaws.s3.model.ListObjectsOutput;
import com.amazonaws.s3.model.ListObjectsRequest;
import com.amazonaws.s3.model.GetObjectOutput;
import com.amazonaws.s3.model.GetObjectRequest;
import com.amazonaws.s3.model.PutObjectOutput;
import com.amazonaws.s3.model.PutObjectRequest;

import com.amazonaws.test.AwsClientTestFixture;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.s3.*;
import software.amazon.awssdk.crt.auth.credentials.CredentialsProvider;
import software.amazon.awssdk.crt.io.*;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpRequest;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.Log.LogSubject;

public class S3NativeClientTest extends AwsClientTestFixture {
    private static final String BUCKET = System.getProperty("crt.test_s3_bucket", "aws-crt-canary-bucket");
    private static final String REGION = System.getProperty("crt.test_s3_region", "us-west-2");
    private static final String GET_OBJECT_KEY = System.getProperty("crt.test_s3_get_object_key",
            "get_object_test_10MB.txt");
    private static final String PUT_OBJECT_KEY = System.getProperty("crt.test_s3_put_object_key", "file.upload");
    private static final String PUT_OBJECT_WITH_METADATA_KEY = System.getProperty("crt.test_s3_put_object_with_metadata_key", "file_with_metadata.upload");

    private static final String GET_OBJECT_SPECIAL_CHARACTERS = System.getProperty("crt.test_s3_special_characters_key",
            "filename _@_=_&_?_+_)_.txt");

    private static final String GET_OBJECT_VERSION = System.getProperty("crt.test_s3_get_object_version",
            "2Z_dpqRBdrGjax8dyIZ3XYnASOVkdY9J");

    private static final int DEFAULT_NUM_THREADS = 3;
    private static final int DEFAULT_MAX_HOST_ENTRIES = 8;

    @BeforeClass
    public static void haveAwsCredentials() {
        Assume.assumeTrue(areAwsCredentialsAvailable());
    }

    @Test
    public void testGetObject() {
        skipIfNetworkUnavailable();

        try (final EventLoopGroup elGroup = new EventLoopGroup(DEFAULT_NUM_THREADS);
                final HostResolver resolver = new HostResolver(elGroup, DEFAULT_MAX_HOST_ENTRIES);
                final ClientBootstrap clientBootstrap = new ClientBootstrap(elGroup, resolver);
                final CredentialsProvider provider = getTestCredentialsProvider();
                final S3NativeClient nativeClient = new S3NativeClient(REGION, clientBootstrap, provider, 64_000_000l,
                        100.)) {

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
    public void testGetObjectSpecialCharacters() {
        skipIfNetworkUnavailable();
        try (final EventLoopGroup elGroup = new EventLoopGroup(DEFAULT_NUM_THREADS);
                final HostResolver resolver = new HostResolver(elGroup, DEFAULT_MAX_HOST_ENTRIES);
                final ClientBootstrap clientBootstrap = new ClientBootstrap(elGroup, resolver);
                final CredentialsProvider provider = getTestCredentialsProvider();
                final S3NativeClient nativeClient = new S3NativeClient(REGION, clientBootstrap, provider, 64_000_000l,
                        100.)) {

            final long length[] = { 0 };
            nativeClient.getObject(GetObjectRequest.builder().bucket(BUCKET).key(GET_OBJECT_SPECIAL_CHARACTERS).build(),
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
    public void testGetObjectVersioned() {
        skipIfNetworkUnavailable();

        try (final EventLoopGroup elGroup = new EventLoopGroup(DEFAULT_NUM_THREADS);
                final HostResolver resolver = new HostResolver(elGroup, DEFAULT_MAX_HOST_ENTRIES);
                final ClientBootstrap clientBootstrap = new ClientBootstrap(elGroup, resolver);
                final CredentialsProvider provider = getTestCredentialsProvider();
                final S3NativeClient nativeClient = new S3NativeClient(REGION, clientBootstrap, provider, 64_000_000l,
                        100.)) {

            final long length[] = { 0 };
            nativeClient.getObject(
                    GetObjectRequest.builder().bucket(BUCKET).key(GET_OBJECT_KEY).versionId(GET_OBJECT_VERSION).build(),
                    new ResponseDataConsumer<GetObjectOutput>() {

                        @Override
                        public void onResponse(GetObjectOutput response) {
                            assertNotNull(response);
                            assertEquals(response.versionId(), GET_OBJECT_VERSION);
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
    public void testGetObjectExceptionCatch() throws Throwable {
        skipIfNetworkUnavailable();

        try (final EventLoopGroup elGroup = new EventLoopGroup(DEFAULT_NUM_THREADS);
                final HostResolver resolver = new HostResolver(elGroup, DEFAULT_MAX_HOST_ENTRIES);
                final ClientBootstrap clientBootstrap = new ClientBootstrap(elGroup, resolver);
                final CredentialsProvider provider = getTestCredentialsProvider();
                final S3NativeClient nativeClient = new S3NativeClient(REGION, clientBootstrap, provider, 64_000_000l,
                        100.)) {

            nativeClient.getObject(GetObjectRequest.builder().bucket(BUCKET).key("_NON_EXIST_OBJECT_").build(),
                    new ResponseDataConsumer<GetObjectOutput>() {

                        @Override
                        public void onResponse(GetObjectOutput response) {
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
        } catch (CompletionException e) {
            try {
                throw e.getCause();
            } catch (CrtS3RuntimeException causeException) {
                /**
                 * Assert the exceptions are set correctly.
                 */
                assertTrue(causeException.errorName.equals("AWS_ERROR_S3_INVALID_RESPONSE_STATUS"));
                assertTrue(causeException.getAwsErrorCode().equals("NoSuchKey"));
                assertTrue(causeException.getAwsErrorMessage().equals("The specified key does not exist."));
                assertTrue(causeException.getStatusCode() == 404);
            }
        }
    }

    @Test
    public void testPutObject() {
        skipIfNetworkUnavailable();

        try (final EventLoopGroup elGroup = new EventLoopGroup(DEFAULT_NUM_THREADS);
                final HostResolver resolver = new HostResolver(elGroup, DEFAULT_MAX_HOST_ENTRIES);
                final ClientBootstrap clientBootstrap = new ClientBootstrap(elGroup, resolver);
                final CredentialsProvider provider = getTestCredentialsProvider();
                final S3NativeClient nativeClient = new S3NativeClient(REGION, clientBootstrap, provider, 64_000_000l,
                        100.)) {

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

    @Test
    public void testConcurrentRequests() {
        skipIfNetworkUnavailable();

        //Log.initLoggingToStdout(Log.LogLevel.Trace);
        //Log.log(Log.LogLevel.Debug, LogSubject.CommonGeneral, ">>>>>>>>>> START OF Test S3 Concurrent Requests >>>>>>>>>>");

        try (final EventLoopGroup elGroup = new EventLoopGroup(DEFAULT_NUM_THREADS);
                final HostResolver resolver = new HostResolver(elGroup, DEFAULT_MAX_HOST_ENTRIES);
                final ClientBootstrap clientBootstrap = new ClientBootstrap(elGroup, resolver);
                final CredentialsProvider provider = getTestCredentialsProvider();
                final S3NativeClient nativeClient = new S3NativeClient(REGION, clientBootstrap, provider, 64_000_000l,
                        10.)) {

            final long lengthWritten[] = { 0 };
            final long contentLength = 1024l;
            final long length[] = { 0 };
            List<CompletableFuture<?>> futures = new ArrayList<CompletableFuture<?>>();
            final int concurrentNum = 20;
            for (int i = 0; i < concurrentNum; i++) {
                futures.add(
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
                                }));

                // Issue with single byte seems to be here!
                futures.add(nativeClient.putObject(PutObjectRequest.builder().bucket(BUCKET).key(PUT_OBJECT_KEY)
                        .contentLength(contentLength).build(), buffer -> {
                            while (buffer.hasRemaining()) {
                                buffer.put((byte) 65); // A single byte! This is likely where the allocation issue is occuring! - BUMP AGAIN to rerun CI - want to make extra sure it's fixed
                                ++lengthWritten[0];
                            }

                            return lengthWritten[0] == contentLength;
                        }));
            }
            CompletableFuture<?> allFutures = CompletableFuture
                    .allOf(futures.toArray(new CompletableFuture<?>[futures.size()]));
            allFutures.join();
        }

        // // Dump stack trace here
        // CRT.dumpNativeMemory();
        // // TEST - adding a delay to see if GC race is here
        // try
        // {
        //     Thread.sleep(1000);
        // }
        // catch (Exception e)
        // {
        //     Log.log(Log.LogLevel.Debug, LogSubject.CommonGeneral, "Exception occured while trying to sleep for a second!");
        // }
    }

    private class CancelTestData<T> {
        public int ExpectedPartCount;
        public int PartCount;
        public CompletableFuture<T> ResultFuture;
        public CompletableFuture<Void> VerifyFinishFuture = new CompletableFuture<Void>();

        public CancelTestData(int ExpectedPartCount) {
            this.ExpectedPartCount = ExpectedPartCount;
        }
    }

    private void FinishCancelTest(CancelTestData<?> testData, CancellationException cancellationException) {
        CrtS3RuntimeException runtimeException = null;

        try {
            testData.VerifyFinishFuture.join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof CrtS3RuntimeException) {
                runtimeException = (CrtS3RuntimeException) e.getCause();
            }
        }

        assertTrue(runtimeException != null);
        assertTrue(runtimeException.errorName.equals("AWS_ERROR_S3_CANCELED"));
        assertTrue(cancellationException != null);
        assertTrue(testData.PartCount == testData.ExpectedPartCount);
    }

    private class CancelResponseDataConsumer implements ResponseDataConsumer<GetObjectOutput> {
        private CancelTestData<GetObjectOutput> cancelTestData;

        public CancelResponseDataConsumer(CancelTestData<GetObjectOutput> cancelTestData) {
            this.cancelTestData = cancelTestData;
        }

        @Override
        public void onResponse(GetObjectOutput response) {
            assertNotNull(response);
        }

        @Override
        public void onResponseData(ByteBuffer bodyBytesIn) {
            ++cancelTestData.PartCount;
        }

        @Override
        public void onFinished() {
            cancelTestData.VerifyFinishFuture.complete(null);
        }

        @Override
        public void onException(final CrtRuntimeException e) {
            cancelTestData.VerifyFinishFuture.completeExceptionally(e);
        }
    }

    public void testGetObjectCancelHelper(CancelTestData<GetObjectOutput> testData,
            CancelResponseDataConsumer dataConsumer) {
        skipIfNetworkUnavailable();

        try (final EventLoopGroup elGroup = new EventLoopGroup(DEFAULT_NUM_THREADS);
                final HostResolver resolver = new HostResolver(elGroup, DEFAULT_MAX_HOST_ENTRIES);
                final ClientBootstrap clientBootstrap = new ClientBootstrap(elGroup, resolver);
                final CredentialsProvider provider = getTestCredentialsProvider();
                final S3NativeClient nativeClient = new S3NativeClient(REGION, clientBootstrap, provider, 64_000_000l,
                        100.)) {

            CancellationException cancelException = null;

            try {
                testData.ResultFuture = nativeClient
                        .getObject(GetObjectRequest.builder().bucket(BUCKET).key(GET_OBJECT_KEY).build(), dataConsumer);
                testData.ResultFuture.join();
            } catch (CancellationException e) {
                cancelException = e;
            }

            FinishCancelTest(testData, cancelException);
        }
    }

    @Test
    public void testGetObjectCancelHeaders() {
        final CancelTestData<GetObjectOutput> testData = new CancelTestData<GetObjectOutput>(0);

        testGetObjectCancelHelper(testData, new CancelResponseDataConsumer(testData) {
            @Override
            public void onResponseHeaders(final int statusCode, final HttpHeader[] headers) {
                super.onResponseHeaders(statusCode, headers);

                testData.ResultFuture.cancel(true);
            }
        });
    }

    @Test
    public void testGetObjectCancelDuringParts() {
        final CancelTestData<GetObjectOutput> testData = new CancelTestData<GetObjectOutput>(1);

        testGetObjectCancelHelper(testData, new CancelResponseDataConsumer(testData) {
            @Override
            public void onResponseData(ByteBuffer bodyBytesIn) {
                super.onResponseData(bodyBytesIn);

                if (testData.PartCount == testData.ExpectedPartCount) {
                    testData.ResultFuture.cancel(true);
                }
            }
        });
    }

    private class CancelRequestDataSupplier implements RequestDataSupplier {

        private CancelTestData<PutObjectOutput> cancelTestData;
        private long lengthWritten;
        private long partSize;
        private int numParts;

        public CancelRequestDataSupplier(int numParts, CancelTestData<PutObjectOutput> cancelTestData) {
            this.numParts = numParts;
            this.cancelTestData = cancelTestData;
        }

        public void setPartSize(long partSize) {
            this.partSize = partSize;
        }

        public long contentLength() {
            return this.partSize * this.numParts;
        }

        @Override
        public boolean getRequestBytes(ByteBuffer buffer) {
            while (buffer.hasRemaining()) {
                buffer.put((byte) 42);
                ++this.lengthWritten;
            }

            ++cancelTestData.PartCount;

            return this.lengthWritten == this.contentLength();
        }

        @Override
        public void onFinished() {
            cancelTestData.VerifyFinishFuture.complete(null);
        }

        @Override
        public void onException(final CrtRuntimeException e) {
            cancelTestData.VerifyFinishFuture.completeExceptionally(e);
        }
    }

    public void testPutObjectCancelHelper(CancelTestData<PutObjectOutput> testData,
            CancelRequestDataSupplier dataSupplier) {
        skipIfNetworkUnavailable();
        final long partSize5MB = 5l * 1024l * 1024l;

        try (final EventLoopGroup elGroup = new EventLoopGroup(DEFAULT_NUM_THREADS);
                final HostResolver resolver = new HostResolver(elGroup, DEFAULT_MAX_HOST_ENTRIES);
                final ClientBootstrap clientBootstrap = new ClientBootstrap(elGroup, resolver);
                final CredentialsProvider provider = getTestCredentialsProvider();

                final S3NativeClient nativeClient = new S3NativeClient(REGION, clientBootstrap, provider, partSize5MB,
                        100.)) {

            CancellationException cancelException = null;

            dataSupplier.setPartSize(partSize5MB);

            try {
                testData.ResultFuture = nativeClient.putObject(PutObjectRequest.builder().bucket(BUCKET)
                        .key(PUT_OBJECT_KEY).contentLength(dataSupplier.contentLength()).build(), dataSupplier);

                testData.ResultFuture.join();
            } catch (CancellationException e) {
                cancelException = e;
            }

            FinishCancelTest(testData, cancelException);
        }
    }

    @Test
    public void testPutObjectCancelParts() {
        final CancelTestData<PutObjectOutput> testData = new CancelTestData<PutObjectOutput>(2);

        testPutObjectCancelHelper(testData, new CancelRequestDataSupplier(10, testData) {
            @Override
            public boolean getRequestBytes(ByteBuffer buffer) {
                boolean result = super.getRequestBytes(buffer);

                if (testData.PartCount == testData.ExpectedPartCount) {
                    testData.ResultFuture.cancel(true);
                }

                return result;
            }
        });
    }

    @Test
    public void testPutObjectCancelHeaders() {
        final CancelTestData<PutObjectOutput> testData = new CancelTestData<PutObjectOutput>(2);

        testPutObjectCancelHelper(testData, new CancelRequestDataSupplier(2, testData) {
            @Override
            public void onResponseHeaders(final int statusCode, final HttpHeader[] headers) {
                super.onResponseHeaders(statusCode, headers);

                testData.ResultFuture.cancel(true);
            }
        });
    }

    @Test
    public void testRetryOptions() {
        skipIfNetworkUnavailable();

        try (final EventLoopGroup elGroup = new EventLoopGroup(DEFAULT_NUM_THREADS);
                final HostResolver resolver = new HostResolver(elGroup, DEFAULT_MAX_HOST_ENTRIES);
                final ClientBootstrap clientBootstrap = new ClientBootstrap(elGroup, resolver);
                final CredentialsProvider provider = getTestCredentialsProvider()) {

            final StandardRetryOptions standardRetryOptions = new StandardRetryOptions()
                    .withBackoffRetryOptions(new ExponentialBackoffRetryOptions().withEventLoopGroup(elGroup));

            try (final S3Client s3Client = new S3Client(new S3ClientOptions().withClientBootstrap(clientBootstrap)
                    .withCredentialsProvider(provider).withRegion(REGION).withPartSize(64_000_000l)
                    .withThroughputTargetGbps(100.).withStandardRetryOptions(standardRetryOptions));
                    final S3NativeClient nativeClient = new S3NativeClient(REGION, s3Client)) {

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
        final S3MetaRequest request = new S3MetaRequest();
        when(mockInternalClient.makeMetaRequest(any(S3MetaRequestOptions.class))).thenReturn(request);

        final S3NativeClient nativeClient = new S3NativeClient(REGION, mockInternalClient);

        customHeadersLambda.run(nativeClient, customHeaders);

        ArgumentCaptor<S3MetaRequestOptions> optionsArgument = ArgumentCaptor.forClass(S3MetaRequestOptions.class);
        verify(mockInternalClient).makeMetaRequest(optionsArgument.capture());
        List<S3MetaRequestOptions> options = optionsArgument.getAllValues();

        assertEquals(options.size(), 1);

        S3MetaRequestOptions getObjectOptions = options.get(0);
        validateCustomHeaders(getObjectOptions.getHttpRequest().getHeaders(), customHeaders);
        reset(mockInternalClient);
        request.close();
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
        final S3MetaRequest request = new S3MetaRequest();
        when(mockInternalClient.makeMetaRequest(any(S3MetaRequestOptions.class))).thenReturn(request);

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
        reset(mockInternalClient);
        request.close();
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

    @Test
    public void testPutObjectWithUserDefinedMetadata() throws Exception {
        skipIfNetworkUnavailable();

        try (final EventLoopGroup elGroup = new EventLoopGroup(DEFAULT_NUM_THREADS);
                final HostResolver resolver = new HostResolver(elGroup, DEFAULT_MAX_HOST_ENTRIES);
                final ClientBootstrap clientBootstrap = new ClientBootstrap(elGroup, resolver);
                final CredentialsProvider provider = getTestCredentialsProvider();
                final S3NativeClient nativeClient = new S3NativeClient(REGION, clientBootstrap, provider, 64_000_000l,
                        100.)) {

            final long contentLength = 1024l;
            final long lengthWritten[] = { 0 };
            final String userMetadataKey = "CustomKey1";
            final String userMetadataValue = "SampleValue";

            final Map<String, String> userMetadata = new HashMap<String, String>();
            userMetadata.put(userMetadataKey, userMetadataValue);

            // put with metadata
            nativeClient.putObject(
                    PutObjectRequest.builder()
                            .bucket(BUCKET)
                            .key(PUT_OBJECT_WITH_METADATA_KEY)
                            .contentLength(contentLength)
                            .metadata(userMetadata)
                            .build(),
                    buffer -> {
                        while (buffer.hasRemaining()) {
                            buffer.put((byte) 42);
                            ++lengthWritten[0];
                        }

                        return lengthWritten[0] == contentLength;
                    }).join();

            // wait propagation
            waitForPropagation(nativeClient, BUCKET, PUT_OBJECT_WITH_METADATA_KEY, userMetadataKey);

            // get
            final long length[] = { 0 };
            final GetObjectOutput getObjectOutput = nativeClient.getObject(GetObjectRequest.builder()
                            .bucket(BUCKET)
                            .key(PUT_OBJECT_WITH_METADATA_KEY)
                            .build(),
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
                    }).get();

            assertNotNull(getObjectOutput);
            final Map<String, String> getMetadata = getObjectOutput.metadata();
            assertNotNull(getMetadata);
            // Amazon S3 stores user-defined metadata keys in lowercase.
            // reference: https://docs.aws.amazon.com/AmazonS3/latest/userguide/UsingMetadata.html
            assertEquals(userMetadataValue, getMetadata.get(userMetadataKey.toLowerCase()));
        }
    }

    private void waitForPropagation(final S3NativeClient nativeClient,
                                    final String bucket,
                                    final String key,
                                    final String userMetadataKey) throws Exception {
        final int maxRetries = 5;
        final Duration intervalBetweenRetries = Duration.ofSeconds(5);

        for (int i = 0; i < maxRetries; i++) {
            final GetObjectOutput getObjectOutput = nativeClient.getObject(GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build(),
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
                    }).get();

            assertNotNull(getObjectOutput);
            final Map<String, String> getMetadata = getObjectOutput.metadata();

            if (getMetadata != null && getMetadata.get(userMetadataKey.toLowerCase()) != null) {
                return;
            }
            Thread.sleep(intervalBetweenRetries.toMillis());
        }

        assertTrue("Propagation timed out for bucket " + bucket + ", key " + key, false);
    }
}
