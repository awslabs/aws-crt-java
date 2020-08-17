package software.amazon.awssdk.crt.io;

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;

public class ServerBootstrap extends CrtResource {

    public ServerBootstrap(final EventLoopGroup elg) {
        acquireNativeHandle(serverBootstrapNew(this, elg.getNativeHandle()));
        addReferenceTo(elg);
    }
    @Override
    protected void releaseNativeHandle() {
        if (!isNull()) {
            serverBootstrapDestroy(getNativeHandle());
        }
    }

    @Override
    protected boolean canReleaseReferencesImmediately() {
        return false;
    }

    private static native long serverBootstrapNew(ServerBootstrap bootstrap, long elg) throws CrtRuntimeException;
    private static native void serverBootstrapDestroy(long bootstrap);
}
