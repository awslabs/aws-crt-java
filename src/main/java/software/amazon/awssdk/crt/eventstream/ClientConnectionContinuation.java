package software.amazon.awssdk.crt.eventstream;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Java wrapper for event-stream-rpc client continuation.
 */
public class ClientConnectionContinuation extends CrtResource {

    /**
     * Package private invoked from JNI. Do not call directly.
     */
    ClientConnectionContinuation(long ptr) {
        acquireNativeHandle(ptr);
    }

    /**
     * Initiates a new client stream. Sends new message for the new stream.
     * @param operationName name for the operation to be invoked by the peer endpoint.
     * @param headers headers for the event-stream message, may be null or empty.
     * @param payload payload for the event-stream message, may be null or empty.
     * @param messsageType messageType for the message. Must be ApplicationMessage or ApplicationError
     * @param messageFlags union of flags for MessageFlags.getByteValue()
     * @param callback callback to be invoked upon the message being flushed to the underlying transport.
     */
    public void activate(final String operationName,
                         final List<Header> headers, final byte[] payload,
                         final MessageType messsageType, int messageFlags,
                         MessageFlushCallback callback) {
        if (isNull()) {
            throw new IllegalStateException("close() has already been called on this object.");
        }

        byte[] headersBuf = headers != null ? Header.marshallHeadersForJNI(headers) : null;

        int result = activateContinuation(getNativeHandle(), this, operationName.getBytes(StandardCharsets.UTF_8),
                headersBuf, payload, messsageType.getEnumValue(), messageFlags, callback);

        if (result != 0) {
            int errorCode = CRT.awsLastError();
            throw new CrtRuntimeException(errorCode);
        }
    }

    /**
     * Sends the initial message on a continuation, and begins the message flow for a stream.
     * @param operationName name of the operation to invoke on the server.
     * @param headers list of additional event stream headers to include on the message.
     * @param payload payload for the message
     * @param messsageType message type. Must be either ApplicationMessage or ApplicationError
     * @param messageFlags message flags for the message.
     * @return Completeable future for syncing with the connection completing or failing.
     */
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
                    messageFlush.completeExceptionally(new CrtRuntimeException(errorCode));
                }
            }
        });

        return messageFlush;
    }

    /**
     * Sends message on the continuation
     * @param headers list of additional event stream headers to include on the message.
     * @param payload payload for the message
     * @param messsageType message type. Must be either ApplicationMessage or ApplicationError
     * @param messageFlags message flags for the message, use TerminateStream to cause this message
     *                     to close the continuation after sending.
     * @param callback completion callback to be invoked when the message is synced to the underlying
     *                 transport.
     */
    public void sendMessage(final List<Header> headers, final byte[] payload,
                         final MessageType messsageType, int messageFlags,
                         MessageFlushCallback callback) {
        if (isNull()) {
            throw new IllegalStateException("close() has already been called on this object.");
        }

        byte[] headersBuf = headers != null ? Header.marshallHeadersForJNI(headers) : null;

        int result = sendContinuationMessage(getNativeHandle(),
                headersBuf, payload, messsageType.getEnumValue(), messageFlags, callback);

        if (result != 0) {
            int errorCode = CRT.awsLastError();
            throw new CrtRuntimeException(errorCode);
        }
    }

    /**
     * Sends message on the continuation
     * @param headers list of additional event stream headers to include on the message.
     * @param payload payload for the message
     * @param messsageType message type. Must be either ApplicationMessage or ApplicationError
     * @param messageFlags message flags for the message, use TerminateStream to cause this message
     *                     to close the continuation after sending.
     * @return Future for syncing when the message is flushed to the transport or fails.
     */
    public CompletableFuture<Void> sendMessage(final List<Header> headers, final byte[] payload,
                                            final MessageType messsageType, int messageFlags) {
        if (isNull()) {
            throw new IllegalStateException("close() has already been called on this object.");
        }

        CompletableFuture<Void> messageFlush = new CompletableFuture<>();

        sendMessage(headers, payload, messsageType, messageFlags, new MessageFlushCallback() {
            @Override
            public void onCallbackInvoked(int errorCode) {
                if (errorCode == 0) {
                    messageFlush.complete(null);
                } else {
                    messageFlush.completeExceptionally(new CrtRuntimeException(errorCode));
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

    private static native int activateContinuation(long continuationPtr, ClientConnectionContinuation continuation, byte[] operationName, byte[] serialized_headers, byte[] payload, int message_type, int message_flags, MessageFlushCallback callback);
    private static native int sendContinuationMessage(long continuationPtr, byte[] serialized_headers, byte[] payload, int message_type, int message_flags, MessageFlushCallback callback);
    private static native void releaseContinuation(long continuationPtr);
}
