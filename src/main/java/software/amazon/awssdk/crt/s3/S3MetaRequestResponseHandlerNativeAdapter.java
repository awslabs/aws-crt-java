/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.s3;

import software.amazon.awssdk.crt.http.HttpHeader;

import java.nio.ByteBuffer;

class S3MetaRequestResponseHandlerNativeAdapter {
    private S3MetaRequestResponseHandler responseHandler;

    /**
     * The handler's zero-copy delivery, or null when this request uses byte[]
     * delivery because the handler did not opt in or the client has no pool to
     * borrow from. Resolved once here rather than per chunk.
     */
    private final S3BorrowedBufferDelivery borrowedDelivery;

    /**
     * @param responseHandler      the customer's handler
     * @param clientHasBufferPool  whether the client has a direct buffer pool;
     *                             without one there is nothing to borrow, so
     *                             the handler's opt-in is ignored
     */
    S3MetaRequestResponseHandlerNativeAdapter(S3MetaRequestResponseHandler responseHandler,
                                              boolean clientHasBufferPool) {
        this.responseHandler = responseHandler;
        this.borrowedDelivery = clientHasBufferPool && responseHandler instanceof S3BorrowedBufferDelivery
                ? (S3BorrowedBufferDelivery) responseHandler
                : null;
    }

    /**
     * Passed to native with each meta request, to select body_callback_ex
     * (borrowed-buffer path) or body_callback (byte[] copy).
     */
    boolean usesBorrowedDelivery() {
        return borrowedDelivery != null;
    }

    int onResponseBody(byte[] bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
        return this.responseHandler.onResponseBody(ByteBuffer.wrap(bodyBytesIn), objectRangeStart, objectRangeEnd);
    }

    /**
     * Borrowed-buffer (zero-copy) delivery; called from native, and only for a
     * request whose usesBorrowedDelivery() was true. A null here would mean
     * native selected body_callback_ex for a request that did not ask for it,
     * so the NPE is the correct outcome: it fails that meta request loudly
     * rather than quietly copying and hiding the dispatch bug.
     */
    int onResponseBodyBorrowed(S3BorrowedBuffer buffer, long objectRangeStart, long objectRangeEnd) {
        return this.borrowedDelivery.onResponseBodyBorrowed(buffer, objectRangeStart, objectRangeEnd);
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

    void onErrorResumeToken(final int errorCode, final ResumeToken resumeToken) {
        responseHandler.onErrorResumeToken(errorCode, resumeToken);
    }
}
