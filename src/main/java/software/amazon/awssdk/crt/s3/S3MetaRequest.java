package software.amazon.awssdk.crt.s3;

import java.util.concurrent.CompletableFuture;
import software.amazon.awssdk.crt.CrtResource;

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

    /**
     * Pauses meta request and returns a token that can be used to resume a meta request.
     * For PutObject resume, input stream should always start at the begining,
     * already uploaded parts will be skipped, but checksums on those will be verified if request specified checksum algo. 
     * @return token to resume request. might be null if request has not started executing yet
     */
    public String pause() {
        return s3MetaRequestPause(getNativeHandle());
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native void s3MetaRequestDestroy(long s3MetaRequest);

    private static native void s3MetaRequestCancel(long s3MetaRequest);

    private static native String s3MetaRequestPause(long s3MetaRequest);
}
