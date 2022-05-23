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
     * Invoked to provide the request body as it is received.
     *
     * @param bodyBytesIn The body data for this chunk of the object
     * @param objectRangeStart The byte index of the object that this refers to. For example, for an HTTP message that
     *  has a range header, the first chunk received will have a range_start that matches the range header's range-start
     * @param objectRangeEnd corresponds to the past-of-end chunk offset, i.e. objectRangeStart + the chunk length
     * @return The number of bytes to move the sliding window by. Repeatedly returning zero will eventually cause the
     * sliding window to fill up and data to stop flowing until the user slides the window back open.
     */
    default int onResponseBody(ByteBuffer bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
        return 0;
    }

    /**
     * Invoked when the entire meta request execution is complete.
     *
     * @param errorCode The CRT error code
     * @param responseStatus statusCode of the HTTP response
     * @param errorPayload body of the error response. Can be null if the request completed successfully
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
