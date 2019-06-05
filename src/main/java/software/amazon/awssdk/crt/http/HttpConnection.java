/*
 * Copyright 2010-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.awssdk.crt.http;

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContext;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static software.amazon.awssdk.crt.CRT.AWS_CRT_SUCCESS;


/**
 * This class wraps aws-c-http to provide the basic HTTP request/response functionality via the AWS Common Runtime.
 *
 * HttpConnection represents a single connection to a HTTP service endpoint.
 *
 * This class is not thread safe and should not be called from different threads.
 */
public class HttpConnection extends CrtResource {
    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static final int DEFAULT_HTTP_PORT = 80;
    private static final int DEFAULT_HTTPS_PORT = 443;
    private static final int DEFAULT_MAX_WINDOW_SIZE = Integer.MAX_VALUE;

    private final ClientBootstrap clientBootstrap;
    private final SocketOptions socketOptions;
    private final TlsContext tlsContext;
    private final int windowSize;
    private final URI uri;
    private final int port;
    private final boolean useTls;

    private final CompletableFuture<HttpConnection> connectedFuture;
    private final CompletableFuture<Void> shutdownFuture;

    /**
     * Creates a new CompletableFuture for a new HttpConnection.
     * @param uri Must be non-null and contain a hostname
     * @param bootstrap The ClientBootstrap to use for the Connection
     * @param socketOptions The SocketOptions to use for the Connection
     * @param tlsContext The TlsContext to use for the Connection
     * @return CompletableFuture indicating when the connection has completed
     * @throws CrtRuntimeException if Native threw a CrtRuntimeException
     */
    public static CompletableFuture<HttpConnection> createConnection(URI uri, ClientBootstrap bootstrap,
                                                                     SocketOptions socketOptions, TlsContext tlsContext) throws CrtRuntimeException {
        HttpConnection conn = new HttpConnection(uri, bootstrap, socketOptions, tlsContext, DEFAULT_MAX_WINDOW_SIZE);
        return conn.connect();
    }

    /**
     * Creates a new CompletableFuture for a new HttpConnection.
     * @param uri Must be non-null and contain a hostname
     * @param bootstrap The ClientBootstrap to use for the Connection
     * @param socketOptions The SocketOptions to use for the Connection
     * @param tlsContext The TlsContext to use for the Connection
     * @param windowSize The Initial Window size for requests made on this connection
     * @return CompletableFuture indicating when the connection has completed
     * @throws CrtRuntimeException if Native threw a CrtRuntimeException
     */
    public static CompletableFuture<HttpConnection> createConnection(URI uri, ClientBootstrap bootstrap,
                                                                     SocketOptions socketOptions,
                                                                     TlsContext tlsContext,
                                                                     int windowSize) throws CrtRuntimeException {
        HttpConnection conn = new HttpConnection(uri, bootstrap, socketOptions, tlsContext, windowSize);
        return conn.connect();
    }

    /**
     * Constructs a new HttpConnection.
     * @param uri Must be non-null and contain a hostname
     * @param bootstrap The ClientBootstrap to use for the Connection
     * @param socketOptions The SocketOptions to use for the Connection
     * @param tlsContext The TlsContext to use for the Connection
     */
    private HttpConnection(URI uri, ClientBootstrap bootstrap, SocketOptions socketOptions, TlsContext tlsContext, int windowSize) {
        if (uri == null) {  throw new IllegalArgumentException("URI must not be null"); }
        if (uri.getScheme() == null) { throw new IllegalArgumentException("URI does not have a Scheme"); }
        if (!HTTP.equals(uri.getScheme()) && !HTTPS.equals(uri.getScheme())) { throw new IllegalArgumentException("URI has unknown Scheme"); }
        if (uri.getHost() == null) { throw new IllegalArgumentException("URI does not have a Host name"); }
        if (bootstrap == null || bootstrap.isNull()) {  throw new IllegalArgumentException("ClientBootstrap must not be null"); }
        if (socketOptions == null || socketOptions.isNull()) { throw new IllegalArgumentException("SocketOptions must not be null"); }
        if (HTTPS.equals(uri.getScheme()) && tlsContext == null) { throw new IllegalArgumentException("TlsContext must not be null if https is used"); }
        if (windowSize <= 0) { throw new  IllegalArgumentException("Window Size must be greater than zero.");}

        int port = uri.getPort();

        /* Pick a default port based on the scheme if one wasn't set in the URI */
        if (port == -1) {
            if (HTTP.equals(uri.getScheme()))  { port = DEFAULT_HTTP_PORT; }
            if (HTTPS.equals(uri.getScheme())) { port = DEFAULT_HTTPS_PORT; }
        }

        if (port <= 0 || 65535 < port) {
            throw new IllegalArgumentException("Invalid or missing port: " + uri);
        }

        this.uri = uri;
        this.port = port;
        this.useTls = HTTPS.equals(uri.getScheme());
        this.clientBootstrap = bootstrap;
        this.socketOptions = socketOptions;
        this.tlsContext = tlsContext;
        this.windowSize = windowSize;
        this.connectedFuture = new CompletableFuture<>();
        this.shutdownFuture = new CompletableFuture<>();
    }

