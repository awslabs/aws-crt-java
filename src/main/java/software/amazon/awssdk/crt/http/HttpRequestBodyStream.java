/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.http;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;

/**
 * Interface that Native code knows how to call when handling Http Request bodies
 *
 */
public interface HttpRequestBodyStream {


    /**
     * Called from Native when the Http Request has a Body (Eg PUT/POST requests).
     * Note that this function may be called many times as Native sends the Request Body.
     *
     * Do NOT keep a reference to this ByteBuffer past the lifetime of this function call. The CommonRuntime reserves
     * the right to use DirectByteBuffers pointing to memory that only lives as long as the function call.
     *
     * @param bodyBytesOut The Buffer to write the Request Body Bytes to.
     * @return True if Request body is complete, false otherwise.
     */
    default boolean sendRequestBody(ByteBuffer bodyBytesOut) {
        /* Optional Callback, return empty request body by default unless user wants to return one. */
        return true;
    }

    /**
     * FFM variant of {@link #sendRequestBody(ByteBuffer)}.
     * <p>
     * Called from native when the meta request was created with
     * {@link software.amazon.awssdk.crt.s3.S3MetaRequestOptions#withUseFFM(boolean)
     * useFFM=true}. The native layer passes the destination buffer as a raw
     * pointer ({@code address}) and its capacity ({@code length}) as {@code long}
     * primitives — no {@code DirectByteBuffer} wrapper object is allocated.
     * <p>
     * Implementations should write up to {@code length} bytes of request body
     * data into the native buffer starting at {@code address} and return the
     * number of bytes actually written. Returning {@code 0} signals that the
     * body is complete (end-of-stream).
     * <p>
     * The default implementation bridges to the {@link #sendRequestBody(ByteBuffer)}
     * overload via a {@link MemorySegment} → {@link ByteBuffer} view, so existing
     * implementations that only override the {@code ByteBuffer} version continue
     * to work correctly (at the cost of the {@code ByteBuffer} wrapper allocation
     * that FFM mode is trying to avoid).
     *
     * @param address Raw native pointer to the start of the destination buffer.
     * @param length  Capacity of the destination buffer in bytes.
     * @return Number of bytes written into the buffer, or {@code 0} when the
     *         body is fully consumed (end-of-stream).
     */
    default int sendRequestBody(long address, long length) {
        // Default: wrap the native buffer as a ByteBuffer and delegate to the
        // existing overload so that implementations that only override the
        // ByteBuffer version still work.
        MemorySegment seg = MemorySegment.ofAddress(address)
                .reinterpret(length, Arena.ofAuto(), null);
        ByteBuffer buf = seg.asByteBuffer();
        boolean done = sendRequestBody(buf);
        // Return how many bytes were written (ByteBuffer.position() tracks this).
        // If done==true and nothing was written, return 0 to signal end-of-stream.
        return buf.position();
    }

    /**
     * Called from native when the processing needs the stream to rewind itself back to its beginning.
     * If the stream does not support rewinding or the rewind fails, false should be returned
     *
     * Signing requires a rewindable stream, but basic http does not.
     *
     * @return True if the stream was successfully rewound, false otherwise.
     */
    default boolean resetPosition() { return false; }

    /**
     * Called from native when the processing needs to know the length of the stream.
     * If the stream does not know/support length, 0 should be returned.
     *
     * Signing requires a rewindable stream, but basic http does not.
     *
     * @return Stream length, or 0 if unknown stream or length is unsupported
     */
    default long getLength() { return 0; }
}
