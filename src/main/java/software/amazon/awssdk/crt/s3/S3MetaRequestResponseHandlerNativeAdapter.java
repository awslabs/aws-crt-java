package software.amazon.awssdk.crt.s3;

import java.nio.ByteBuffer;

import software.amazon.awssdk.crt.utils.ByteBufferUtils;

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
}
