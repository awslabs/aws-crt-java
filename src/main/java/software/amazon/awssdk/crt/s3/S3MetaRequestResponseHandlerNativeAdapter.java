package software.amazon.awssdk.crt.s3;

class S3MetaRequestResponseHandlerNativeAdapter {
    private S3MetaRequestResponseHandler responseHandler;

    S3MetaRequestResponseHandlerNativeAdapter(S3MetaRequestResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }
    
    int onResponseBody(byte[] bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
        return this.responseHandler.onResponseBody(bodyBytesIn, objectRangeStart, objectRangeEnd);
    }

    void onFinished(int errorCode) {
        this.responseHandler.onFinished(errorCode);
    }
}
