package software.amazon.awssdk.crt.test;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.auth.credentials.Credentials;
import software.amazon.awssdk.crt.auth.credentials.CredentialsProvider;
import software.amazon.awssdk.crt.auth.credentials.DefaultChainCredentialsProvider;
import software.amazon.awssdk.crt.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.crt.auth.signing.AwsSigningConfig;
import software.amazon.awssdk.crt.http.*;
import software.amazon.awssdk.crt.io.*;
import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.Log.LogLevel;
import software.amazon.awssdk.crt.Log.LogSubject;
import software.amazon.awssdk.crt.s3.*;
import software.amazon.awssdk.crt.s3.ChecksumConfig.ChecksumLocation;
import software.amazon.awssdk.crt.s3.S3MetaRequestOptions.MetaRequestType;
import software.amazon.awssdk.crt.utils.ByteBufferUtils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import java.util.function.Predicate;
import java.util.stream.DoubleStream;

public class S3ClientTest extends CrtTestFixture {

    static final String BUCKET_NAME = System.getenv("CRT_S3_TEST_BUCKET_NAME") == null
            ? "aws-c-s3-test-bucket"
            : System.getenv("CRT_S3_TEST_BUCKET_NAME");


    static final String PUBLIC_BUCKET_NAME = String.format("%s-public", BUCKET_NAME);
    static final String REGION = "us-west-2";
    static final String ENDPOINT = String.format("%s.s3.%s.amazonaws.com", BUCKET_NAME, REGION);
    static final String S3EXPRESS_ENDPOINT_USW2_AZ1 = String.format("%s--usw2-az1--x-s3.s3express-usw2-az1.us-west-2.amazonaws.com", BUCKET_NAME);
    static final String S3EXPRESS_ENDPOINT_USE1_AZ4 = String.format("%s--use1-az4--x-s3.s3express-use1-az4.us-east-1.amazonaws.com", BUCKET_NAME);

    static final String PRE_EXIST_1MB_PATH = "/pre-existing-1MB";
    static final String PRE_EXIST_10MB_PATH = "/pre-existing-10MB";
    static final String UPLOAD_DIR = "/upload";

    public S3ClientTest() {
    }

    private S3Client createS3Client(S3ClientOptions options, int numThreads) {

        return createS3Client(options, numThreads, 0);
    }

    private S3Client createS3Client(S3ClientOptions options, int numThreads, int cpuGroup) {
        try (EventLoopGroup elg = new EventLoopGroup(cpuGroup, numThreads)) {
            return createS3Client(options, elg);
        }
    }

