
/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.test;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.http.Http2StreamManager;
import software.amazon.awssdk.crt.http.Http2Request;
import software.amazon.awssdk.crt.http.Http2Stream;
import software.amazon.awssdk.crt.http.HttpStreamManagerOptions;
import software.amazon.awssdk.crt.http.HttpClientConnection;
import software.amazon.awssdk.crt.http.HttpClientConnectionManager;
import software.amazon.awssdk.crt.http.HttpClientConnectionManagerOptions;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpProxyOptions;
import software.amazon.awssdk.crt.http.HttpRequestBodyStream;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.http.HttpStreamBaseResponseHandler;
import software.amazon.awssdk.crt.http.HttpStreamBase;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;
import software.amazon.awssdk.crt.utils.ByteBufferUtils;
import software.amazon.awssdk.crt.Log;

public class Http2ClientLocalHostTest extends HttpClientTestFixture {

    private Http2StreamManager createStreamManager(URI uri, int numConnections) {

        try (EventLoopGroup eventLoopGroup = new EventLoopGroup(1);
                HostResolver resolver = new HostResolver(eventLoopGroup);
                ClientBootstrap bootstrap = new ClientBootstrap(eventLoopGroup, resolver);
                SocketOptions sockOpts = new SocketOptions();
                TlsContextOptions tlsOpts = TlsContextOptions.createDefaultClient().withAlpnList("h2").withVerifyPeer(false);
                TlsContext tlsContext = createHttpClientTlsContext(tlsOpts)) {
            HttpStreamManagerOptions options = new HttpStreamManagerOptions();
            options.withClientBootstrap(bootstrap)
                    .withSocketOptions(sockOpts)
                    .withTlsContext(tlsContext)
                    .withUri(uri)
                    .withMaxConnections(numConnections);

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
                new HttpHeader(":authority", uri.getHost()),
                new HttpHeader("content-length", Long.toString(bodyLength))
        };
        HttpRequestBodyStream bodyStream = null;
        if(bodyLength > 0) {
            bodyStream = createBodyStreamWithLength(bodyLength);
        }
        Http2Request request = new Http2Request(requestHeaders, bodyStream);

        return request;
    }

    @Test
    public void testParallelRequestsStress() throws Exception {
        skipIfLocalhostUnavailable();
        Log.initLoggingToStderr(Log.LogLevel.Trace);
        URI uri = new URI("https://localhost:8443/echo");
        try(
        Http2StreamManager streamManager = createStreamManager(uri, 100)) {
            int numberToAcquire = 500 * 100;

            Http2Request request = createHttp2Request("GET", uri, 0);
            List<CompletableFuture<Void>> requestCompleteFutures = new ArrayList<>();
            List<CompletableFuture<Http2Stream>> acquireCompleteFutures = new ArrayList<>();
            final AtomicInteger numStreamsFailures = new AtomicInteger(0);
            for (int i = 0; i < numberToAcquire; i++) {
                final CompletableFuture<Void> requestCompleteFuture = new CompletableFuture<Void>();
                requestCompleteFutures.add(requestCompleteFuture);
                acquireCompleteFutures.add(streamManager.acquireStream(request, new HttpStreamBaseResponseHandler() {
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
                        requestCompleteFuture.complete(null);
                    }
                }));
            }
            for (CompletableFuture<Http2Stream> f : acquireCompleteFutures) {
                f.get(30, TimeUnit.SECONDS);
            }
            // Wait for all Requests to complete
            for (CompletableFuture<Void> f : requestCompleteFutures) {
                f.get(30, TimeUnit.SECONDS);
            }
            Assert.assertTrue(numStreamsFailures.get() == 0);
        }
        CrtResource.logNativeResources();
        CrtResource.waitForNoResources();
    }
}
