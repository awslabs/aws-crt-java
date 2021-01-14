package software.amazon.awssdk.crt.s3;

import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpHeaderBlock;

import java.nio.ByteBuffer;

class S3MetaRequestResponseHandlerNativeAdapter {
    private S3MetaRequestResponseHandler responseHandler;

    S3MetaRequestResponseHandlerNativeAdapter(S3MetaRequestResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    int onResponseBody(ByteBuffer bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
        byte[] payload = new byte[bodyBytesIn.limit()];
        bodyBytesIn.get(payload);
        return this.responseHandler.onResponseBody(payload, objectRangeStart, objectRangeEnd);
    }

    void onFinished(int errorCode) {
        this.responseHandler.onFinished(errorCode);
    }
    
    void onResponseHeaders(final int statusCode, final HttpHeader[] headers) {
        responseHandler.onResponseHeaders(statusCode, headers);
    }
}
