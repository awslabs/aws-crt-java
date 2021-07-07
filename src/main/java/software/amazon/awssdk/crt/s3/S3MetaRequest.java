package software.amazon.awssdk.crt.s3;

import java.util.concurrent.CompletableFuture;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.Log;

public class S3MetaRequest extends CrtResource {

    private final CompletableFuture<Void> shutdownComplete = new CompletableFuture<>();

    public S3MetaRequest() {

    }

    private void onShutdownComplete() {
        releaseReferences();
        
        this.shutdownComplete.complete(null);
    }

    /**
     * Determines whether a resource releases its dependencies at the same time the
     * native handle is released or if it waits. Resources that wait are responsible
     * for calling releaseReferences() manually.
     */
    @Override
    protected boolean canReleaseReferencesImmediately() {
        return false;
    }

    /**
     * Cleans up the native resources associated with this client. The client is
     * unusable after this call
     */
    @Override
    protected void releaseNativeHandle() {
        if (!isNull()) {
            s3MetaRequestDestroy(getNativeHandle());
        }
    }

    void setMetaRequestNativeHandle(long nativeHandle) {
        acquireNativeHandle(nativeHandle);
    }

    public CompletableFuture<Void> getShutdownCompleteFuture() { return shutdownComplete; }

    public void cancel() {
        s3MetaRequestCancel(getNativeHandle());
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native void s3MetaRequestDestroy(long s3MetaRequest);

    private static native void s3MetaRequestCancel(long s3MetaRequest);
}
