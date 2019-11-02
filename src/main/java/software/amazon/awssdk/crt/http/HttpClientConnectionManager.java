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
public class HttpClientConnectionManager extends CrtResource {
    public static final int DEFAULT_MAX_BUFFER_SIZE = 16 * 1024;
    public static final int DEFAULT_MAX_WINDOW_SIZE = Integer.MAX_VALUE;
    public static final int DEFAULT_MAX_CONNECTIONS = 2;
    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static final int DEFAULT_HTTP_PORT = 80;
    private static final int DEFAULT_HTTPS_PORT = 443;

    private final int windowSize;
    private final URI uri;
    private final int port;
    private final boolean useTls;
    private final int maxConnections;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final CompletableFuture<Void> shutdownComplete = new CompletableFuture<>();

    /**
     * The queue of Connection Acquisition requests.
     */
    private final Queue<CompletableFuture<HttpClientConnection>> connectionAcquisitionRequests = new ConcurrentLinkedQueue<>();

    public HttpClientConnectionManager(ClientBootstrap clientBootstrap, SocketOptions socketOptions, TlsContext tlsContext,  URI uri) {
        this(clientBootstrap, socketOptions, tlsContext, uri, DEFAULT_MAX_BUFFER_SIZE, DEFAULT_MAX_WINDOW_SIZE, DEFAULT_MAX_CONNECTIONS);
    }

    public HttpClientConnectionManager(ClientBootstrap clientBootstrap, SocketOptions socketOptions, TlsContext tlsContext,
                                      URI uri, int bufferSize, int windowSize, int maxConnections) {

        if (uri == null) {  throw new IllegalArgumentException("URI must not be null"); }
        if (uri.getScheme() == null) { throw new IllegalArgumentException("URI does not have a Scheme"); }
        if (!HTTP.equals(uri.getScheme()) && !HTTPS.equals(uri.getScheme())) { throw new IllegalArgumentException("URI has unknown Scheme"); }
        if (uri.getHost() == null) { throw new IllegalArgumentException("URI does not have a Host name"); }
        if (clientBootstrap == null || clientBootstrap.isNull()) {  throw new IllegalArgumentException("ClientBootstrap must not be null"); }
        if (socketOptions == null || socketOptions.isNull()) { throw new IllegalArgumentException("SocketOptions must not be null"); }
        if (HTTPS.equals(uri.getScheme()) && tlsContext == null) { throw new IllegalArgumentException("TlsContext must not be null if https is used"); }
        if (bufferSize <= 0) { throw new  IllegalArgumentException("Buffer Size must be greater than zero."); }
        if (windowSize <= 0) { throw new  IllegalArgumentException("Window Size must be greater than zero."); }
        if (maxConnections <= 0) { throw new  IllegalArgumentException("Max Connections must be greater than zero."); }

        int port = uri.getPort();

        /* Pick a default port based on the scheme if one wasn't set in the URI */
        if (port == -1) {
            if (HTTP.equals(uri.getScheme()))  { port = DEFAULT_HTTP_PORT; }
            if (HTTPS.equals(uri.getScheme())) { port = DEFAULT_HTTPS_PORT; }
        }

        this.windowSize = windowSize;
        this.uri = uri;
        this.port = port;
        this.useTls = HTTPS.equals(uri.getScheme());
        this.maxConnections = maxConnections;

        acquireNativeHandle(httpClientConnectionManagerNew(this,
                                            clientBootstrap.getNativeHandle(),
                                            socketOptions.getNativeHandle(),
                                            useTls ? tlsContext.getNativeHandle() : 0,
                                            windowSize,
                                            uri.getHost(),
                                            port,
                                            maxConnections));

        /* we don't need to add a reference to socketOptions since it's copied during connection manager construction */
         addReferenceTo(clientBootstrap);
         if (useTls) {
             addReferenceTo(tlsContext);
         }
    }

