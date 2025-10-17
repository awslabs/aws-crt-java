package software.amazon.awssdk.crt.s3;

public enum ErrorType {
    SUCCESS,
    THROTTLING,
    SERVER_ERROR,
    CONFIGURED_TIMEOUT,
    IO,
    OTHER
}