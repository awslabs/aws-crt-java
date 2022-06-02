package software.amazon.awssdk.crt.http;

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.AsyncCallback;
import software.amazon.awssdk.crt.io.TlsContext;

import java.util.concurrent.CompletableFuture;
import java.net.URI;
import java.nio.charset.Charset;

/**
 * Manages a Pool of HTTP/2 Streams. Creates and manages HTTP/2 connections
 * under the hood.
 */
public class Http2StreamManager extends CrtResource {

    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static final int DEFAULT_HTTP_PORT = 80;
    private static final int DEFAULT_HTTPS_PORT = 443;
    private final static Charset UTF8 = java.nio.charset.StandardCharsets.UTF_8;

    private final URI uri;
    private final int port;
    private final int maxConnections;
    private final int idealConcurrentStreamsPerConnection;
    private final int maxConcurrentStreamsPerConnection;
    private final CompletableFuture<Void> shutdownComplete = new CompletableFuture<>();

    /**
     * Factory function for Http2StreamManager instances
     *
     * @param options configuration options
     * @return a new instance of an Http2StreamManager
     */
    public static Http2StreamManager create(HttpStreamManagerOptions options) {
        return new Http2StreamManager(options);
    }

    private Http2StreamManager(HttpStreamManagerOptions options) {
        URI uri = options.getUri();
        if (uri == null) {
            throw new IllegalArgumentException("URI must not be null");
        }
        if (uri.getScheme() == null) {
            throw new IllegalArgumentException("URI does not have a Scheme");
        }
        if (!HTTP.equals(uri.getScheme()) && !HTTPS.equals(uri.getScheme())) {
            throw new IllegalArgumentException("URI has unknown Scheme");
        }
        if (uri.getHost() == null) {
            throw new IllegalArgumentException("URI does not have a Host name");
        }

        ClientBootstrap clientBootstrap = options.getClientBootstrap();
        if (clientBootstrap == null) {
            throw new IllegalArgumentException("ClientBootstrap must not be null");
        }

        SocketOptions socketOptions = options.getSocketOptions();
        if (socketOptions == null) {
            throw new IllegalArgumentException("SocketOptions must not be null");
        }

        boolean useTls = HTTPS.equals(uri.getScheme());
        TlsContext tlsContext = options.getTlsContext();
        if (useTls && tlsContext == null) {
            throw new IllegalArgumentException("TlsContext must not be null if https is used");
        }

        int maxConnections = options.getMaxConnections();
        if (maxConnections <= 0) {
            throw new IllegalArgumentException("Max Connections must be greater than zero.");
        }

        int maxConcurrentStreamsPerConnection = options.getMaxConcurrentStreamsPerConnection();
        if (maxConcurrentStreamsPerConnection <= 0) {
            throw new IllegalArgumentException("Max Concurrent Streams Per Connection must be greater than zero.");
        }

        int idealConcurrentStreamsPerConnection = options.getIdealConcurrentStreamsPerConnection();
        if (idealConcurrentStreamsPerConnection <= 0
                || idealConcurrentStreamsPerConnection > maxConcurrentStreamsPerConnection) {
            throw new IllegalArgumentException(
                    "Ideal Concurrent Streams Per Connection must be greater than zero and smaller than max.");
        }

        int port = options.getPort();
        if (port == -1) {
            port = uri.getPort();
            /* Pick a default port based on the scheme if one wasn't set */
            if (port == -1) {
                if (HTTP.equals(uri.getScheme())) {
                    port = DEFAULT_HTTP_PORT;
                }
                if (HTTPS.equals(uri.getScheme())) {
                    port = DEFAULT_HTTPS_PORT;
                }
            }
        }

        HttpProxyOptions proxyOptions = options.getProxyOptions();

        this.uri = uri;
        this.port = port;
        this.maxConnections = maxConnections;
        this.idealConcurrentStreamsPerConnection = idealConcurrentStreamsPerConnection;
        this.maxConcurrentStreamsPerConnection = maxConcurrentStreamsPerConnection;

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

        acquireNativeHandle(http2StreamManagerNew(this,
                clientBootstrap.getNativeHandle(),
                socketOptions.getNativeHandle(),
                useTls ? tlsContext.getNativeHandle() : 0,
                Http2ConnectionSetting.marshallSettingsForJNI(options.getInitialSettingsList()),
                uri.getHost().getBytes(UTF8),
                port,
                proxyConnectionType,
                proxyHost != null ? proxyHost.getBytes(UTF8) : null,
                proxyPort,
                proxyTlsContext != null ? proxyTlsContext.getNativeHandle() : 0,
                proxyAuthorizationType,
                proxyAuthorizationUsername != null ? proxyAuthorizationUsername.getBytes(UTF8) : null,
                proxyAuthorizationPassword != null ? proxyAuthorizationPassword.getBytes(UTF8) : null,
                options.isManualWindowManagement(),
                monitoringThroughputThresholdInBytesPerSecond,
                monitoringFailureIntervalInSeconds,
                maxConnections,
                idealConcurrentStreamsPerConnection,
                maxConcurrentStreamsPerConnection));

        /*
         * we don't need to add a reference to socketOptions since it's copied during
         * connection manager construction
         */
        addReferenceTo(clientBootstrap);
        if (useTls) {
            addReferenceTo(tlsContext);
        }
    }

