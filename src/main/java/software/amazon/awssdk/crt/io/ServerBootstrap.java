package software.amazon.awssdk.crt.io;

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;

public class ServerBootstrap extends CrtResource {
    private EventLoopGroup eventLoopGroup = null;

    public ServerBootstrap(final EventLoopGroup elg) {
        eventLoopGroup = elg;
        acquireNativeHandle(serverBootstrapNew(this, eventLoopGroup.getNativeHandle()));
        addReferenceTo(eventLoopGroup);
    }
    @Override
    protected void releaseNativeHandle() {
        if (!isNull()) {
            serverBootstrapDestroy(getNativeHandle());
            removeReferenceTo(eventLoopGroup);
        }
    }

    @Override
    protected boolean canReleaseReferencesImmediately() {
        return false;
    }

    private static native long serverBootstrapNew(ServerBootstrap bootstrap, long elg) throws CrtRuntimeException;
    private static native void serverBootstrapDestroy(long bootstrap);
}