    /**
     * Schedules a connection task on the EventLoop to connect to the Http Endpoint
     * @return Future indicating when the connection has completed.
     */
    private CompletableFuture<HttpConnection> connect() throws CrtRuntimeException {
        if (!isNull()) {
            return connectedFuture;
        }

        acquire(httpConnectionNew(this,
                clientBootstrap.native_ptr(),
                socketOptions.native_ptr(),
                useTls ? tlsContext.native_ptr() : 0,
                windowSize,
                uri.getHost(),
                port));

        return connectedFuture;
    }

    /**
     * Schedules an HttpRequest on the Native EventLoop for this HttpConnection.
     *
     * @param request The Request to make to the Server.
     * @param streamHandler The Stream Handler to be called from the Native EventLoop
     * @throws CrtRuntimeException
     * @return The HttpStream that represents this Request/Response Pair. It can be closed at any time during the
     *          request/response, but must be closed by the user thread making this request when it's done.
     */
    public HttpStream makeRequest(HttpRequest request, CrtHttpStreamHandler streamHandler) throws CrtRuntimeException {
        if (isShutdownComplete() || isNull()) {
            throw new IllegalStateException("HttpConnection has been shut down, can't make requests on it.");
        }

        HttpStream stream = httpConnectionMakeRequest(native_ptr(),
                request.getMethod(),
                request.getEncodedPath(),
                request.getHeaders(),
                streamHandler);

        if (stream == null || stream.isNull()) {
            throw new IllegalStateException("HttpStream is null");
        }

        return stream;
    }

    /**
     * Closes and frees this HttpConnection and any native sub-resources associated with this connection
     */
    @Override
    public void close() {
        if (didConnectSuccessfully() && !isShutdownComplete()) {
            /**
             * We have to wait for the connection to finish shutting down to avoid race conditions between
             * shutdown tasks and memory release tasks.
             *
             * The httpConnectionShutdown() call schedules shutdown tasks on the Native EventLoop that may send
             * HTTP/TLS/TCP shutdown messages to peers if necessary and will eventually cause internal connection
             * memory to stop being accessed.
             *
             * The httpConnectionRelease() call will begin releasing internal connection memory. If the shutdown isn't
             * complete before httpConnectionRelease(), it can lead to the shutdown tasks accessing memory that's been
             * released, resulting in Segfaults.
             */
            try {
                // Give Shutdown 10 seconds to complete, otherwise throw a Timeout Exception
                this.shutdown().get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (!isNull()) {
            try {
                /**
                 * FIXME: The above shutdown().get() should be enough to avoid race conditions, but aws-c-http has a
                 * bug in the way it orders it's shutdown callbacks. Add an artificial sleep here to avoid Race
                 * Condition with the EventLoop when shutting down the TLS Connection.
                 *
                 * Tracking Issue: https://github.com/awslabs/aws-c-http/issues/66
                 * TraceLog: https://gist.github.com/alexw91/e6205fd38ecc530a55b956c98ca189dc
                 */
                Thread.sleep(1000);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            httpConnectionRelease(release());
        }

        super.close();
    }

    /** Called from Native EventLoop when the connection is established the first time **/
    private void onConnectionComplete(int errorCode) {
        if (errorCode == AWS_CRT_SUCCESS) {
            connectedFuture.complete(this);
        } else {
            /* The user can't close this HttpConnection object since they never got a reference to it through the
                CompletableFuture, so we need to close ourselves on Connection Error. */
            this.close();
            connectedFuture.completeExceptionally(new HttpException(errorCode));
        }
    }

    /** Called from Native EventLoop when the connection is shutdown. **/
    private void onConnectionShutdown(int errorCode) {
        if (errorCode == AWS_CRT_SUCCESS) {
           shutdownFuture.complete(null);
        } else {
            shutdownFuture.completeExceptionally(new HttpException(errorCode));
        }
    }

    /**
     * Returns the Shutdown Future
     * @return CompletableFuture that will be completed when the connection is shut down.
     */
    public CompletableFuture<Void> getShutdownFuture() {
        return shutdownFuture;
    }

    private boolean didConnectSuccessfully() {
        return connectedFuture.isDone() && !connectedFuture.isCompletedExceptionally();
    }


    private boolean isShutdownComplete() {
        return shutdownFuture.isDone();
    }

    /**
     * Schedules a task on the Native EventLoop to shut down the current connection
     * @return When this future completes, the shutdown is complete
     */
    public CompletableFuture<Void> shutdown() {
        if (isNull()) {
           return shutdownFuture;
        }
        try {
            httpConnectionShutdown(native_ptr());
        } catch (CrtRuntimeException e) {
            shutdownFuture.completeExceptionally(e);
        }

        return shutdownFuture;
    }

    /*******************************************************************************
     * Native methods
     ******************************************************************************/
    private static native long httpConnectionNew(HttpConnection thisObj,
                                                 long client_bootstrap,
                                                 long socketOptions,
                                                 long tlsContext,
                                                 int windowSize,
                                                 String endpoint,
                                                 int port) throws CrtRuntimeException;

    private static native void httpConnectionShutdown(long connection) throws CrtRuntimeException;
    private static native void httpConnectionRelease(long connection);

    private static native HttpStream httpConnectionMakeRequest(long connection,
                                                               String method,
                                                               String uri,
                                                               HttpHeader[] headers,
                                                               CrtHttpStreamHandler crtHttpStreamHandler) throws CrtRuntimeException;
}
