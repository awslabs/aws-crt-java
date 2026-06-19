/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.s3;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;

import software.amazon.awssdk.crt.http.HttpHeader;

class S3MetaRequestResponseHandlerNativeAdapter {
    private S3MetaRequestResponseHandler responseHandler;

    S3MetaRequestResponseHandlerNativeAdapter(S3MetaRequestResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    /**
     * Standard JNI path: native code allocates a {@code byte[]} containing a copy
     * of the response body chunk and passes it here. Used when {@code useFFM=false}.
     */
    int onResponseBody(byte[] bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
        return this.responseHandler.onResponseBody(ByteBuffer.wrap(bodyBytesIn), objectRangeStart, objectRangeEnd);
    }

    /**
     * FFM path: native code passes the raw native pointer ({@code address}) and
     * the chunk length ({@code length}) as {@code long} primitives — no heap
     * allocation, no copy. Used when {@code useFFM=true}.
     * <p>
     * We wrap the native memory as a {@link MemorySegment} scoped to a
     * {@link Arena#ofAuto() auto arena} so that the segment is valid for the
     * duration of this call but cannot be retained beyond it (the native buffer
     * is only guaranteed live for the callback's lifetime).
     *
     * @param address        Raw native pointer to the start of the body chunk.
     * @param length         Number of bytes in the chunk.
     * @param objectRangeStart Byte offset of the first byte within the S3 object.
     * @return The number of bytes to increment the flow-control window by.
     */
    int onResponseBodyFFM(long address, long length, long objectRangeStart) {
        // Wrap the native pointer as a MemorySegment — zero copy.
        // reinterpret() is required to give the segment a known size; the
        // Arena.ofAuto() cleanup action is null because the native side owns
        // the memory and will free it after the callback returns.
        MemorySegment segment = MemorySegment.ofAddress(address)
                .reinterpret(length, Arena.ofAuto(), null);
        long objectRangeEnd = objectRangeStart + length;
        return this.responseHandler.onResponseBody(segment, objectRangeStart, objectRangeEnd);
    }

    void onFinished(int errorCode, int responseStatus, byte[] errorPayload, String errorOperationName, int checksumAlgorithm, boolean didValidateChecksum, Throwable cause, final ByteBuffer headersBlob) {
        HttpHeader[] errorHeaders = headersBlob == null ? null : HttpHeader.loadHeadersFromMarshalledHeadersBlob(headersBlob);
        S3FinishedResponseContext context = new S3FinishedResponseContext(errorCode, responseStatus, errorPayload, errorOperationName, ChecksumAlgorithm.getEnumValueFromInteger(checksumAlgorithm), didValidateChecksum, cause, errorHeaders);
        this.responseHandler.onFinished(context);
    }

    void onResponseHeaders(final int statusCode, final ByteBuffer headersBlob) {
        responseHandler.onResponseHeaders(statusCode, HttpHeader.loadHeadersFromMarshalledHeadersBlob(headersBlob));
    }

    void onProgress(final S3MetaRequestProgress progress) {
        responseHandler.onProgress(progress);
    }

    void onTelemetry(final S3RequestMetrics requestMetrics) {
        responseHandler.onTelemetry(requestMetrics);
    }
}
