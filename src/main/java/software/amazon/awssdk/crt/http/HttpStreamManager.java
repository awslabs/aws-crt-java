/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.http;

import software.amazon.awssdk.crt.CrtRuntimeException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages a unified pool for HTTP/1.1 and/or HTTP/2 connections with automatic protocol negotiation.
 *
 * <p>This class provides a high-level abstraction over HTTP connection management that can handle
 * both HTTP/1.1 and HTTP/2 protocols. Depending on the configuration, it maintains one or both
 * protocol-specific stream managers internally:
 * <ul>
 *   <li>{@link Http1StreamManager} - Manages HTTP/1.1 connections</li>
 *   <li>{@link Http2StreamManager} - Manages HTTP/2 connections</li>
 * </ul>
 *
 * <h3>Protocol Negotiation Modes</h3>
 * The manager supports three operating modes configured via {@link HttpStreamManagerOptions#withExpectedProtocol(HttpVersion)}:
 * <ul>
 *   <li><b>UNKNOWN</b> (default): Attempts HTTP/2 first, automatically falls back to HTTP/1.1 if the server
 *       does not support HTTP/2. Both managers are created to support this fallback behavior.</li>
 *   <li><b>HTTP_2</b>: Only uses HTTP/2. Only the HTTP/2 manager is created. Requests will fail if the
 *       server does not support HTTP/2.</li>
 *   <li><b>HTTP_1_1/HTTP_1_0</b>: Only uses HTTP/1.1. Only the HTTP/1.1 manager is created.</li>
 * </ul>
 *
 * @see HttpStreamManagerOptions
 * @see Http1StreamManager
 * @see Http2StreamManager
 */
public class HttpStreamManager implements AutoCloseable {

    private Http1StreamManager h1StreamManager = null;
    private Http2StreamManager h2StreamManager = null;
    private CompletableFuture<Void> shutdownComplete = null;
    private AtomicLong shutdownNum = new AtomicLong(0);
    private Throwable shutdownCompleteException = null;

    /**
     * Factory function for HttpStreamManager instances.
     *
     * @param options Configuration options that determine protocol behavior and connection settings.
     *                Must not be null.
     * @return A new instance of an HttpStreamManager configured according to the provided options.
     */
    public static HttpStreamManager create(HttpStreamManagerOptions options) {
        return new HttpStreamManager(options);
    }

    /**
     * Constructs a new HttpStreamManager with the specified options.
     *
     * <p>Based on the expected protocol in the options, this constructor initializes one or both
     * protocol-specific managers:
     * <ul>
     *   <li>If expectedProtocol is {@link HttpVersion#UNKNOWN}: Creates both HTTP/1.1 and HTTP/2 managers
     *       to enable automatic fallback from HTTP/2 to HTTP/1.1</li>
     *   <li>If expectedProtocol is {@link HttpVersion#HTTP_2}: Creates only the HTTP/2 manager</li>
     *   <li>If expectedProtocol is {@link HttpVersion#HTTP_1_1} or {@link HttpVersion#HTTP_1_0}: Creates only
     *       the HTTP/1.1 manager</li>
     * </ul>
     *
     * <p>The constructor also sets up shutdown handlers for each created manager. The shutdown counter
     * ensures that the overall shutdown is only complete when all active managers have shut down.
     * The counter starts at 0 if both managers are created, or 1 if only one manager is created,
     * and completes when it reaches 2.
     *
     * @param options Configuration options containing protocol preferences and manager-specific settings.
     *                The options must include valid configurations for any managers that will be created
     *                based on the expectedProtocol setting.
     */
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

    /**
     * Helper method to acquire an HTTP/1.1 stream and complete the provided future.
     *
     * <p>This private method encapsulates the logic for acquiring a stream from the HTTP/1.1
     * manager and bridging the result to a CompletableFuture. It is used both as the primary
     * acquisition path when only HTTP/1.1 is configured, and as a fallback when HTTP/2
     * acquisition fails due to protocol mismatch.
     *
     * @param request The HTTP request to make to the server.
     * @param streamHandler The response handler for the stream.
     * @param completionFuture The future to complete with the acquired stream or exception.
     */
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
     * Acquires an HTTP stream from the manager to make a request to the server.
     *
     * <p>This method handles protocol negotiation and fallback based on the manager's configuration:
     * <ul>
     *   <li>If configured for HTTP/2 only: Acquires an {@link Http2Stream}</li>
     *   <li>If configured for HTTP/1.1 only: Acquires an HTTP/1.1 stream</li>
     *   <li>If configured with UNKNOWN protocol (default): Attempts HTTP/2 first. If the server
     *       responds with HTTP/1.1 (indicated by an AWS_ERROR_HTTP_STREAM_MANAGER_UNEXPECTED_HTTP_VERSION
     *       error), automatically falls back to acquiring an HTTP/1.1 stream.</li>
     * </ul>
     *
     * <p>When both HTTP/2 and HTTP/1.1 managers are active, this method automatically handles
     * protocol version mismatch. If the HTTP/2 manager detects that the server only supports
     * HTTP/1.1, the request is transparently retried using the HTTP/1.1 manager. This ensures
     * seamless operation regardless of the server's capabilities.
     *
     * @param request The HTTP request to make to the server.
     * @param streamHandler The response handler that will be called from the native event loop
     *                      when response data arrives.
     * @return A CompletableFuture that completes with an {@link HttpStreamBase} when the stream
     *         has been successfully acquired from the pool. The actual type will be {@link Http2Stream}
     *         or an HTTP/1.1 stream depending on protocol negotiation.
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
                    completionFuture.complete(stream);
                }
            });
            return completionFuture;
        }
        this.h1AcquireStream(request, streamHandler, completionFuture);
        return completionFuture;
    }

    /**
     * Returns a future that will be completed when the manager has fully shut down.
     *
     * <p>This future is completed after {@link #close()} has been called and all underlying
     * protocol-specific managers have completed their shutdown process. The shutdown process
     * is asynchronous, so calling {@link #close()} returns immediately while cleanup continues
     * in the background.
     *
     * @return A CompletableFuture that completes when all underlying managers have shut down.
     */
    public CompletableFuture<Void> getShutdownCompleteFuture() {
        return shutdownComplete;
    }

    /**
     * Closes the stream manager and initiates shutdown of all underlying connection managers.
     *
     * <p>Calling close() begins an asynchronous shutdown process for all active protocol-specific
     * managers (HTTP/1.1 and/or HTTP/2). This method returns immediately while shutdown continues
     * in the background. To wait for complete shutdown, use {@link #getShutdownCompleteFuture()}.
     */
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
