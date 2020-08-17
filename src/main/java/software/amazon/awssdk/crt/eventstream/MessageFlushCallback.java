package software.amazon.awssdk.crt.eventstream;

public interface MessageFlushCallback {
    void onCallbackInvoked(int errorCode);
}
