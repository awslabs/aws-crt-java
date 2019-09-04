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

import java.net.URI;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContext;

/**
 * Manages a Pool of Http Connections
 */
public class HttpConnectionPoolManager extends CrtResource {
    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static final int DEFAULT_HTTP_PORT = 80;
    private static final int DEFAULT_HTTPS_PORT = 443;

    private final ClientBootstrap clientBootstrap;
    private final SocketOptions socketOptions;
    private final TlsContext tlsContext;
    private final int windowSize;
    private final URI uri;
    private final int port;
    private final int maxConnections;
    private final HttpProxyOptions proxyOptions;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    /**
     * The queue of Connection Acquisition requests.
     */
    private final Queue<CompletableFuture<HttpConnection>> connectionAcquisitionRequests = new ConcurrentLinkedQueue<>();

    public static HttpConnectionPoolManager create(HttpConnectionPoolManagerOptions options) {
        return new HttpConnectionPoolManager(options);
    }

    private HttpConnectionPoolManager(HttpConnectionPoolManagerOptions options) {

        URI uri = options.getUri();
        if (uri == null) {  throw new IllegalArgumentException("URI must not be null"); }
        if (uri.getScheme() == null) { throw new IllegalArgumentException("URI does not have a Scheme"); }
        if (!HTTP.equals(uri.getScheme()) && !HTTPS.equals(uri.getScheme())) { throw new IllegalArgumentException("URI has unknown Scheme"); }
        if (uri.getHost() == null) { throw new IllegalArgumentException("URI does not have a Host name"); }

        ClientBootstrap clientBootstrap = options.getClientBootstrap();
        if (clientBootstrap == null || clientBootstrap.isNull()) {  throw new IllegalArgumentException("ClientBootstrap must not be null"); }

        SocketOptions socketOptions = options.getSocketOptions();
        if (socketOptions == null || socketOptions.isNull()) { throw new IllegalArgumentException("SocketOptions must not be null"); }

        boolean useTls = HTTPS.equals(uri.getScheme());
        if (useTls && options.getTlsContext() == null) { throw new IllegalArgumentException("TlsContext must not be null if https is used"); }

        int windowSize = options.getWindowSize();
        if (windowSize <= 0) { throw new  IllegalArgumentException("Window Size must be greater than zero."); }

        int maxConnections = options.getMaxConnections();
        if (maxConnections <= 0) { throw new  IllegalArgumentException("Max Connections must be greater than zero."); }

        int port = uri.getPort();
        /* Pick a default port based on the scheme if one wasn't set in the URI */
        if (port == -1) {
            if (HTTP.equals(uri.getScheme()))  { port = DEFAULT_HTTP_PORT; }
            if (HTTPS.equals(uri.getScheme())) { port = DEFAULT_HTTPS_PORT; }
        }

        this.clientBootstrap = clientBootstrap;
        this.socketOptions = socketOptions;
        this.tlsContext = options.getTlsContext();
        this.windowSize = windowSize;
        this.uri = uri;
        this.port = port;
        this.maxConnections = maxConnections;
        this.proxyOptions = options.getProxyOptions();

        acquire(httpConnectionManagerNew(clientBootstrap.native_ptr(),
                                            socketOptions.native_ptr(),
                                            useTls ? tlsContext.native_ptr() : 0,
                                            windowSize,
                                            uri.getHost(),
                                            port,
                                            maxConnections,
                                            proxyOptions != null ? proxyOptions.native_ptr() : 0));
    }

    /** Called from Native when a new connection is acquired **/
    private void onConnectionAcquired(long connection, int errorCode) {
        CompletableFuture<HttpConnection> connectionRequest = connectionAcquisitionRequests.poll();

        if (connectionRequest == null) {
            throw new IllegalStateException("No Future for Connection Acquisition");
        }

        if (errorCode != CRT.AWS_CRT_SUCCESS) {
            connectionRequest.completeExceptionally(new HttpException(errorCode));
            return;
        }

        HttpConnection conn = new HttpConnection(this, connection);
        connectionRequest.complete(conn);
    }

    /**
     * Request a HttpConnection from the Connection Pool.
     * @return A Future for a HttpConnection that will be completed when a connection is acquired.
     */
    public CompletableFuture<HttpConnection> acquireConnection() {
        if (isClosed.get() || isNull()) {
            throw new IllegalStateException("HttpConnectionPoolManager has been closed, can't acquire new connections");
        }

        CompletableFuture<HttpConnection> connRequest = new CompletableFuture<>();
        connectionAcquisitionRequests.add(connRequest);

        httpConnectionManagerAcquireConnection(this, this.native_ptr());
        return connRequest;
    }

    /**
     * Releases this HttpConnection back into the Connection Pool, and allows another Request to acquire this connection.
     * @param conn
     */
    public void releaseConnection(HttpConnection conn) {
        conn.close();
    }

    protected void releaseConnectionPointer(long connection_ptr) {
        if (!isNull()) {
            httpConnectionManagerReleaseConnection(this.native_ptr(), connection_ptr);
        }
    }

    private void closePendingAcquisitions(Throwable throwable) {
        while (connectionAcquisitionRequests.size() > 0) {
            // Remove and complete future from connectionAcquisitionRequests Queue
            CompletableFuture<HttpConnection> future = connectionAcquisitionRequests.poll();
            if (future != null) {
                future.completeExceptionally(throwable);
            }
        }
    }

    /**
     * Closes this Connection Pool and any pending Connection Acquisitions
     */
    public void close() {
        isClosed.set(true);
        closePendingAcquisitions(new RuntimeException("Connection Manager Closing. Closing Pending Connection Acquisitions."));
        if (!isNull()) {
            httpConnectionManagerRelease(release());
        }
    }

    /*******************************************************************************
     * Getter methods
     ******************************************************************************/

    public int getMaxConnections() {
        return maxConnections;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public URI getUri() {
        return uri;
    }

    /*******************************************************************************
     * Native methods
     ******************************************************************************/

    private static native long httpConnectionManagerNew(long client_bootstrap,
                                                        long socketOptions,
                                                        long tlsContext,
                                                        int windowSize,
                                                        String endpoint,
                                                        int port,
                                                        int maxConns,
                                                        long proxyOptions) throws CrtRuntimeException;

    private static native void httpConnectionManagerRelease(long conn_manager) throws CrtRuntimeException;

    private static native void httpConnectionManagerAcquireConnection(HttpConnectionPoolManager thisObj, long conn_manager) throws CrtRuntimeException;

    private static native void httpConnectionManagerReleaseConnection(long conn_manager, long connection) throws CrtRuntimeException;

}
