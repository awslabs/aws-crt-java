package software.amazon.awssdk.crt;

public enum ErrorType {
    SUCCESS,
    THROTTLING,
    SERVER_ERROR,
    CONFIGURED_TIMEOUT,
    IO,
    OTHER
}