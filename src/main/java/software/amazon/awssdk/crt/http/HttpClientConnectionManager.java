/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.http;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsConnectionOptions;
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

    private final long windowSize;
    private final URI uri;
    private final int port;
    private final int maxConnections;
    private final CompletableFuture<Void> shutdownComplete = new CompletableFuture<>();
    private final HttpVersion expectedHttpVersion;

    /**
     * Factory function for HttpClientConnectionManager instances
     *
     * @param options configuration options
     * @return a new instance of an HttpClientConnectionManager
     */
    public static HttpClientConnectionManager create(HttpClientConnectionManagerOptions options) {
        return new HttpClientConnectionManager(options);
    }

    private HttpClientConnectionManager(HttpClientConnectionManagerOptions options) {

        options.validateOptions();
        URI uri = options.getUri();
        ClientBootstrap clientBootstrap = options.getClientBootstrap();
        SocketOptions socketOptions = options.getSocketOptions();
        boolean useTls = HTTPS.equals(uri.getScheme());
        TlsContext tlsContext = options.getTlsContext();
        TlsConnectionOptions tlsConnectionOptions = options.getTlsConnectionOptions();

        long windowSize = options.getWindowSize();
        int maxConnections = options.getMaxConnections();
        int port = options.getPort();
        if (port == -1) {
            port = uri.getPort();
            /* Pick a default port based on the scheme if one wasn't set */
            if (port == -1) {
                if (HTTP.equals(uri.getScheme()))  { port = DEFAULT_HTTP_PORT; }
                if (HTTPS.equals(uri.getScheme())) { port = DEFAULT_HTTPS_PORT; }
            }
        }

        HttpProxyOptions proxyOptions = options.getProxyOptions();

        this.windowSize = windowSize;
        this.uri = uri;
        this.port = port;
        this.maxConnections = maxConnections;
        this.expectedHttpVersion = options.getExpectedHttpVersion();

        int proxyConnectionType = 0;
        String proxyHost = null;
        int proxyPort = 0;
        TlsContext proxyTlsContext = null;
        int proxyAuthorizationType = 0;
        String proxyAuthorizationUsername = null;
        String proxyAuthorizationPassword = null;

        if (proxyOptions != null) {
            proxyConnectionType = proxyOptions.getConnectionType().getValue();
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
                                            useTls && tlsContext!=null ? tlsContext.getNativeHandle() : 0,
                                            useTls && tlsConnectionOptions!=null ? tlsConnectionOptions.getNativeHandle() : 0,
                                            windowSize,
                                            uri.getHost().getBytes(UTF8),
                                            port,
                                            maxConnections,
                                            proxyConnectionType,
                                            proxyHost != null ? proxyHost.getBytes(UTF8) : null,
                                            proxyPort,
                                            proxyTlsContext != null ? proxyTlsContext.getNativeHandle() : 0,
                                            proxyAuthorizationType,
                                            proxyAuthorizationUsername != null ? proxyAuthorizationUsername.getBytes(UTF8) : null,
                                            proxyAuthorizationPassword != null ? proxyAuthorizationPassword.getBytes(UTF8) : null,
                                            options.isManualWindowManagement(),
                                            options.getMaxConnectionIdleInMilliseconds(),
                                            monitoringThroughputThresholdInBytesPerSecond,
                                            monitoringFailureIntervalInSeconds,
                                            expectedHttpVersion.getValue()));

        /* we don't need to add a reference to socketOptions since it's copied during connection manager construction */
         addReferenceTo(clientBootstrap);
         if (useTls) {
            if (tlsContext != null) {
                addReferenceTo(tlsContext);
            }
            if (tlsConnectionOptions != null) {
                addReferenceTo(tlsConnectionOptions);
            }
         }
    }

    /**
     * Request a HttpClientConnection from the Connection Pool.
     * @return A Future for a HttpClientConnection that will be completed when a connection is acquired.
     */
    public CompletableFuture<HttpClientConnection> acquireConnection() {
        if (isNull()) {
            throw new IllegalStateException("HttpClientConnectionManager has been closed, can't acquire new connections");
        }

        CompletableFuture<HttpClientConnection> returnedFuture = new CompletableFuture<>();
        httpClientConnectionManagerAcquireConnection(this.getNativeHandle(), returnedFuture);
        return returnedFuture;
    }

    /**
     * Releases this HttpClientConnection back into the Connection Pool, and allows another Request to acquire this connection.
     * @param conn Connection to release
     */
    public void releaseConnection(HttpClientConnection conn) {
        conn.close();
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

    /**
     * @return concurrency metrics for the current manager
     */
    public HttpManagerMetrics getManagerMetrics() {
        if (isNull()) {
            throw new IllegalStateException("HttpClientConnectionManager has been closed, can't fetch metrics");
        }
        return httpConnectionManagerFetchMetrics(getNativeHandle());
    }

    /**
     * @return size of the per-connection streaming read window for response handling
     */
    public long getWindowSize() {
        return windowSize;
    }

    /**
     * @return uri the connection manager is making connections to
     */
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
                                                        long tlsConnectionOptions,
                                                        long windowSize,
                                                        byte[] endpoint,
                                                        int port,
                                                        int maxConns,
                                                        int proxyConnectionType,
                                                        byte[] proxyHost,
                                                        int proxyPort,
                                                        long proxyTlsContext,
                                                        int proxyAuthorizationType,
                                                        byte[] proxyAuthorizationUsername,
                                                        byte[] proxyAuthorizationPassword,
                                                        boolean isManualWindowManagement,
                                                        long maxConnectionIdleInMilliseconds,
                                                        long monitoringThroughputThresholdInBytesPerSecond,
                                                        int monitoringFailureIntervalInSeconds,
                                                        int expectedProtocol) throws CrtRuntimeException;

    private static native void httpClientConnectionManagerRelease(long conn_manager) throws CrtRuntimeException;

    private static native void httpClientConnectionManagerAcquireConnection(long conn_manager, CompletableFuture<HttpClientConnection> acquireFuture) throws CrtRuntimeException;

    private static native HttpManagerMetrics httpConnectionManagerFetchMetrics(long conn_manager) throws CrtRuntimeException;

}
