package software.amazon.awssdk.crt.s3;

import java.util.concurrent.CompletableFuture;
import software.amazon.awssdk.crt.CleanableCrtResource;

public class S3MetaRequest extends CleanableCrtResource {

    private final CompletableFuture<Void> shutdownComplete = new CompletableFuture<>();

    public S3MetaRequest() {

    }

    void setMetaRequestNativeHandle(long nativeHandle) {
        acquireNativeHandle(nativeHandle, S3MetaRequest::s3MetaRequestDestroy);
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
