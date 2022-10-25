/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
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
     * For PutObject resume, input stream should always start at the beginning,
     * already uploaded parts will be skipped, but checksums on those will be verified if request specified checksum algo.
     * @return token to resume request. might be null if request has not started executing yet
     */
    public String pause() {
        return s3MetaRequestPause(getNativeHandle());
    }

    /**
     * Increment the flow-control window, so that response data continues downloading.
     * <p>
     * If the client was created with {@link S3ClientOptions#withReadBackpressureEnabled} set true,
     * each S3MetaRequest has a flow-control window that shrinks as response
     * body data is downloaded (headers do not affect the size of the window).
     * {@link S3ClientOptions#withInitialReadWindowSize} sets the starting size for each S3MetaRequest's window.
     * Whenever the window reaches zero, data stops downloading.
     * Increment the window to keep data flowing.
     * Maintain a larger window to keep up a high download throughput,
     * parts cannot download in parallel unless the window is large enough to hold multiple parts.
     * Maintain a smaller window to limit the amount of data buffered in memory.
     * <p>
     * If backpressure is disabled this call has no effect, data is downloaded as fast as possible.
     * <p>
     * WARNING: This feature is experimental.
     * Currently, backpressure is only applied to GetObject requests which are split into multiple parts,
     * and you may still receive some data after the window reaches zero.
     *
     * @param bytes size to increment window by

     * @see S3ClientOptions#withReadBackpressureEnabled
     */
    public void incrementReadWindow(long bytes) {
        s3MetaRequestIncrementReadWindow(getNativeHandle(), bytes);
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native void s3MetaRequestDestroy(long s3MetaRequest);

    private static native void s3MetaRequestCancel(long s3MetaRequest);

    private static native String s3MetaRequestPause(long s3MetaRequest);

    private static native void s3MetaRequestIncrementReadWindow(long s3MetaRequest, long bytes);
}
