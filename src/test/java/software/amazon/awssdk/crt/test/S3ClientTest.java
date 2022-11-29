package software.amazon.awssdk.crt.test;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.auth.credentials.CredentialsProvider;
import software.amazon.awssdk.crt.auth.credentials.DefaultChainCredentialsProvider;
import software.amazon.awssdk.crt.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.http.HttpRequestBodyStream;
import software.amazon.awssdk.crt.io.*;
import software.amazon.awssdk.crt.s3.*;
import software.amazon.awssdk.crt.s3.S3MetaRequestOptions.MetaRequestType;
import software.amazon.awssdk.crt.s3.ChecksumAlgorithm;
import software.amazon.awssdk.crt.s3.ChecksumConfig.ChecksumLocation;
import software.amazon.awssdk.crt.utils.ByteBufferUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.DoubleStream;

public class S3ClientTest extends CrtTestFixture {

    static final String ENDPOINT = System.getenv("ENDPOINT") == null ?
            "aws-crt-test-stuff-us-west-2.s3.us-west-2.amazonaws.com" : System.getenv("ENDPOINT");
    static final String REGION = System.getenv("REGION") == null ? "us-west-2" : System.getenv("REGION");

    static final String COPY_SOURCE_BUCKET = "aws-crt-test-stuff-us-west-2";
    static final String COPY_SOURCE_KEY = "crt-canary-obj.txt";
    static final String X_AMZ_COPY_SOURCE_HEADER = "x-amz-copy-source";

    public S3ClientTest() {
    }

    private S3Client createS3Client(S3ClientOptions options, int numThreads) {
        return createS3Client(options, numThreads, 0);
    }

    private S3Client createS3Client(S3ClientOptions options, int numThreads, int cpuGroup) {
        try (EventLoopGroup elg = new EventLoopGroup(numThreads, cpuGroup)) {
            return createS3Client(options, elg);
        }
    }

    private S3Client createS3Client(S3ClientOptions options, EventLoopGroup elg) {
        try (HostResolver hostResolver = new HostResolver(elg);
                ClientBootstrap clientBootstrap = new ClientBootstrap(elg, hostResolver)) {
            Assert.assertNotNull(clientBootstrap);

            try (DefaultChainCredentialsProvider credentialsProvider = new DefaultChainCredentialsProvider.DefaultChainCredentialsProviderBuilder()
                    .withClientBootstrap(clientBootstrap).build()) {
                Assert.assertNotNull(credentialsProvider);
                options.withClientBootstrap(clientBootstrap).withCredentialsProvider(credentialsProvider);
                return new S3Client(options);
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            System.err.println(ex.getMessage());
            return null;
        }
    }

    private S3Client createS3Client(S3ClientOptions options) {
        return createS3Client(options, 1);
    }

    @Test
    public void testS3ClientCreateDestroy() {
        skipIfNetworkUnavailable();

        S3ClientOptions clientOptions = new S3ClientOptions().withEndpoint(ENDPOINT).withRegion(REGION)
                .withComputeContentMd5(true);
        try (S3Client client = createS3Client(clientOptions)) {

        }
    }

    /* Test that a client can be created successfully with retry options. */
    @Test
    public void testS3ClientCreateDestroyRetryOptions() {
        skipIfNetworkUnavailable();

        try (EventLoopGroup elg = new EventLoopGroup(0, 1); EventLoopGroup retry_elg = new EventLoopGroup(0, 1)) {

            final StandardRetryOptions standardRetryOptions = new StandardRetryOptions().withInitialBucketCapacity(123)
                    .withBackoffRetryOptions(new ExponentialBackoffRetryOptions().withEventLoopGroup(retry_elg));

            try (S3Client client = createS3Client(new S3ClientOptions().withEndpoint(ENDPOINT).withRegion(REGION)
                    .withStandardRetryOptions(standardRetryOptions), elg)) {

            }
        }
    }

    /*
     * Test that a client can be created successfully with retry options that do not
     * specify an ELG.
     */
    @Test
    public void testS3ClientCreateDestroyRetryOptionsUnspecifiedELG() {
        skipIfNetworkUnavailable();

        try (EventLoopGroup elg = new EventLoopGroup(0, 1)) {

            final StandardRetryOptions standardRetryOptions = new StandardRetryOptions().withInitialBucketCapacity(123)
                    .withBackoffRetryOptions(new ExponentialBackoffRetryOptions().withMaxRetries(30));

            try (S3Client client = createS3Client(new S3ClientOptions().withEndpoint(ENDPOINT).withRegion(REGION)
                    .withStandardRetryOptions(standardRetryOptions), elg)) {

            }
        }
    }

    @Test
    public void testS3Get() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());

