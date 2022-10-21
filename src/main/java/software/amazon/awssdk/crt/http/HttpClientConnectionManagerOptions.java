/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.http;

import java.net.URI;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsConnectionOptions;
import software.amazon.awssdk.crt.io.TlsContext;

/**
 * Contains all the configuration options for a HttpConnectionPoolManager instance
 */
public class HttpClientConnectionManagerOptions {
    public static final int DEFAULT_MAX_BUFFER_SIZE = 16 * 1024;
    public static final int DEFAULT_MAX_WINDOW_SIZE = Integer.MAX_VALUE;
    public static final int DEFAULT_MAX_CONNECTIONS = 2;

    private ClientBootstrap clientBootstrap;
    private SocketOptions socketOptions;
    private TlsContext tlsContext;
    private TlsConnectionOptions tlsConnectionOptions;
    private int windowSize = DEFAULT_MAX_WINDOW_SIZE;
    private int bufferSize = DEFAULT_MAX_BUFFER_SIZE;
    private URI uri;
    private int port = -1;
    private int maxConnections = DEFAULT_MAX_CONNECTIONS;
    private HttpProxyOptions proxyOptions;
    private boolean manualWindowManagement = false;
    private HttpMonitoringOptions monitoringOptions;
    private long maxConnectionIdleInMilliseconds = 0;
    private HttpVersion expectedHttpVersion = HttpVersion.HTTP_1_1;

    private static final String HTTP = "http";
    private static final String HTTPS = "https";

    /**
     * Default constructor
     */
    public HttpClientConnectionManagerOptions() {
    }

    /**
     * Sets the client bootstrap instance to use to create the pool's connections
     * @param clientBootstrap ClientBootstrap to use
     * @return this
     */
    public HttpClientConnectionManagerOptions withClientBootstrap(ClientBootstrap clientBootstrap) {
        this.clientBootstrap = clientBootstrap;
        return this;
    }

    /**
     * Gets the client bootstrap instance to use to create the pool's connections
     * @return ClientBootstrap used by this connection manager
     */
    public ClientBootstrap getClientBootstrap() { return clientBootstrap; }

    /**
     * Sets the socket options to use for connections in the connection pool
     * @param socketOptions The socket options to use for all connections in the manager
     * @return this
     */
    public HttpClientConnectionManagerOptions withSocketOptions(SocketOptions socketOptions) {
        this.socketOptions = socketOptions;
        return this;
    }

    /**
     * @return the socket options to use for connections in the connection pool
     */
    public SocketOptions getSocketOptions() { return socketOptions; }

    /**
     * Sets the tls context to use for connections in the connection pool
     * @param tlsContext The TlsContext to use
     * @return this
     */
    public HttpClientConnectionManagerOptions withTlsContext(TlsContext tlsContext) {
        this.tlsContext = tlsContext;
        return this;
    }

    /**
     * @return the tls context used by connections in the connection pool
     */
    public TlsContext getTlsContext() { return tlsContext; }

    /**
     * Sets the connection-specific TLS options to use for connections in the connection pool.
     * Either TLS context or TLS connection options will be enough to set up TLS connection.
     * If both set, an exception will be raised.
     * @param tlsConnectionOptions The TlsConnectionOptions to use
     * @return this
     */
    public HttpClientConnectionManagerOptions withTlsConnectionOptions(TlsConnectionOptions tlsConnectionOptions) {
        this.tlsConnectionOptions = tlsConnectionOptions;
        return this;
    }

    /**
     * @return the tls context used by connections in the connection pool
     */
    public TlsConnectionOptions getTlsConnectionOptions() { return tlsConnectionOptions; }

    /**
     * Sets the starting size of each HTTP stream's flow-control window.
     * This is only used when "manual window management" is enabled.
     *
     * @param windowSize The initial window size for each HTTP stream
     * @return this
     * @see #withManualWindowManagement
     */
    public HttpClientConnectionManagerOptions withWindowSize(int windowSize) {
        this.windowSize = windowSize;
        return this;
    }

    /**
     * @return The starting size of each HTTP stream's flow-control window.
     */
    public int getWindowSize() { return windowSize; }

    /**
     * @deprecated Sets the IO buffer size to use for connections in the connection pool
     * @param bufferSize Size of I/O buffer per connection
     * @return this
     */
    public HttpClientConnectionManagerOptions withBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    /**
     * @deprecated
     * @return the IO buffer size to use for connections in the connection pool
     */
    public int getBufferSize() { return bufferSize; }

    /**
     * Sets the URI to use for connections in the connection pool
     * @param uri The endpoint URI to connect to
     * @return this
     */
    public HttpClientConnectionManagerOptions withUri(URI uri) {
        this.uri = uri;
        return this;
    }

    /**
     * @return the URI to use for connections in the connection pool
     */
    public URI getUri() { return uri; }

    /**
     * Sets the port to connect to for connections in the connection pool
     * @param port The port to connect to
     * @return this
     */
    public HttpClientConnectionManagerOptions withPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * @return the port to connect to for connections in the connection pool.
     *         Returns -1 if none has been explicitly set.
     */
    public int getPort() { return port; }

