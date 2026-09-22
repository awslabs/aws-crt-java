/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.s3;

import java.nio.ByteBuffer;
import software.amazon.awssdk.crt.http.HttpHeader;

/**
 * Interface called by native code to provide S3MetaRequest responses.
 */
public interface S3MetaRequestResponseHandler {

    /**
     * Invoked to provide response headers received during the execution of the meta request.
     * Note: the statusCode in this callback is not the final statusCode. It is possible that the statusCode in `onResponseHeaders`
     * is 200, and then the request fail leading to a different statusCode in the final `onFinished` callback.
     *
     * @param statusCode statusCode of the HTTP response
     * @param headers the headers received
     */
    default void onResponseHeaders(final int statusCode, final HttpHeader[] headers) {
    }

    /**
     * Invoked to provide the response body as it is received.
     * <p>
     * Note that if the client was created with {@link S3ClientOptions#withReadBackpressureEnabled} set true,
     * you must maintain the flow-control window.
     * The flow-control window shrinks as you receive body data via this callback.
     * Whenever the flow-control window reaches zero, data will stop downloading.
     * To keep data flowing, you must increment the window by returning a number
     * from this method, or by calling {@link S3MetaRequest#incrementReadWindow}.
     * </p>
     * If backpressure is disabled, you do not need to maintain the flow-control window,
     * data will arrive as fast as possible.
     *
     * @param bodyBytesIn The body data for this chunk of the object
     * @param objectRangeStart The byte index of the object that this refers to. For example, for an HTTP message that
     *  has a range header, the first chunk received will have a range_start that matches the range header's range-start
     * @param objectRangeEnd corresponds to the past-of-end chunk offset, i.e. objectRangeStart + the chunk length
     * @return The number of bytes to increment the flow-control window by
     * (calling {@link S3MetaRequest#incrementReadWindow} has the same effect).
     * This value is ignored if backpressure is disabled.
     *
     * @see S3ClientOptions#withReadBackpressureEnabled
     */
    default int onResponseBody(ByteBuffer bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
        return 0;
    }

    /**
     * Invoked when the entire meta request execution is complete.
     * @param context a wrapper object containing the following fields
     */
    default void onFinished(S3FinishedResponseContext context) {
    }

    /**
     * Invoked to report progress of the meta request execution.
     * The meaning of "progress" depends on the {@link S3MetaRequestOptions.MetaRequestType}.
     * For PUT_OBJECT, it refers to bytes uploaded.
     * For COPY_OBJECT, it refers to bytes copied.
     * For GET_OBJECT, it refers to bytes downloaded.
     * For anything else, it refers to response body bytes received.
     * @param progress information about the progress of the meta request execution
     */
    default void onProgress(final S3MetaRequestProgress progress) {
    }

    /**
     * Invoked to report telemetry of every request made to S3.
     * Each meta request may or may not be split into multiple requests for faster execution.
     * However, when it is split, each request is considered as an independent ranged_get/upload_part
     * and receives its own set of metrics with details irrespective of success or failure.
     * More details on the specific metrics collected is provided on {@link S3RequestMetrics}
     * @param requestMetrics telemetry data for an individual http request attempt within the meta request
     */
    default void onTelemetry(S3RequestMetrics requestMetrics) {
    }

    /**
     * Invoked with a resume token when the meta request fails unexpectedly.
     * Allows persisting state for later resume without re-transferring completed parts.
     * Supported for both upload (PUT) and download (GET) meta requests.
     * Always invoked exactly once on unexpected failure; not invoked on success or
     * explicit pause (use {@link S3MetaRequest#pauseAsync} for that).
     * The token is null when no resumable state was captured.
     * <p>
     * WARNING: for a file download with
     * {@link S3MetaRequestOptions#withResponseFileDeleteOnFailure} set true, the deletion
     * is respected. The partial file is deleted on error, leaving nothing to resume on,
     * and this callback fires with a null token. Do not set responseFileDeleteOnFailure
     * if you intend to resume from this callback's token.
     *
     * @param errorCode the CRT error code that caused the meta request to fail
     * @param resumeToken resumable state captured at failure time, null if no
     *        resumable state was captured
     */
    default void onErrorResumeToken(final int errorCode, final ResumeToken resumeToken) {
    }

    /**
     * Optional zero-copy overload: invoked instead of
     * {@link #onResponseBody(ByteBuffer, long, long)} when the handler
     * overrides this method AND a
     * {@link S3ClientOptions#withDirectByteBufferPool direct buffer pool}
     * is attached to the client.
     *
     * <p>The {@link S3BorrowedBuffer} keeps the underlying pool slot alive
     * until {@link S3BorrowedBuffer#close() close()} is called, letting the
     * customer hold the buffer across async boundaries (e.g. queuing into a
     * reactive publisher, writing to {@code AsynchronousFileChannel}). The
     * buffer's contract requires an explicit close. See the
     * {@link S3BorrowedBuffer} class Javadoc for the lifetime rules.</p>
     *
     * <p>Handlers that do NOT override this method never receive borrowed
     * delivery: the client uses the {@code byte[]}-copy path and their bytes
     * arrive through {@link #onResponseBody(ByteBuffer, long, long)} with its
     * usual safe-to-retain contract. This makes opt-in explicit. Customers
     * who never touch this method see zero behavior change when a pool is
     * attached. Zero-copy delivery is ONLY available by overriding this
     * method.</p>
     *
     * @param buffer  a borrowed direct-buffer view into pool memory;
     *                MUST be closed by the customer if not consumed
     *                synchronously
     * @param objectRangeStart the byte index of the object that this refers
     *                         to (matches the ByteBuffer overload semantics)
     * @param objectRangeEnd   {@code objectRangeStart + buffer.asByteBuffer().remaining()}
     * @return the number of bytes to increment the read window by (same as
     *         the ByteBuffer overload)
     * @see S3BorrowedBuffer
     * @see S3ClientOptions#withDirectByteBufferPool
     */
    default int onResponseBody(S3BorrowedBuffer buffer, long objectRangeStart, long objectRangeEnd) {
        // Unreachable via normal client dispatch (native only routes here
        // when the handler overrides this method), but kept safe for direct
        // invocation: copy to heap so the ByteBuffer overload's
        // safe-to-retain contract holds unconditionally.
        try (S3BorrowedBuffer autoClose = buffer) {
            return onResponseBody(ByteBuffer.wrap(buffer.toByteArray()), objectRangeStart, objectRangeEnd);
        }
    }
}
