package com.canary;

import static software.amazon.awssdk.http.SdkHttpConfigurationOption.TRUST_ALL_CERTIFICATES;
import static software.amazon.awssdk.http.SdkHttpConfigurationOption.PROTOCOL;

import org.reactivestreams.Publisher;
import software.amazon.awssdk.http.*;
import software.amazon.awssdk.http.async.AsyncExecuteRequest;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.async.SdkAsyncHttpResponseHandler;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.utils.AttributeMap;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.canary.CanaryUtils.createDataCollector;
import static com.canary.CanaryUtils.printResult;

public class SDKNettyClientCanary {
    private final AtomicInteger opts = new AtomicInteger(0);
    private URI uri;
    private int benchNum;

    public static AttributeMap.Builder trustAllTlsAttributeMapBuilder() {
        return AttributeMap.builder().put(TRUST_ALL_CERTIFICATES, true);
    }

    private static class TestResponseHandler implements SdkAsyncHttpResponseHandler {
        @Override
        public void onHeaders(SdkHttpResponse headers) {
        }

        @Override
        public void onStream(Publisher<ByteBuffer> stream) {
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
            public void onStream(Publisher<ByteBuffer> stream) {
            }

            @Override
            public void onError(Throwable error) {
                numStreamsFailures.incrementAndGet();
            }
        };
        return AsyncExecuteRequest.builder()
                .request(request)
                .requestContentPublisher(new EmptyPublisher())
                .responseHandler(handler)
                .build();
    }

    private SdkHttpFullRequest createRequest(URI uri) {
        return SdkHttpFullRequest.builder()
                .uri(uri)
                .method(SdkHttpMethod.GET)
                .encodedPath("/echo")
                .putHeader("content-length", "0")
                .build();
    }

    private void concurrentRequests(SdkAsyncHttpClient sdkHttpClient, int concurrentNum,
            AtomicInteger numStreamsFailures, AtomicInteger opts, AtomicBoolean done) throws Exception {

        while (!done.get()) {

            SdkHttpFullRequest request = SdkHttpFullRequest.builder()
                    .uri(uri)
                    .method(SdkHttpMethod.GET)
                    .build();
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
            requestCompleteFuture.get(30, TimeUnit.SECONDS);
        }
    }

    private void runCanary(int warmupLoops, int loops, long timerSecs) throws Exception {
        ArrayList<Double> warmupResults = new ArrayList<>();
        ArrayList<Double> results = new ArrayList<>();
        AtomicInteger streamFailed = new AtomicInteger(0);

        SdkAsyncHttpClient sdkHttpClient = NettyNioAsyncHttpClient.builder()
                .buildWithDefaults(trustAllTlsAttributeMapBuilder()
                        .put(PROTOCOL, Protocol.HTTP2)
                        .build());

        AtomicInteger opts = new AtomicInteger(0);
        AtomicBoolean done = new AtomicBoolean(false);
        ScheduledExecutorService scheduler = createDataCollector(warmupLoops, loops, timerSecs, opts, done,
                warmupResults, results);
        concurrentRequests(sdkHttpClient, benchNum, streamFailed, opts, done);
        scheduler.shutdown();

        System.out.println("Failed request num: " + streamFailed.get());
        System.out.println("////////////// warmup results //////////////");
        printResult(warmupResults);
        System.out.println("////////////// real results //////////////");
        printResult(results);
    }

    public static void main(String[] args) throws Exception {
        SDKNettyClientCanary canary = new SDKNettyClientCanary();
        /* TODO: make all those number configurable */
        canary.uri = new URI("https://localhost:8443/echo");
        canary.benchNum = 100;
        canary.runCanary(5, 5, 10);
    }
}
