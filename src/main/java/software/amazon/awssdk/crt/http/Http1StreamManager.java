/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.http;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtRuntimeException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages a Pool of HTTP/1.1 Streams. Creates and manages HTTP/1.1 connections
 * under the hood. Will grab a connection from HttpClientConnectionManager to
 * make request on it, and will return it back until the request finishes.
 */
public class Http1StreamManager implements AutoCloseable {

    private HttpClientConnectionManager connectionManager = null;

    /**
     * Holds the per-acquire context needed by the onConnectionAcquired callback.
     */
    private static class PendingAcquire {
        final HttpClientConnectionManager connManager;
        final HttpRequestBase request;
        final HttpStreamBaseResponseHandler streamHandler;
        final boolean useManualDataWrites;

        PendingAcquire(HttpClientConnectionManager connManager, HttpRequestBase request,
                HttpStreamBaseResponseHandler streamHandler, boolean useManualDataWrites) {
            this.connManager = connManager;
            this.request = request;
            this.streamHandler = streamHandler;
            this.useManualDataWrites = useManualDataWrites;
        }
    }

    private static final ConcurrentHashMap<CompletableFuture<HttpStream>, PendingAcquire> pendingAcquires =
            new ConcurrentHashMap<>();

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

    public CompletableFuture<HttpStream> acquireStream(HttpRequest request,
            HttpStreamBaseResponseHandler streamHandler) {
        return this.acquireStream((HttpRequestBase) request, streamHandler);
    }

    public CompletableFuture<HttpStream> acquireStream(HttpRequest request,
            HttpStreamBaseResponseHandler streamHandler, boolean useManualDataWrites) {
        return this.acquireStream((HttpRequestBase) request, streamHandler, useManualDataWrites);
    }

    public CompletableFuture<HttpStream> acquireStream(HttpRequestBase request,
            HttpStreamBaseResponseHandler streamHandler) {
        return this.acquireStream(request, streamHandler, false);
    }

    /**
     * Request an HTTP/1.1 HttpStream from StreamManager.
     * The native layer acquires a connection and invokes onConnectionAcquired() on the
     * connection's event-loop thread, passing through the request and handler objects.
     * The Java callback performs makeRequest + activate + future.complete() — all
     * guaranteed on the event-loop thread.
     */
    public CompletableFuture<HttpStream> acquireStream(HttpRequestBase request,
            HttpStreamBaseResponseHandler streamHandler, boolean useManualDataWrites) {
        CompletableFuture<HttpStream> completionFuture = new CompletableFuture<>();
        HttpClientConnectionManager connManager = this.connectionManager;

        try {
            HttpClientConnectionManager.httpClientConnectionManagerAcquireStream(
                    connManager.getNativeHandle(),
                    completionFuture,
                    request,
                    streamHandler,
                    useManualDataWrites);
        } catch (CrtRuntimeException ex) {
            completionFuture.completeExceptionally(ex);
        }
        return completionFuture;
    }

    /**
     * Called from native (on the connection's event-loop thread) when a connection is acquired.
     * All parameters are passed through from C — no Java-side state/lookup needed.
     *
     * Performs makeRequest + activate + future.complete() — all on the event-loop thread,
     * so no stream callbacks can fire until this method returns to C.
     */
    private static void onConnectionAcquired(
            CompletableFuture<HttpStream> acquireFuture,
            long nativeConnectionBinding,
            long nativeConnManager,
            int errorCode,
            HttpRequestBase request,
            HttpStreamBaseResponseHandler streamHandler,
            boolean useManualDataWrites) {

        if (errorCode != CRT.AWS_CRT_SUCCESS) {
            acquireFuture.completeExceptionally(new HttpException(errorCode));
            return;
        }

        /* We are on the connection's event-loop thread. */
        HttpClientConnection conn = new HttpClientConnection(nativeConnectionBinding);

        try {
            HttpStreamBase stream = conn.makeRequest(request, streamHandler, useManualDataWrites);

            /* Activate — safe because we're on the event-loop thread. */
            stream.activate();
            acquireFuture.complete((HttpStream) stream);
        } catch (Exception ex) {
            conn.close();
            acquireFuture.completeExceptionally(ex);
        }
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
