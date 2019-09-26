package software.amazon.awssdk.crt.io;

import java.nio.ByteBuffer;
import software.amazon.awssdk.crt.CrtResource;

/**
 * Wrapper class around a Java DirectByteBuffer and Native buffer that hooks into the CrtResource Memory Leak Tracker.
 */
public class CrtByteBuffer extends CrtResource {
    private CrtBufferPool pool;
    private final ByteBuffer directBuffer;

    public static CrtByteBuffer alloc(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be >= 0");
        }
        return newCrtByteBuffer(size);
    }

    /* Called by Native */
    private CrtByteBuffer(ByteBuffer javaBuffer, long nativeBufferPtr) {
        this.directBuffer = javaBuffer;
        acquireNativeHandle(nativeBufferPtr);
    }

    public void releaseBackToPool() {
        if (pool == null) {
            throw new IllegalStateException("No Pool Configured");
        }
        pool.releaseBuffer(this);
    }

    public ByteBuffer getBuffer() {
        if (isNull()) {
            throw new IllegalStateException("This CrtByteBuffer has been closed");
        }
        return this.directBuffer;
    }

    /**
     * Zero's out this Buffers Memory, and resets it's position and limit.
     */
    public void wipe() {
        zeroCrtByteBuffer(getNativeHandle(), directBuffer.capacity());
        // Set position to zero and limit to capacity.
        directBuffer.clear();
    }

    /**
     * Cleans up the buffer's associated native handle
     */
    @Override
    protected void releaseNativeHandle() {
        if (!isNull()) {
            releaseCrtByteBuffer(getNativeHandle());
        }
    }

    /**
     * Determines whether a resource releases its dependencies at the same time the native handle is released or if it waits.
     * Resources that wait are responsible for calling releaseReferences() manually.
     */
    @Override
    protected boolean canReleaseReferencesImmediately() { return true; }

    protected CrtByteBuffer withPool(CrtBufferPool pool) {
        if (this.pool != null) {
            throw new IllegalStateException("Pool already configured");
        }

        this.pool = pool;
        return this;
    }

    // Native Methods
    private static native CrtByteBuffer newCrtByteBuffer(int size);
    private static native void zeroCrtByteBuffer(long thisPtr, int capacity);
    private static native void releaseCrtByteBuffer(long thisPtr);

}
