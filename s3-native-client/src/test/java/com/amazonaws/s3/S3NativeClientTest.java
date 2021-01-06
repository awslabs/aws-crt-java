package com.amazonaws.s3;

import com.amazonaws.s3.model.GetObjectRequest;
import com.amazonaws.s3.model.PutObjectRequest;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.auth.credentials.DefaultChainCredentialsProvider;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;

import java.io.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.*;

public class S3NativeClientTest {
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

    public static void main(String [] args) throws FileNotFoundException, ExecutionException, InterruptedException {
        final int numTransfers = Integer.parseInt(System.getProperty("aws.crt.s3.benchmark.transfers", "160"));
        final int concurrentTransfers = Integer.parseInt(System.getProperty("aws.crt.s3.benchmark.concurrent", "8")); /* should be 1.6 * expectedGbps */

        //note: usage of executor service automatically assigns threads for receiving the data
        //      and may be a bottleneck for performance
        final ExecutorService executorService = Executors.newFixedThreadPool(concurrentTransfers);
        try (final EventLoopGroup elGroup = new EventLoopGroup(18); //num of IO threads
             final HostResolver resolver = new HostResolver(elGroup, 128);
             final ClientBootstrap clientBootstrap = new ClientBootstrap(elGroup, resolver)) {
            final String region = args[0];
            final S3NativeClient nativeClient = new S3NativeClient(elGroup, clientBootstrap, region,
                    new DefaultChainCredentialsProvider.DefaultChainCredentialsProviderBuilder()
                            .withClientBootstrap(clientBootstrap).build());
            final String operation = args[1];
            final File fileArg = args.length > 4 ? new File(args[4]) : null;
            if (operation.equalsIgnoreCase("get")) {
                final GetObjectRequest request = GetObjectRequest.builder()
                        .bucket(args[2])
                        .key(args[3])
                        .build();
                OutputStream os[] = new OutputStream[] { System.out };
                if (fileArg != null) {
                    os[0] = new FileOutputStream(fileArg);
                }
                for (int transferIdx = 0; transferIdx < numTransfers; ++transferIdx) {
                    final TransferStats stats = new TransferStats();
                    final int transferIndex = transferIdx;

                    executorService.submit( () -> {
                        nativeClient.getObject(request, new ResponseDataConsumer() {
                            @Override
                            public void onFinished() {
                                System.err.printf("[%d] Average: %.3fgbps; Peak: %.3fgbps; First-byte latency: %dms%n",
                                        transferIndex, stats.avgGbps(), stats.peakGbps(), stats.latency());
                                if (transferIndex == 0 && fileArg != null) {
                                    try {
                                        os[0].close();  //close only non stdout output
                                    } catch (IOException e) { }
                                }
                            }

                            public void onException(CrtRuntimeException e) {
                            }

                            @Override
                            public void onResponseData(byte[] bodyBytesIn) {
                                try {
                                    if (transferIndex == 0) {
                                        os[0].write(bodyBytesIn);
                                    }
                                    stats.recordRead(bodyBytesIn.length);
                                } catch (Exception e) {
                                }
                            }
                        });
                    }); //submit transfer task to executor service
                } //transfers iteration
                executorService.shutdown();
                executorService.awaitTermination(2, TimeUnit.HOURS);
            } else if (operation.equalsIgnoreCase("put")) {
                final PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                        .bucket(args[2])
                        .key(args[3]);
                final InputStream in[] = { System.in };
                if (fileArg != null) {
                    if (!fileArg.exists()) {
                        System.err.println("No file exists at: " + fileArg.getAbsolutePath());
                        System.exit(-1);
                    }
                    in[0] = new FileInputStream(fileArg);
                    requestBuilder.contentLength(fileArg.length());
                }
                final PutObjectRequest request = requestBuilder.build();

                System.err.println("Putting object: " + request.key());
                nativeClient.putObject(request, new RequestDataSupplier() {
                    @Override
                    public boolean getRequestBytes(byte[] buffer) {
                        int bytesRead = 0;
                        try {
                            int toRead = buffer.length;
                            while (toRead != 0 && (bytesRead = in[0].read(buffer, bytesRead, toRead)) != -1) {
                                toRead = buffer.length - bytesRead;
                            }
                        } catch (IOException e) {
                            return true;
                        }
                        return bytesRead == -1; //return true when done reading
                    }

                    @Override
                    public boolean resetPosition() {
                        try {
                            in[0].reset();
                            return true;
                        } catch (IOException e) {
                            return false;
                        }
                    }

                    @Override
                    public long getLength() {
                        return fileArg.length();
                    }
                });
            } else {
                CompletableFuture<Void> errorFuture = new CompletableFuture();
                errorFuture.completeExceptionally(new RuntimeException("Unknown CLI operation: [" + operation + "]"));
            }

            if (operation.equalsIgnoreCase("get")) {
                System.err.printf("Totals: Average: %.3fgbps; Peak: %.3fgbps; First-byte latency: %dms%n",
                        TransferStats.global.avgGbps(), TransferStats.global.peakGbps(),
                        TransferStats.global.latency());
            }
            System.err.println("Transfer complete!");
        }
    }
}
