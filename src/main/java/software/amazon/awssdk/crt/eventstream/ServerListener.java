package software.amazon.awssdk.crt.eventstream;


import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.io.ServerBootstrap;
import software.amazon.awssdk.crt.io.ServerTlsContext;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContext;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * Event-stream-rpc server listener. Once it begins listening, it will provide
 * new connections as they arrive.
 */
public class ServerListener extends CrtResource {
    private final CompletableFuture<Void> shutdownComplete = new CompletableFuture<>();
    private TlsContext tlsContext = null;
    private final ServerBootstrap serverBootstrap;
    private int boundPort = -1;

    /**
     * Instantiates a server listener. Once this function completes, the server is configured
     * and listening for new connections.
     * @param hostName name of the host to listen on. Can be a dns name, ip address, or unix
     *                 domain socket (or named pipe on windows) name.
     * @param port port to listen on. Ignored for local domain sockets.
     * @param socketOptions socket options to apply to the listening socket.
     * @param tlsContext optional tls context to apply to the connection if you want to use TLS.
     * @param serverBootstrap bootstrap object for handling connections.
     * @param handler functor interface for handling incoming connections and connection closures.
     */
    public ServerListener(final String hostName, short port, final SocketOptions socketOptions,
                          final ServerTlsContext tlsContext, final ServerBootstrap serverBootstrap,
                          final ServerListenerHandler handler) {

        long tlsContextPtr = tlsContext != null ? tlsContext.getNativeHandle(): 0;
        long serverHandler = serverListenerNew(this, hostName.getBytes(StandardCharsets.UTF_8), port,
                socketOptions.getNativeHandle(), tlsContextPtr, serverBootstrap.getNativeHandle(),
                handler);

        boundPort = getBoundPort(serverHandler);

        acquireNativeHandle(serverHandler);

        if (tlsContext != null) {
            addReferenceTo(tlsContext);
            this.tlsContext = tlsContext;
        }
        addReferenceTo(serverBootstrap);
        this.serverBootstrap = serverBootstrap;
    }

    @Override
    protected void releaseNativeHandle() {
        if (!isNull()) {
            release(getNativeHandle());
        }
    }

    @Override
    protected boolean canReleaseReferencesImmediately() {
        return false;
    }

    /**
     * @return the port which the listener socket is bound to.
     */
    public int getBoundPort() {
        return boundPort;
    }

    /**
     * Invoked from JNI. Completes the shutdownComplete future.
     */
    private void onShutdownComplete() {
        Log.log(Log.LogLevel.Trace, Log.LogSubject.EventStreamServerListener, "ServerListener.onShutdownComplete");
        releaseReferences();
        this.shutdownComplete.complete(null);
    }

    /**
     * @return future to synchronize shutdown completion of this object.
     */
    public CompletableFuture<Void> getShutdownCompleteFuture() { return shutdownComplete; }

    private static native long serverListenerNew(ServerListener serverListener, byte[] hostName,
                                                 short port, long socketOptionsHandle,
                                                 long tlsContextHandle, long bootstrapHandle,
                                                 ServerListenerHandler handler);
    private static native int getBoundPort(long serverListener);

    private static native void release(long serverListenerPtr);

}