    private S3Client createS3Client(S3ClientOptions options, EventLoopGroup elg) {
        try (HostResolver hostResolver = new HostResolver(elg);
                ClientBootstrap clientBootstrap = new ClientBootstrap(elg, hostResolver);) {
            Assert.assertNotNull(clientBootstrap);

            try (DefaultChainCredentialsProvider credentialsProvider = new DefaultChainCredentialsProvider.DefaultChainCredentialsProviderBuilder()
                    .withClientBootstrap(clientBootstrap).build();
                    AwsSigningConfig signingConfig = AwsSigningConfig.getDefaultS3SigningConfig(REGION,
                            credentialsProvider);) {
                Assert.assertNotNull(credentialsProvider);
                options.withClientBootstrap(clientBootstrap).withSigningConfig(signingConfig);
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

    private RuntimeException makeExceptionFromFinishedResponseContext(S3FinishedResponseContext context) {
        return new RuntimeException(String.format("error code:(%d)(%s) response status code(%d), error payload(%s)",
                context.getErrorCode(),
                CRT.awsErrorName(context.getErrorCode()),
                context.getResponseStatus(),
                new String(context.getErrorPayload(), java.nio.charset.StandardCharsets.UTF_8),
                context.getCause()));
    }

    @Test
    public void testS3ClientCreateDestroy() {
        skipIfAndroid();
        skipIfNetworkUnavailable();

        S3ClientOptions clientOptions = new S3ClientOptions().withRegion(REGION)
                .withComputeContentMd5(true)
                .withMemoryLimitInBytes(5L * 1024 * 1024 * 1024);
        try (S3Client client = createS3Client(clientOptions)) {

        }
    }

    @Test
    public void testS3ClientCreateDestroyWithTLS() {
        skipIfAndroid();
        skipIfNetworkUnavailable();

        try (TlsContextOptions tlsContextOptions = TlsContextOptions.createDefaultClient();
                TlsContext tlsContext = new TlsContext(tlsContextOptions);) {
            S3ClientOptions clientOptions = new S3ClientOptions()
                    .withRegion(REGION)
                    .withTlsContext(tlsContext);
            try (S3Client client = createS3Client(clientOptions)) {
            }
        }
    }

    @Test
    public void testS3ClientCreateDestroyWithCredentialsProvider() {
        skipIfAndroid();
        skipIfNetworkUnavailable();

        try (EventLoopGroup elg = new EventLoopGroup(0, 1);
                HostResolver hostResolver = new HostResolver(elg);
                ClientBootstrap clientBootstrap = new ClientBootstrap(elg, hostResolver);
                DefaultChainCredentialsProvider credentialsProvider = new DefaultChainCredentialsProvider.DefaultChainCredentialsProviderBuilder()
                        .withClientBootstrap(clientBootstrap).build();) {
            S3ClientOptions clientOptions = new S3ClientOptions().withRegion(REGION)
                    .withClientBootstrap(clientBootstrap).withCredentialsProvider(credentialsProvider);
            try (S3Client client = new S3Client(clientOptions)) {
                assertNotNull(client);
            }
        }
    }

    @Test
    public void testS3ClientCreateDestroyWithoutSigningConfig() throws Exception {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        try (EventLoopGroup elg = new EventLoopGroup(0, 1);
                HostResolver hostResolver = new HostResolver(elg);
                ClientBootstrap clientBootstrap = new ClientBootstrap(elg, hostResolver);) {
            S3ClientOptions clientOptions = new S3ClientOptions().withRegion(REGION)
                    .withClientBootstrap(clientBootstrap);
            try (S3Client client = new S3Client(clientOptions)) {

            }
        }
    }

    /* Test that a client can be created successfully with retry options. */
    @Test
    public void testS3ClientCreateDestroyRetryOptions() {
        skipIfAndroid();
        skipIfNetworkUnavailable();

        try (EventLoopGroup elg = new EventLoopGroup(0, 1); EventLoopGroup retry_elg = new EventLoopGroup(0, 1)) {

            final StandardRetryOptions standardRetryOptions = new StandardRetryOptions().withInitialBucketCapacity(123)
                    .withBackoffRetryOptions(new ExponentialBackoffRetryOptions().withEventLoopGroup(retry_elg));

            try (S3Client client = createS3Client(new S3ClientOptions().withRegion(REGION)
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
        skipIfAndroid();
        skipIfNetworkUnavailable();

        try (EventLoopGroup elg = new EventLoopGroup(0, 1)) {

            final StandardRetryOptions standardRetryOptions = new StandardRetryOptions().withInitialBucketCapacity(123)
                    .withBackoffRetryOptions(new ExponentialBackoffRetryOptions().withMaxRetries(30));

            try (S3Client client = createS3Client(new S3ClientOptions().withRegion(REGION)
                    .withStandardRetryOptions(standardRetryOptions), elg)) {

            }
        }
    }

    /*
     * Test that a client can be created successfully with Tcp Keep Alive options.
     */
    @Test
    public void testS3ClientCreateDestroyTcpKeepAliveOptions() {
        skipIfAndroid();
        skipIfNetworkUnavailable();

        try (EventLoopGroup elg = new EventLoopGroup(0, 1); EventLoopGroup retry_elg = new EventLoopGroup(0, 1)) {

            S3TcpKeepAliveOptions tcpKeepAliveOptions = new S3TcpKeepAliveOptions();
            tcpKeepAliveOptions.setKeepAliveIntervalSec((short) 10);
            tcpKeepAliveOptions.setKeepAliveTimeoutSec((short) 20);
            tcpKeepAliveOptions.setKeepAliveMaxFailedProbes((short) 30);

            try (S3Client client = createS3Client(new S3ClientOptions().withRegion(REGION)
                    .withS3TcpKeepAliveOptions(tcpKeepAliveOptions), elg)) {

            }
        }
    }

    /*
     * Test that a client can be created successfully with monitoring options.
     */
    @Test
    public void testS3ClientCreateDestroyMonitoringOptions() {
        skipIfAndroid();
        skipIfNetworkUnavailable();

        try (EventLoopGroup elg = new EventLoopGroup(0, 1); EventLoopGroup retry_elg = new EventLoopGroup(0, 1)) {

            HttpMonitoringOptions monitoringOptions = new HttpMonitoringOptions();
            monitoringOptions.setMinThroughputBytesPerSecond(100);
            monitoringOptions.setAllowableThroughputFailureIntervalSeconds(10);
            try (S3Client client = createS3Client(new S3ClientOptions().withRegion(REGION)
                    .withHttpMonitoringOptions(monitoringOptions).withConnectTimeoutMs(10), elg)) {

            }
        }
    }

    /*
     * Test that a client can be created successfully with proxy options.
     */
    @Test
    public void testS3ClientCreateDestroyHttpProxyOptions() {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        try (EventLoopGroup elg = new EventLoopGroup(0, 1);
                EventLoopGroup retry_elg = new EventLoopGroup(0, 1);
                TlsContextOptions tlsContextOptions = TlsContextOptions.createDefaultClient();
                TlsContext tlsContext = new TlsContext(tlsContextOptions);) {
            HttpProxyOptions proxyOptions = new HttpProxyOptions();
            proxyOptions.setHost("localhost");
            proxyOptions.setConnectionType(HttpProxyOptions.HttpProxyConnectionType.Tunneling);
            proxyOptions.setPort(80);
            proxyOptions.setTlsContext(tlsContext);
            proxyOptions.setAuthorizationType(HttpProxyOptions.HttpProxyAuthorizationType.Basic);
            proxyOptions.setAuthorizationUsername("username");
            proxyOptions.setAuthorizationPassword("password");
            try (S3Client client = createS3Client(new S3ClientOptions()
                    .withRegion(REGION)
                    .withProxyOptions(proxyOptions), elg)) {
            }
        }
    }

    /*
     * Test that a client can be created successfully with proxy environment
     * variable setting.
     */
    @Test
    public void testS3ClientCreateDestroyHttpProxyEnvironmentVariableSetting() {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        try (EventLoopGroup elg = new EventLoopGroup(0, 1);
                EventLoopGroup retry_elg = new EventLoopGroup(0, 1);
                TlsContextOptions tlsContextOptions = TlsContextOptions.createDefaultClient();
                TlsContext tlsContext = new TlsContext(tlsContextOptions);
                TlsConnectionOptions tlsConnectionOptions = new TlsConnectionOptions(tlsContext);) {
            HttpProxyEnvironmentVariableSetting environmentVariableSetting = new HttpProxyEnvironmentVariableSetting();
            environmentVariableSetting.setConnectionType(HttpProxyOptions.HttpProxyConnectionType.Tunneling);
            environmentVariableSetting.setEnvironmentVariableType(
                    HttpProxyEnvironmentVariableSetting.HttpProxyEnvironmentVariableType.DISABLED);
            environmentVariableSetting.setTlsConnectionOptions(tlsConnectionOptions);
            try (S3Client client = createS3Client(new S3ClientOptions().withRegion(REGION)
                    .withProxyEnvironmentVariableSetting(environmentVariableSetting), elg)) {
            }
        }
    }

    @Test
    public void testS3Get() {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());
        S3ClientOptions clientOptions = new S3ClientOptions().withRegion(REGION);
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
                        onFinishedFuture.completeExceptionally(makeExceptionFromFinishedResponseContext(context));
                        return;
                    }
                    onFinishedFuture.complete(Integer.valueOf(context.getErrorCode()));
                }
            };

            HttpHeader[] headers = { new HttpHeader("Host", ENDPOINT) };
            HttpRequest httpRequest = new HttpRequest("GET", PRE_EXIST_1MB_PATH, headers, null);

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
    public void testS3GetWithResponseFilePath() {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());
        S3ClientOptions clientOptions = new S3ClientOptions().withRegion(REGION);
        try (S3Client client = createS3Client(clientOptions)) {
            CompletableFuture<Integer> onFinishedFuture = new CompletableFuture<>();
            Path responsePath = Files.createTempFile("testS3GetFilePath", ".txt");
            S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {
                @Override
                public void onFinished(S3FinishedResponseContext context) {
                    Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3,
                            "Meta request finished with error code " + context.getErrorCode());
                    if (context.getErrorCode() != 0) {
                        onFinishedFuture.completeExceptionally(makeExceptionFromFinishedResponseContext(context));
                        return;
                    }
                    onFinishedFuture.complete(Integer.valueOf(context.getErrorCode()));
                }
            };

            HttpHeader[] headers = { new HttpHeader("Host", ENDPOINT) };
            HttpRequest httpRequest = new HttpRequest("GET", PRE_EXIST_1MB_PATH, headers, null);

            S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                    .withMetaRequestType(MetaRequestType.GET_OBJECT).withHttpRequest(httpRequest)
                    .withResponseFilePath(responsePath)
                    .withResponseHandler(responseHandler);

            try (S3MetaRequest metaRequest = client.makeMetaRequest(metaRequestOptions)) {
                Assert.assertEquals(Integer.valueOf(0), onFinishedFuture.get());
            }
            Files.deleteIfExists(responsePath);
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testS3GetWithSizeHint() {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());
        S3ClientOptions clientOptions = new S3ClientOptions().withRegion(REGION);
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
                        onFinishedFuture.completeExceptionally(makeExceptionFromFinishedResponseContext(context));
                        return;
                    }
                    onFinishedFuture.complete(Integer.valueOf(context.getErrorCode()));
                }
            };

