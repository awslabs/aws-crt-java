package software.amazon.awssdk.crt.eventstream;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ServerConnection extends CrtResource {

    ServerConnection(long connectionPtr) {
        // tell c-land we're acquiring
        acquire(connectionPtr);
        acquireNativeHandle(connectionPtr);
    }

    public boolean isConnectionClosed() {
        return isClosed(getNativeHandle());
    }

    public void closeConnection(int shutdownError) {
        closeConnection(getNativeHandle(), shutdownError);
    }

    public CompletableFuture<Void> sendProtocolMessage(final List<Header> headers, final byte[] payload,
                                                       final MessageType messsageType, int messageFlags) {
        CompletableFuture<Void> messageFlush = new CompletableFuture<>();

        sendProtocolMessage(headers, payload, messsageType, messageFlags, new MessageFlushCallback() {
            @Override
            public void onCallbackInvoked(int errorCode) {
                if (errorCode == 0) {
                    messageFlush.complete(null);
                } else {
                    messageFlush.completeExceptionally(new CrtRuntimeException(errorCode, CRT.awsErrorString(errorCode)));
                }
            }
        });

        return messageFlush;
    }

    public void sendProtocolMessage(final List<Header> headers, final byte[] payload,
                                    final MessageType messsageType, int messageFlags, MessageFlushCallback callback) {
        byte[] headersBuf = headers != null ? Header.marshallHeadersForJNI(headers) : null;

        int result = sendProtocolMessage(getNativeHandle(), headersBuf, payload, messsageType.getEnumValue(), messageFlags, callback);

        if (result != 0) {
            int errorCode = CRT.awsLastError();
            throw new CrtRuntimeException(errorCode, CRT.awsErrorString(errorCode));
        }
    }

    @Override
    protected void releaseNativeHandle() {
        if (!isNull()) {
            release(getNativeHandle());
        }
    }

    @Override
    protected boolean canReleaseReferencesImmediately() {
        return true;
    }

    private static native void acquire(long connectionPtr);
    private static native void release(long connectionPtr);
    private static native void closeConnection(long connectionPtr, int shutdownError);
    private static native boolean isClosed(long connectionPtr);
    private static native int sendProtocolMessage(long connectionPtr, byte[] serialized_headers, byte[] payload, int message_type, int message_flags, MessageFlushCallback callback);
}
