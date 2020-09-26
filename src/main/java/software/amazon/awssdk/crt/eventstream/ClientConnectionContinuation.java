package software.amazon.awssdk.crt.eventstream;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ClientConnectionContinuation extends CrtResource {

    ClientConnectionContinuation(long ptr) {
        acquireNativeHandle(ptr);
    }

    public void activate(final String operationName,
                         final List<Header> headers, final byte[] payload,
                         final MessageType messsageType, int messageFlags,
                         MessageFlushCallback callback) {
        byte[] headersBuf = headers != null ? Header.marshallHeadersForJNI(headers) : null;

        int result = activateContinuation(getNativeHandle(), operationName.getBytes(StandardCharsets.UTF_8),
                headersBuf, payload, messsageType.getEnumValue(), messageFlags, callback);

        if (result != 0) {
            int errorCode = CRT.awsLastError();
            throw new CrtRuntimeException(errorCode, CRT.awsErrorString(errorCode));
        }
    }

    public CompletableFuture<Void> activate(final String operationName,
                                      final List<Header> headers, final byte[] payload,
                                      final MessageType messsageType, int messageFlags) {
        CompletableFuture<Void> messageFlush = new CompletableFuture<>();

        activate(operationName, headers, payload, messsageType, messageFlags, new MessageFlushCallback() {
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

    public void sendMessage(final List<Header> headers, final byte[] payload,
                         final MessageType messsageType, int messageFlags,
                         MessageFlushCallback callback) {
        byte[] headersBuf = headers != null ? Header.marshallHeadersForJNI(headers) : null;

        int result = sendContinuationMessage(getNativeHandle(),
                headersBuf, payload, messsageType.getEnumValue(), messageFlags, callback);

        if (result != 0) {
            int errorCode = CRT.awsLastError();
            throw new CrtRuntimeException(errorCode, CRT.awsErrorString(errorCode));
        }
    }

    public CompletableFuture<Void> sendMessage(final List<Header> headers, final byte[] payload,
                                            final MessageType messsageType, int messageFlags) {
        CompletableFuture<Void> messageFlush = new CompletableFuture<>();

        sendMessage(headers, payload, messsageType, messageFlags, new MessageFlushCallback() {
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

    @Override
    protected void releaseNativeHandle() {
        if (!isNull()) {
            releaseContinuation(getNativeHandle());
        }
    }

    @Override
    protected boolean canReleaseReferencesImmediately() {
        return true;
    }

    private static native int activateContinuation(long continuationPtr, byte[] operationName, byte[] serialized_headers, byte[] payload, int message_type, int message_flags, MessageFlushCallback callback);
    private static native int sendContinuationMessage(long continuationPtr, byte[] serialized_headers, byte[] payload, int message_type, int message_flags, MessageFlushCallback callback);
    private static native void releaseContinuation(long continuationPtr);
}
