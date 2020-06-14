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
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.SocketOptions;
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
    private int windowSize = DEFAULT_MAX_WINDOW_SIZE;
    private int bufferSize = DEFAULT_MAX_BUFFER_SIZE;
    private URI uri;
    private int port;
    private int maxConnections = DEFAULT_MAX_CONNECTIONS;
    private HttpProxyOptions proxyOptions;
    private boolean manualWindowManagement = false;
    private HttpMonitoringOptions monitoringOptions;
    private long maxConnectionIdleInMilliseconds = 0;

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
     * Sets the IO channel window size to use for connections in the connection pool
     * @param windowSize The initial window size to use for each connection
     * @return this
     */
    public HttpClientConnectionManagerOptions withWindowSize(int windowSize) {
        this.windowSize = windowSize;
        return this;
    }

    /**
     * @return the IO channel window size to use for connections in the connection pool
     */
    public int getWindowSize() { return windowSize; }

    /**
     * Sets the IO buffer size to use for connections in the connection pool
     * @param bufferSize Size of I/O buffer per connection
     * @return this
     */
    public HttpClientConnectionManagerOptions withBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    /**
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
     * @return the port to connect to for connections in the connection pool
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
     * If set to true, then the TCP read back pressure mechanism will be enabled. You should
     * only use this if you're allowing http response body data to escape the callbacks. E.g. you're
     * putting the data into a queue for another thread to process and need to make sure the memory
     * usage is bounded (e.g. reactive streams).
     * If this is enabled, you must call HttpStream.UpdateWindow() for every
     * byte read from the OnIncomingBody callback.
     * @return true if manual window management is used, false otherwise
     */
    public boolean isManualWindowManagement() { return manualWindowManagement; }

    /**
     * If set to true, then the TCP read back pressure mechanism will be enabled. You should
     * only use this if you're allowing http response body data to escape the callbacks. E.g. you're
     * putting the data into a queue for another thread to process and need to make sure the memory
     * usage is bounded (e.g. reactive streams).
     * If this is enabled, you must call HttpStream.UpdateWindow() for every
     * byte read from the OnIncomingBody callback.
     * @param manualWindowManagement true to enable manual window management, false to use automatic window management
     * @return this
     */
    public HttpClientConnectionManagerOptions withManualWindowManagement(boolean manualWindowManagement) {
        this.manualWindowManagement = manualWindowManagement;
        return this;
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
}

