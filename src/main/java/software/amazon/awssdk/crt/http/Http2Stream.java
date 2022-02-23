/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.http;

import software.amazon.awssdk.crt.CrtRuntimeException;

import java.util.concurrent.CompletableFuture;

public class Http2Stream extends HttpStream {

    protected Http2Stream(long ptr) {
        super(ptr);
    }

    /**
     * Reset the HTTP/2 stream. Note that if the stream closes before this async
     * call is fully processed, the RST_STREAM frame will not be sent.
     *
     * @param http2_stream HTTP/2 stream.
     * @param http2_error  aws_http2_error_code. Reason to reset the stream.
     */
    public void resetStream(final Http2ClientConnection.Http2ErrorCode errorCode) {
        http2StreamResetStream(getNativeHandle(), errorCode.getValue());
    }

    /**
     * @TODO getters for reset stream. Not sure anyone needs it though.
     */
    private static native void http2StreamResetStream(long http_stream, int errorCode);

    @Override
    public void writeChunk(final byte[] chunkData, boolean isFinalChunk, final HttpStreamWriteChunkCompletionCallback chunkCompletionCallback) {
        throw new RuntimeException("Not implemented");
    }
    @Override
    public CompletableFuture<Void> writeChunk(final byte[] chunkData, boolean isFinalChunk) {
        throw new RuntimeException("Not implemented");
    }
}