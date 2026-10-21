/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.s3;

/**
 * Opts a response handler into zero-copy delivery, where each body chunk
 * arrives as a view into the client's
 * {@link S3ClientOptions#withDirectBufferPoolOptions direct buffer pool}
 * rather than being copied into a heap {@code byte[]}. Implement it alongside
 * {@link S3MetaRequestResponseHandler}:
 *
 * <pre>{@code
 * class MyHandler implements S3MetaRequestResponseHandler, S3BorrowedBufferDelivery {
 *     public int onResponseBodyBorrowed(S3BorrowedBuffer buffer,
 *                                       long objectRangeStart, long objectRangeEnd) {
 *         try (S3BorrowedBuffer b = buffer) {   // MUST be closed on every path
 *             // read b.asByteBuffer() here
 *         }
 *         return (int) (objectRangeEnd - objectRangeStart);
 *     }
 * }
 * }</pre>
 *
 * <p>A direct buffer pool on the client and this interface on the handler are both required.
 * With only one of the two, chunks keep arriving via
 * {@link S3MetaRequestResponseHandler#onResponseBody(java.nio.ByteBuffer, long, long)}.
 * Because the opt-in is an interface, a wrong method name or signature fails
 * the build rather than quietly reverting to heap delivery.</p>
 *
 * @see S3BorrowedBuffer
 * @see S3ClientOptions#withDirectBufferPoolOptions
 */
public interface S3BorrowedBufferDelivery {

    /**
     * Invoked with one chunk of the response body, read straight from pool
     * memory, instead of
     * {@link S3MetaRequestResponseHandler#onResponseBody(java.nio.ByteBuffer, long, long)}.
     * Throwing from here fails the meta request.
     *
     * <p><b>Return promptly.</b> The pool slot backing {@code buffer} stays
     * leased for as long as this method runs, and then until
     * {@link S3BorrowedBuffer#close()}. Time spent here is pool capacity
     * withheld from every other request: a ceiling of N parts allows only N
     * deliveries at once, so a handler that hashes, compresses, or writes to
     * slow storage inline can stall unrelated requests, and the sizing
     * guidance on {@link S3ClientOptions#withDirectBufferPoolOptions} assumes
     * it does not. Do that work after the buffer is closed, or on a copy taken
     * with {@link S3BorrowedBuffer#toByteArray()}, which is safe to hand to
     * another thread.</p>
     *
     * @param buffer a borrowed view into pool memory, which MUST always be
     *               closed. See {@link S3BorrowedBuffer} for the lifetime,
     *               leak-detection and flow-control rules.
     * @param objectRangeStart the byte index of the object this chunk starts at
     * @param objectRangeEnd   {@code objectRangeStart + buffer.asByteBuffer().remaining()}
     * @return bytes to increment the read window by, as for
     *         {@link S3MetaRequestResponseHandler#onResponseBody(java.nio.ByteBuffer, long, long)}
     */
    int onResponseBodyBorrowed(S3BorrowedBuffer buffer, long objectRangeStart, long objectRangeEnd);
}