            HttpHeader[] headers = { new HttpHeader("Host", ENDPOINT) };
            HttpRequest httpRequest = new HttpRequest("GET", PRE_EXIST_1MB_PATH, headers, null);

            S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                    .withMetaRequestType(MetaRequestType.GET_OBJECT).withHttpRequest(httpRequest)
                    .withResponseHandler(responseHandler)
                    // Passing a 5GB size hint intentionally to verify
                    // that an incorrect size_hint and size_hint > UINT32.MAX work fine.
                    .withObjectSizeHint(5L * 1024 * 1024 * 1024);

            try (S3MetaRequest metaRequest = client.makeMetaRequest(metaRequestOptions)) {
                Assert.assertEquals(Integer.valueOf(0), onFinishedFuture.get());
            }
        } catch (InterruptedException | ExecutionException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testS3GetErrorFinishedResponseContextHasAllData() {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());
        S3ClientOptions clientOptions = new S3ClientOptions().withRegion(REGION);
        try (S3Client client = createS3Client(clientOptions)) {
            CompletableFuture<Integer> onFinishedFuture = new CompletableFuture<>();
            S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {

                @Override
                public int onResponseBody(ByteBuffer bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
                    return 0;
                }

                @Override
                public void onFinished(S3FinishedResponseContext context) {
                    Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3,
                            "Meta request finished with error code " + context.getErrorCode());
                    try {
                        assertNotNull(context.getErrorHeaders());
                        assertTrue(context.getErrorCode() > 0);
                        assertTrue(context.getResponseStatus() >= 400);
                        assertNotNull(context.getErrorPayload());
                        String payload = new String(context.getErrorPayload(), StandardCharsets.UTF_8);
                        assertTrue(payload.contains("<Error>"));
                        assertNotNull(context.getErrorOperationName());
                        assertFalse(context.getErrorOperationName().isEmpty());
                        onFinishedFuture.complete(0); // Assertions passed
                    } catch (AssertionError e) {
                        onFinishedFuture.complete(-1); // Assertions failed
                    }
                }
            };

            HttpHeader[] headers = { new HttpHeader("Host", ENDPOINT) };
            HttpRequest httpRequest = new HttpRequest("GET", "/key_does_not_exist", headers, null);

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
    public void testS3GetAfterClientIsClose() {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());
        S3ClientOptions clientOptions = new S3ClientOptions().withRegion(REGION);
        S3Client client = createS3Client(clientOptions);
        client.close();
        S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {

            @Override
            public int onResponseBody(ByteBuffer bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
                return 0;
            }

            @Override
            public void onFinished(S3FinishedResponseContext context) {
            }
        };

        HttpHeader[] headers = { new HttpHeader("Host", ENDPOINT) };
        HttpRequest httpRequest = new HttpRequest("GET", PRE_EXIST_1MB_PATH, headers, null);

        S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                .withMetaRequestType(MetaRequestType.GET_OBJECT).withHttpRequest(httpRequest)
                .withResponseHandler(responseHandler);

        assertThrows(IllegalStateException.class, () -> client.makeMetaRequest(metaRequestOptions));
    }

    @Test
    public void testS3CallbackExceptionIsProperlyPropagated() {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());
        S3ClientOptions clientOptions = new S3ClientOptions().withRegion(REGION);
        RuntimeException expectedException = new RuntimeException("Exception From a Java Function");

        try (S3Client client = createS3Client(clientOptions)) {
            CompletableFuture<Integer> onFinishedFuture = new CompletableFuture<>();
            S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {

                @Override
                public int onResponseBody(ByteBuffer bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
                    throw expectedException;
                }

                @Override
                public void onFinished(S3FinishedResponseContext context) {
                    Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3,
                            "Meta request finished with error code " + context.getErrorCode());
                    if (context.getErrorCode() != 0) {
                        onFinishedFuture.completeExceptionally(context.getCause());
                        return;
                    }
                    onFinishedFuture.complete(Integer.valueOf(context.getErrorCode()));
                }
            };

            HttpHeader[] headers = { new HttpHeader("Host", ENDPOINT) };
            HttpRequest httpRequest = new HttpRequest("GET", PRE_EXIST_1MB_PATH, headers, null);

            S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                    .withMetaRequestType(MetaRequestType.GET_OBJECT).withHttpRequest(httpRequest)
                    .withResponseHandler(responseHandler);

            try (S3MetaRequest metaRequest = client.makeMetaRequest(metaRequestOptions)) {
                ExecutionException ex = assertThrows(ExecutionException.class, () -> onFinishedFuture.get());
                Assert.assertSame(expectedException, ex.getCause());
            }
        }
    }

    @Test
    public void testS3GetWithEndpoint() {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());

        S3ClientOptions clientOptions = new S3ClientOptions().withRegion(REGION);
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
                        onFinishedFuture.completeExceptionally(makeExceptionFromFinishedResponseContext(context));
                        return;
                    }
                    onFinishedFuture.complete(Integer.valueOf(context.getErrorCode()));
                }
            };

            HttpHeader[] headers = { new HttpHeader("Host", ENDPOINT + ":443") };
            HttpRequest httpRequest = new HttpRequest("GET", PRE_EXIST_1MB_PATH, headers, null);

            S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                    .withMetaRequestType(MetaRequestType.GET_OBJECT).withHttpRequest(httpRequest)
                    .withResponseHandler(responseHandler)
                    .withEndpoint(URI.create("https://" + ENDPOINT + ":443"));

            try (S3MetaRequest metaRequest = client.makeMetaRequest(metaRequestOptions)) {
                Assert.assertEquals(Integer.valueOf(0), onFinishedFuture.get());
            }
        } catch (Exception ex /* InterruptedException | ExecutionException ex */) {
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
        skipIfAndroid();
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());
        final long fileSize = 1 * 1024 * 1024;
        final long initialReadWindowSize = 1024;
        S3ClientOptions clientOptions = new S3ClientOptions()

                .withRegion(REGION)
                .withReadBackpressureEnabled(true)
                .withInitialReadWindowSize(initialReadWindowSize)
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
            HttpRequest httpRequest = new HttpRequest("GET", PRE_EXIST_1MB_PATH, headers, null);

            S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                    .withMetaRequestType(MetaRequestType.GET_OBJECT)
                    .withHttpRequest(httpRequest)
                    .withResponseHandler(responseHandler);
            long accumulated_data_size = 0;
            long accumulated_window_increments = initialReadWindowSize;

            try (S3MetaRequest metaRequest = client.makeMetaRequest(metaRequestOptions)) {

                while (!onFinishedFuture.isDone()) {
                    // Check if we've received data since last loop
                    long lastDownloadSizeDelta = downloadSizeDelta.getAndSet(0);
                    accumulated_data_size += lastDownloadSizeDelta;
                    long max_data_allowed = accumulated_window_increments;

                    // Figure out how long it's been since we last received data
                    Instant currentTime = clock.instant();
                    if (lastDownloadSizeDelta != 0) {
                        lastTimeSomethingHappened = clock.instant();
                    }

                    Duration timeSinceSomethingHappened = Duration.between(lastTimeSomethingHappened, currentTime);
                    Assert.assertTrue(accumulated_data_size <=  max_data_allowed);

                    // If it seems like data has stopped flowing, then we know a stall happened due
                    // to backpressure.
                    if (timeSinceSomethingHappened.compareTo(ifNothingHappensAfterThisLongItStalled) >= 0) {
                        stallCount += 1;
                        long current_window = accumulated_window_increments - accumulated_data_size;
                        /* If stalled, it must be the window reach to 0 */
                        Assert.assertTrue(current_window == 0);
                        lastTimeSomethingHappened = currentTime;

                        // Increment window so that download continues...
                        metaRequest.incrementReadWindow(windowIncrementSize);
                        accumulated_window_increments += windowIncrementSize;
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
        skipIfAndroid();
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());
        Log.initLoggingToFile(LogLevel.Trace, "/Users/dengket/project/crts/aws-crt-java/log");
        final long fileSize = 1 * 1024 * 1024;
        S3ClientOptions clientOptions = new S3ClientOptions()

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
            HttpRequest httpRequest = new HttpRequest("GET", PRE_EXIST_1MB_PATH, headers, null);

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
        skipIfAndroid();
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());

        S3ClientOptions clientOptions = new S3ClientOptions().withRegion(REGION);
        boolean expectedException = false;
        byte[] madeUpCredentials = "I am a madeup credentials".getBytes();
        StaticCredentialsProvider.StaticCredentialsProviderBuilder builder = new StaticCredentialsProvider.StaticCredentialsProviderBuilder()
                .withAccessKeyId(madeUpCredentials).withSecretAccessKey(madeUpCredentials);
        try (S3Client client = createS3Client(clientOptions);
                CredentialsProvider emptyCredentialsProvider = builder.build();
                AwsSigningConfig signingConfig = AwsSigningConfig.getDefaultS3SigningConfig(REGION,
                        emptyCredentialsProvider);) {
            CompletableFuture<Integer> onFinishedFuture = new CompletableFuture<>();
            S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {
                @Override
                public void onFinished(S3FinishedResponseContext context) {
                    Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3,
                            "Meta request finished with error code " + context.getErrorCode());
                    if (context.getErrorCode() != 0) {
                        onFinishedFuture.completeExceptionally(new CrtRuntimeException(context.getErrorCode()));
                        return;
                    }
                    onFinishedFuture.complete(Integer.valueOf(context.getErrorCode()));
                }
            };

            HttpHeader[] headers = { new HttpHeader("Host", ENDPOINT) };
            HttpRequest httpRequest = new HttpRequest("GET", PRE_EXIST_1MB_PATH, headers, null);
            S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                    .withMetaRequestType(MetaRequestType.GET_OBJECT).withHttpRequest(httpRequest)
                    .withResponseHandler(responseHandler).withSigningConfig(signingConfig);

            try (S3MetaRequest metaRequest = client.makeMetaRequest(metaRequestOptions)) {
                Assert.assertEquals(Integer.valueOf(0), onFinishedFuture.get());
            }
        } catch (InterruptedException | ExecutionException ex) {
            expectedException = true;
            /*
             * Maybe better to have a cause of the max retries exceed to be more informative
             */
            if (!(ex.getCause() instanceof CrtRuntimeException)) {
                Assert.fail(ex.getMessage());
            }
        }
        Assert.assertTrue(expectedException);
    }

    @Test
    public void testS3GetWithSignConfigShouldSignHeader() throws Exception {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());

        S3ClientOptions clientOptions = new S3ClientOptions().withRegion(REGION);
        Predicate<String> shouldSignHeader = name -> !name.equalsIgnoreCase("DoNotSignThis");
        try (S3Client client = createS3Client(clientOptions);
                EventLoopGroup elg = new EventLoopGroup(0, 1);
                HostResolver hostResolver = new HostResolver(elg);
                ClientBootstrap clientBootstrap = new ClientBootstrap(elg, hostResolver);
                DefaultChainCredentialsProvider credentialsProvider = new DefaultChainCredentialsProvider.DefaultChainCredentialsProviderBuilder()
                        .withClientBootstrap(clientBootstrap).build();
                AwsSigningConfig signingConfig = AwsSigningConfig.getDefaultS3SigningConfig(REGION,
                        credentialsProvider);) {
            CompletableFuture<Integer> onFinishedFuture = new CompletableFuture<>();
            S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {
                @Override
                public void onFinished(S3FinishedResponseContext context) {
                    Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3,
                            "Meta request finished with error code " + context.getErrorCode());
                    if (context.getErrorCode() != 0) {
                        onFinishedFuture.completeExceptionally(makeExceptionFromFinishedResponseContext(context));
                        return;
                    }
                    onFinishedFuture.complete(Integer.valueOf(context.getErrorCode()));
                }
            };

            HttpHeader[] headers = { new HttpHeader("Host", ENDPOINT) };
            HttpRequest httpRequest = new HttpRequest("GET", PRE_EXIST_1MB_PATH, headers, null);

            signingConfig.setShouldSignHeader(shouldSignHeader);
            S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                    .withMetaRequestType(MetaRequestType.GET_OBJECT).withHttpRequest(httpRequest)
                    .withResponseHandler(responseHandler).withSigningConfig(signingConfig);

            try (S3MetaRequest metaRequest = client.makeMetaRequest(metaRequestOptions)) {
                Assert.assertEquals(Integer.valueOf(0), onFinishedFuture.get());
            }
        }
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

    private String uploadObjectPathInit(String objectPath) {
        return UPLOAD_DIR + objectPath;
    }

    private void testS3PutHelper(boolean useFile, boolean unknownContentLength, String objectPath, boolean s3express,
            int contentLength, boolean contentMD5) throws IOException {
        /* Give a default file options, which should have no affect on non-file path tests and also works for every file-based tests. */
        FileIoOptions fIoOptions = new FileIoOptions(true, 10.0, true);
        S3ClientOptions clientOptions = new S3ClientOptions().withRegion(REGION).withEnableS3Express(s3express)
                .withComputeContentMd5(contentMD5).withFileIoOptions(fIoOptions);
        Path uploadFilePath = Files.createTempFile("testS3PutFilePath", ".txt");
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
                        onFinishedFuture.completeExceptionally(makeExceptionFromFinishedResponseContext(context));
                        return;
                    }
                    onFinishedFuture.complete(Integer.valueOf(context.getErrorCode()));
                }
            };

            HttpHeader[] headers = {
                    new HttpHeader("Host", s3express ? S3EXPRESS_ENDPOINT_USW2_AZ1 : ENDPOINT)
            };
            HttpRequest httpRequest;
            String path = objectPath == null ? "/put_object_test_10MB.txt" : objectPath;
            String encodedPath = Uri.encodeUriPath(uploadObjectPathInit(path));
            final ByteBuffer payload = ByteBuffer.wrap(createTestPayload(contentLength));
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
            if (useFile) {
                Files.write(uploadFilePath, createTestPayload(contentLength));
                httpRequest = new HttpRequest("PUT", encodedPath, headers, null);
            } else {
                httpRequest = new HttpRequest("PUT", encodedPath, headers, payloadStream);
            }

            if (!unknownContentLength) {
                httpRequest.addHeader(
                        new HttpHeader("Content-Length", Integer.valueOf(contentLength).toString()));
            }
            AwsSigningConfig config = AwsSigningConfig.getDefaultS3SigningConfig(REGION, null);
            S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                    .withMetaRequestType(MetaRequestType.PUT_OBJECT).withHttpRequest(httpRequest)
                    .withResponseHandler(responseHandler);

            if (!contentMD5) {
                ChecksumConfig checksumConfig = new ChecksumConfig().withChecksumAlgorithm(ChecksumAlgorithm.SHA1)
                        .withChecksumLocation(ChecksumLocation.TRAILER).withValidateChecksum(true);
                metaRequestOptions = metaRequestOptions.withChecksumConfig(checksumConfig);
            }
            if (s3express) {
                config.setAlgorithm(AwsSigningConfig.AwsSigningAlgorithm.SIGV4_S3EXPRESS);
                metaRequestOptions = metaRequestOptions.withSigningConfig(config);
            }
            if (useFile) {
                metaRequestOptions = metaRequestOptions.withRequestFilePath(uploadFilePath);
            }
            try (S3MetaRequest metaRequest = client.makeMetaRequest(metaRequestOptions)) {
                Assert.assertEquals(Integer.valueOf(0), onFinishedFuture.get());
            }
        } catch (InterruptedException | ExecutionException ex) {
            Assert.fail(ex.getMessage());
        } finally {
            Files.deleteIfExists(uploadFilePath);
        }
    }

    @Test
    public void testS3Put() throws IOException {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());
        testS3PutHelper(false, false, null, false, 16 * 1024 * 1024, false);
    }

    // MD5 is not FIPS allowed. Make sure the cypto lib we used still supports MD5.
    @Test
    public void testS3PutWithMD5() throws IOException {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());
        testS3PutHelper(false, true, null, false, 16 * 1024 * 1024, true);
    }

    // Test that we can upload by passing a filepath instead of an HTTP body stream
    @Test
    public void testS3PutFilePath() throws IOException {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());
        testS3PutHelper(true, false, null, false, 10 * 1024 * 1024, false);
    }

    // Test that we can upload without provide the content length
    @Test
    public void testS3PutUnknownContentLength() throws IOException {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());
        testS3PutHelper(false, true, null, false, 10 * 1024 * 1024, false);
    }

    // Test that we can upload to a path with special characters
    @Test
    public void testS3PutSpecialCharPath() throws IOException {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());
        testS3PutHelper(false, true, "/put_object_test_10MB@$%.txt", false, 10 * 1024 * 1024, false);
    }

    @Test
    public void testS3PutS3Express() throws IOException {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());
        testS3PutHelper(false, false, null, true, 16 * 1024 * 1024, false);
    }

    @Test
    public void testS3PutS3ExpressSpecialCharPath() throws IOException {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());
        testS3PutHelper(false, true, "/put_object_test_10MB@$%.txt", true, 10 * 1024 * 1024, false);
    }

    // Test that passing a nonexistent file path will cause an error
    @Test
    public void testS3PutNonexistentFilePath() throws IOException {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());

        S3ClientOptions clientOptions = new S3ClientOptions().withRegion(REGION);
        try (S3Client client = createS3Client(clientOptions)) {
            // response handler does nothing, it just needs to exist for this test
            S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {
            };

            HttpHeader[] headers = {
                    new HttpHeader("Host", ENDPOINT),
                    new HttpHeader("Content-Length", String.valueOf(1024)),
            };
            HttpRequest httpRequest = new HttpRequest("PUT", uploadObjectPathInit("/put_nonexistent_file"), headers,
                    null);

            S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                    .withMetaRequestType(MetaRequestType.PUT_OBJECT)
                    .withHttpRequest(httpRequest)
                    .withRequestFilePath(Paths.get("obviously_nonexistent_file.derp"))
                    .withResponseHandler(responseHandler);

            // makeMetaRequest() should fail
            Throwable thrown = assertThrows(Throwable.class,
                    () -> client.makeMetaRequest(metaRequestOptions));

            // exception should indicate the file doesn't exist
            String exceptionString = thrown.toString();
            Assert.assertTrue(exceptionString.contains("AWS_ERROR_FILE_INVALID_PATH"));
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
                    onFinishedFuture.completeExceptionally(new CrtRuntimeException(context.getErrorCode()));
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
        skipIfAndroid();
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());

        S3ClientOptions clientOptions = new S3ClientOptions()

                .withRegion(REGION);
        try (S3Client client = createS3Client(clientOptions)) {
            CompletableFuture<Integer> onFinishedFuture = new CompletableFuture<>();
            CompletableFuture<Void> onProgressFuture = new CompletableFuture<>();
            S3MetaRequestResponseHandler responseHandler = createTestPutPauseResumeHandler(onFinishedFuture,
                    onProgressFuture);

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

            HttpRequest httpRequest = new HttpRequest("PUT", uploadObjectPathInit("/put_object_test_128MB"), headers,
                    payloadStream);

            S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                    .withMetaRequestType(MetaRequestType.PUT_OBJECT)
                    .withChecksumAlgorithm(ChecksumAlgorithm.CRC32)
                    .withHttpRequest(httpRequest)
                    .withResponseHandler(responseHandler);

            ResumeToken resumeToken;
            try (S3MetaRequest metaRequest = client.makeMetaRequest(metaRequestOptions)) {
                onProgressFuture.get();

                resumeToken = metaRequest.pause();
                Assert.assertNotNull(resumeToken);

                Throwable thrown = assertThrows(Throwable.class,
                        () -> onFinishedFuture.get());

                Assert.assertEquals("AWS_ERROR_S3_PAUSED", ((CrtRuntimeException) thrown.getCause()).errorName);
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
                    uploadObjectPathInit("/put_object_test_128MB"), headersResume, payloadStreamResume);

            CompletableFuture<Integer> onFinishedFutureResume = new CompletableFuture<>();
            CompletableFuture<Void> onProgressFutureResume = new CompletableFuture<>();
            S3MetaRequestResponseHandler responseHandlerResume = createTestPutPauseResumeHandler(onFinishedFutureResume,
                    onProgressFutureResume);
            S3MetaRequestOptions metaRequestOptionsResume = new S3MetaRequestOptions()
                    .withMetaRequestType(MetaRequestType.PUT_OBJECT)
                    .withHttpRequest(httpRequestResume)
                    .withResponseHandler(responseHandlerResume)
                    .withChecksumAlgorithm(ChecksumAlgorithm.CRC32)
                    .withResumeToken(new ResumeToken.PutResumeTokenBuilder()
                            .withPartSize(resumeToken.getPartSize())
                            .withTotalNumParts(resumeToken.getTotalNumParts())
                            .withNumPartsCompleted(resumeToken.getNumPartsCompleted())
                            .withUploadId(resumeToken.getUploadId())
                            .build());

            try (S3MetaRequest metaRequest = client.makeMetaRequest(metaRequestOptionsResume)) {
                Integer finish = onFinishedFutureResume.get();
                Assert.assertEquals(Integer.valueOf(0), finish);
            }
        } catch (InterruptedException | ExecutionException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    private void testS3RoundTripWithChecksumHelper(ChecksumAlgorithm algo, ChecksumLocation location, boolean MPU,
            boolean provide_full_object_checksum) throws IOException {

        S3ClientOptions clientOptions = new S3ClientOptions().withRegion(REGION);
        if (MPU) {
            clientOptions.withPartSize(5 * 1024 * 1024);
            clientOptions.withMultipartUploadThreshold(5 * 1024 * 1024);
        } else {
            clientOptions.withPartSize(10 * 1024 * 1024);
            clientOptions.withMultipartUploadThreshold(20 * 1024 * 1024);
        }
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
                        onPutFinishedFuture.completeExceptionally(makeExceptionFromFinishedResponseContext(context));
                        return;
                    }
                    onPutFinishedFuture.complete(Integer.valueOf(context.getErrorCode()));
                }
            };

            final ByteBuffer payload = ByteBuffer.wrap(createTestPayload(10 * 1024 * 1024));

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
            ArrayList<HttpHeader> headers = new ArrayList<HttpHeader>();
            headers.add(new HttpHeader("Host", ENDPOINT));
            headers.add(new HttpHeader("Content-Length", Integer.valueOf(payload.capacity()).toString()));
            if (provide_full_object_checksum) {
                switch (algo) {
                    case CRC32:
                        headers.add(new HttpHeader("x-amz-checksum-crc32", "1BObvg=="));
                        break;
                    case CRC64NVME:
                        headers.add(new HttpHeader("x-amz-checksum-crc64nvme", "fIa08UXfyzk="));
                        break;

                    default:
                        Assert.fail("Unsupported checksum algorithm for full object checksum");
                        break;
                }
            }

            String objectPath = uploadObjectPathInit(
                    "/prefix/round_trip/java_round_trip_test_fc_" + location.name() + "_" + algo.name())
                    + (MPU ? "_mpu" : "") + (provide_full_object_checksum ? "_full_object" : "");
            HttpRequest httpRequest = new HttpRequest("PUT", objectPath, headers.toArray(new HttpHeader[0]),
                    payloadStream);
            ChecksumConfig config = new ChecksumConfig();

            if (!provide_full_object_checksum) {
                /* If the checksum provided for the full object via header, skip the checksum from client. */
                config.withChecksumAlgorithm(algo)
                        .withChecksumLocation(location);
            }
            S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                    .withMetaRequestType(MetaRequestType.PUT_OBJECT).withHttpRequest(httpRequest)
                    .withResponseHandler(responseHandler)
                    .withChecksumConfig(config);

            try (S3MetaRequest metaRequest = client.makeMetaRequest(metaRequestOptions)) {
                Assert.assertEquals(Integer.valueOf(0), onPutFinishedFuture.get());
            }

            // Get request!

            HttpHeader[] getHeaders = { new HttpHeader("Host", ENDPOINT), };

            HttpRequest httpGetRequest = new HttpRequest("GET", objectPath, getHeaders, null);

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
                        onGetFinishedFuture.completeExceptionally(makeExceptionFromFinishedResponseContext(context));
                        return;
                    }
                    if (!context.isChecksumValidated()) {
                        onGetFinishedFuture.completeExceptionally(
                                new RuntimeException("Checksum was not validated"));
                        return;
                    }
                    if (context.getChecksumAlgorithm() != algo) {
                        onGetFinishedFuture.completeExceptionally(
                                new RuntimeException("Checksum was not validated via expected algo: " + algo.name()));
                        return;
                    }
                    onGetFinishedFuture.complete(Integer.valueOf(context.getErrorCode()));
                }
            };
            ArrayList<ChecksumAlgorithm> algorList = new ArrayList<ChecksumAlgorithm>();
            algorList.add(ChecksumAlgorithm.CRC64NVME);
            algorList.add(ChecksumAlgorithm.CRC32C);
            algorList.add(ChecksumAlgorithm.CRC32);
            algorList.add(ChecksumAlgorithm.SHA1);
            algorList.add(ChecksumAlgorithm.SHA256);
            ChecksumConfig validateChecksumConfig = new ChecksumConfig().withValidateChecksum(true)
                    .withValidateChecksumAlgorithmList(algorList);
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
    public void testS3PutTrailerChecksums() throws Exception {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());
        testS3RoundTripWithChecksumHelper(ChecksumAlgorithm.CRC64NVME, ChecksumLocation.TRAILER, false, false);
    }

    @Test
    public void testS3PutHeaderChecksums() throws Exception {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());
        testS3RoundTripWithChecksumHelper(ChecksumAlgorithm.CRC64NVME, ChecksumLocation.HEADER, false, false);
    }

    @Test
    public void testS3RoundTripWithFullObjectMPU() throws Exception {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());
        testS3RoundTripWithChecksumHelper(ChecksumAlgorithm.CRC64NVME, ChecksumLocation.NONE, true, true);
    }

    @Test
    public void testS3RoundTripWithFullObjectSinglePart() throws Exception {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());
        testS3RoundTripWithChecksumHelper(ChecksumAlgorithm.CRC64NVME, ChecksumLocation.NONE, false, true);
    }

    @Test
    public void testS3RoundTripWithFullObjectMPUCRC32() throws Exception {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());
        testS3RoundTripWithChecksumHelper(ChecksumAlgorithm.CRC32, ChecksumLocation.NONE, true, true);
    }

    @Test
    public void testS3RoundTripWithFullObjectSinglePartCRC32() throws Exception {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());
        testS3RoundTripWithChecksumHelper(ChecksumAlgorithm.CRC32, ChecksumLocation.NONE, false, true);
    }

    @Test
    public void testS3GetS3ExpressOverride() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());
        CompletableFuture<Credentials> orig_creds_future = new CompletableFuture<Credentials>();
        Credentials fake_creds = new Credentials("my_access".getBytes(),
                "dont_tell_anyone".getBytes(), "token".getBytes());

        S3ClientOptions clientOptions = new S3ClientOptions().withRegion(REGION).withEnableS3Express(true)
                .withS3ExpressCredentialsProviderFactory(
                        new S3ExpressCredentialsProviderFactory() {
                            public S3ExpressCredentialsProvider createS3ExpressCredentialsProvider(S3Client client) {
                                S3ExpressCredentialsProviderHandler handler = new S3ExpressCredentialsProviderHandler() {
                                    public CompletableFuture<Credentials> getS3ExpressCredentials(
                                            S3ExpressCredentialsProperties properties,
                                            Credentials origCredentials) {
                                        CompletableFuture<Credentials> future = new CompletableFuture<Credentials>();
                                        orig_creds_future.complete(origCredentials);
                                        Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3,
                                                "Get creds for : " + properties.getHostValue());
                                        Credentials creds = new Credentials("access_key".getBytes(),
                                                "secret_access_key".getBytes(), "session_token".getBytes());
                                        future.complete(creds);
                                        return future;
                                    }

                                    public CompletableFuture<Void> destroyProvider() {
                                        CompletableFuture<Void> future = new CompletableFuture<Void>();
                                        future.complete(null);
                                        return future;
                                    }
                                };
                                S3ExpressCredentialsProvider provider = new S3ExpressCredentialsProvider(handler);
                                return provider;
                            }
                        });
        try (S3Client client = createS3Client(clientOptions)) {
            CompletableFuture<Integer> onFinishedFuture = new CompletableFuture<>();
            S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {

                @Override
                public int onResponseBody(ByteBuffer bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
                    byte[] bytes = new byte[bodyBytesIn.remaining()];
                    bodyBytesIn.get(bytes);
                    return 0;
                }

                @Override
                public void onFinished(S3FinishedResponseContext context) {
                    Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3,
                            "Meta request finished with error code " + context.getErrorCode());
                    if (context.getErrorCode() != 0) {
                        onFinishedFuture.completeExceptionally(new CrtRuntimeException(context.getErrorCode()));
                        return;
                    }
                    onFinishedFuture.complete(Integer.valueOf(context.getErrorCode()));
                }
            };

            HttpHeader[] headers = { new HttpHeader("Host", S3EXPRESS_ENDPOINT_USW2_AZ1) };
            HttpRequest httpRequest = new HttpRequest("GET", PRE_EXIST_10MB_PATH, headers, null);

            AwsSigningConfig config = new AwsSigningConfig();
            config.setAlgorithm(AwsSigningConfig.AwsSigningAlgorithm.SIGV4_S3EXPRESS);
            config.setCredentials(fake_creds);

            S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                    .withMetaRequestType(MetaRequestType.GET_OBJECT).withHttpRequest(httpRequest)
                    .withResponseHandler(responseHandler).withSigningConfig(config);

            try (S3MetaRequest metaRequest = client.makeMetaRequest(metaRequestOptions)) {
                Assert.assertEquals(Integer.valueOf(0), onFinishedFuture.get());
            }
        } catch (InterruptedException | ExecutionException ex) {
            if (!(ex.getCause() instanceof CrtRuntimeException)) {
                Assert.fail(ex.getMessage());
            } else {
                CrtRuntimeException cause = (CrtRuntimeException) ex.getCause();
                Assert.assertTrue(cause.errorName.equals("AWS_ERROR_S3_INVALID_RESPONSE_STATUS"));
            }
        } finally {
            /*
             * Check the request level override of the credentials was passed along to the
             * s3express provider override
             */
            Credentials resolved_creds = orig_creds_future.get();
            assertTrue(Arrays.equals(resolved_creds.getAccessKeyId(), fake_creds.getAccessKeyId()));
            assertTrue(Arrays.equals(resolved_creds.getSecretAccessKey(), fake_creds.getSecretAccessKey()));
            assertTrue(Arrays.equals(resolved_creds.getSessionToken(), fake_creds.getSessionToken()));
        }
    }

    private void putS3ExpressHelper(String region, S3Client client) throws Exception {

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
                    onFinishedFuture.completeExceptionally(makeExceptionFromFinishedResponseContext(context));
                    return;
                }
                onFinishedFuture.complete(Integer.valueOf(context.getErrorCode()));
            }
        };

        HttpHeader[] headers = {
                new HttpHeader("Host",
                        region.equals("us-east-1") ? S3EXPRESS_ENDPOINT_USE1_AZ4 : S3EXPRESS_ENDPOINT_USW2_AZ1),
        };
        HttpRequest httpRequest;
        String path = uploadObjectPathInit("/put_object_test_10MB.txt");
        String encodedPath = Uri.encodeUriPath(path);
        final ByteBuffer payload = ByteBuffer.wrap(createTestPayload(10 * 1024 * 1024));
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
        httpRequest = new HttpRequest("PUT", encodedPath, headers, payloadStream);

        AwsSigningConfig config = new AwsSigningConfig();
        config.setAlgorithm(AwsSigningConfig.AwsSigningAlgorithm.SIGV4_S3EXPRESS);
        config.setRegion(region);
        S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                .withMetaRequestType(MetaRequestType.PUT_OBJECT).withHttpRequest(httpRequest)
                .withResponseHandler(responseHandler).withSigningConfig(config);

        try (S3MetaRequest metaRequest = client.makeMetaRequest(metaRequestOptions)) {
            Assert.assertEquals(Integer.valueOf(0), onFinishedFuture.get());
        }
    }

    @Test
    public void testS3PutS3ExpressOverrideSamples() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());
        S3ClientOptions clientOptions = new S3ClientOptions().withRegion(REGION).withEnableS3Express(true)
                .withS3ExpressCredentialsProviderFactory(
                        new S3ExpressCredentialsProviderFactory() {
                            public S3ExpressCredentialsProvider createS3ExpressCredentialsProvider(S3Client client) {
                                S3ExpressCredentialsProviderHandler handler = new S3ExpressCredentialsProviderHandlerSample(
                                        client);
                                S3ExpressCredentialsProvider provider = new S3ExpressCredentialsProvider(handler);
                                return provider;
                            }
                        });

        try (S3Client client = createS3Client(clientOptions)) {
            putS3ExpressHelper("us-west-2", client);
            putS3ExpressHelper("us-east-1", client);
        } catch (InterruptedException | ExecutionException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void testS3PutS3ExpressMultiRegionDefault() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());
        S3ClientOptions clientOptions = new S3ClientOptions().withRegion(REGION).withEnableS3Express(true);

        try (S3Client client = createS3Client(clientOptions)) {
            putS3ExpressHelper("us-west-2", client);
            putS3ExpressHelper("us-east-1", client);
        } catch (InterruptedException | ExecutionException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    // TODO: copy is disabled currently because it does not work correctly on c
    // side. reenable once its fixed in crt.
    // @Test
    public void testS3Copy() {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        Assume.assumeTrue(hasAwsCredentials());

        // In C, we used the same bucket for copy tests.
        String COPY_SOURCE_BUCKET = BUCKET_NAME;
        String COPY_SOURCE_KEY = "crt-canary-obj.txt";
        String X_AMZ_COPY_SOURCE_HEADER = "x-amz-copy-source";

        S3ClientOptions clientOptions = new S3ClientOptions().withRegion(REGION);
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
                        System.out.println("Test failed with error payload: "
                                + new String(context.getErrorPayload(), StandardCharsets.UTF_8));
                        onFinishedFuture.completeExceptionally(makeExceptionFromFinishedResponseContext(context));
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
        skipIfAndroid();
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
                Integer.toString((int) Math.ceil(vipsNeeded / 5))));
        System.out.println(String.format("REGION=%s, WARMUP=%s", region, sampleDelay));

        // Ignore stats during warm up time, they skew results
        TransferStats.global.withSampleDelay(Duration.ofSeconds(sampleDelay));
        try (TlsContext tlsCtx = createTlsContextOptions(getContext().trustStore)) {
            S3ClientOptions clientOptions = new S3ClientOptions().withRegion(region)
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
                                onFinishedFuture
                                        .completeExceptionally(makeExceptionFromFinishedResponseContext(context));
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
        skipIfAndroid();
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
                Integer.toString((int) Math.ceil(vipsNeeded / 5))));
        System.out.println(String.format("REGION=%s, WARMUP=%s", region, sampleDelay));

        // Ignore stats during warm up time, they skew results
        TransferStats.global.withSampleDelay(Duration.ofSeconds(sampleDelay));

        try (TlsContext tlsCtx = createTlsContextOptions(getContext().trustStore)) {
            S3ClientOptions clientOptions = new S3ClientOptions().withRegion(region)
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
                                onFinishedFuture
                                        .completeExceptionally(makeExceptionFromFinishedResponseContext(context));
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
