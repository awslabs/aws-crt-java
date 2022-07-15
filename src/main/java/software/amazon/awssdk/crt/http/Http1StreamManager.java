package software.amazon.awssdk.crt.http;

import software.amazon.awssdk.crt.CrtRuntimeException;

import java.util.concurrent.CompletableFuture;

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
     * @param streamHandler   HttpStreamResponseHandler. The Stream Handler to be called from the Native EventLoop
     * @return A future for a HttpStream that will be completed when the stream is
     *         acquired.
     * @throws CrtRuntimeException Exception happens from acquiring stream.
     */
    public CompletableFuture<HttpStream> acquireStream(HttpRequest request,
            HttpStreamResponseHandler streamHandler) {
        CompletableFuture<HttpStream> completionFuture = new CompletableFuture<>();
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


    /**
     * Request an HTTP/1.1 HttpStream from StreamManager.
     *
     * @param request         HttpRequestBase. The Request to make to the Server.
     * @param streamHandler   HttpStreamBaseResponseHandler. The Stream Handler to be called from the Native EventLoop
     * @return A future for a HttpStreamBase that will be completed when the stream is
     *         acquired.
     * @throws CrtRuntimeException Exception happens from acquiring stream.
     */
    public CompletableFuture<HttpStreamBase> acquireStream(HttpRequestBase request,
            HttpStreamBaseResponseHandler streamHandler) {
        CompletableFuture<HttpStreamBase> completionFuture = new CompletableFuture<>();
        HttpClientConnectionManager connManager = this.connectionManager;
        this.connectionManager.acquireConnection().whenComplete((conn, throwable) -> {
            if (throwable != null) {
                completionFuture.completeExceptionally(throwable);
            } else {
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

    @Override
    public void close() {
        this.connectionManager.close();
    }
}
