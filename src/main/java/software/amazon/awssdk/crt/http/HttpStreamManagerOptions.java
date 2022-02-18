package software.amazon.awssdk.crt.http;

import java.net.URI;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContext;

/**
 * Contains all the configuration options for a Http2StreamManager
 * instance
 */
public class HttpStreamManagerOptions {
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
    public HttpStreamManagerOptions() {
    }

    /**
     * Sets the client bootstrap instance to use to create the pool's connections
     *
     * @param clientBootstrap ClientBootstrap to use
     * @return this
     */
    public HttpStreamManagerOptions withClientBootstrap(ClientBootstrap clientBootstrap) {
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
    public HttpStreamManagerOptions withSocketOptions(SocketOptions socketOptions) {
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
    public HttpStreamManagerOptions withTlsContext(TlsContext tlsContext) {
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
    public HttpStreamManagerOptions withWindowSize(int windowSize) {
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

    /**
     * For HTTP/2 stream manager only.
     *
     * The ideal number of concurrent streams for a connection. Stream manager will
     * try to create a new connection if one connection reaches this number. But, if
     * the max connections reaches, manager will reuse connections to create the
     * acquired steams as much as possible.
     *
     * @param idealConcurrentStreamsPerConnection The ideal number of concurrent
     *                                            streams for a connection
     * @return this
     */
    public HttpStreamManagerOptions withIdealConcurrentStreamsPerConnection(int idealConcurrentStreamsPerConnection) {
        this.idealConcurrentStreamsPerConnection = idealConcurrentStreamsPerConnection;
        return this;
    }

    /**
     * @return The ideal number of concurrent streams for a connection used for
     *         manager
     */
    public int getIdealConcurrentStreamsPerConnection() {
        return idealConcurrentStreamsPerConnection;
    }

    /**
     * Default is no limit, which will use the limit from the server. 0 will be
     * considered as using the default value.
     * The real number of concurrent streams per connection will be controlled by
     * the minimal value of the setting from other end and the value here.
     *
     * @param maxConcurrentStreamsPerConnection The max number of concurrent
     *                                          streams for a connection
     * @return
     */
    public HttpStreamManagerOptions withMaxConcurrentStreamsPerConnection(int maxConcurrentStreamsPerConnection) {
        this.maxConcurrentStreamsPerConnection = maxConcurrentStreamsPerConnection;
        return this;
    }

    /**
     * @return The max number of concurrent streams for a connection set for
     *         manager.
     *         It could be different than the real limits, which is the minimal set
     *         for manager and the settings from the other side.
     */
    public int getMaxConcurrentStreamsPerConnection() {
        return maxConcurrentStreamsPerConnection;
    }

    /**
     * Sets the URI to use for connections in the connection pool
     *
     * @param uri The endpoint URI to connect to
     * @return this
     */
    public HttpStreamManagerOptions withUri(URI uri) {
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
    public HttpStreamManagerOptions withPort(int port) {
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

    /**
     * The max number of connections will be open at same time. If all the
     * connections are full, manager will wait until available to vender more
     * streams
     *
     * @param maxConnections
     * @return
     */
    public HttpStreamManagerOptions withMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
        return this;
    }

    /**
     * @return the maximum number of connections allowed in the connection pool
     */
    public int getMaxConnections() {
        return maxConnections;
    }

    public HttpStreamManagerOptions withProxyOptions(HttpProxyOptions proxyOptions) {
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

    public HttpStreamManagerOptions withManualWindowManagement(boolean manualWindowManagement) {
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
    public HttpStreamManagerOptions withMonitoringOptions(HttpMonitoringOptions monitoringOptions) {
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
