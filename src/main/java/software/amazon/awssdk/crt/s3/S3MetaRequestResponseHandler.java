/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.s3;

import java.lang.foreign.MemorySegment;
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
     * FFM variant of {@link #onResponseBody(ByteBuffer, long, long)}.
     * <p>
     * Invoked instead of the {@code ByteBuffer} overload when the meta request was
     * created with {@link S3MetaRequestOptions#withUseFFM(boolean) useFFM=true}.
     * The body data is delivered as a {@link MemorySegment} that is a zero-copy
     * view directly into native (off-heap) memory — no {@code byte[]} is allocated
     * and no data is copied.
     * <p>
     * <b>Do NOT</b> retain a reference to {@code bodyBytesIn} beyond the lifetime
     * of this method call. The underlying native memory is only guaranteed to be
     * valid for the duration of the callback.
     * <p>
     * The default implementation falls back to the {@code ByteBuffer} overload by
     * copying the data, so existing implementations that only override the
     * {@code ByteBuffer} version continue to work correctly.
     *
     * @param bodyBytesIn     A zero-copy view of the native response body chunk.
     * @param objectRangeStart Byte offset of the first byte in this chunk within
     *                         the full S3 object.
     * @param objectRangeEnd   Past-the-end byte offset (i.e.
     *                         {@code objectRangeStart + chunk length}).
     * @return The number of bytes to increment the flow-control window by.
     *         Ignored when backpressure is disabled.
     */
    default int onResponseBody(MemorySegment bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
        // Default: copy into a heap ByteBuffer and delegate to the ByteBuffer overload
        // so that implementations that only override the ByteBuffer version still work.
        ByteBuffer buf = ByteBuffer.wrap(bodyBytesIn.toArray(java.lang.foreign.ValueLayout.JAVA_BYTE));
        return onResponseBody(buf, objectRangeStart, objectRangeEnd);
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
}