    /**
     * Sets the maximum number of connections allowed in the connection pool
     * @param maxConnections maximum number of connections to pool
     * @return this
     */
    public HttpClientConnectionManagerOptions withMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
        return this;
    }

    /**
     * @return the maximum number of connections allowed in the connection pool
     */
    public int getMaxConnections() { return maxConnections; }

    /**
     * Sets the proxy options for connections in the connection pool
     * @param proxyOptions HttpProxyOptions for this connection manager, or null to disable proxying
     * @return this
     */
    public HttpClientConnectionManagerOptions withProxyOptions(HttpProxyOptions proxyOptions) {
        this.proxyOptions = proxyOptions;
        return this;
    }

    /**
     * @return the proxy options for connections in the connection pool
     */
    public HttpProxyOptions getProxyOptions() { return proxyOptions; }

    /**
     * @return true if manual window management is used, false otherwise
     * @see #withManualWindowManagement
     */
    public boolean isManualWindowManagement() { return manualWindowManagement; }

    /**
     * If set to true, then you must manage the read backpressure mechanism. You should
     * only use this if you're allowing http response body data to escape the callbacks. E.g. you're
     * putting the data into a queue for another thread to process and need to make sure the memory
     * usage is bounded (e.g. reactive streams).
     * <p>
     * When enabled, each HttpStream has a flow-control window that shrinks as response body data is downloaded
     * (headers do not affect the window). {@link #withWindowSize} determines the starting size of each
     * HttpStream's window, in bytes. Data stops downloading whenever the window reaches zero.
     * Increment the window to keep data flowing by calling {@link HttpStreamBase#incrementWindow},
     * or by returning a size from {@link HttpStreamResponseHandler#onResponseBody}.
     * Maintain a larger window to keep up a high download throughput,
     * or use a smaller window to limit how much data could get buffered in memory.
     *
     * @param manualWindowManagement true to enable manual window management, false to use automatic window management
     * @return this
     */
    public HttpClientConnectionManagerOptions withManualWindowManagement(boolean manualWindowManagement) {
        this.manualWindowManagement = manualWindowManagement;
        return this;
    }

    /**
     * Set the expected protocol version of the connection to be made, default is HTTP/1.1
     *
     * @param expectedHttpVersion The expected protocol version of the connection made
     * @return this
     */
    public HttpClientConnectionManagerOptions withExpectedHttpVersion(HttpVersion expectedHttpVersion) {
        this.expectedHttpVersion = expectedHttpVersion;
        return this;
    }

    /**
     * @return Return the expected HTTP protocol version.
     */
    public HttpVersion getExpectedHttpVersion() {
        return expectedHttpVersion;
    }

    /**
     * Sets maximum amount of time, in milliseconds, that the connection can be idle in the manager before
     * getting culled by the manager
     * @param maxConnectionIdleInMilliseconds How long to allow connections to be idle before reaping them
     * @return this
     */
    public HttpClientConnectionManagerOptions withMaxConnectionIdleInMilliseconds(long maxConnectionIdleInMilliseconds) {
        this.maxConnectionIdleInMilliseconds = maxConnectionIdleInMilliseconds;
        return this;
    }

    /**
     * @return How long to allow connections to be idle before reaping them
     */
    public long getMaxConnectionIdleInMilliseconds() { return maxConnectionIdleInMilliseconds; }

    /**
     * Sets the monitoring options for connections in the connection pool
     * @param monitoringOptions Monitoring options for this connection manager, or null to disable monitoring
     * @return this
     */
    public HttpClientConnectionManagerOptions withMonitoringOptions(HttpMonitoringOptions monitoringOptions) {
        this.monitoringOptions = monitoringOptions;
        return this;
    }

    /**
     * @return the monitoring options for connections in the connection pool
     */
    public HttpMonitoringOptions getMonitoringOptions() { return monitoringOptions; }

    /**
     * Validate the connection manager options are valid to use. Throw exceptions if not.
     */
    public void validateOptions() {
        URI uri = this.getUri();
        if (uri == null) {  throw new IllegalArgumentException("URI must not be null"); }
        if (uri.getScheme() == null) { throw new IllegalArgumentException("URI does not have a Scheme"); }
        if (!HTTP.equals(uri.getScheme()) && !HTTPS.equals(uri.getScheme())) { throw new IllegalArgumentException("URI has unknown Scheme"); }
        if (uri.getHost() == null) { throw new IllegalArgumentException("URI does not have a Host name"); }

        if (clientBootstrap == null) {  throw new IllegalArgumentException("ClientBootstrap must not be null"); }

        if (socketOptions == null) { throw new IllegalArgumentException("SocketOptions must not be null"); }

        if(tlsContext!= null && tlsConnectionOptions != null) {
            throw new IllegalArgumentException("Cannot set both TlsContext and TlsConnectionOptions.");
        }
        boolean useTls = HTTPS.equals(uri.getScheme());
        boolean tlsSet = (tlsContext!= null || tlsConnectionOptions != null);
        if (useTls && !tlsSet) { throw new IllegalArgumentException("TlsContext or TlsConnectionOptions must not be null if https is used"); }

        if (windowSize <= 0) { throw new  IllegalArgumentException("Window Size must be greater than zero."); }

        if (maxConnections <= 0) { throw new  IllegalArgumentException("Max Connections must be greater than zero."); }
    }
}
