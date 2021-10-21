/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.http;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;

import java.util.concurrent.CompletableFuture;

/**
 * An HttpStream represents a single Http Request/Response pair within a HttpClientConnection, and wraps the native resources
 * from the aws-c-http library.
 *
 * Can be used to update the Window size, or to abort the stream early in the middle of sending/receiving Http Bodies.
 */
public class HttpStream extends CrtResource {

    public interface HttpStreamWriteChunkCompletionCallback {
        void onChunkCompleted(int errorCode);
    }

    /* Native code will call this constructor during HttpClientConnection.makeRequest() */
    protected HttpStream(long ptr) {
        acquireNativeHandle(ptr);
    }

    /**
     * Determines whether a resource releases its dependencies at the same time the native handle is released or if it waits.
     * Resources that wait are responsible for calling releaseReferences() manually.
     */
    @Override
    protected boolean canReleaseReferencesImmediately() { return true; }

    /**
     * Cleans up the stream's associated native handle
     */
    @Override
    protected void releaseNativeHandle() {
        if (!isNull()) {
            httpStreamRelease(getNativeHandle());
        }
    }

    /**
     * Opens the Sliding Read/Write Window by the number of bytes passed as an argument for this HttpStream.
     *
     * This function should only be called if the user application previously returned less than the length of the input
     * ByteBuffer from a onResponseBody() call in a HttpStreamResponseHandler, and should be &lt;= to the total number of
     * un-acked bytes.
     *
     * @param windowSize How many bytes to increment the sliding window by.
     */
    public void incrementWindow(int windowSize) {
        if (windowSize < 0) {
            throw new IllegalArgumentException("windowSize must be >= 0. Actual value: " + windowSize);
        }
        if (!isNull()) {
            httpStreamIncrementWindow(getNativeHandle(), windowSize);
        }
    }

    /**
     * Use only for Http 1.1 Chunked Encoding. At some later point we may adapt this interface for H2, but not yet.
     * You must call activate() before using this function.
     *
     * @param chunkData chunk of data to send.
     * @param isFinalChunk if set to true, this will terminate the request stream.
     * @param chunkCompletionCallback Invoked upon the data being flushed to the wire or an error occurring.
     */
    public void writeChunk(final byte[] chunkData, boolean isFinalChunk, final HttpStreamWriteChunkCompletionCallback chunkCompletionCallback) {
        if (chunkCompletionCallback == null) {
            throw new IllegalArgumentException("You must supply a chunkCompletionCallback");
        }

        if (chunkData == null) {
            throw new IllegalArgumentException("You must provide a non-null chunkData");
        }

        int error = httpStreamWriteChunk(getNativeHandle(), chunkData, isFinalChunk, chunkCompletionCallback);

        if (error != 0) {
            int lastError = CRT.awsLastError();
            throw new CrtRuntimeException(lastError);
        }
    }

    /**
     * Use only for Http 1.1 Chunked Encoding. At some later point we may adapt this interface for H2, but not yet.
     * You must call activate() before using this function.
     *
     * @param chunkData chunk of data to send.
     * @param isFinalChunk if set to true, this will terminate the request stream.
     * @return completable future which will complete upon the data being flushed to the wire or an error occurring.
     */
    public CompletableFuture<Void> writeChunk(final byte[] chunkData, boolean isFinalChunk) {
        CompletableFuture<Void> completionFuture = new CompletableFuture<>();

        HttpStreamWriteChunkCompletionCallback completionCallback = new HttpStreamWriteChunkCompletionCallback() {
            @Override
            public void onChunkCompleted(int errorCode) {
                if (errorCode == 0) {
                    completionFuture.complete(null);
                } else {
                    completionFuture.completeExceptionally(new CrtRuntimeException(errorCode));
                }
            }
        };

        writeChunk(chunkData, isFinalChunk, completionCallback);
        return completionFuture;
    }

    /**
     * Activates the client stream.
     */
    public void activate() {
        if (!isNull()) {
            httpStreamActivate(getNativeHandle(), this);
        }
    }

    /**
     * Retrieves the Http Response Status Code
     * @return The Http Response Status Code
     */
    public int getResponseStatusCode() {
        if (!isNull()) {
            return httpStreamGetResponseStatusCode(getNativeHandle());
        }
        throw new IllegalStateException("Can't get Status Code on Closed Stream");
    }

    private static native void httpStreamRelease(long http_stream);
    private static native void httpStreamIncrementWindow(long http_stream, int window_size);
    private static native void httpStreamActivate(long http_stream, HttpStream streamObj);
    private static native int httpStreamGetResponseStatusCode(long http_stream);
    private static native int httpStreamWriteChunk(long http_stream, byte[] chunkData, boolean isFinalChunk, HttpStreamWriteChunkCompletionCallback completionCallback);
}
