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
 *     public int onResponseBodyBorrowed(S3BorrowedBuffer buffer, long rangeStart, long rangeEnd) {
 *         try (S3BorrowedBuffer b = buffer) {   // MUST be closed on every path
 *             // read b.asByteBuffer() here
 *         }
 *         return (int) (rangeEnd - rangeStart);
 *     }
 * }
 * }</pre>
 *
 * <p>A pool on the client and this interface on the handler are both required.
 * With only one of the two, chunks keep arriving via
 * {@link S3MetaRequestResponseHandler#onResponseBody(java.nio.ByteBuffer, long, long)}.
 * The opt-in is an interface rather than a method so that the compiler checks
 * it: a wrong name or signature fails the build instead of silently falling
 * back to heap delivery.</p>
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
