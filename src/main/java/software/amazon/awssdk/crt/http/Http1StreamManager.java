/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.http;

import software.amazon.awssdk.crt.CrtRuntimeException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages a Pool of HTTP/1.1 Streams. Creates and manages HTTP/1.1 connections
 * under the hood. Will grab a connection from HttpClientConnectionManager to
 * make request on it, and will return it back until the request finishes.
 */
public class Http1StreamManager implements AutoCloseable {

    private HttpClientConnectionManager connectionManager = null;

    /**
     * Factory function for Http1StreamManager instances
     *
     * @param options the connection manager options configure to connection manager under the hood
     * @return a new instance of an Http1StreamManager
     */
    public static Http1StreamManager create(HttpClientConnectionManagerOptions options) {
        return new Http1StreamManager(options);
    }

    private Http1StreamManager(HttpClientConnectionManagerOptions options) {
        this.connectionManager = HttpClientConnectionManager.create(options);
    }

    public CompletableFuture<Void> getShutdownCompleteFuture() {
        return this.connectionManager.getShutdownCompleteFuture();
    }

    /**
     * Request an HTTP/1.1 HttpStream from StreamManager.
     *
     * @param request         HttpRequest. The Request to make to the Server.
     * @param streamHandler   HttpStreamBaseResponseHandler. The Stream Handler to be called from the Native EventLoop
     * @return A future for a HttpStream that will be completed when the stream is
     *         acquired.
     * @throws CrtRuntimeException Exception happens from acquiring stream.
     */
    public CompletableFuture<HttpStream> acquireStream(HttpRequest request,
            HttpStreamBaseResponseHandler streamHandler) {
        return this.acquireStream((HttpRequestBase) request, streamHandler);
    }

    /**
     * Request an HTTP/1.1 HttpStream from StreamManager.
     *
     * @param request         HttpRequest. The Request to make to the Server.
     * @param streamHandler   HttpStreamBaseResponseHandler. The Stream Handler to be called from the Native EventLoop
     * @param useManualDataWrites A boolean variable to signal that body will be streamed using async writes.
     * @return A future for a HttpStream that will be completed when the stream is
     *         acquired.
     * @throws CrtRuntimeException Exception happens from acquiring stream.
     */
    public CompletableFuture<HttpStream> acquireStream(HttpRequest request,
            HttpStreamBaseResponseHandler streamHandler, boolean useManualDataWrites) {
        return this.acquireStream((HttpRequestBase) request, streamHandler, useManualDataWrites);
    }

    /**
     * Request an HTTP/1.1 HttpStream from StreamManager.
     *
     * @param request         HttpRequestBase. The Request to make to the Server.
     * @param streamHandler   HttpStreamBaseResponseHandler. The Stream Handler to be called from the Native EventLoop
     * @return A future for a HttpStream that will be completed when the stream is
     *         acquired.
     * @throws CrtRuntimeException Exception happens from acquiring stream.
     */
    public CompletableFuture<HttpStream> acquireStream(HttpRequestBase request,
            HttpStreamBaseResponseHandler streamHandler) {
        return this.acquireStream(request, streamHandler, false); // overloading to ensure backward-compatibility
    }

