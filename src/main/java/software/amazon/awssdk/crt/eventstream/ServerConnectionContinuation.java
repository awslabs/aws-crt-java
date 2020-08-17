package software.amazon.awssdk.crt.eventstream;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ServerConnectionContinuation extends CrtResource {

    ServerConnectionContinuation(long continuationPtr) {
        // tell c land we're acquiring
        acquire(continuationPtr);
        acquireNativeHandle(continuationPtr);
    }

    public boolean isClosed() {
        return isClosed(getNativeHandle());
    }

    public CompletableFuture<Void> sendMessage(final List<Header> headers, final byte[] payload,
                                                       final MessageType messsageType, int messageFlags) {
        CompletableFuture<Void> messageFlush = new CompletableFuture<>();

        sendMessage(headers, payload, messsageType, messageFlags, errorCode -> {
            if (errorCode == 0) {
                messageFlush.complete(null);
            } else {
                messageFlush.completeExceptionally(new CrtRuntimeException(errorCode, CRT.awsErrorString(errorCode)));
            }
        });

        return messageFlush;
    }

    public void sendMessage(final List<Header> headers, final byte[] payload,
                                    final MessageType messageType, int messageFlags, MessageFlushCallback callback) {
        byte[] headersBuf = Header.marshallHeadersForJNI(headers);

        int result = sendContinuationMessage(getNativeHandle(), headersBuf, payload, messageType.getEnumValue(), messageFlags, callback);

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

    private static native void acquire(long continuationPtr);
    private static native void release(long continuationPtr);
    private static native boolean isClosed(long continuationPtr);
    private static native int sendContinuationMessage(long continuation, byte[] serialized_headers, byte[] payload, int message_type, int message_flags, MessageFlushCallback callback);
}
