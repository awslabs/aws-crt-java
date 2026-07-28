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
            /**
             * Cancel the meta request before drop the refcount.
             * The meta request is not referenced by Java any longer, everything from native to Java will be ignored.
             * Cancelling the meta request instead of letting it keep flowing.
             * Note: If the meta request has not finished yet, it will be finished with `AWS_ERROR_S3_CANCELED`.
             **/
            s3MetaRequestCancel(getNativeHandle());
            s3MetaRequestDestroy(getNativeHandle());
        }
    }

    void setMetaRequestNativeHandle(long nativeHandle) {
        acquireNativeHandle(nativeHandle);
    }

    public CompletableFuture<Void> getShutdownCompleteFuture() { return shutdownComplete; }

    public void cancel() {
        if (isNull()) {
            throw new IllegalStateException("S3MetaRequest has been closed.");
        }
        s3MetaRequestCancel(getNativeHandle());
    }

    /**
     * Pauses meta request and returns a token that can be used to resume a meta request.
     * For PutObject resume, input stream should always start at the beginning,
     * already uploaded parts will be skipped, but checksums on those will be verified if request specified checksum algo.
     * @return token to resume request. might be null if request has not started executing yet
     */
    public ResumeToken pause() {
        if (isNull()) {
            throw new IllegalStateException("S3MetaRequest has been closed.");
        }
        return s3MetaRequestPause(getNativeHandle());
    }

    /**
     * Asynchronously pause the meta request. Works for both uploads (PUT) and downloads (GET).
     * The returned future completes once all in-flight work has finished (in-flight parts for
     * uploads, file writes for downloads) and the resume token is ready.
     * <p>
     * For PutObject resume, input stream should always start at the beginning,
     * already uploaded parts will be skipped, but checksums on those will be verified if
     * the request specified a checksum algorithm.
     * <p>
     * Note: consuming a download (GET) resume token to resume via meta request options is not
     * supported yet. To resume a download, issue a new ranged GET starting at
     * {@link ResumeToken#getContinuesDownloadedBytes()} (offset from
     * {@link ResumeToken#getObjectRangeStart()}) through the end of the original download.
     *
     * @return future completed with the resume token once the pause completes. The token may be
     *         null if the request had not progressed far enough to produce one (equivalent to
     *         restarting the transfer). Completed exceptionally with a CrtRuntimeException if
     *         the pause failed.
     */
    public CompletableFuture<ResumeToken> pauseAsync() {
        if (isNull()) {
            throw new IllegalStateException("S3MetaRequest has been closed.");
        }
        CompletableFuture<ResumeToken> future = new CompletableFuture<>();
        try {
            s3MetaRequestPauseAsync(getNativeHandle(), future);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
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
        if (isNull()) {
            throw new IllegalStateException("S3MetaRequest has been closed.");
        }
        s3MetaRequestIncrementReadWindow(getNativeHandle(), bytes);
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native void s3MetaRequestDestroy(long s3MetaRequest);

    private static native void s3MetaRequestCancel(long s3MetaRequest);

    private static native ResumeToken s3MetaRequestPause(long s3MetaRequest);

    private static native void s3MetaRequestPauseAsync(long s3MetaRequest, CompletableFuture<ResumeToken> future);

    private static native void s3MetaRequestIncrementReadWindow(long s3MetaRequest, long bytes);
}
