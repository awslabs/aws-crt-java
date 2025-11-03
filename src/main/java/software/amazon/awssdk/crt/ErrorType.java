package software.amazon.awssdk.crt;

public enum ErrorType {
    SUCCESS("Success"),
    OTHER("Other"),
    THROTTLING("Throttling"),
    SERVER_ERROR("ServerError"),
    CONFIGURED_TIMEOUT("ConfiguredTimeout"),
    IO("IO"),
    ;

    public final String name;

    ErrorType(String name) {
        this.name = name;
    }
}