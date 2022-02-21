/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.http;

import software.amazon.awssdk.crt.CrtRuntimeException;

import java.util.concurrent.CompletableFuture;

/**
 * Manages a pool for either HTTP/1.1 or HTTP/2 connection.
 * For HTTP/1.1, it will grab a connection from HttpClientConnectionManager to
 * make request on it, and will return it back until the request finishes.
 * For HTTP/2, it will use Http2StreamManager under the hood.
 */
public class HttpStreamManager implements AutoCloseable {

    private HttpClientConnectionManager connectionManager = null;
    private Http2StreamManager h2StreamManager = null;
    private Exception initException = null;
    private CompletableFuture<Void> shutdownComplete = null;

    /**
     * Factory function for HttpStreamManager instances
     *
     * @param options configuration options
     * @return a new instance of an HttpStreamManager
     */
    public static HttpStreamManager create(HttpStreamManagerOptions options) {
        return new HttpStreamManager(options);
    }

    private HttpClientConnectionManagerOptions getConnManagerOptFromStreamManagerOpt(HttpStreamManagerOptions options) {

        HttpClientConnectionManagerOptions connManagerOptions = new HttpClientConnectionManagerOptions();
        connManagerOptions.withClientBootstrap(options.getClientBootstrap())
                .withSocketOptions(options.getSocketOptions())
                .withTlsContext(options.getTlsContext())
                .withWindowSize(options.getWindowSize())
                .withUri(options.getUri())
                .withMaxConnections(options.getMaxConnections())
                .withPort(options.getPort())
                .withProxyOptions(options.getProxyOptions())
                .withManualWindowManagement(options.isManualWindowManagement())
                .withMonitoringOptions(options.getMonitoringOptions());
        return connManagerOptions;
    }

    private HttpStreamManager(HttpStreamManagerOptions options) {
        HttpClientConnectionManagerOptions connManagerOptions = getConnManagerOptFromStreamManagerOpt(options);
        this.connectionManager = HttpClientConnectionManager.create(connManagerOptions);
        try (HttpClientConnection conn = this.connectionManager.acquireConnection().get()) {
            if (conn.getVersion() == HttpVersion.HTTP_2) {
                /*
                 * Create Http2StreamManager and clean up connection manager later as we don't
                 * need it anymore
                 */
                this.h2StreamManager = Http2StreamManager.create(options);
                this.connectionManager.releaseConnection(conn);
                this.connectionManager.close();
                this.connectionManager = null;
                this.shutdownComplete = this.h2StreamManager.getShutdownCompleteFuture();
            } else {
                this.connectionManager.releaseConnection(conn);
                this.shutdownComplete = this.connectionManager.getShutdownCompleteFuture();
            }
        } catch (Exception ex) {
            this.initException = ex;
        }
    }

    /**
     * Get the protocol version the manager runs on.
     */
    public HttpVersion getHttpVersion() {
        if (this.h2StreamManager != null) {
            return HttpVersion.HTTP_2;
        }
        return HttpVersion.HTTP_1_1;
    }

    /**
     * Request a HttpStream from StreamManager. If the streamManager is made with
     * HTTP/2 connection under the hood, it will be Http2Stream.
     *
     * @param request
     * @param streamHandler
     * @return A future for a Http2Stream that will be completed when the stream is
     *         acquired.
     * @throws CrtRuntimeException
     */
    public CompletableFuture<HttpStream> acquireStream(Http2Request request,
            HttpStreamResponseHandler streamHandler) {
        return this.acquireStream((HttpRequestBase) request, streamHandler);
    }

    public CompletableFuture<HttpStream> acquireStream(HttpRequest request,
            HttpStreamResponseHandler streamHandler) {
        return this.acquireStream((HttpRequestBase) request, streamHandler);
    }

    public CompletableFuture<HttpStream> acquireStream(HttpRequestBase request,
            HttpStreamResponseHandler streamHandler) {
        CompletableFuture<HttpStream> completionFuture = new CompletableFuture<>();

        if (this.initException != null) {
            completionFuture.completeExceptionally(this.initException);
            return completionFuture;
        }
        if (this.h2StreamManager != null) {
            this.h2StreamManager.acquireStream(request, streamHandler).whenComplete((stream, throwable) -> {
                if (throwable != null) {
                    completionFuture.completeExceptionally(throwable);
                } else {
                    completionFuture.complete((HttpStream) stream);
                }
            });
            return completionFuture;
        }
        HttpClientConnectionManager connManager = this.connectionManager;
        this.connectionManager.acquireConnection().whenComplete((conn, throwable) -> {
            if (throwable != null) {
                completionFuture.completeExceptionally(throwable);
            } else {
                try {
                    HttpStream stream = conn.makeRequest(request, new HttpStreamResponseHandler() {
                        @Override
                        public void onResponseHeaders(HttpStream stream, int responseStatusCode, int blockType,
                                HttpHeader[] nextHeaders) {
                            streamHandler.onResponseHeaders(stream, responseStatusCode, blockType, nextHeaders);
                        }

                        @Override
                        public void onResponseHeadersDone(HttpStream stream, int blockType) {
                            streamHandler.onResponseHeadersDone(stream, blockType);
                        }

                        @Override
                        public int onResponseBody(HttpStream stream, byte[] bodyBytesIn) {
                            return streamHandler.onResponseBody(stream, bodyBytesIn);
                        }

                        @Override
                        public void onResponseComplete(HttpStream stream, int errorCode) {
                            streamHandler.onResponseComplete(stream, errorCode);
                            /* Release the connection back */
                            connManager.releaseConnection(conn);
                        }
                    });
                    completionFuture.complete(stream);
                    /* Active the stream for user */
                    try {
                        stream.activate();
                    } catch (CrtRuntimeException e) {
                        /* If activate failed, complete callback will not be invoked */
                        streamHandler.onResponseComplete(stream, e.errorCode);
                        /* Release the connection back */
                        connManager.releaseConnection(conn);
                    }
                } catch (Exception ex) {
                    connManager.releaseConnection(conn);
                    completionFuture.completeExceptionally(ex);
                }
            }
        });
        return completionFuture;
    }

    public CompletableFuture<Void> getShutdownCompleteFuture() {
        return shutdownComplete;
    }

    @Override
    public void close() {
        if (this.connectionManager != null) {
            this.connectionManager.close();
        }
        if (this.h2StreamManager != null) {
            this.h2StreamManager.close();
        }
    }
}
