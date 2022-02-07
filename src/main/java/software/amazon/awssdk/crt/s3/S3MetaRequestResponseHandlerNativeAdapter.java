package software.amazon.awssdk.crt.s3;

import software.amazon.awssdk.crt.http.HttpHeader;

import java.nio.ByteBuffer;

class S3MetaRequestResponseHandlerNativeAdapter {
    private S3MetaRequestResponseHandler responseHandler;

    S3MetaRequestResponseHandlerNativeAdapter(S3MetaRequestResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    int onResponseBody(byte[] bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
        return this.responseHandler.onResponseBody(ByteBuffer.wrap(bodyBytesIn), objectRangeStart, objectRangeEnd);
    }

    void onFinished(int errorCode, int responseStatus, byte[] errorPayload) {
        this.responseHandler.onFinished(errorCode, responseStatus, errorPayload);
    }

    void onResponseHeaders(final int statusCode, final ByteBuffer headersBlob) {
        responseHandler.onResponseHeaders(statusCode, HttpHeader.loadHeadersFromMarshalledHeadersBlob(headersBlob));
    }

    void onProgress(final S3MetaRequestProgress progress) {
        responseHandler.onProgress(progress);
    }
}
