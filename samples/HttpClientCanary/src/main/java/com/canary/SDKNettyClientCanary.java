package com.canary;

import static java.nio.charset.StandardCharsets.UTF_8;
import static software.amazon.awssdk.http.SdkHttpConfigurationOption.TRUST_ALL_CERTIFICATES;
import static software.amazon.awssdk.http.SdkHttpConfigurationOption.PROTOCOL;

import org.reactivestreams.Publisher;
import io.reactivex.Flowable;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import software.amazon.awssdk.http.*;
import software.amazon.awssdk.http.async.AsyncExecuteRequest;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.async.SdkAsyncHttpResponseHandler;
import software.amazon.awssdk.http.async.SdkHttpContentPublisher;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.utils.AttributeMap;
import software.amazon.awssdk.http.nio.netty.Http2Configuration;
import software.amazon.awssdk.utils.StringUtils;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.canary.CanaryUtils.createDataCollector;
import static com.canary.CanaryUtils.printResult;
import static software.amazon.awssdk.utils.StringUtils.isBlank;

public class SDKNettyClientCanary {
    private final AtomicInteger opts = new AtomicInteger(0);
    private URI uri;
    private int batchNum;
    private int maxStreams = 100;
    private int maxConnections = 50;
    private String nettyResultPath;
    /* If the body length is larger than 0, the request will be a PUT request. Otherwise, it will be a GET request. */
    private long bodyLength = 0;
    private String data = null;
    private boolean timer = false; /* If true, instead of collect ops/sec, it collects the time/ops */

    public static AttributeMap.Builder trustAllTlsAttributeMapBuilder() {
        return AttributeMap.builder().put(TRUST_ALL_CERTIFICATES, true);
    }

    private static class TestResponseHandler implements SdkAsyncHttpResponseHandler {
        @Override
        public void onHeaders(SdkHttpResponse headers) {
        }

        @Override
        public void onStream(Publisher<ByteBuffer> stream) {
            Flowable.fromPublisher(stream).forEach(b -> {
                // System.out.println(b);
            });
        }

        @Override
        public void onError(Throwable error) {
        }
    }

    private AsyncExecuteRequest createExecuteRequest(AtomicInteger numStreamsFailures) {
        SdkHttpRequest request = createRequest(uri);
        SdkAsyncHttpResponseHandler handler = new TestResponseHandler() {
            @Override
            public void onHeaders(SdkHttpResponse headers) {
                opts.incrementAndGet();
                if (!headers.isSuccessful()) {
                    numStreamsFailures.incrementAndGet();
                }
            }

            @Override
            public void onError(Throwable error) {
                numStreamsFailures.incrementAndGet();
            }
        };
        this.data = StringUtils.repeat("*", 10);
        AsyncExecuteRequest.Builder builder = AsyncExecuteRequest.builder()
                .request(request)
                .responseHandler(handler);
        Publisher<ByteBuffer> dataPublisher = Flowable.just(ByteBuffer.wrap(data.getBytes(UTF_8)));
        if (this.bodyLength > 0) {
            builder.requestContentPublisher(new SdkHttpContentPublisher() {
                @Override
                public Optional<Long> contentLength() {
                    return Optional.of((long) data.length());
                }

                @Override
                public void subscribe(Subscriber<? super ByteBuffer> s) {
                    dataPublisher.subscribe(s);
                }
            });
        } else{
            builder.requestContentPublisher(new EmptyPublisher());
        }
        return builder.build();
    }

    private SdkHttpFullRequest createRequest(URI uri) {

        SdkHttpFullRequest.Builder builder = SdkHttpFullRequest.builder()
                .uri(uri)
                .encodedPath(uri.getPath());
        if (this.bodyLength > 0) {
            builder.method(SdkHttpMethod.PUT);
        } else{
            builder.method(SdkHttpMethod.GET);
        }

        return builder.build();
    }

    private void concurrentRequests(SdkAsyncHttpClient sdkHttpClient, int concurrentNum,
            AtomicInteger numStreamsFailures, AtomicInteger opts, AtomicBoolean done) throws Exception {

        while (!done.get()) {

            final AtomicInteger requestCompleted = new AtomicInteger(0);
            final CompletableFuture<Void> requestCompleteFuture = new CompletableFuture<Void>();

            for (int i = 0; i < concurrentNum; i++) {
                try {
                    CompletableFuture<Void> httpClientFuture = sdkHttpClient
                            .execute(createExecuteRequest(numStreamsFailures))
                            .whenComplete((r, t) -> {
                                if (t != null) {
                                    numStreamsFailures.incrementAndGet();
                                }
                                opts.incrementAndGet();
                                int requestCompletedNum = requestCompleted.incrementAndGet();
                                if (requestCompletedNum == concurrentNum) {
                                    requestCompleteFuture.complete(null);
                                }
                            });
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
            // Wait for all Requests to complete
            requestCompleteFuture.get();
        }
    }

    private void runCanary(int warmupLoops, int loops, long timerSecs) throws Exception {
        ArrayList<Double> warmupResults = new ArrayList<>();
        ArrayList<Double> results = new ArrayList<>();
        AtomicInteger streamFailed = new AtomicInteger(0);

        System.out.println("batchNum: "+ this.batchNum);
        System.out.println("maxStreams: "+ this.maxStreams);
        System.out.println("maxConnections: "+ this.maxConnections);
        System.out.println("bodyLength: " + this.bodyLength);
        SdkAsyncHttpClient sdkHttpClient = NettyNioAsyncHttpClient.builder().http2Configuration(Http2Configuration.builder()
                .maxStreams((long) this.maxStreams).build())
                .maxConcurrency(this.batchNum)
                .buildWithDefaults(trustAllTlsAttributeMapBuilder()
                        .put(PROTOCOL, Protocol.HTTP2)
                        .build());

        AtomicInteger opts = new AtomicInteger(0);
        AtomicBoolean done = new AtomicBoolean(false);
        ScheduledExecutorService scheduler = createDataCollector(warmupLoops, loops, timerSecs, opts, done,
                warmupResults, results);
        concurrentRequests(sdkHttpClient, batchNum, streamFailed, opts, done);
        scheduler.shutdown();

        System.out.println("Failed request num: " + streamFailed.get());
        System.out.println("////////////// warmup results //////////////");
        printResult(warmupResults);
        System.out.println("////////////// real results //////////////");
        double avg_result = printResult(results);
        BufferedWriter writer = new BufferedWriter(new FileWriter(this.nettyResultPath, false));
        writer.append(Double.toString(avg_result));
        writer.close();
    }

    public static void main(String[] args) throws Exception {
        SDKNettyClientCanary canary = new SDKNettyClientCanary();

        canary.uri = new URI(System.getProperty("aws.crt.http.canary.uri", "https://localhost:8443/echo"));
        canary.maxConnections = Integer.parseInt(System.getProperty("aws.crt.http.canary.maxConnections", "8"));
        canary.maxStreams = Integer.parseInt(System.getProperty("aws.crt.http.canary.maxStreams", "20"));
        canary.nettyResultPath = System.getProperty("aws.crt.http.canary.nettyResultPath", "netty_result.txt");
        canary.bodyLength = Long.parseLong(System.getProperty("aws.crt.http.canary.bodyLength", "0"));

        canary.batchNum = canary.maxStreams * canary.maxConnections;
        canary.runCanary(5, 5, 30);
        System.exit(0);
    }
}
