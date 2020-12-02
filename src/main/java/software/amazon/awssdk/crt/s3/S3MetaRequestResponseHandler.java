package software.amazon.awssdk.crt.s3;

public interface S3MetaRequestResponseHandler {

    default int onResponseBody(byte[] bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
        return 0;
    }

    default void onFinished(int errorCode) {

    }
}
