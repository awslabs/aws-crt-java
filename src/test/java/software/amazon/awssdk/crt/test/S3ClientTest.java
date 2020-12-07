package software.amazon.awssdk.crt.test;

import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.s3.S3Client;
import software.amazon.awssdk.crt.s3.S3ClientOptions;
import software.amazon.awssdk.crt.s3.S3MetaRequest;
import software.amazon.awssdk.crt.s3.S3MetaRequestOptions;
import software.amazon.awssdk.crt.s3.S3MetaRequestResponseHandler;
import software.amazon.awssdk.crt.s3.S3MetaRequestOptions.MetaRequestType;
import software.amazon.awssdk.crt.utils.ByteBufferUtils;
import software.amazon.awssdk.crt.auth.credentials.DefaultChainCredentialsProvider;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.http.HttpRequestBodyStream;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.CrtRuntimeException;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class S3ClientTest extends CrtTestFixture {

    static final String ENDPOINT = "aws-crt-test-stuff-us-west-2.s3.us-west-2.amazonaws.com";
    static final String REGION = "us-west-2";

    public S3ClientTest() {
    }

    private S3Client createS3Client(S3ClientOptions options, int numThreads) {
        try (EventLoopGroup elg = new EventLoopGroup(numThreads);
                HostResolver hostResolver = new HostResolver(elg);
                ClientBootstrap clientBootstrap = new ClientBootstrap(elg, hostResolver)) {
            Assert.assertNotNull(clientBootstrap);

            try (DefaultChainCredentialsProvider credentialsProvider = new DefaultChainCredentialsProvider.DefaultChainCredentialsProviderBuilder()
                    .withClientBootstrap(clientBootstrap).build()) {
                options.withRegion(REGION).withClientBootstrap(clientBootstrap)
                        .withCredentialsProvider(credentialsProvider);
                return new S3Client(options);
            }
        }
    }

    private S3Client createS3Client(S3ClientOptions options) {
        return createS3Client(options, 1);
    }

    @Test
    public void testS3ClientCreateDestroy() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);

        S3ClientOptions clientOptions = new S3ClientOptions().withEndpoint(ENDPOINT);
        try (S3Client client = createS3Client(clientOptions)) {

        }
    }

    @Test
    public void testS3Get() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        Assume.assumeTrue(hasAwsCredentials());

        S3ClientOptions clientOptions = new S3ClientOptions().withEndpoint(ENDPOINT);
        try (S3Client client = createS3Client(clientOptions)) {
            CompletableFuture<Integer> onFinishedFuture = new CompletableFuture<>();
            S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {

                @Override
                public int onResponseBody(byte[] bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
                    Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3,
                            "Body Response: " + bodyBytesIn.toString());
                    return 0;
                }

                @Override
                public void onFinished(int errorCode) {
                    Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3,
                            "Meta request finished with error code " + errorCode);
                    if (errorCode != 0) {
                        onFinishedFuture.completeExceptionally(new CrtRuntimeException(errorCode));
                        return;
                    }
                    onFinishedFuture.complete(Integer.valueOf(errorCode));
                }
            };

            HttpHeader[] headers = { new HttpHeader("Host", ENDPOINT) };
            HttpRequest httpRequest = new HttpRequest("GET", "/get_object_test_1MB.txt", headers, null);

            S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                    .withMetaRequestType(MetaRequestType.GET_OBJECT)
                    .withHttpRequest(httpRequest)
                    .withResponseHandler(responseHandler);

            try (S3MetaRequest metaRequest = client.makeMetaRequest(metaRequestOptions)) {
                Assert.assertEquals(Integer.valueOf(0), onFinishedFuture.get());
            }
        } catch (InterruptedException | ExecutionException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    private byte[] createTestPayload() {
        String msg = "This is an S3 Java CRT Client Test";
        ByteBuffer payload = ByteBuffer.allocate(1024 * 1024);
        while (true) {
            try {
                payload.put(msg.getBytes());
            }
            catch (BufferOverflowException ex1) {
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
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        Assume.assumeTrue(hasAwsCredentials());

        S3ClientOptions clientOptions = new S3ClientOptions().withEndpoint(ENDPOINT);
        try (S3Client client = createS3Client(clientOptions)) {
            CompletableFuture<Integer> onFinishedFuture = new CompletableFuture<>();
            S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {

                @Override
                public int onResponseBody(byte[] bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
                    Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3, "Body Response: " + bodyBytesIn.toString());
                    return 0;
                }

                @Override
                public void onFinished(int errorCode) {
                    Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3,
                            "Meta request finished with error code " + errorCode);
                    if (errorCode != 0) {
                        onFinishedFuture.completeExceptionally(new CrtRuntimeException(errorCode));
                        return;
                    }
                    onFinishedFuture.complete(Integer.valueOf(errorCode));
                }
            };

            final ByteBuffer payload = ByteBuffer.wrap(createTestPayload());
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

    static class TransferStats {
        static final double GBPS = 1000 * 1000 * 1000;

        long bytesRead = 0;
        long bytesSampled = 0;
        long bytesPeak = 0;
        double bytesAvg = 0;
        Instant startTime = Instant.now();
        Instant lastSampleTime = Instant.now();
        int msToFirstByte = 0;

        public void recordRead(long size) {
            Instant now = Instant.now();
            recordRead(size, now);
            if (this != global) {
                synchronized (global) {
                    global.recordRead(size, now);
                }
            }
        }

        private void recordRead(long size, Instant now) {
            bytesRead += size;
            if (msToFirstByte == 0) {
                msToFirstByte = (int) ChronoUnit.MILLIS.between(startTime, now);
                if (this != global) {
                    synchronized (global) {
                        global.recordLatency(msToFirstByte);
                    }
                }
            }
            if (now.minusSeconds(1).isAfter(lastSampleTime)) {
                long bytesThisSecond = bytesRead - bytesSampled;
                bytesSampled += bytesThisSecond;
                bytesAvg = (bytesAvg + bytesThisSecond) * 0.5;
                if (bytesThisSecond > bytesPeak) {
                    bytesPeak = bytesThisSecond;
                }
                lastSampleTime = now;
            }
        }

        private void recordLatency(long latencyMs) {
            msToFirstByte = (int)Math.ceil((msToFirstByte + latencyMs) * 0.5);
        }

        public double avgGbps() {
            return (bytesAvg * 8) / GBPS;
        }

        public double peakGbps() {
            return (bytesPeak * 8) / GBPS;
        }

        public int latency() {
            return msToFirstByte;
        }

        public static TransferStats global = new TransferStats();
    }

    @Test
    public void benchmarkS3Get() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        Assume.assumeTrue(hasAwsCredentials());
        Assume.assumeNotNull(System.getProperty("aws.crt.s3.benchmark"));

        // Log.initLoggingToStdout(Log.LogLevel.Debug);

        // Override defaults with values from system properties, via -D on mvn commandline
        final int threadCount = Integer.parseInt(System.getProperty("aws.crt.s3.benchmark.threads", "0"));
        final String region = System.getProperty("aws.crt.s3.benchmark.region", "us-west-2");
        final String bucket = System.getProperty("aws.crt.s3.benchmark.bucket",
                (region == "us-west-2") ? "aws-crt-canary-bucket" : String.format("aws-crt-canary-bucket-%s", region));
        final String endpoint = System.getProperty("aws.crt.s3.benchmark.endpoint",
                String.format("%s.s3.amazonaws.com", bucket));
        final String objectName = System.getProperty("aws.crt.s3.benchmark.object",
                "crt-canary-obj-single-part-9223372036854775807");
        final boolean multiPart = Boolean.parseBoolean(System.getProperty("aws.crt.s3.benchmark.multipart", "false"));
        final boolean useTls = Boolean.parseBoolean(System.getProperty("aws.crt.s3.benchmark.tls", "false"));
        final double expectedGbps = Double.parseDouble(System.getProperty("aws.crt.s3.benchmark.gbps", "5"));
        final int numTransfers = Integer.parseInt(System.getProperty("aws.crt.s3.benchmark.transfers", "160"));
        final int concurrentTransfers = Integer.parseInt(System.getProperty("aws.crt.s3.benchmark.concurrent", "8")); /* should be 1.6 * expectedGbps */

        S3ClientOptions clientOptions = new S3ClientOptions().withRegion(region).withEndpoint(endpoint)
                .withThroughputTargetGbps(expectedGbps);

        try (S3Client client = createS3Client(clientOptions, threadCount)) {
            HttpHeader[] headers = { new HttpHeader("Host", endpoint) };
            HttpRequest httpRequest = new HttpRequest("GET", String.format("/%s", objectName), headers, null);

            List<CompletableFuture<Integer>> requestFutures = new LinkedList<>();

            // Each meta request will acquire a slot, and release it when it's done
            Semaphore concurrentSlots = new Semaphore(concurrentTransfers);

            for (int transferIdx = 0; transferIdx < numTransfers; ++transferIdx) {
                try {
                    concurrentSlots.acquire();
                } catch (InterruptedException ex) {
                    Assert.fail(ex.toString());
                }

                final int myIdx = transferIdx;
                CompletableFuture<Integer> onFinishedFuture = new CompletableFuture<>();
                requestFutures.add(onFinishedFuture);

                S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {

                    TransferStats stats = new TransferStats();

                    @Override
                    public int onResponseBody(byte[] bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
                        synchronized (stats) {
                            stats.recordRead(bodyBytesIn.length);
                        }
                        return 0;
                    }

                    @Override
                    public void onFinished(int errorCode) {
                        // release the slot first
                        concurrentSlots.release();

                        if (errorCode != 0) {
                            onFinishedFuture.completeExceptionally(new CrtRuntimeException(errorCode));
                            return;
                        }

                        System.out.println(String.format("Transfer %d:  Avg: %.3f Gbps Peak: %.3f Gbps First Byte: %dms",
                                myIdx + 1, stats.avgGbps(), stats.peakGbps(), stats.latency()));

                        onFinishedFuture.complete(Integer.valueOf(errorCode));
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
            for (CompletableFuture request : requestFutures) {
                try {
                    request.join();
                } catch (CompletionException ex) {
                    System.out.println(ex.toString());
                    --completedTransfers;
                }
            }

            // Dump overall stats
            TransferStats overall = TransferStats.global;
            System.out
                    .println(String.format("%d/%d successful transfers, Avg: %.3f Gbps Peak: %.3f Gbps Avg Latency: %dms",
                            completedTransfers, numTransfers, overall.avgGbps(), overall.peakGbps(), overall.latency()));
        }
    }
}
