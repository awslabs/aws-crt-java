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
import java.nio.charset.Charset;
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
    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static final int DEFAULT_HTTP_PORT = 80;
    private static final int DEFAULT_HTTPS_PORT = 443;
    private final static Charset UTF8 = java.nio.charset.StandardCharsets.UTF_8;

    private final int windowSize;
    private final URI uri;
    private final int port;
    private final int maxConnections;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final CompletableFuture<Void> shutdownComplete = new CompletableFuture<>();

    /**
     * The queue of Connection Acquisition requests.
     */
    private final Queue<CompletableFuture<HttpClientConnection>> connectionAcquisitionRequests = new ConcurrentLinkedQueue<>();

    public static HttpClientConnectionManager create(HttpClientConnectionManagerOptions options) {
        return new HttpClientConnectionManager(options);
    }

    private HttpClientConnectionManager(HttpClientConnectionManagerOptions options) {
        URI uri = options.getUri();
        if (uri == null) {  throw new IllegalArgumentException("URI must not be null"); }
        if (uri.getScheme() == null) { throw new IllegalArgumentException("URI does not have a Scheme"); }
        if (!HTTP.equals(uri.getScheme()) && !HTTPS.equals(uri.getScheme())) { throw new IllegalArgumentException("URI has unknown Scheme"); }
        if (uri.getHost() == null) { throw new IllegalArgumentException("URI does not have a Host name"); }

        ClientBootstrap clientBootstrap = options.getClientBootstrap();
        if (clientBootstrap == null) {  throw new IllegalArgumentException("ClientBootstrap must not be null"); }

        SocketOptions socketOptions = options.getSocketOptions();
        if (socketOptions == null) { throw new IllegalArgumentException("SocketOptions must not be null"); }

        boolean useTls = HTTPS.equals(uri.getScheme());
        TlsContext tlsContext = options.getTlsContext();
        if (useTls && tlsContext == null) { throw new IllegalArgumentException("TlsContext must not be null if https is used"); }

        int windowSize = options.getWindowSize();
        if (windowSize <= 0) { throw new  IllegalArgumentException("Window Size must be greater than zero."); }

        int bufferSize = options.getBufferSize();
        if (bufferSize <= 0) { throw new  IllegalArgumentException("Buffer Size must be greater than zero."); }

        int maxConnections = options.getMaxConnections();
        if (maxConnections <= 0) { throw new  IllegalArgumentException("Max Connections must be greater than zero."); }

        int port = uri.getPort();
        /* Pick a default port based on the scheme if one wasn't set in the URI */
        if (port == -1) {
            if (HTTP.equals(uri.getScheme()))  { port = DEFAULT_HTTP_PORT; }
            if (HTTPS.equals(uri.getScheme())) { port = DEFAULT_HTTPS_PORT; }
        }

        HttpProxyOptions proxyOptions = options.getProxyOptions();

        this.windowSize = windowSize;
        this.uri = uri;
        this.port = port;
        this.maxConnections = maxConnections;

        String proxyHost = null;
        int proxyPort = 0;
        TlsContext proxyTlsContext = null;
        int proxyAuthorizationType = 0;
        String proxyAuthorizationUsername = null;
        String proxyAuthorizationPassword = null;

        if (proxyOptions != null) {
            proxyHost = proxyOptions.getHost();
            proxyPort = proxyOptions.getPort();
            proxyTlsContext = proxyOptions.getTlsContext();
            proxyAuthorizationType = proxyOptions.getAuthorizationType().getValue();
            proxyAuthorizationUsername = proxyOptions.getAuthorizationUsername();
            proxyAuthorizationPassword = proxyOptions.getAuthorizationPassword();
        }

        HttpMonitoringOptions monitoringOptions = options.getMonitoringOptions();
        long monitoringThroughputThresholdInBytesPerSecond = 0;
        int monitoringFailureIntervalInSeconds = 0;
        if (monitoringOptions != null) {
            monitoringThroughputThresholdInBytesPerSecond = monitoringOptions.getMinThroughputBytesPerSecond();
            monitoringFailureIntervalInSeconds = monitoringOptions.getAllowableThroughputFailureIntervalSeconds();
        }

        acquireNativeHandle(httpClientConnectionManagerNew(this,
                                            clientBootstrap.getNativeHandle(),
                                            socketOptions.getNativeHandle(),
                                            useTls ? tlsContext.getNativeHandle() : 0,
                                            windowSize,
                                            uri.getHost().getBytes(UTF8),
                                            port,
                                            maxConnections,
                                            proxyHost != null ? proxyHost.getBytes(UTF8) : null,
                                            proxyPort,
                                            proxyTlsContext != null ? proxyTlsContext.getNativeHandle() : 0,
                                            proxyAuthorizationType,
                                            proxyAuthorizationUsername != null ? proxyAuthorizationUsername.getBytes(UTF8) : null,
                                            proxyAuthorizationPassword != null ? proxyAuthorizationPassword.getBytes(UTF8) : null,
                                            options.isManualWindowManagement(),
                                            options.getMaxConnectionIdleInMilliseconds(),
                                            monitoringThroughputThresholdInBytesPerSecond,
                                            monitoringFailureIntervalInSeconds));

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
     * @param conn Connection to release
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

    /**
     * @return maximum number of connections this connection manager will pool
     */
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
                                                        byte[] endpoint,
                                                        int port,
                                                        int maxConns,
                                                        byte[] proxyHost,
                                                        int proxyPort,
                                                        long proxyTlsContext,
                                                        int proxyAuthorizationType,
                                                        byte[] proxyAuthorizationUsername,
                                                        byte[] proxyAuthorizationPassword,
                                                        boolean isManualWindowManagement,
                                                        long maxConnectionIdleInMilliseconds,
                                                        long monitoringThroughputThresholdInBytesPerSecond,
                                                        int monitoringFailureIntervalInSeconds) throws CrtRuntimeException;

    private static native void httpClientConnectionManagerRelease(long conn_manager) throws CrtRuntimeException;

    private static native void httpClientConnectionManagerAcquireConnection(HttpClientConnectionManager thisObj, long conn_manager) throws CrtRuntimeException;

    private static native void httpClientConnectionManagerReleaseConnection(long conn_manager, long connection) throws CrtRuntimeException;

}
