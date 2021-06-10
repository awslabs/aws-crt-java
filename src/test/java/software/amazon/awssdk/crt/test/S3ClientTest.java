package software.amazon.awssdk.crt.test;

import software.amazon.awssdk.crt.io.*;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.DoubleStream;
import java.util.LinkedList;
import java.util.List;

import software.amazon.awssdk.crt.io.StandardRetryOptions;
import software.amazon.awssdk.crt.io.ExponentialBackoffRetryOptions;

public class S3ClientTest extends CrtTestFixture {

    static final String ENDPOINT = "aws-crt-test-stuff-us-west-2.s3.us-west-2.amazonaws.com";
    static final String REGION = "us-west-2";

    public S3ClientTest() {
    }

	private S3Client createS3Client(S3ClientOptions options, int numThreads) {
    	return createS3Client(options, numThreads, 0);
	}

    private S3Client createS3Client(S3ClientOptions options, int numThreads, int cpuGroup) {
        try (EventLoopGroup elg = new EventLoopGroup(cpuGroup, numThreads);
                HostResolver hostResolver = new HostResolver(elg);
                ClientBootstrap clientBootstrap = new ClientBootstrap(elg, hostResolver)) {
            Assert.assertNotNull(clientBootstrap);

            try (DefaultChainCredentialsProvider credentialsProvider = new DefaultChainCredentialsProvider.DefaultChainCredentialsProviderBuilder()
                    .withClientBootstrap(clientBootstrap).build()) {
                Assert.assertNotNull(credentialsProvider);
                options.withClientBootstrap(clientBootstrap)
                        .withCredentialsProvider(credentialsProvider);
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
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);

        S3ClientOptions clientOptions = new S3ClientOptions().withEndpoint(ENDPOINT).withRegion(REGION);
        try (S3Client client = createS3Client(clientOptions)) {

        }
    }

    @Test
    public void testS3ClientCreateDestroyRetryOptions() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);

        /* No backoff retry options specified. */
        try (StandardRetryOptions standardRetryOptions = new StandardRetryOptions.Builder().withInitialBucketCapcity(100).build();
             S3Client client = createS3Client(new S3ClientOptions().withEndpoint(ENDPOINT).withRegion(REGION).withStandardRetryOptions(standardRetryOptions) )) {
        }

        /* Backoff retry options specified. */
        try (EventLoopGroup retryElg = new EventLoopGroup(0,1);
             ExponentialBackoffRetryOptions backoffRetryOptions = new ExponentialBackoffRetryOptions.Builder().withEventLoopGroup(retryElg).build();
             StandardRetryOptions standardRetryOptions = new StandardRetryOptions.Builder().withInitialBucketCapcity(100).withBackoffRetryOptions(backoffRetryOptions).build();
             S3Client client = createS3Client(new S3ClientOptions().withEndpoint(ENDPOINT).withRegion(REGION).withStandardRetryOptions(standardRetryOptions) )) {
        }
    }

    @Test
    public void testS3Get() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
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
            return Arrays.stream(bytesPerSecond.toArray(new Long[1]))
                    .mapToDouble(a -> a.doubleValue() * 8 / GBPS);
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
            return (count > 0) ?  Math.sqrt((sumOfSquares / count) - (avg * avg)) : 0;
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
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        Assume.assumeTrue(hasAwsCredentials());
        Assume.assumeNotNull(System.getProperty("aws.crt.s3.benchmark"));

        //Log.initLoggingToStdout(LogLevel.Trace);

        // Override defaults with values from system properties, via -D on mvn commandline
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
        // avg of .3Gbps per connection, 32 connections per vip, 5 seconds per vip resolution
        final int vipsNeeded = (int)Math.ceil(expectedGbps / 0.5 / 10);
        final int sampleDelay = Integer.parseInt(System.getProperty("aws.crt.s3.benchmark.warmup", new Integer((int)Math.ceil(vipsNeeded / 5)).toString()));
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
                        public void onFinished(int errorCode) {
                            // release the slot first
                            concurrentSlots.release();

                            if (errorCode != 0) {
                                onFinishedFuture.completeExceptionally(new CrtRuntimeException(errorCode));
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
}
