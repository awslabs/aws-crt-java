package software.amazon.awssdk.crt.s3;

import software.amazon.awssdk.crt.http.HttpHeader;

public interface S3MetaRequestResponseHandler {
    default void onResponseHeaders(final int statusCode, final HttpHeader[] headers) { }
    
    default int onResponseBody(byte[] bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
        return 0;
    }

    default void onFinished(int errorCode) { }
}
