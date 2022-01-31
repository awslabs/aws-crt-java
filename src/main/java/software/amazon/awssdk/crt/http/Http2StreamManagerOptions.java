package software.amazon.awssdk.crt.http;

import java.net.URI;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContext;

/**
 * Contains all the configuration options for a Http2StreamManager
 * instance
 */
public class Http2StreamManagerOptions {
    public static final int DEFAULT_MAX_WINDOW_SIZE = Integer.MAX_VALUE;
    public static final int DEFAULT_MAX = Integer.MAX_VALUE;
    public static final int DEFAULT_MAX_CONNECTIONS = 2;

    private ClientBootstrap clientBootstrap;
    private SocketOptions socketOptions;
    private TlsContext tlsContext;
    private URI uri;
    private int port = -1;
    private boolean manualWindowManagement = false;
    private int windowSize = DEFAULT_MAX_WINDOW_SIZE;
    private HttpMonitoringOptions monitoringOptions;
    private HttpProxyOptions proxyOptions;

    private int idealConcurrentStreamsPerConnection = 100;
    private int maxConcurrentStreamsPerConnection = DEFAULT_MAX;

    private int maxConnections = DEFAULT_MAX_CONNECTIONS;

    /**
     * Default constructor
     */
    public Http2StreamManagerOptions() {
    }

    /**
     * Sets the client bootstrap instance to use to create the pool's connections
     *
     * @param clientBootstrap ClientBootstrap to use
     * @return this
     */
    public Http2StreamManagerOptions withClientBootstrap(ClientBootstrap clientBootstrap) {
        this.clientBootstrap = clientBootstrap;
        return this;
    }

    /**
     * Gets the client bootstrap instance to use to create the pool's connections
     *
     * @return ClientBootstrap used by this connection manager
     */
    public ClientBootstrap getClientBootstrap() {
        return clientBootstrap;
    }

    /**
     * Sets the socket options to use for connections in the connection pool
     *
     * @param socketOptions The socket options to use for all connections in the
     *                      manager
     * @return this
     */
    public Http2StreamManagerOptions withSocketOptions(SocketOptions socketOptions) {
        this.socketOptions = socketOptions;
        return this;
    }

    /**
     * @return the socket options to use for connections in the connection pool
     */
    public SocketOptions getSocketOptions() {
        return socketOptions;
    }

    /**
     * Sets the tls context to use for connections in the connection pool
     *
     * @param tlsContext The TlsContext to use
     * @return this
     */
    public Http2StreamManagerOptions withTlsContext(TlsContext tlsContext) {
        this.tlsContext = tlsContext;
        return this;
    }

    /**
     * @return the tls context used by connections in the connection pool
     */
    public TlsContext getTlsContext() {
        return tlsContext;
    }

    /**
     * Sets the IO channel window size to use for connections in the connection pool
     *
     * @param windowSize The initial window size to use for each connection
     * @return this
     */
    public Http2StreamManagerOptions withWindowSize(int windowSize) {
        this.windowSize = windowSize;
        return this;
    }

    /**
     * @return the IO channel window size to use for connections in the connection
     *         pool
     */
    public int getWindowSize() {
        return windowSize;
    }

    public Http2StreamManagerOptions withIdealConcurrentStreamsPerConnection(int idealConcurrentStreamsPerConnection) {
        this.idealConcurrentStreamsPerConnection = idealConcurrentStreamsPerConnection;
        return this;
    }

    public int getIdealConcurrentStreamsPerConnection() {
        return idealConcurrentStreamsPerConnection;
    }

    public Http2StreamManagerOptions withMaxConcurrentStreamsPerConnection(int maxConcurrentStreamsPerConnection) {
        this.maxConcurrentStreamsPerConnection = maxConcurrentStreamsPerConnection;
        return this;
    }

    public int getMaxConcurrentStreamsPerConnection() {
        return maxConcurrentStreamsPerConnection;
    }

    /**
     * Sets the URI to use for connections in the connection pool
     *
     * @param uri The endpoint URI to connect to
     * @return this
     */
    public Http2StreamManagerOptions withUri(URI uri) {
        this.uri = uri;
        return this;
    }

    /**
     * @return the URI to use for connections in the connection pool
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Sets the port to connect to for connections in the connection pool
     *
     * @param port The port to connect to
     * @return this
     */
    public Http2StreamManagerOptions withPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * @return the port to connect to for connections in the connection pool.
     *         Returns -1 if none has been explicitly set.
     */
    public int getPort() {
        return port;
    }

    public Http2StreamManagerOptions withMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
        return this;
    }

    /**
     * @return the maximum number of connections allowed in the connection pool
     */
    public int getMaxConnections() {
        return maxConnections;
    }

    public Http2StreamManagerOptions withProxyOptions(HttpProxyOptions proxyOptions) {
        this.proxyOptions = proxyOptions;
        return this;
    }

    /**
     * @return the proxy options for connections in the connection pool
     */
    public HttpProxyOptions getProxyOptions() {
        return proxyOptions;
    }

    public boolean isManualWindowManagement() {
        return manualWindowManagement;
    }

    public Http2StreamManagerOptions withManualWindowManagement(boolean manualWindowManagement) {
        this.manualWindowManagement = manualWindowManagement;
        return this;
    }

    /**
     * Sets the monitoring options for connections in the connection pool
     *
     * @param monitoringOptions Monitoring options for this connection manager, or
     *                          null to disable monitoring
     * @return this
     */
    public Http2StreamManagerOptions withMonitoringOptions(HttpMonitoringOptions monitoringOptions) {
        this.monitoringOptions = monitoringOptions;
        return this;
    }

    /**
     * @return the monitoring options for connections in the connection pool
     */
    public HttpMonitoringOptions getMonitoringOptions() {
        return monitoringOptions;
    }
}
