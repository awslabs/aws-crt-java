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

    /**
     * Tracks whether this stream was created with a body stream on the request.
     * Used to guard against calling writeData() on a stream that already has a body
     * stream — HTTP/1.1 requires exactly one body framing mechanism per message
     * (RFC 9112, Section 6), so a body stream and manual writes cannot coexist.
     */
    private boolean hasBodyStream = false;

    /*
     * Native code will call this constructor during
     * HttpClientConnection.makeRequest()
     */
    protected HttpStream(long ptr) {
        super(ptr);
    }

    /**
     * Package-private. Called by HttpClientConnection.makeRequest() to record
     * whether the originating request had a body stream attached.
     */
    void setHasBodyStream(boolean hasBodyStream) {
        this.hasBodyStream = hasBodyStream;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>HTTP/1.1 restriction:</b> An HTTP/1.1 request message body is framed by
     * exactly one mechanism: either a {@code Content-Length} header declaring the
     * body size upfront, or {@code Transfer-Encoding: chunked} for streaming
     * (RFC 9112, Section 6). A sender MUST NOT combine both framing mechanisms
     * (RFC 9112, Section 6.2: "A sender MUST NOT send a Content-Length header field
     * in any message that contains a Transfer-Encoding header field").
     * <p>
     * Because the framing is committed at the start of the message, a body stream
     * and manual {@code writeData()} calls cannot coexist on the same HTTP/1.1
     * stream — doing so would either violate the declared {@code Content-Length} or
     * require switching transfer-encoding mid-message, which the protocol does not
     * permit. If the request was created with an {@link HttpRequestBodyStream},
     * calling this method will throw {@link IllegalStateException}.
     * <p>
     * HTTP/2 does not have this restriction. HTTP/2 uses its own DATA frame
     * framing (RFC 9113, Section 8.1), where the body stream and manual writes
     * both append DATA frames to the same outgoing queue.
     * <p>
     * <b>Migration from writeChunk:</b> This method supersedes the deprecated
     * {@link #writeChunk} methods. Use {@code writeData} for all manual body data
     * writes on both HTTP/1.1 and HTTP/2 streams.
     */
    @Override
    public void writeData(final byte[] data, boolean endStream,
            final HttpStreamWriteDataCompletionCallback completionCallback) {
        if (hasBodyStream) {
            throw new IllegalStateException(
                "Cannot call writeData() on an HTTP/1.1 stream that was created with a body stream. "
                + "HTTP/1.1 requires exactly one body framing mechanism per message (RFC 9112, Section 6). "
                + "A body stream and manual writeData() calls cannot coexist. "
                + "Create the stream with useManualDataWrites=true and no body stream to use writeData().");
        }
        super.writeData(data, endStream, completionCallback);
    }

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
     * @deprecated Use {@link HttpStreamBase#writeData(byte[], boolean, HttpStreamWriteDataCompletionCallback)} instead.
     *             writeData() works for both HTTP/1.1 and HTTP/2, whereas writeChunk() is HTTP/1.1 only.
     */
    @Deprecated
    public void writeChunk(final byte[] chunkData, boolean isFinalChunk,
            final HttpStreamWriteChunkCompletionCallback chunkCompletionCallback) {
        if (isNull()) {
            throw new IllegalStateException("HttpStream has been closed.");
        }

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
     * @deprecated Use {@link HttpStreamBase#writeData(byte[], boolean)} instead.
     *             writeData() works for both HTTP/1.1 and HTTP/2, whereas writeChunk() is HTTP/1.1 only.
     */
    @Deprecated
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
