/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package com.canary;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.io.*;
import software.amazon.awssdk.crt.http.*;
import software.amazon.awssdk.crt.utils.ByteBufferUtils;
import software.amazon.awssdk.crt.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.canary.CanaryUtils.createDataCollector;
import static com.canary.CanaryUtils.printResult;

/**
 * A sample for testing the custom private key operations. See the Java V2 SDK
 * sample for a more in-depth
 * sample with additional options that can be configured via the terminal. This
 * is for testing primarily.
 */
public class Http2StreamManagerCanary {
    private final AtomicInteger opts = new AtomicInteger(0);
    private static final String HTTPS = "https";
    private URI uri;
    private int maxStreams = 100;
    private int maxConnections = 50;
    private int batchNum;
    private String nettyResultPath;
    /* If the body length is larger than 0, the request will be a PUT request. Otherwise, it will be a GET request. */
    private int bodyLength = 0;

    private Http2StreamManager createStreamManager(URI uri, int numConnections) {

        try (EventLoopGroup eventLoopGroup = new EventLoopGroup(0 /* default to number of cores */);
                HostResolver resolver = new HostResolver(eventLoopGroup);
                ClientBootstrap bootstrap = new ClientBootstrap(eventLoopGroup, resolver);
                SocketOptions sockOpts = new SocketOptions();
                TlsContextOptions tlsOpts = TlsContextOptions.createDefaultClient().withAlpnList("h2")
                        .withVerifyPeer(false);
                TlsContext tlsContext = new TlsContext(tlsOpts)) {
            boolean useTls = HTTPS.equals(uri.getScheme());
            Http2StreamManagerOptions options = new Http2StreamManagerOptions();
            HttpClientConnectionManagerOptions connectionManagerOptions = new HttpClientConnectionManagerOptions();
            connectionManagerOptions.withClientBootstrap(bootstrap)
                    .withSocketOptions(sockOpts)
                    .withUri(uri)
                    .withMaxConnections(numConnections);
            if (useTls) {
                connectionManagerOptions.withTlsContext(tlsContext);
            } else {
                options.withPriorKnowledge(true);
            }
            options.withConnectionManagerOptions(connectionManagerOptions)
                    .withIdealConcurrentStreamsPerConnection(this.maxStreams)
                    .withMaxConcurrentStreamsPerConnection(this.maxStreams);

            return Http2StreamManager.create(options);
        }
    }

    private HttpRequestBodyStream createBodyStreamWithLength(long bodyLength) {
        final long payloadSize = bodyLength;
        final String payloadString = "This is CRT HTTP test.";

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
        return payloadStream;
    }

    private Http2Request createHttp2Request(String method, URI uri, long bodyLength) {
        HttpHeader[] requestHeaders = new HttpHeader[] {
                new HttpHeader(":method", method),
                new HttpHeader(":path", uri.getPath()),
                new HttpHeader(":scheme", uri.getScheme()),
                new HttpHeader(":authority", uri.getAuthority()),
        };
        HttpRequestBodyStream bodyStream = null;
        if (bodyLength > 0) {
            bodyStream = createBodyStreamWithLength(bodyLength);
        }
        Http2Request request = new Http2Request(requestHeaders, bodyStream);

        return request;
    }

    private void concurrentRequests(Http2StreamManager streamManager, int concurrentNum,
            AtomicInteger numStreamsFailures, AtomicInteger opts, AtomicBoolean done) throws Exception {
        Http2Request request = createHttp2Request(this.bodyLength == 0?"GET": "PUT", uri, this.bodyLength);
        while (!done.get()) {
            final AtomicInteger requestCompleted = new AtomicInteger(0);
            final CompletableFuture<Void> requestCompleteFuture = new CompletableFuture<Void>();
            for (int i = 0; i < concurrentNum; i++) {
                streamManager.acquireStream(request, new HttpStreamBaseResponseHandler() {
                    @Override
                    public void onResponseHeaders(HttpStreamBase stream, int responseStatusCode, int blockType,
                            HttpHeader[] nextHeaders) {
                        if (responseStatusCode != 200) {
                            numStreamsFailures.incrementAndGet();
                        }
                    }

                    @Override
                    public void onResponseComplete(HttpStreamBase stream, int errorCode) {
                        if (errorCode != CRT.AWS_CRT_SUCCESS) {
                            numStreamsFailures.incrementAndGet();
                        }
                        stream.close();

                        opts.incrementAndGet();
                        int requestCompletedNum = requestCompleted.incrementAndGet();
                        if (requestCompletedNum == concurrentNum) {
                            requestCompleteFuture.complete(null);
                        }
                    }
                });
            }
            // Wait for all Requests to complete
            requestCompleteFuture.get(30, TimeUnit.SECONDS);
        }
    }

    private void runCanary(int warmupLoops, int loops, long timerSecs) throws Exception {
        ArrayList<Double> warmupResults = new ArrayList<>();
        ArrayList<Double> results = new ArrayList<>();
        AtomicInteger streamFailed = new AtomicInteger(0);

        System.out.println("batchNum: " + this.batchNum);
        System.out.println("maxStreams: " + this.maxStreams);
        System.out.println("maxConnections: " + this.maxConnections);
        try (Http2StreamManager streamManager = createStreamManager(uri, this.maxConnections)) {
            AtomicInteger opts = new AtomicInteger(0);
            AtomicBoolean done = new AtomicBoolean(false);
            ScheduledExecutorService scheduler = createDataCollector(warmupLoops, loops, timerSecs, opts, done,
                    warmupResults, results);
            concurrentRequests(streamManager, batchNum, streamFailed, opts, done);
            scheduler.shutdown();
        }
        System.out.println("Failed request num: " + streamFailed.get());
        System.out.println("////////////// warmup results //////////////");
        printResult(warmupResults);
        System.out.println("////////////// real results //////////////");
        double avg_result = printResult(results);
        BufferedReader reader = new BufferedReader(new FileReader(nettyResultPath));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        reader.close();
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        String content = stringBuilder.toString();
        double netty_result = Double.parseDouble(content);
        if (avg_result < netty_result) {
            System.out.println("CRT result is smaller than netty. CRT: " + avg_result + " Netty: " + netty_result);
            System.exit(-1);
        }

        CrtResource.logNativeResources();
        CrtResource.waitForNoResources();

    }

    public static void main(String[] args) throws Exception {
        System.out.println("Current JVM version - " + System.getProperty("java.version"));

        Http2StreamManagerCanary canary = new Http2StreamManagerCanary();

        canary.uri = new URI(System.getProperty("aws.crt.http.canary.uri", "https://localhost:8443/echo"));
        canary.maxConnections = Integer.parseInt(System.getProperty("aws.crt.http.canary.maxConnections", "8"));
        canary.maxStreams = Integer.parseInt(System.getProperty("aws.crt.http.canary.maxStreams", "20"));
        canary.nettyResultPath = System.getProperty("aws.crt.http.canary.nettyResultPath", "netty_result.txt");
        canary.bodyLength = Integer.parseInt(System.getProperty("aws.crt.http.canary.bodyLength", "0"));

        canary.batchNum = canary.maxStreams * canary.maxConnections;
        canary.runCanary(5, 5, 30);
    }
}
