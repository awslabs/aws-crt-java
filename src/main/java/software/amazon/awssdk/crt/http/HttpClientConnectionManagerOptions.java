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

    public HttpClientConnectionManagerOptions() {
    }

    /**
     * Sets the client bootstrap instance to use to create the pool's connections
     */
    public HttpClientConnectionManagerOptions withClientBootstrap(ClientBootstrap clientBootstrap) {
        this.clientBootstrap = clientBootstrap;
        return this;
    }

    /**
     * Gets the client bootstrap instance to use to create the pool's connections
     */
    public ClientBootstrap getClientBootstrap() { return clientBootstrap; }

    /**
     * Sets the socket options to use for connections in the connection pool
     */
    public HttpClientConnectionManagerOptions withSocketOptions(SocketOptions socketOptions) {
        this.socketOptions = socketOptions;
        return this;
    }

    /**
     * Gets the socket options to use for connections in the connection pool
     */
    public SocketOptions getSocketOptions() { return socketOptions; }

    /**
     * Sets the tls context to use for connections in the connection pool
     */
    public HttpClientConnectionManagerOptions withTlsContext(TlsContext tlsContext) {
        this.tlsContext = tlsContext;
        return this;
    }

    /**
     * Gets the tls context to use for connections in the connection pool
     */
    public TlsContext getTlsContext() { return tlsContext; }

    /**
     * Sets the IO channel window size to use for connections in the connection pool
     */
    public HttpClientConnectionManagerOptions withWindowSize(int windowSize) {
        this.windowSize = windowSize;
        return this;
    }

    /**
     * Gets the IO channel window size to use for connections in the connection pool
     */
    public int getWindowSize() { return windowSize; }

    /**
     * Sets the IO buffer size to use for connections in the connection pool
     */
    public HttpClientConnectionManagerOptions withBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    /**
     * Gets the IO buffer size to use for connections in the connection pool
     */
    public int getBufferSize() { return bufferSize; }


    /**
     * Sets the URI to use for connections in the connection pool
     */
    public HttpClientConnectionManagerOptions withUri(URI uri) {
        this.uri = uri;
        return this;
    }

    /**
     * Gets the URI to use for connections in the connection pool
     */
    public URI getUri() { return uri; }

    /**
     * Sets the port to connect to for connections in the connection pool
     */
    public HttpClientConnectionManagerOptions withPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Gets the port to connect to for connections in the connection pool
     */
    public int getPort() { return port; }

    /**
     * Sets the maximum number of connections allowed in the connection pool
     */
    public HttpClientConnectionManagerOptions withMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
        return this;
    }

    /**
     * Gets the maximum number of connections allowed in the connection pool
     */
    public int getMaxConnections() { return maxConnections; }

    /**
     * Sets the proxy options for connections in the connection pool
     */
    public HttpClientConnectionManagerOptions withProxyOptions(HttpProxyOptions proxyOptions) {
        this.proxyOptions = proxyOptions;
        return this;
    }

    /**
     * Gets the proxy options for connections in the connection pool
     */
    public HttpProxyOptions getProxyOptions() { return proxyOptions; }
}