    /**
     * Request a Http2Stream from StreamManager.
     *
     * @param request       The Request to make to the Server.
     * @param streamHandler The Stream Handler to be called from the Native
     *                      EventLoop
     * @return A future for a Http2Stream that will be completed when the stream is
     *         acquired.
     */
    public CompletableFuture<Http2Stream> acquireStream(Http2Request request,
        HttpStreamBaseResponseHandler streamHandler) {
        return this.acquireStream((HttpRequestBase) request, streamHandler);
    }

    public CompletableFuture<Http2Stream> acquireStream(HttpRequest request,
        HttpStreamBaseResponseHandler streamHandler) {
        return this.acquireStream((HttpRequestBase) request, streamHandler);
    }

    public CompletableFuture<Http2Stream> acquireStream(HttpRequestBase request,
        HttpStreamBaseResponseHandler streamHandler) {
        CompletableFuture<Http2Stream> completionFuture = new CompletableFuture<>();
        AsyncCallback acquireStreamCompleted = AsyncCallback.wrapFuture(completionFuture, null);
        if (isNull()) {
            completionFuture.completeExceptionally(new IllegalStateException(
                    "Http2StreamManager has been closed, can't acquire new streams"));
            return completionFuture;
        }
        try {
            http2StreamManagerAcquireStream(this.getNativeHandle(),
                    request.marshalForJni(),
                    request.getBodyStream(),
                    new HttpStreamResponseHandlerNativeAdapter(streamHandler),
                    acquireStreamCompleted);
        } catch (CrtRuntimeException ex) {
            completionFuture.completeExceptionally(ex);
        }
        return completionFuture;
    }

    /**
     * Called from Native when all Streams from this Stream manager have finished
     * and underlying resources like connections opened under the hood has been
     * cleaned up
     * begin releasing Native Resources that Http2StreamManager depends on.
     */
    private void onShutdownComplete() {
        releaseReferences();

        this.shutdownComplete.complete(null);
    }

    /**
     * Determines whether a resource releases its dependencies at the same time the
     * native handle is released or if it waits.
     * Resources that wait are responsible for calling releaseReferences() manually.
     */
    @Override
    protected boolean canReleaseReferencesImmediately() {
        return false;
    }

    /**
     * Closes this Connection Pool and any pending Connection Acquisitions
     */
    @Override
    protected void releaseNativeHandle() {
        if (!isNull()) {
            /*
             * Release our Native pointer and schedule tasks on the Native Event Loop to
             * start sending HTTP/TLS/TCP
             * connection shutdown messages to peers for any open Connections.
             */
            http2StreamManagerRelease(getNativeHandle());
        }
    }

    public CompletableFuture<Void> getShutdownCompleteFuture() {
        return shutdownComplete;
    }

    /*******************************************************************************
     * Native methods
     ******************************************************************************/

    private static native long http2StreamManagerNew(Http2StreamManager thisObj,
            long client_bootstrap,
            long socketOptions,
            long tlsContext,
            long[] marshalledSettings,
            byte[] endpoint,
            int port,
            int proxyConnectionType,
            byte[] proxyHost,
            int proxyPort,
            long proxyTlsContext,
            int proxyAuthorizationType,
            byte[] proxyAuthorizationUsername,
            byte[] proxyAuthorizationPassword,
            boolean isManualWindowManagement,
            long monitoringThroughputThresholdInBytesPerSecond,
            int monitoringFailureIntervalInSeconds,
            int maxConns,
            int ideal_concurrent_streams_per_connection,
            int max_concurrent_streams_per_connection) throws CrtRuntimeException;

    private static native void http2StreamManagerRelease(long stream_manager) throws CrtRuntimeException;

    private static native void http2StreamManagerAcquireStream(long stream_manager,
            byte[] marshalledRequest,
            HttpRequestBodyStream bodyStream,
            HttpStreamResponseHandlerNativeAdapter responseHandler,
            AsyncCallback completedCallback) throws CrtRuntimeException;
}
