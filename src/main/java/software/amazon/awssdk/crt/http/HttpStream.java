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
 * An HttpStream represents a single HTTP/1.1 specific Http Request/Response.
 */
public class HttpStream extends HttpStreamBase {

    /*
     * Native code will call this constructor during
     * HttpClientConnection.makeRequest()
     */
    protected HttpStream(long ptr) {
        super(ptr);
    }

    /*******************************************************************************
     * Shared method
     ******************************************************************************/
    /**
     * Completion interface for writing chunks to an http stream
     */
    public interface HttpStreamWriteChunkCompletionCallback {
        void onChunkCompleted(int errorCode);
    }

    /**
     * Use only for Http 1.1 Chunked Encoding.
     * You must call activate() before using this function.
     *
     * @param chunkData               chunk of data to send.
     * @param isFinalChunk            if set to true, this will terminate the
     *                                request stream.
     * @param chunkCompletionCallback Invoked upon the data being flushed to the
     *                                wire or an error occurring.
     */
    public void writeChunk(final byte[] chunkData, boolean isFinalChunk,
            final HttpStreamWriteChunkCompletionCallback chunkCompletionCallback) {
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
     * Use only for Http 1.1 Chunked Encoding.
     * You must call activate() before using this function.
     *
     * @param chunkData    chunk of data to send.
     * @param isFinalChunk if set to true, this will terminate the request stream.
     * @return completable future which will complete upon the data being flushed to
     *         the wire or an error occurring.
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

    /*******************************************************************************
     * Native methods
     ******************************************************************************/

    private static native int httpStreamWriteChunk(long http_stream, byte[] chunkData, boolean isFinalChunk,
            HttpStreamWriteChunkCompletionCallback completionCallback);
}
