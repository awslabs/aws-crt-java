package software.amazon.awssdk.crt.io;

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;

import java.io.Closeable;

public class HostResolver extends CrtResource implements Closeable {
    private final static int DEFAULT_MAX_ENTRIES = 8;
    private final EventLoopGroup elg;
    private final int maxEntries;

    public HostResolver(EventLoopGroup elg) throws CrtRuntimeException {
        this(elg, DEFAULT_MAX_ENTRIES);
    }

    public HostResolver(EventLoopGroup elg, int maxEntries) throws CrtRuntimeException {
        this.elg = elg;
        this.maxEntries = maxEntries;
        acquire(hostResolverNew(elg.native_ptr(), maxEntries));
    }

    @Override
    public void close() {
        if (native_ptr() != 0) {
            hostResolverRelease(release());
        }
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long hostResolverNew(long el_group, int max_entries) throws CrtRuntimeException;
    private static native void hostResolverRelease(long host_resolver);
}
