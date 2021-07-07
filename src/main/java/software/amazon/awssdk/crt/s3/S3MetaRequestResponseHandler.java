package software.amazon.awssdk.crt.s3;

import java.nio.ByteBuffer;
import software.amazon.awssdk.crt.http.HttpHeader;

public interface S3MetaRequestResponseHandler {
    default void onResponseHeaders(final int statusCode, final HttpHeader[] headers) {
    }

    default int onResponseBody(ByteBuffer bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
        return 0;
    }

    default void onFinished(int errorCode, int responseStatus, byte[] errorPayload) {
    }
}