    /** Called from Native when a new connection is acquired **/
    private void onConnectionAcquired(long connection, int errorCode) {
        CompletableFuture<HttpClientConnection> connectionRequest = connectionAcquisitionRequests.poll();

        if (connectionRequest == null) {
            throw new IllegalStateException("No Future for Connection Acquisition");
        }

        if (errorCode != CRT.AWS_CRT_SUCCESS) {
            connectionRequest.completeExceptionally(new HttpException(errorCode));
            return;
        }

        HttpClientConnection conn = new HttpClientConnection(this, connection);
        connectionRequest.complete(conn);
    }

    /**
     * Request a HttpClientConnection from the Connection Pool.
     * @return A Future for a HttpClientConnection that will be completed when a connection is acquired.
     */
    public CompletableFuture<HttpClientConnection> acquireConnection() {
        if (isClosed.get() || isNull()) {
            throw new IllegalStateException("HttpClientConnectionManager has been closed, can't acquire new connections");
        }

        CompletableFuture<HttpClientConnection> connRequest = new CompletableFuture<>();
        connectionAcquisitionRequests.add(connRequest);

        httpClientConnectionManagerAcquireConnection(this, this.getNativeHandle());
        return connRequest;
    }

    /**
     * Releases this HttpClientConnection back into the Connection Pool, and allows another Request to acquire this connection.
     * @param conn
     */
    public void releaseConnection(HttpClientConnection conn) {
        conn.close();
    }

    protected void releaseConnectionPointer(long connection_ptr) {
        if (!isNull()) {
            httpClientConnectionManagerReleaseConnection(this.getNativeHandle(), connection_ptr);
        }
    }

    private void closePendingAcquisitions(Throwable throwable) {
        while (connectionAcquisitionRequests.size() > 0) {
            // Remove and complete future from connectionAcquisitionRequests Queue
            CompletableFuture<HttpClientConnection> future = connectionAcquisitionRequests.poll();
            if (future != null) {
                future.completeExceptionally(throwable);
            }
        }
    }

    /**
     * Called from Native when all Connections in this Connection Pool have finished shutting down and it is safe to
     * begin releasing Native Resources that HttpClientConnectionManager depends on.
     */
    private void onShutdownComplete() {
        releaseReferences();

        this.shutdownComplete.complete(null);
    }

    /**
     * Determines whether a resource releases its dependencies at the same time the native handle is released or if it waits.
     * Resources that wait are responsible for calling releaseReferences() manually.
     */
    @Override
    protected boolean canReleaseReferencesImmediately() { return false; }

    /**
     * Closes this Connection Pool and any pending Connection Acquisitions
     */
    @Override
    protected void releaseNativeHandle() {
        isClosed.set(true);
        closePendingAcquisitions(new RuntimeException("Connection Manager Closing. Closing Pending Connection Acquisitions."));
        if (!isNull()) {
            /*
             * Release our Native pointer and schedule tasks on the Native Event Loop to start sending HTTP/TLS/TCP
             * connection shutdown messages to peers for any open Connections.
             */
            httpClientConnectionManagerRelease(getNativeHandle());
        }
    }

    public CompletableFuture<Void> getShutdownCompleteFuture() { return shutdownComplete; }

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

    private static native long httpClientConnectionManagerNew(HttpClientConnectionManager thisObj,
                                                              long client_bootstrap,
                                                              long socketOptions,
                                                              long tlsContext,
                                                              int windowSize,
                                                              String endpoint,
                                                              int port,
                                                              int maxConns) throws CrtRuntimeException;

    private static native void httpClientConnectionManagerRelease(long conn_manager) throws CrtRuntimeException;

    private static native void httpClientConnectionManagerAcquireConnection(HttpClientConnectionManager thisObj, long conn_manager) throws CrtRuntimeException;

    private static native void httpClientConnectionManagerReleaseConnection(long conn_manager, long connection) throws CrtRuntimeException;

}