        S3ClientOptions clientOptions = new S3ClientOptions().withEndpoint(ENDPOINT).withRegion(REGION);
        try (S3Client client = createS3Client(clientOptions)) {
            CompletableFuture<Integer> onFinishedFuture = new CompletableFuture<>();
            S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {

                @Override
                public int onResponseBody(ByteBuffer bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
                    byte[] bytes = new byte[bodyBytesIn.remaining()];
                    bodyBytesIn.get(bytes);
                    Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3, "Body Response: " + Arrays.toString(bytes));
                    return 0;
                }

                @Override
                public void onFinished(S3FinishedResponseContext context) {
                    Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3,
                            "Meta request finished with error code " + context.getErrorCode());
                    if (context.getErrorCode() != 0) {
                        onFinishedFuture.completeExceptionally(
                                new CrtS3RuntimeException(context.getErrorCode(), context.getResponseStatus(), context.getErrorPayload()));
                        return;
                    }
                    onFinishedFuture.complete(Integer.valueOf(context.getErrorCode()));
                }
            };

            HttpHeader[] headers = { new HttpHeader("Host", ENDPOINT) };
            HttpRequest httpRequest = new HttpRequest("GET", "/get_object_test_1MB.txt", headers, null);

            S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                    .withMetaRequestType(MetaRequestType.GET_OBJECT).withHttpRequest(httpRequest)
                    .withResponseHandler(responseHandler);

            try (S3MetaRequest metaRequest = client.makeMetaRequest(metaRequestOptions)) {
                Assert.assertEquals(Integer.valueOf(0), onFinishedFuture.get());
            }
        } catch (InterruptedException | ExecutionException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testS3GetWithEndpoint() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());

        S3ClientOptions clientOptions = new S3ClientOptions().withEndpoint(ENDPOINT).withRegion(REGION);
        try (S3Client client = createS3Client(clientOptions)) {
            CompletableFuture<Integer> onFinishedFuture = new CompletableFuture<>();
            S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {

                @Override
                public int onResponseBody(ByteBuffer bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
                    byte[] bytes = new byte[bodyBytesIn.remaining()];
                    bodyBytesIn.get(bytes);
                    Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3, "Body Response: " + Arrays.toString(bytes));
                    return 0;
                }

                @Override
                public void onFinished(S3FinishedResponseContext context) {
                    Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3,
                            "Meta request finished with error code " + context.getErrorCode());
                    if (context.getErrorCode() != 0) {
                        onFinishedFuture.completeExceptionally(
                                new CrtS3RuntimeException(context.getErrorCode(), context.getResponseStatus(), context.getErrorPayload()));
                        return;
                    }
                    onFinishedFuture.complete(Integer.valueOf(context.getErrorCode()));
                }
            };

            HttpHeader[] headers = { new HttpHeader("Host", ENDPOINT) };
            HttpRequest httpRequest = new HttpRequest("GET", "/get_object_test_1MB.txt", headers, null);

            S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                    .withMetaRequestType(MetaRequestType.GET_OBJECT).withHttpRequest(httpRequest)
                    .withResponseHandler(responseHandler)
                    .withEndpoint(URI.create("https://" + ENDPOINT + ":443"));

            try (S3MetaRequest metaRequest = client.makeMetaRequest(metaRequestOptions)) {
                Assert.assertEquals(Integer.valueOf(0), onFinishedFuture.get());
            }
        } catch (Exception ex /*InterruptedException | ExecutionException ex*/) {
            Assert.fail(ex.getMessage());
        }
    }

    /**
     * Test read-backpressure by repeatedly:
     * - letting the download stall
     * - incrementing the read window
     * - repeat...
     */
    @Test
    public void testS3GetWithBackpressure() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());

        final String filePath = "/get_object_test_1MB.txt";
        final long fileSize = 1 * 1024 * 1024;
        S3ClientOptions clientOptions = new S3ClientOptions()
                .withEndpoint(ENDPOINT)
                .withRegion(REGION)
                .withReadBackpressureEnabled(true)
                .withInitialReadWindowSize(1024)
                .withPartSize(fileSize / 4);
        final long windowIncrementSize = clientOptions.getPartSize() / 2;

        // how long to wait after download stalls, before incrementing read window again
        final Duration ifNothingHappensAfterThisLongItStalled = Duration.ofSeconds(1);
        int stallCount = 0;
        Clock clock = Clock.systemUTC();
        Instant lastTimeSomethingHappened = clock.instant();
        AtomicLong downloadSizeDelta = new AtomicLong(0);

        try (S3Client client = createS3Client(clientOptions)) {
            CompletableFuture<Integer> onFinishedFuture = new CompletableFuture<>();
            S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {

                @Override
                public int onResponseBody(ByteBuffer bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
                    int numBytes = bodyBytesIn.remaining();
                    downloadSizeDelta.addAndGet(numBytes);
                    Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3, "Body Response numBytes:" + numBytes);
                    return 0;
                }

                @Override
                public void onFinished(S3FinishedResponseContext context) {
                    Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3,
                            "Meta request finished with error code " + context.getErrorCode());
                    onFinishedFuture.complete(Integer.valueOf(context.getErrorCode()));
                }
            };

            HttpHeader[] headers = { new HttpHeader("Host", ENDPOINT) };
            HttpRequest httpRequest = new HttpRequest("GET", filePath, headers, null);

            S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                    .withMetaRequestType(MetaRequestType.GET_OBJECT)
                    .withHttpRequest(httpRequest)
                    .withResponseHandler(responseHandler);

            try (S3MetaRequest metaRequest = client.makeMetaRequest(metaRequestOptions)) {

                while (!onFinishedFuture.isDone()) {
                    // Check if we've received data since last loop
                    long lastDownloadSizeDelta = downloadSizeDelta.getAndSet(0);

                    // Figure out how long it's been since we last received data
                    Instant currentTime = clock.instant();
                    if (lastDownloadSizeDelta != 0) {
                        lastTimeSomethingHappened = clock.instant();
                    }

                    Duration timeSinceSomethingHappened = Duration.between(lastTimeSomethingHappened, currentTime);

                    // If it seems like data has stopped flowing, then we know a stall happened due to backpressure.
                    if (timeSinceSomethingHappened.compareTo(ifNothingHappensAfterThisLongItStalled) >= 0) {
                        stallCount += 1;
                        lastTimeSomethingHappened = currentTime;

                        // Increment window so that download continues...
                        metaRequest.incrementReadWindow(windowIncrementSize);
                    }

                    // Sleep a moment and loop again...
                    Thread.sleep(100);
                }

                // Assert that download stalled due to backpressure at some point
                Assert.assertTrue(stallCount > 0);

                Assert.assertEquals(Integer.valueOf(0), onFinishedFuture.get());
            }
        } catch (InterruptedException | ExecutionException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    // Test that we can increment the flow-control window by returning a number from
    // the onResponseBody callback
    @Test
    public void testS3GetWithBackpressureIncrementViaOnResponseBody() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());

        final String filePath = "/get_object_test_1MB.txt";
        final long fileSize = 1 * 1024 * 1024;
        S3ClientOptions clientOptions = new S3ClientOptions()
                .withEndpoint(ENDPOINT)
                .withRegion(REGION)
                .withReadBackpressureEnabled(true)
                .withInitialReadWindowSize(1024)
                .withPartSize(fileSize / 4);

        try (S3Client client = createS3Client(clientOptions)) {
            CompletableFuture<Integer> onFinishedFuture = new CompletableFuture<>();
            S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {

                @Override
                public int onResponseBody(ByteBuffer bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
                    int numBytes = bodyBytesIn.remaining();
                    Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3, "Body Response numBytes:" + numBytes);
                    return numBytes;
                }

                @Override
                public void onFinished(S3FinishedResponseContext context) {
                    Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3,
                            "Meta request finished with error code " + context.getErrorCode());
                    onFinishedFuture.complete(Integer.valueOf(context.getErrorCode()));
                }
            };

            HttpHeader[] headers = { new HttpHeader("Host", ENDPOINT) };
            HttpRequest httpRequest = new HttpRequest("GET", filePath, headers, null);

            S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                    .withMetaRequestType(MetaRequestType.GET_OBJECT)
                    .withHttpRequest(httpRequest)
                    .withResponseHandler(responseHandler);

            try (S3MetaRequest metaRequest = client.makeMetaRequest(metaRequestOptions)) {
                Assert.assertEquals(Integer.valueOf(0), onFinishedFuture.get(60, TimeUnit.SECONDS));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testS3OverrideRequestCredentials() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());

        S3ClientOptions clientOptions = new S3ClientOptions().withEndpoint(ENDPOINT).withRegion(REGION);
        boolean expectedException = false;
        byte[] madeUpCredentials = "I am a madeup credentials".getBytes();
        StaticCredentialsProvider.StaticCredentialsProviderBuilder builder = new StaticCredentialsProvider.StaticCredentialsProviderBuilder()
                .withAccessKeyId(madeUpCredentials).withSecretAccessKey(madeUpCredentials);
        try (S3Client client = createS3Client(clientOptions);
                CredentialsProvider emptyCredentialsProvider = builder.build()) {
            CompletableFuture<Integer> onFinishedFuture = new CompletableFuture<>();
            S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {
                @Override
                public void onFinished(S3FinishedResponseContext context) {
                    Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3,
                            "Meta request finished with error code " + context.getErrorCode());
                    if (context.getErrorCode() != 0) {
                        onFinishedFuture.completeExceptionally(
                                new CrtS3RuntimeException(context.getErrorCode(), context.getResponseStatus(), context.getErrorPayload()));
                        return;
                    }
                    onFinishedFuture.complete(Integer.valueOf(context.getErrorCode()));
                }
            };

            HttpHeader[] headers = { new HttpHeader("Host", ENDPOINT) };
            HttpRequest httpRequest = new HttpRequest("GET", "/get_object_test_1MB.txt", headers, null);
            S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                    .withMetaRequestType(MetaRequestType.GET_OBJECT).withHttpRequest(httpRequest)
                    .withResponseHandler(responseHandler).withCredentialsProvider(emptyCredentialsProvider);

            try (S3MetaRequest metaRequest = client.makeMetaRequest(metaRequestOptions)) {
                Assert.assertEquals(Integer.valueOf(0), onFinishedFuture.get());
            }
        } catch (InterruptedException | ExecutionException ex) {
            expectedException = true;
            /*
             * Maybe better to have a cause of the max retries exceed to be more informative
             */
            if (!(ex.getCause() instanceof CrtS3RuntimeException)) {
                Assert.fail(ex.getMessage());
            }
        }
        Assert.assertTrue(expectedException);
    }

    private byte[] createTestPayload(int size) {
        String msg = "This is an S3 Java CRT Client Test";
        ByteBuffer payload = ByteBuffer.allocate(size);
        while (true) {
            try {
                payload.put(msg.getBytes());
            } catch (BufferOverflowException ex1) {
                while (true) {
                    try {
                        payload.put("#".getBytes());
                    } catch (BufferOverflowException ex2) {
                        break;
                    }
                }
                break;
            }
        }
        return payload.array();
    }

    @Test
    public void testS3Put() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());

        S3ClientOptions clientOptions = new S3ClientOptions().withEndpoint(ENDPOINT).withRegion(REGION);
        try (S3Client client = createS3Client(clientOptions)) {
            CompletableFuture<Integer> onFinishedFuture = new CompletableFuture<>();
            S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {

                @Override
                public int onResponseBody(ByteBuffer bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
                    Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3, "Body Response: " + bodyBytesIn.toString());
                    return 0;
                }

                @Override
                public void onFinished(S3FinishedResponseContext context) {
                    Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3,
                            "Meta request finished with error code " + context.getErrorCode());
                    if (context.getErrorCode() != 0) {
                        onFinishedFuture.completeExceptionally(
                                new CrtS3RuntimeException(context.getErrorCode(), context.getResponseStatus(), context.getErrorPayload()));
                        return;
                    }
                    onFinishedFuture.complete(Integer.valueOf(context.getErrorCode()));
                }
            };

            final ByteBuffer payload = ByteBuffer.wrap(createTestPayload(1024 * 1024));
            HttpRequestBodyStream payloadStream = new HttpRequestBodyStream() {
                @Override
                public boolean sendRequestBody(ByteBuffer outBuffer) {
                    ByteBufferUtils.transferData(payload, outBuffer);
                    return payload.remaining() == 0;
                }

                @Override
                public boolean resetPosition() {
                    return true;
                }

                @Override
                public long getLength() {
                    return payload.capacity();
                }
            };

            HttpHeader[] headers = { new HttpHeader("Host", ENDPOINT),
                    new HttpHeader("Content-Length", Integer.valueOf(payload.capacity()).toString()), };
            HttpRequest httpRequest = new HttpRequest("PUT", "/put_object_test_1MB.txt", headers, payloadStream);

            S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                    .withMetaRequestType(MetaRequestType.PUT_OBJECT).withHttpRequest(httpRequest)
                    .withResponseHandler(responseHandler);

            try (S3MetaRequest metaRequest = client.makeMetaRequest(metaRequestOptions)) {
                Assert.assertEquals(Integer.valueOf(0), onFinishedFuture.get());
            }
        } catch (InterruptedException | ExecutionException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    private S3MetaRequestResponseHandler createTestPutPauseResumeHandler(CompletableFuture<Integer> onFinishedFuture,
        CompletableFuture<Void> onProgressFuture) {
        return new S3MetaRequestResponseHandler() {
            @Override
            public int onResponseBody(ByteBuffer bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
                Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3, "Body Response: " + bodyBytesIn.toString());
                return 0;
            }

            @Override
            public void onFinished(S3FinishedResponseContext context) {
                Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3,
                        "Meta request finished with error code " + context.getErrorCode());
                if (context.getErrorCode() != 0) {
                    onFinishedFuture.completeExceptionally(
                            new CrtS3RuntimeException(context.getErrorCode(), context.getResponseStatus(), context.getErrorPayload()));
                    return;
                }
                onFinishedFuture.complete(Integer.valueOf(context.getErrorCode()));
            }

            @Override
            public void onProgress(final S3MetaRequestProgress progress) {
                onProgressFuture.complete(null);
            }
        };
    }

    @Test
    public void testS3PutPauseResume() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());

        S3ClientOptions clientOptions = new S3ClientOptions()
            .withEndpoint(ENDPOINT)
            .withRegion(REGION);
        try (S3Client client = createS3Client(clientOptions)) {
            CompletableFuture<Integer> onFinishedFuture = new CompletableFuture<>();
            CompletableFuture<Void> onProgressFuture = new CompletableFuture<>();
            S3MetaRequestResponseHandler responseHandler = createTestPutPauseResumeHandler(onFinishedFuture, onProgressFuture);

            final ByteBuffer payload = ByteBuffer.wrap(createTestPayload(128 * 1024 * 1024));
            HttpRequestBodyStream payloadStream = new HttpRequestBodyStream() {
                @Override
                public boolean sendRequestBody(ByteBuffer outBuffer) {
                    ByteBufferUtils.transferData(payload, outBuffer);
                    return payload.remaining() == 0;
                }

                @Override
                public boolean resetPosition() {
                    return true;
                }

                @Override
                public long getLength() {
                    return payload.capacity();
                }
            };

            HttpHeader[] headers = { new HttpHeader("Host", ENDPOINT),
                new HttpHeader("Content-Length", Integer.valueOf(payload.capacity()).toString()), };

            HttpRequest httpRequest = new HttpRequest("PUT", "/put_object_test_128MB.txt", headers, payloadStream);

            S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                    .withMetaRequestType(MetaRequestType.PUT_OBJECT)
                    .withHttpRequest(httpRequest)
                    .withResponseHandler(responseHandler);

            String resumeToken;
            try (S3MetaRequest metaRequest = client.makeMetaRequest(metaRequestOptions)) {
                onProgressFuture.get();

                resumeToken = metaRequest.pause();

                Throwable thrown = Assert.assertThrows(Throwable.class,
                    () -> onFinishedFuture.get());

                Assert.assertEquals("AWS_ERROR_S3_PAUSED", ((CrtS3RuntimeException)thrown.getCause()).errorName);
            }

            final ByteBuffer payloadResume = ByteBuffer.wrap(createTestPayload(128 * 1024 * 1024));
            HttpRequestBodyStream payloadStreamResume = new HttpRequestBodyStream() {
                @Override
                public boolean sendRequestBody(ByteBuffer outBuffer) {
                    ByteBufferUtils.transferData(payloadResume, outBuffer);
                    return payloadResume.remaining() == 0;
                }

                @Override
                public boolean resetPosition() {
                    return true;
                }

                @Override
                public long getLength() {
                    return payloadResume.capacity();
                }
            };

            HttpHeader[] headersResume = { new HttpHeader("Host", ENDPOINT),
                new HttpHeader("Content-Length", Integer.valueOf(payloadResume.capacity()).toString()), };

            HttpRequest httpRequestResume = new HttpRequest("PUT",
                "/put_object_test_128MB.txt", headersResume, payloadStreamResume);

            CompletableFuture<Integer> onFinishedFutureResume = new CompletableFuture<>();
            CompletableFuture<Void> onProgressFutureResume = new CompletableFuture<>();
            S3MetaRequestResponseHandler responseHandlerResume = createTestPutPauseResumeHandler(onFinishedFutureResume, onProgressFutureResume);
            S3MetaRequestOptions metaRequestOptionsResume = new S3MetaRequestOptions()
                    .withMetaRequestType(MetaRequestType.PUT_OBJECT)
                    .withHttpRequest(httpRequestResume)
                    .withResponseHandler(responseHandlerResume)
                    .withResumeToken(resumeToken);

            try (S3MetaRequest metaRequest = client.makeMetaRequest(metaRequestOptionsResume)) {
                Integer finish = onFinishedFutureResume.get();
                Assert.assertEquals(Integer.valueOf(0), finish);
            }
        } catch (InterruptedException | ExecutionException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testS3PutTrailerChecksums() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());

        S3ClientOptions clientOptions = new S3ClientOptions().withEndpoint(ENDPOINT).withRegion(REGION);
        try (S3Client client = createS3Client(clientOptions)) {
            CompletableFuture<Integer> onPutFinishedFuture = new CompletableFuture<>();
            S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {

                @Override
                public int onResponseBody(ByteBuffer bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
                    Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3, "Body Response: " + bodyBytesIn.toString());
                    return 0;
                }

                @Override
                public void onFinished(S3FinishedResponseContext context) {
                    Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3,
                            "Meta request finished with error code " + context.getErrorCode());
                    if (context.getErrorCode() != 0) {
                        onPutFinishedFuture.completeExceptionally(
                                new CrtS3RuntimeException(context.getErrorCode(), context.getResponseStatus(), context.getErrorPayload()));
                        return;
                    }
                    onPutFinishedFuture.complete(Integer.valueOf(context.getErrorCode()));
                }
            };

            final ByteBuffer payload = ByteBuffer.wrap(createTestPayload(1024 * 1024));

            HttpRequestBodyStream payloadStream = new HttpRequestBodyStream() {
                @Override
                public boolean sendRequestBody(ByteBuffer outBuffer) {
                    ByteBufferUtils.transferData(payload, outBuffer);
                    return payload.remaining() == 0;
                }

                @Override
                public boolean resetPosition() {
                    return true;
                }

                @Override
                public long getLength() {
                    return payload.capacity();
                }
            };

            HttpHeader[] headers = { new HttpHeader("Host", ENDPOINT),
                    new HttpHeader("Content-Length", Integer.valueOf(payload.capacity()).toString()), };

            HttpRequest httpRequest = new HttpRequest("PUT", "/java_round_trip_test_fc.txt", headers, payloadStream);
            ChecksumConfig config = new ChecksumConfig().withChecksumAlgorithm(ChecksumAlgorithm.CRC32).withChecksumLocation(ChecksumLocation.TRAILER);
            S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                    .withMetaRequestType(MetaRequestType.PUT_OBJECT).withHttpRequest(httpRequest)
                    .withResponseHandler(responseHandler)
                    .withChecksumConfig(config);

            try (S3MetaRequest metaRequest = client.makeMetaRequest(metaRequestOptions)) {
                Assert.assertEquals(Integer.valueOf(0), onPutFinishedFuture.get());
            }

            // Get request!

            HttpHeader[] getHeaders = { new HttpHeader("Host", ENDPOINT),};

            HttpRequest httpGetRequest = new HttpRequest("GET", "/java_round_trip_test_fc.txt", getHeaders, null);

            CompletableFuture<Integer> onGetFinishedFuture = new CompletableFuture<>();
            S3MetaRequestResponseHandler getResponseHandler = new S3MetaRequestResponseHandler() {

                @Override
                public int onResponseBody(ByteBuffer bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
                    Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3, "Body Response: " + bodyBytesIn.toString());
                    return 0;
                }

                @Override
                public void onFinished(S3FinishedResponseContext context) {
                    Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3,
                            "Meta request finished with error code " + context.getErrorCode());
                    if (context.getErrorCode() != 0) {
                        onGetFinishedFuture.completeExceptionally(
                                new CrtS3RuntimeException(context.getErrorCode(), context.getResponseStatus(), context.getErrorPayload()));
                        return;
                    }
                    if(!context.isChecksumValidated()) {
                        onGetFinishedFuture.completeExceptionally(
                                new RuntimeException("Checksum was not validated"));
                        return;
                    }
                    if(context.getChecksumAlgorithm() != ChecksumAlgorithm.CRC32) {
                        onGetFinishedFuture.completeExceptionally(
                                new RuntimeException("Checksum was not validated via CRC32"));
                        return;
                    }
                    onGetFinishedFuture.complete(Integer.valueOf(context.getErrorCode()));
                }
            };
            ArrayList<ChecksumAlgorithm> algorList = new  ArrayList<ChecksumAlgorithm>();
            algorList.add(ChecksumAlgorithm.CRC32);
            ChecksumConfig validateChecksumConfig = new ChecksumConfig().withValidateChecksum(true).withValidateChecksumAlgorithmList(algorList);
            S3MetaRequestOptions getRequestOptions = new S3MetaRequestOptions()
                    .withMetaRequestType(MetaRequestType.GET_OBJECT).withHttpRequest(httpGetRequest)
                    .withResponseHandler(getResponseHandler)
                    .withChecksumConfig(validateChecksumConfig);

            try (S3MetaRequest metaRequest = client.makeMetaRequest(getRequestOptions)) {
                Assert.assertEquals(Integer.valueOf(0), onGetFinishedFuture.get());
            }
        } catch (InterruptedException | ExecutionException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testS3GetChecksums() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());

        S3ClientOptions clientOptions = new S3ClientOptions().withEndpoint(ENDPOINT).withRegion(REGION);
        try (S3Client client = createS3Client(clientOptions)) {
            CompletableFuture<Integer> onFinishedFuture = new CompletableFuture<>();
            S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {

                @Override
                public void onFinished(S3FinishedResponseContext context) {
                    if (context.getErrorCode() != 0) {
                        onFinishedFuture.completeExceptionally(
                                new CrtS3RuntimeException(context.getErrorCode(), context.getResponseStatus(), context.getErrorPayload()));
                        return;
                    }
                    if(!context.isChecksumValidated()) {
                        onFinishedFuture.completeExceptionally(
                                new RuntimeException("Checksum was not validated"));
                        return;
                    }
                    if(context.getChecksumAlgorithm() != ChecksumAlgorithm.CRC32) {
                        onFinishedFuture.completeExceptionally(
                                new RuntimeException("Checksum was not validated via CRC32"));
                        return;
                    }
                    onFinishedFuture.complete(Integer.valueOf(context.getErrorCode()));
                }

                @Override
                public int onResponseBody(ByteBuffer bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
                    byte[] bytes = new byte[bodyBytesIn.remaining()];
                    bodyBytesIn.get(bytes);
                    return 0;
                }
            };


            HttpHeader[] headers = { new HttpHeader("Host", ENDPOINT) };
            HttpRequest httpRequest = new HttpRequest("GET", "/java_get_test_fc.txt", headers, null);
            ArrayList<ChecksumAlgorithm> algorList = new  ArrayList<ChecksumAlgorithm>();
            algorList.add(ChecksumAlgorithm.CRC32);
            algorList.add(ChecksumAlgorithm.CRC32C);
            algorList.add(ChecksumAlgorithm.SHA1);
            algorList.add(ChecksumAlgorithm.SHA256);
            ChecksumConfig validateChecksumConfig = new ChecksumConfig().withValidateChecksum(true).withValidateChecksumAlgorithmList(algorList);

            S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                    .withMetaRequestType(MetaRequestType.GET_OBJECT).withHttpRequest(httpRequest)
                    .withResponseHandler(responseHandler)
                    .withChecksumConfig(validateChecksumConfig);

            try (S3MetaRequest metaRequest = client.makeMetaRequest(metaRequestOptions)) {
                Assert.assertEquals(Integer.valueOf(0), onFinishedFuture.get());
            }
        } catch (InterruptedException | ExecutionException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testS3Copy() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());

        S3ClientOptions clientOptions = new S3ClientOptions().withEndpoint(ENDPOINT).withRegion(REGION);
        try (S3Client client = createS3Client(clientOptions)) {
            CompletableFuture<Integer> onFinishedFuture = new CompletableFuture<>();
            final AtomicLong totalBytesTransferred = new AtomicLong();
            final AtomicLong contentLength = new AtomicLong();
            final AtomicInteger progressInvocationCount = new AtomicInteger();

            S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {

                @Override
                public int onResponseBody(ByteBuffer bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
                    Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3, "Body Response: " + bodyBytesIn.toString());
                    return 0;
                }

                @Override
                public void onFinished(S3FinishedResponseContext context) {
                    Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3,
                            "Meta request finished with error code " + context.getErrorCode());
                    if (context.getErrorCode() != 0) {
                        System.out.println("Test failed with error payload: " + new String(context.getErrorPayload(), StandardCharsets.UTF_8));
                        onFinishedFuture.completeExceptionally(
                                new CrtS3RuntimeException(context.getErrorCode(), context.getResponseStatus(), context.getErrorPayload()));
                        return;
                    }
                    onFinishedFuture.complete(Integer.valueOf(context.getErrorCode()));
                }

                @Override
                public void onProgress(S3MetaRequestProgress progress) {
                    progressInvocationCount.incrementAndGet();
                    contentLength.set(progress.getContentLength());
                    totalBytesTransferred.addAndGet(progress.getBytesTransferred());
                }
            };

            // x-amz-copy-source-header is composed of {source_bucket}/{source_key}
            final String copySource = COPY_SOURCE_BUCKET + "/" + COPY_SOURCE_KEY;

            HttpHeader[] headers = { new HttpHeader("Host", ENDPOINT),
                    new HttpHeader(X_AMZ_COPY_SOURCE_HEADER, copySource) };

            HttpRequest httpRequest = new HttpRequest("PUT", "/copy_object_test_5GB.txt", headers, null);

            S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                    .withMetaRequestType(MetaRequestType.COPY_OBJECT).withHttpRequest(httpRequest)
                    .withResponseHandler(responseHandler);

            try (S3MetaRequest metaRequest = client.makeMetaRequest(metaRequestOptions)) {
                Assert.assertEquals(Integer.valueOf(0), onFinishedFuture.get());
                Assert.assertTrue(progressInvocationCount.get() > 0);
                Assert.assertTrue(contentLength.get() > 0);
                Assert.assertEquals(contentLength.get(), totalBytesTransferred.get());
            }
        } catch (InterruptedException | ExecutionException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    static class TransferStats {
        static final double GBPS = 1000 * 1000 * 1000;

        AtomicLong bytesRead = new AtomicLong(0);
        AtomicLong bytesSampled = new AtomicLong(0);
        AtomicLong bytesPeak = new AtomicLong(0);
        ConcurrentLinkedQueue<Long> bytesPerSecond = new ConcurrentLinkedQueue();
        Instant startTime = Instant.now();
        Duration sampleDelay = Duration.ZERO;
        AtomicReference<Instant> lastSampleTime = new AtomicReference<Instant>(Instant.now());
        AtomicInteger msToFirstByte = new AtomicInteger(0);

        public TransferStats withSampleDelay(Duration delay) {
            sampleDelay = delay;
            return this;
        }

        public void recordRead(long size) {
            Instant now = Instant.now();
            recordRead(size, now);
            if (this != global) {
                global.recordRead(size, now);
            }
        }

        private void recordRead(long size, Instant now) {
            bytesRead.addAndGet(size);
            int latency = (int) ChronoUnit.MILLIS.between(startTime, now);
            if (msToFirstByte.compareAndSet(0, latency)) {
                if (this != global) {
                    global.recordLatency(latency);
                }
            }
            if (now.minusSeconds(1).isAfter(lastSampleTime.get())) {
                long bytesThisSecond = bytesRead.get() - bytesSampled.get();
                bytesSampled.getAndAdd(bytesThisSecond);
                bytesPeak.getAndUpdate((peak) -> {
                    return (bytesThisSecond > peak) ? bytesThisSecond : peak;
                });

                bytesPerSecond.add(bytesThisSecond);

                lastSampleTime.set(now);
            }
        }

        private void recordLatency(long latencyMs) {
            msToFirstByte.getAndUpdate((ms) -> {
                return (int) Math.ceil((ms + latencyMs) * 0.5);
            });
        }

        public DoubleStream allSamples() {
            return Arrays.stream(bytesPerSecond.toArray(new Long[1])).mapToDouble(a -> a.doubleValue() * 8 / GBPS);
        }

        public DoubleStream samples() {
            return samples(bytesPerSecond.size());
        }

        public DoubleStream samples(int limit) {
            return allSamples().skip(sampleDelay.getSeconds()).limit(limit);
        }

        public double avgGbps() {
            double avg = samples().average().getAsDouble();
            return avg;
        }

        public double p90Gbps() {
            double[] sorted = samples().sorted().toArray();
            int idx = (int) Math.ceil(sorted.length * 0.1);
            return ((sorted[idx] + sorted[idx + 1]) / 2);
        }

        public double stddev() {
            double sumOfSquares = samples().map((a) -> a * a).sum();
            double avg = samples().average().getAsDouble();
            long count = samples().count();
            return (count > 0) ? Math.sqrt((sumOfSquares / count) - (avg * avg)) : 0;
        }

        public double peakGbps() {
            return (bytesPeak.get() * 8) / GBPS;
        }

        public int latency() {
            return msToFirstByte.get();
        }

        public static TransferStats global = new TransferStats();
    }

    @Test
    public void benchmarkS3Get() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());
        Assume.assumeNotNull(System.getProperty("aws.crt.s3.benchmark"));

        // Log.initLoggingToStdout(LogLevel.Trace);

        // Override defaults with values from system properties, via -D on mvn
        // commandline
        final int threadCount = Integer.parseInt(System.getProperty("aws.crt.s3.benchmark.threads", "0"));
        final String region = System.getProperty("aws.crt.s3.benchmark.region", "us-west-2");
        final String bucket = System.getProperty("aws.crt.s3.benchmark.bucket",
                (region == "us-west-2") ? "aws-crt-canary-bucket" : String.format("aws-crt-canary-bucket-%s", region));
        final String endpoint = System.getProperty("aws.crt.s3.benchmark.endpoint",
                String.format("%s.s3.%s.amazonaws.com", bucket, region));
        final String objectName = System.getProperty("aws.crt.s3.benchmark.object",
                "crt-canary-obj-single-part-9223372036854775807");
        final boolean useTls = Boolean.parseBoolean(System.getProperty("aws.crt.s3.benchmark.tls", "false"));
        final double expectedGbps = Double.parseDouble(System.getProperty("aws.crt.s3.benchmark.gbps", "10"));
        final int numTransfers = Integer.parseInt(System.getProperty("aws.crt.s3.benchmark.transfers", "16"));
        final int concurrentTransfers = Integer.parseInt(
                System.getProperty("aws.crt.s3.benchmark.concurrent", "16")); /* should be 1.6 * expectedGbps */
        // avg of .3Gbps per connection, 32 connections per vip, 5 seconds per vip
        // resolution
        final int vipsNeeded = (int) Math.ceil(expectedGbps / 0.5 / 10);
        final int sampleDelay = Integer.parseInt(System.getProperty("aws.crt.s3.benchmark.warmup",
                new Integer((int) Math.ceil(vipsNeeded / 5)).toString()));
        System.out.println(String.format("REGION=%s, WARMUP=%s", region, sampleDelay));

        // Ignore stats during warm up time, they skew results
        TransferStats.global.withSampleDelay(Duration.ofSeconds(sampleDelay));
        try (TlsContext tlsCtx = createTlsContextOptions(getContext().trustStore)) {
            S3ClientOptions clientOptions = new S3ClientOptions().withRegion(region).withEndpoint(endpoint)
                    .withThroughputTargetGbps(expectedGbps).withTlsContext(useTls ? tlsCtx : null);

            try (S3Client client = createS3Client(clientOptions, threadCount)) {
                HttpHeader[] headers = { new HttpHeader("Host", endpoint) };
                HttpRequest httpRequest = new HttpRequest("GET", String.format("/%s", objectName), headers, null);

                List<CompletableFuture<TransferStats>> requestFutures = new LinkedList<>();

                // Each meta request will acquire a slot, and release it when it's done
                Semaphore concurrentSlots = new Semaphore(concurrentTransfers);

                for (int transferIdx = 0; transferIdx < numTransfers; ++transferIdx) {
                    try {
                        concurrentSlots.acquire();
                    } catch (InterruptedException ex) {
                        Assert.fail(ex.toString());
                    }

                    final int myIdx = transferIdx;
                    CompletableFuture<TransferStats> onFinishedFuture = new CompletableFuture<>();
                    requestFutures.add(onFinishedFuture);

                    S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {

                        TransferStats stats = new TransferStats();

                        @Override
                        public int onResponseBody(ByteBuffer bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
                            stats.recordRead(bodyBytesIn.remaining());
                            return 0;
                        }

                        @Override
                        public void onFinished(S3FinishedResponseContext context) {
                            // release the slot first
                            concurrentSlots.release();

                            if (context.getErrorCode() != 0) {
                                onFinishedFuture.completeExceptionally(
                                        new CrtS3RuntimeException(context.getErrorCode(), context.getResponseStatus(), context.getErrorPayload()));
                                return;
                            }

                            synchronized (System.out) {
                                System.out.println(
                                        String.format("Transfer %d:  Avg: %.3f Gbps Peak: %.3f Gbps First Byte: %dms",
                                                myIdx + 1, stats.avgGbps(), stats.peakGbps(), stats.latency()));
                            }

                            onFinishedFuture.complete(stats);
                        }
                    };

                    S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                            .withMetaRequestType(MetaRequestType.GET_OBJECT).withHttpRequest(httpRequest)
                            .withResponseHandler(responseHandler);

                    try (S3MetaRequest metaRequest = client.makeMetaRequest(metaRequestOptions)) {

                    }
                }

                // Finish each future, and deduct failures from completedTransfers
                int completedTransfers = numTransfers;
                double totalGbps = 0;
                for (CompletableFuture<TransferStats> request : requestFutures) {
                    try {
                        request.join();
                        totalGbps += request.get().avgGbps();
                    } catch (CompletionException | InterruptedException | ExecutionException ex) {
                        System.out.println(ex.toString());
                        Throwable cause = ex.getCause();
                        if (cause != ex && cause != null) {
                            System.out.println(cause.toString());
                        }
                        cause = cause.getCause();
                        if (cause != null && cause != ex) {
                            System.out.println(cause.toString());
                        }
                        --completedTransfers;
                    }
                }

                // Dump overall stats
                TransferStats overall = TransferStats.global;
                System.out.println(String.format("%d/%d successful transfers", completedTransfers, numTransfers));
                System.out.println(String.format("Avg: %.3f Gbps", overall.avgGbps()));
                System.out.println(String.format("Peak: %.3f Gbps", overall.peakGbps()));
                System.out.println(String.format("P90: %.3f Gbps (stddev: %.3f)", overall.p90Gbps(), overall.stddev()));
                System.out.println(String.format("Avg Latency: %dms", overall.latency()));
                System.out.flush();

                try {
                    File csvFile = new File("samples.csv");
                    try (PrintWriter writer = new PrintWriter(csvFile)) {
                        writer.println("seconds,gbps");
                        AtomicInteger idx = new AtomicInteger(0);
                        overall.allSamples().mapToObj((gbps) -> {
                            return String.format("%d,%.3f", idx.getAndIncrement(), gbps);
                        }).forEach(writer::println);
                    }
                } catch (FileNotFoundException ex) {
                    System.err.println(ex.getMessage());
                }
            }
        }
    }

    @Test
    public void benchmarkS3Put() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());
        Assume.assumeNotNull(System.getProperty("aws.crt.s3.benchmark"));

        // Override defaults with values from system properties, via -D on mvn
        // commandline
        final int threadCount = Integer.parseInt(System.getProperty("aws.crt.s3.benchmark.threads", "0"));
        final String region = System.getProperty("aws.crt.s3.benchmark.region", "us-west-2");
        final String bucket = System.getProperty("aws.crt.s3.benchmark.bucket",
                (region == "us-west-2") ? "aws-crt-canary-bucket" : String.format("aws-crt-canary-bucket-%s", region));
        final String endpoint = System.getProperty("aws.crt.s3.benchmark.endpoint",
                String.format("%s.s3.%s.amazonaws.com", bucket, region));
        final boolean useTls = Boolean.parseBoolean(System.getProperty("aws.crt.s3.benchmark.tls", "false"));
        final double expectedGbps = Double.parseDouble(System.getProperty("aws.crt.s3.benchmark.gbps", "10"));
        final int numTransfers = Integer.parseInt(System.getProperty("aws.crt.s3.benchmark.transfers", "16"));
        final int concurrentTransfers = Integer.parseInt(
                System.getProperty("aws.crt.s3.benchmark.concurrent", "16")); /* should be 1.6 * expectedGbps */
        // avg of .3Gbps per connection, 32 connections per vip, 5 seconds per vip
        // resolution
        final int vipsNeeded = (int) Math.ceil(expectedGbps / 0.5 / 10);
        final int sampleDelay = Integer.parseInt(System.getProperty("aws.crt.s3.benchmark.warmup",
                new Integer((int) Math.ceil(vipsNeeded / 5)).toString()));
        System.out.println(String.format("REGION=%s, WARMUP=%s", region, sampleDelay));

        // Ignore stats during warm up time, they skew results
        TransferStats.global.withSampleDelay(Duration.ofSeconds(sampleDelay));

        try (TlsContext tlsCtx = createTlsContextOptions(getContext().trustStore)) {
            S3ClientOptions clientOptions = new S3ClientOptions().withRegion(region).withEndpoint(endpoint)
                    .withThroughputTargetGbps(expectedGbps).withTlsContext(useTls ? tlsCtx : null);

            try (S3Client client = createS3Client(clientOptions, threadCount)) {
                List<CompletableFuture<TransferStats>> requestFutures = new LinkedList<>();

                // Each meta request will acquire a slot, and release it when it's done
                Semaphore concurrentSlots = new Semaphore(concurrentTransfers);

                for (int transferIdx = 0; transferIdx < numTransfers; ++transferIdx) {
                    try {
                        concurrentSlots.acquire();
                    } catch (InterruptedException ex) {
                        Assert.fail(ex.toString());
                    }

                    final int myIdx = transferIdx;
                    CompletableFuture<TransferStats> onFinishedFuture = new CompletableFuture<>();
                    requestFutures.add(onFinishedFuture);

                    S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {
                        TransferStats stats = new TransferStats();

                        @Override
                        public void onFinished(S3FinishedResponseContext context) {
                            // release the slot first
                            concurrentSlots.release();

                            if (context.getErrorCode() != 0) {
                                onFinishedFuture.completeExceptionally(
                                        new CrtS3RuntimeException(context.getErrorCode(), context.getResponseStatus(), context.getErrorPayload()));
                                return;
                            }

                            synchronized (System.out) {
                                System.out.println(String.format("Transfer %d finished.", myIdx + 1));
                            }

                            onFinishedFuture.complete(stats);
                        }
                    };

                    final long payloadSize = 5L * 1024L * 1024L * 1024L;
                    final String payloadString = "This is an S3 Test.  This is an S3 Test.  This is an S3 Test.  This is an S3 Test.";

                    HttpRequestBodyStream payloadStream = new HttpRequestBodyStream() {

                        private long remainingBody = payloadSize;

                        @Override
                        public boolean sendRequestBody(ByteBuffer outBuffer) {

                            byte[] payloadBytes = null;

                            try {
                                payloadBytes = payloadString.getBytes("ASCII");
                            } catch (Exception ex) {
                                System.out.println("Encountered error trying to get payload bytes.");
                                return true;
                            }

                            while (remainingBody > 0 && outBuffer.remaining() > 0) {
                                long amtToTransfer = Math.min(remainingBody, (long) outBuffer.remaining());
                                amtToTransfer = Math.min(amtToTransfer, (long) payloadBytes.length);

                                // Transfer the data
                                outBuffer.put(payloadBytes, 0, (int) amtToTransfer);

                                remainingBody -= amtToTransfer;
                            }

                            return remainingBody == 0;
                        }

                        @Override
                        public boolean resetPosition() {
                            return true;
                        }

                        @Override
                        public long getLength() {
                            return payloadSize;
                        }
                    };

                    HttpHeader[] headers = { new HttpHeader("Host", endpoint),
                            new HttpHeader("Content-Length", Long.valueOf(payloadSize).toString()), };
                    HttpRequest httpRequest = new HttpRequest("PUT",
                            String.format("/put_object_test_5GB_%d.txt", myIdx + 1), headers, payloadStream);

                    S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                            .withMetaRequestType(MetaRequestType.PUT_OBJECT).withHttpRequest(httpRequest)
                            .withResponseHandler(responseHandler);

                    try (S3MetaRequest metaRequest = client.makeMetaRequest(metaRequestOptions)) {
                    }

                }

                // Finish each future, and deduct failures from completedTransfers
                int completedTransfers = numTransfers;
                for (CompletableFuture<TransferStats> request : requestFutures) {
                    try {
                        request.join();
                    } catch (CompletionException ex) {
                        System.out.println(ex.toString());
                        Throwable cause = ex.getCause();
                        if (cause != ex && cause != null) {
                            System.out.println(cause.toString());
                        }
                        cause = cause.getCause();
                        if (cause != null && cause != ex) {
                            System.out.println(cause.toString());
                        }
                        --completedTransfers;
                    }
                }
            }
        }
    }
}
