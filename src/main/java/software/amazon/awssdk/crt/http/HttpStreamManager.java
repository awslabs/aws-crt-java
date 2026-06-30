/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.http;

import java.util.concurrent.CompletableFuture;

/**
 * Manages a Pool of HTTP Streams. Wraps either Http1StreamManager or Http2StreamManager
 * depending on the expected protocol configured via HttpStreamManagerOptions.
 */
public class HttpStreamManager implements AutoCloseable {

    private Http1StreamManager h1StreamManager = null;
    private Http2StreamManager h2StreamManager = null;
    private CompletableFuture<Void> shutdownComplete = null;

    /**
     * Factory function for HttpStreamManager instances
     *
     * @param options the stream manager options to configure the manager
     * @return a new instance of an HttpStreamManager
     */
    public static HttpStreamManager create(HttpStreamManagerOptions options) {
        return new HttpStreamManager(options);
    }

    private HttpStreamManager(HttpStreamManagerOptions options) {
        if (options.getExpectedProtocol() == HttpVersion.UNKNOWN) {
            throw new IllegalArgumentException("UNKNOWN protocol is not supported. Please specify either HTTP_2 or HTTP_1_1/HTTP_1_0.");
        }

        if (options.getExpectedProtocol() == HttpVersion.HTTP_2) {
            this.h2StreamManager = Http2StreamManager.create(options.getHTTP2StreamManagerOptions());
            this.shutdownComplete = this.h2StreamManager.getShutdownCompleteFuture();
        } else {
            this.h1StreamManager = Http1StreamManager.create(options.getHTTP1ConnectionManagerOptions());
            this.shutdownComplete = this.h1StreamManager.getShutdownCompleteFuture();
        }
    }

    /**
     * Request an HttpStream from StreamManager.
     *
     * @param request         HttpRequestBase. The Request to make to the Server.
     * @param streamHandler   HttpStreamBaseResponseHandler. The Stream Handler to be called from the Native EventLoop
     * @return A future for a HttpStreamBase that will be completed when the stream is
     *         acquired.
     */
    public CompletableFuture<HttpStreamBase> acquireStream(HttpRequestBase request,
            HttpStreamBaseResponseHandler streamHandler) {
        if (this.h2StreamManager != null) {
            return this.h2StreamManager.acquireStream(request, streamHandler)
                    .thenApply(stream -> (HttpStreamBase) stream);
        } else {
            return this.h1StreamManager.acquireStream(request, streamHandler, false)
                    .thenApply(stream -> (HttpStreamBase) stream);
        }
    }

    /**
     * Request an HttpStream from StreamManager.
     *
     * @param request         HttpRequestBase. The Request to make to the Server.
     * @param streamHandler   HttpStreamBaseResponseHandler. The Stream Handler to be called from the Native EventLoop
     * @param useManualDataWrites A boolean variable to signal that body will be streamed using async writes.
     * @return A future for a HttpStreamBase that will be completed when the stream is
     *         acquired.
     */
    public CompletableFuture<HttpStreamBase> acquireStream(HttpRequestBase request,
            HttpStreamBaseResponseHandler streamHandler, boolean useManualDataWrites) {
        if (this.h2StreamManager != null) {
            return this.h2StreamManager.acquireStream(request, streamHandler, useManualDataWrites)
                    .thenApply(stream -> (HttpStreamBase) stream);
        } else {
            return this.h1StreamManager.acquireStream(request, streamHandler, useManualDataWrites)
                    .thenApply(stream -> (HttpStreamBase) stream);
        }
    }

    public CompletableFuture<Void> getShutdownCompleteFuture() {
        return shutdownComplete;
    }

    /**
     * @return concurrency metrics for the current manager
     */
    public HttpManagerMetrics getManagerMetrics() {
        if (this.h2StreamManager != null) {
            return this.h2StreamManager.getManagerMetrics();
        } else {
            return this.h1StreamManager.getManagerMetrics();
        }
    }

    /**
     * @return maximum number of connections this connection manager will pool
     */
    public int getMaxConnections() {
        if (this.h2StreamManager != null) {
            return this.h2StreamManager.getMaxConnections();
        } else {
            return this.h1StreamManager.getMaxConnections();
        }
    }

    /**
     * Abort an in-flight stream obtained from this manager. Cancels the HTTP exchange
     * and ensures the underlying connection/stream slot is properly released back to the
     * pool so it can be reused by new requests.
     *
     * <p>This is the correct way to cancel an in-flight request from outside the stream
     * callback lifecycle (e.g., on SDK timeout). For HTTP/1.1, the connection release is
     * guaranteed by the {@code AtomicBoolean} guard and {@code isNull()} check inside
     * {@code Http1StreamManager.acquireStream()} — calling {@code cancel()} triggers
     * {@code onResponseComplete} on activated streams, or the post-activate guard catches
     * streams closed before activation. For HTTP/2, the native layer handles stream slot
     * accounting internally.
     *
     * <p>Safe to call with null (e.g., if the stream was never acquired), after normal
     * completion, or multiple times.
     *
     * @param stream the stream to abort, or null
     */
    public void abortStream(HttpStreamBase stream) {
        if (stream != null && !stream.isNull()) {
            stream.cancel();
            stream.close();
        }
    }

    @Override
    public void close() {
        if (this.h1StreamManager != null) {
            this.h1StreamManager.close();
        }
        if (this.h2StreamManager != null) {
            this.h2StreamManager.close();
        }
    }
}
