package software.amazon.awssdk.crt.eventstream;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Wrapper around aws-event-stream-rpc-server continuation. This class is marked AutoClosable.
 * Note that by default ServerConnectionContinuationHandler will invoke close() in
 * ServerConnectionContinuationHandler::onContinuationClosed().
 */
public class ServerConnectionContinuation extends CrtResource {

    /**
     * Invoked from JNI
     */
    ServerConnectionContinuation(long continuationPtr) {
        // tell c land we're acquiring
        acquire(continuationPtr);
        acquireNativeHandle(continuationPtr);
    }

    /**
     * @return true if the continuation has been closed. False otherwise.
     */
    public boolean isClosed() {
        return isClosed(getNativeHandle());
    }

    /**
     * Sends message on the continuation
     * @param headers list of additional event stream headers to include on the message.
     * @param payload payload for the message
     * @param messageType message type. Must be either ApplicationMessage or ApplicationError
     * @param messageFlags message flags for the message, use TerminateStream to cause this message
     *                     to close the continuation after sending.
     * @return Future for syncing when the message is flushed to the transport or fails.
     */
    public CompletableFuture<Void> sendMessage(final List<Header> headers, final byte[] payload,
                                                       final MessageType messageType, int messageFlags) {
        CompletableFuture<Void> messageFlush = new CompletableFuture<>();

        sendMessage(headers, payload, messageType, messageFlags, errorCode -> {
            if (errorCode == 0) {
                messageFlush.complete(null);
            } else {
                messageFlush.completeExceptionally(new CrtRuntimeException(errorCode));
            }
        });

        return messageFlush;
    }

    /**
     * Sends message on the continuation
     * @param headers list of additional event stream headers to include on the message.
     * @param payload payload for the message
     * @param messageType message type. Must be either ApplicationMessage or ApplicationError
     * @param messageFlags message flags for the message, use TerminateStream to cause this message
     *                     to close the continuation after sending.
     * @param callback completion callback to be invoked when the message is synced to the underlying
     *                 transport.
     */
    public void sendMessage(final List<Header> headers, final byte[] payload,
                                    final MessageType messageType, int messageFlags, MessageFlushCallback callback) {
        byte[] headersBuf = headers != null ? Header.marshallHeadersForJNI(headers): null;

        int result = sendContinuationMessage(getNativeHandle(), headersBuf, payload, messageType.getEnumValue(), messageFlags, callback);

        if (result != 0) {
            int errorCode = CRT.awsLastError();
            throw new CrtRuntimeException(errorCode);
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