    /**
     * Request an HTTP/1.1 HttpStream from StreamManager.
     *
     * <p>The returned future completes with an {@link HttpStream} that will be activated immediately
     * after the future is completed. The stream's response callbacks (onResponseHeaders, etc.) may
     * begin firing as soon as activate is called.
     *
     * <p><b>Important:</b> Operations on the stream (such as {@code cancel()}) must only be invoked
     * after {@code activate()} has been called. If {@code cancel()} is called before activate, it
     * is a no-op — the stream will still proceed normally once activate runs, which may lead to
     * unexpected behavior. Since there is a brief window between future completion and activation,
     * callers that need to perform operations on the stream (from a future callback such as
     * {@code thenAccept} or {@code whenComplete}) should call {@code stream.activate()} first.
     * {@code activate()} is idempotent — calling it multiple times is safe and has no effect after
     * the first successful call.
     *
     * @param request         HttpRequestBase. The Request to make to the Server.
     * @param streamHandler   HttpStreamBaseResponseHandler. The Stream Handler to be called from the Native EventLoop
     * @param useManualDataWrites A boolean variable to signal that body will be streamed using async writes.
     * @return A future for a HttpStream that will be completed when the stream is
     *         acquired and will be activated immediately after.
     * @throws CrtRuntimeException Exception happens from acquiring stream.
     */
    public CompletableFuture<HttpStream> acquireStream(HttpRequestBase request,
            HttpStreamBaseResponseHandler streamHandler, boolean useManualDataWrites) {
        CompletableFuture<HttpStream> completionFuture = new CompletableFuture<>();
        HttpClientConnectionManager connManager = this.connectionManager;

        connManager.acquireConnection().whenComplete((conn, throwable) -> {
            if (throwable != null) {
                completionFuture.completeExceptionally(throwable);
            } else {
                // Guard ensures the connection is released exactly once, regardless of
                // whether release happens via onResponseComplete, activate failure, or
                // external cancellation (e.g., SDK timeout calling cancel+close).
                AtomicBoolean connectionReleased = new AtomicBoolean(false);

                try {
                    HttpStreamBase stream = conn.makeRequest(request, new HttpStreamBaseResponseHandler() {
                        @Override
                        public void onResponseHeaders(HttpStreamBase stream, int responseStatusCode, int blockType,
                                HttpHeader[] nextHeaders) {
                            streamHandler.onResponseHeaders(stream, responseStatusCode, blockType, nextHeaders);
                        }

                        @Override
                        public void onResponseHeadersDone(HttpStreamBase stream, int blockType) {
                            streamHandler.onResponseHeadersDone(stream, blockType);
                        }

                        @Override
                        public int onResponseBody(HttpStreamBase stream, byte[] bodyBytesIn) {
                            return streamHandler.onResponseBody(stream, bodyBytesIn);
                        }

                        @Override
                        public void onResponseComplete(HttpStreamBase stream, int errorCode) {
                            streamHandler.onResponseComplete(stream, errorCode);
                            /* Release the connection back (at most once) */
                            if (connectionReleased.compareAndSet(false, true)) {
                                connManager.releaseConnection(conn);
                            }
                        }
                    }, useManualDataWrites);
                    /* Complete the future before activate. We cannot guarantee this callback
                     * runs on the connection's event-loop thread (CompletableFuture.whenComplete
                     * runs on the calling thread if the future is already completed when whenComplete
                     * is attached). If we activated first, stream callbacks could fire on the
                     * event-loop thread before the caller has the stream handle — a race condition.
                     * By completing first, the caller has the handle before callbacks begin. */
                    completionFuture.complete((HttpStream) stream);
                    try {
                        stream.activate();
                    } catch (CrtRuntimeException e) {
                        /* If activate failed, complete callback will not be invoked */
                        streamHandler.onResponseComplete(stream, e.errorCode);
                        if (connectionReleased.compareAndSet(false, true)) {
                            connManager.releaseConnection(conn);
                        }
                        completionFuture.completeExceptionally(e);
                        return;
                    }
                } catch (Exception ex) {
                    if (connectionReleased.compareAndSet(false, true)) {
                        connManager.releaseConnection(conn);
                    }
                    completionFuture.completeExceptionally(ex);
                }
            }
        });
        return completionFuture;
    }

    /**
     * @return concurrency metrics for the current manager
     */
    public HttpManagerMetrics getManagerMetrics() {
        return this.connectionManager.getManagerMetrics();
    }

    /**
     * @return maximum number of connections this manager will pool
     */
    public int getMaxConnections() {
        return this.connectionManager.getMaxConnections();
    }

    @Override
    public void close() {
        this.connectionManager.close();
    }
}
