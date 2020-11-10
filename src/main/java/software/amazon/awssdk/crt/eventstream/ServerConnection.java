package software.amazon.awssdk.crt.eventstream;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Wrapper around event-stream-rpc-server-connection. Note this class is AutoClosable.
 * By default the ServerConnectionHandler::onClosed callback calls close().
 */
public class ServerConnection extends CrtResource {
    CompletableFuture<Integer> closedFuture = new CompletableFuture<>();

    /**
     * Invoked from JNI.
     */
    ServerConnection(long connectionPtr) {
        // tell c-land we're acquiring
        acquire(connectionPtr);
        acquireNativeHandle(connectionPtr);
    }

    /**
     * @return true if the connection is open. False otherwise.
     */
    public boolean isConnectionOpen() {
        if (isNull()) {
            return false;
        }
        return isOpen(getNativeHandle());
    }

    /**
     * Closes the connection with shutdownError
     * @param shutdownError error code to shutdown the connection with. If
     *                      shutting down cleanly, use 0.
     */
    public void closeConnection(int shutdownError) {
        if (isNull()) {
            throw new IllegalStateException("close() has already been called on this object.");
        }
        closeConnection(getNativeHandle(), shutdownError);
    }

    /**
     * Sends a protocol message on the connection. Returns a completable future for synchronizing on the message
     * flushing to the underlying transport.
     * @param headers List of event-stream headers. Can be null.
     * @param payload Payload to send for the message. Can be null.
     * @param messsageType Message type for the rpc message.
     * @param messageFlags Union of message flags from MessageFlags.getByteValue()
     * @return completable future for synchronizing on the message flushing to the underlying transport.
     */
    public CompletableFuture<Void> sendProtocolMessage(final List<Header> headers, final byte[] payload,
                                                       final MessageType messsageType, int messageFlags) {
        if (isNull()) {
            throw new IllegalStateException("close() has already been called on this object.");
        }
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

    /**
     * Sends a protocol message on the connection. Returns a completable future for synchronizing on the message
     * flushing to the underlying transport.
     * @param headers List of event-stream headers. Can be null.
     * @param payload Payload to send for the message. Can be null.
     * @param messsageType Message type for the rpc message.
     * @param messageFlags Union of message flags from MessageFlags.getByteValue()
     * @param callback invoked upon the message flushing to the underlying transport.
     */
    public void sendProtocolMessage(final List<Header> headers, final byte[] payload,
                                    final MessageType messsageType, int messageFlags, MessageFlushCallback callback) {
        if (isNull()) {
            throw new IllegalStateException("close() has already been called on this object.");
        }
        byte[] headersBuf = headers != null ? Header.marshallHeadersForJNI(headers) : null;

        int result = sendProtocolMessage(getNativeHandle(), headersBuf, payload, messsageType.getEnumValue(), messageFlags, callback);

        if (result != 0) {
            int errorCode = CRT.awsLastError();
            throw new CrtRuntimeException(errorCode, CRT.awsErrorString(errorCode));
        }
    }

    /**
     * @return a future which completes upon the connection closing
     */
    public CompletableFuture<Integer> getClosedFuture() {
        return closedFuture;
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
    private static native boolean isOpen(long connectionPtr);
    private static native int sendProtocolMessage(long connectionPtr, byte[] serialized_headers, byte[] payload, int message_type, int message_flags, MessageFlushCallback callback);
}
