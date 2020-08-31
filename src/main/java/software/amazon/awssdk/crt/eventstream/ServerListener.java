package software.amazon.awssdk.crt.eventstream;


import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.io.ServerBootstrap;
import software.amazon.awssdk.crt.io.ServerTlsContext;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContext;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class ServerListener extends CrtResource {
    private final CompletableFuture<Void> shutdownComplete = new CompletableFuture<>();
    private TlsContext tlsContext = null;
    private final ServerBootstrap serverBootstrap;

    public ServerListener(final String hostName, short port, final SocketOptions socketOptions,
                          final ServerTlsContext tlsContext, final ServerBootstrap serverBootstrap,
                          final ServerListenerHandler handler) {

        long tlsContextPtr = tlsContext != null ? tlsContext.getNativeHandle(): 0;
        acquireNativeHandle(serverListenerNew(this, hostName.getBytes(StandardCharsets.UTF_8), port,
                socketOptions.getNativeHandle(), tlsContextPtr, serverBootstrap.getNativeHandle(),
                handler));

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
            removeReferenceTo(serverBootstrap);
            if (tlsContext != null) {
                removeReferenceTo(tlsContext);
            }
        }
    }

    @Override
    protected boolean canReleaseReferencesImmediately() {
        return false;
    }

    private void onShutdownComplete() {
        Log.log(Log.LogLevel.Trace, Log.LogSubject.EventStreamServerListener, "ServerListener.onShutdownComplete");

        releaseReferences();

        this.shutdownComplete.complete(null);
    }

    public CompletableFuture<Void> getShutdownCompleteFuture() { return shutdownComplete; }

    private static native long serverListenerNew(ServerListener serverListener, byte[] hostName,
                                                 short port, long socketOptionsHandle,
                                                 long tlsContextHandle, long bootstrapHandle,
                                                 ServerListenerHandler handler);
    private static native void release(long serverListenerPtr);

}
