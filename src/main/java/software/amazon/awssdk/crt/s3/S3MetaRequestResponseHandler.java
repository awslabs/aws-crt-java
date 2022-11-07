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
     * Invoked to provide response headers received during execution of the meta request, both for
     * success and error HTTP status codes.
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
     * <p>
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
     * Currently, the progress callback is invoked only for the CopyObject meta request type.
     * TODO: support this callback for all types of meta requests
     * @param progress information about the progress of the meta request execution
     */
    default void onProgress(final S3MetaRequestProgress progress) {
    }
}
