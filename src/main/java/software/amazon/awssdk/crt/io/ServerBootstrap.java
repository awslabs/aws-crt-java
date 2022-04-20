package software.amazon.awssdk.crt.io;

import software.amazon.awssdk.crt.CleanableCrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;

/**
 * This class wraps the aws_server_bootstrap from aws-c-io to provide
 * a server context for all protocol stacks in the AWS Common Runtime.
 */
public class ServerBootstrap extends CleanableCrtResource {

    /**
     * @param elg event loop group to map server connections into
     */
    public ServerBootstrap(final EventLoopGroup elg) {
        acquireNativeHandle(serverBootstrapNew(elg.getNativeHandle()), ServerBootstrap::serverBootstrapDestroy);
    }

    private static native long serverBootstrapNew(long elg) throws CrtRuntimeException;
    private static native void serverBootstrapDestroy(long bootstrap);
}
