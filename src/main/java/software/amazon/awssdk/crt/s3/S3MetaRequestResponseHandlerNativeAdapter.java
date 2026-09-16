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
     * True iff the customer's handler overrides
     * {@link S3MetaRequestResponseHandler#onResponseBody(S3BorrowedBuffer, long, long)}.
     * Detected once at construction via reflection so the native side can
     * decide which aws-c-s3 body callback to register at meta-request
     * creation time (see {@code s3ClientMakeMetaRequest} in
     * {@code src/native/s3_client.c}). Called from native via a cached
     * jmethodID on {@link #getSupportsBorrowedBufferOverload()}.
     */
    private final boolean supportsBorrowedBufferOverload;

    S3MetaRequestResponseHandlerNativeAdapter(S3MetaRequestResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
        this.supportsBorrowedBufferOverload = detectBorrowedBufferOverload(responseHandler);
    }

    /**
     * Reflection probe: does the handler override the borrowed-buffer body
     * callback overload? If the {@code getDeclaringClass()} of the resolved
     * method is the interface itself, only the default is present and the
     * customer has NOT opted in.
     *
     * <p>Called exactly once per adapter (per meta-request). Reflection
     * lookup happens off the hot path.</p>
     */
    private static boolean detectBorrowedBufferOverload(S3MetaRequestResponseHandler handler) {
        try {
            java.lang.reflect.Method m = handler.getClass().getMethod(
                "onResponseBody", S3BorrowedBuffer.class, long.class, long.class);
            return m.getDeclaringClass() != S3MetaRequestResponseHandler.class;
        } catch (NoSuchMethodException e) {
            // The default is defined on S3MetaRequestResponseHandler so this
            // should never happen for a well-formed handler. Treat as "not
            // opted in" and fall back to the ByteBuffer/byte[] paths.
            return false;
        }
    }

    /**
     * Package-private accessor invoked from native ({@code s3ClientMakeMetaRequest})
     * to select the aws-c-s3 body callback: {@code body_callback_ex} when
     * this returns true AND a DBZ pool is attached; {@code body_callback}
     * otherwise.
     */
    boolean getSupportsBorrowedBufferOverload() {
        return supportsBorrowedBufferOverload;
    }

    int onResponseBody(byte[] bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
        return this.responseHandler.onResponseBody(ByteBuffer.wrap(bodyBytesIn), objectRangeStart, objectRangeEnd);
    }

    // Direct ByteBuffer path. Called by the pool-aware callback in
    // s3_client.c when the client was constructed with a pool. The
    // delivered ByteBuffer is a slice over pool-owned memory.
    //
    // WARNING: The ByteBuffer is valid only for the duration of this
    //          call. The user's handler MUST consume or copy the bytes
    //          before returning. See S3MetaRequestResponseHandler
    //          Javadoc for the contract.
    int onResponseBody(ByteBuffer bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
        return this.responseHandler.onResponseBody(
            bodyBytesIn, objectRangeStart, objectRangeEnd);
    }

    /**
     * Borrowed-buffer path: called from native ({@code s_on_body_ex_dbz})
     * when the handler has opted in and a DBZ pool is attached. The native
     * side has already acquired an extra ref on the underlying ticket and
     * constructed the {@link S3BorrowedBuffer}; the customer owns the slot's
     * lifetime from here on and MUST close it (or use one of the auto-close
     * paths like {@link S3BorrowedBuffer#toByteArray()}).
     */
    int onResponseBody(S3BorrowedBuffer buffer, long objectRangeStart, long objectRangeEnd) {
        return this.responseHandler.onResponseBody(buffer, objectRangeStart, objectRangeEnd);
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
