/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.http;

import software.amazon.awssdk.crt.CrtRuntimeException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages a pool for either HTTP/1.1 or HTTP/2 connection.
 *
 * Contains two stream manager for two protocols under the hood.
 */
public class HttpStreamManager implements AutoCloseable {

    private Http1StreamManager h1StreamManager = null;
    private Http2StreamManager h2StreamManager = null;
    private CompletableFuture<Void> shutdownComplete = null;
    private AtomicLong shutdownNum = new AtomicLong(0);
    private Throwable shutdownCompleteException = null;

    /**
     * Factory function for HttpStreamManager instances
     *
     * @param options configuration options
     * @return a new instance of an HttpStreamManager
     */
    public static HttpStreamManager create(HttpStreamManagerOptions options) {
        return new HttpStreamManager(options);
    }

    private HttpStreamManager(HttpStreamManagerOptions options) {
        this.shutdownComplete = new CompletableFuture<Void>();
        if (options.getExpectedProtocol() == HttpVersion.UNKNOWN) {
            this.h1StreamManager = Http1StreamManager.create(options.getHTTP1ConnectionManagerOptions());
            this.h2StreamManager = Http2StreamManager.create(options.getHTTP2StreamManagerOptions());
        } else {
            if (options.getExpectedProtocol() == HttpVersion.HTTP_2) {
                this.h2StreamManager = Http2StreamManager.create(options.getHTTP2StreamManagerOptions());
            } else {
                this.h1StreamManager = Http1StreamManager.create(options.getHTTP1ConnectionManagerOptions());
            }
            /* Only one manager created. */
            this.shutdownNum.addAndGet(1);
        }
        if (this.h1StreamManager != null) {
            this.h1StreamManager.getShutdownCompleteFuture().whenComplete((v, throwable) -> {
                if (throwable != null) {
                    this.shutdownCompleteException = throwable;
                }
                long shutdownNum = this.shutdownNum.addAndGet(1);
                if (shutdownNum == 2) {
                    /* both connectionManager and the h2StreamManager has been shutdown. */
                    if (this.shutdownCompleteException != null) {
                        this.shutdownComplete.completeExceptionally(this.shutdownCompleteException);
                    } else {
                        this.shutdownComplete.complete(null);
                    }
                }
            });
        }
        if (this.h2StreamManager != null) {
            this.h2StreamManager.getShutdownCompleteFuture().whenComplete((v, throwable) -> {
                if (throwable != null) {
                    this.shutdownCompleteException = throwable;
                }
                long shutdownNum = this.shutdownNum.addAndGet(1);
                if (shutdownNum == 2) {
                    /* both connectionManager and the h2StreamManager has been shutdown. */
                    if (this.shutdownCompleteException != null) {
                        this.shutdownComplete.completeExceptionally(this.shutdownCompleteException);
                    } else {
                        this.shutdownComplete.complete(null);
                    }
                }
            });
        }
    }

    private void h1AcquireStream(HttpRequestBase request,
            HttpStreamBaseResponseHandler streamHandler, CompletableFuture<HttpStreamBase> completionFuture) {

        this.h1StreamManager.acquireStream(request, streamHandler).whenComplete((stream, throwable) -> {
            if (throwable != null) {
                completionFuture.completeExceptionally(throwable);
            } else {
                completionFuture.complete(stream);
            }
        });
    }

    /**
     * Request a HttpStream from StreamManager. If the streamManager is made with
     * HTTP/2 connection under the hood, it will be Http2Stream.
     *
     * @param request           HttpRequestBase. The Request to make to the Server.
     * @param streamHandler     HttpStreamBaseResponseHandler. The Stream Handler to be called from the Native EventLoop
     * @return A future for a Http2Stream that will be completed when the stream is
     *         acquired.
     */
    public CompletableFuture<HttpStreamBase> acquireStream(HttpRequestBase request,
            HttpStreamBaseResponseHandler streamHandler) {
        CompletableFuture<HttpStreamBase> completionFuture = new CompletableFuture<>();
        if (this.h2StreamManager != null) {
            this.h2StreamManager.acquireStream(request, streamHandler).whenComplete((stream, throwable) -> {
                if (throwable != null) {
                    if (throwable instanceof CrtRuntimeException) {
                        CrtRuntimeException exception = (CrtRuntimeException) throwable;
                        if (exception.errorName.equals("AWS_ERROR_HTTP_STREAM_MANAGER_UNEXPECTED_HTTP_VERSION")  && this.h1StreamManager != null) {
                            this.h1AcquireStream(request, streamHandler, completionFuture);
                        } else {
                            completionFuture.completeExceptionally(throwable);
                        }
                    } else {
                        completionFuture.completeExceptionally(throwable);
                    }
                } else {
                    completionFuture.complete((Http2Stream) stream);
                }
            });
            return completionFuture;
        }
        this.h1AcquireStream(request, streamHandler, completionFuture);
        return completionFuture;
    }

    public CompletableFuture<Void> getShutdownCompleteFuture() {
        return shutdownComplete;
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
