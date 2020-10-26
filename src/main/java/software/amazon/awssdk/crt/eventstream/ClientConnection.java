package software.amazon.awssdk.crt.eventstream;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.ClientTlsContext;
import software.amazon.awssdk.crt.io.SocketOptions;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Wrapper around an event stream rpc client initiated connection.
 */
public class ClientConnection extends CrtResource {

    /**
     * Only for internal usage. This is invoked from JNI to create a new java wrapper for the connection.
     */
    ClientConnection(long clientConnection) {
        acquireNativeHandle(clientConnection);
        acquireClientConnection(clientConnection);
    }

    /**
     * Closes the connection if it hasn't been closed already.
     * @param shutdownErrorCode aws-c-* error code to shutdown with. Specify 0 for success.
     */
    public void closeConnection(int shutdownErrorCode) {
        if (isNull()) {
            throw new IllegalStateException("close() has already been called on this object.");
        }
        closeClientConnection(getNativeHandle(), shutdownErrorCode);
    }

    /**
     * Returns true if the connection is closed, false otherwise.
     */
    public boolean isClosed() {
        if (isNull()) {
            throw new IllegalStateException("close() has already been called on this object.");
        }
        return isClientConnectionClosed(getNativeHandle());
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

        sendProtocolMessage(headers, payload, messsageType, messageFlags, errorCode -> {
            if (errorCode == 0) {
                messageFlush.complete(null);
            } else {
                messageFlush.completeExceptionally(new CrtRuntimeException(errorCode, CRT.awsErrorString(errorCode)));
            }
        });

        return messageFlush;
    }

    /**
     * Sends a protocol message on the connection. Callback will be invoked upon the message flushing to the underlying
     * transport
     *
     * @param headers List of event-stream headers. Can be null.
     * @param payload Payload to send for the message. Can be null.
     * @param messsageType Message type for the rpc message.
     * @param messageFlags Union of message flags from MessageFlags.getByteValue()
     * @param callback will be invoked upon the message flushing to the underlying transport
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
     * Create a new stream. Activate() must be called on the stream for it to actually initiate the new stream.
     * @param continuationHandler handler to process continuation messages and state changes.
     * @return The new continuation object.
     */
    public ClientConnectionContinuation newStream(final ClientConnectionContinuationHandler continuationHandler) {
        if (isNull()) {
            throw new IllegalStateException("close() has already been called on this object.");
        }

        long continuationHandle = newClientStream(getNativeHandle(), continuationHandler);

        if (continuationHandle == 0) {
            int lastError = CRT.awsLastError();
            throw new CrtRuntimeException(lastError, CRT.awsErrorString(lastError));
        }

        ClientConnectionContinuation connectionContinuation = new ClientConnectionContinuation(continuationHandle);
        continuationHandler.continuation = connectionContinuation;

        return connectionContinuation;
    }

    /**
     * Initiates a new outgoing event-stream-rpc connection. The future will be completed once the connection either
     * succeeds or fails.
     * @param hostName hostname to connect to, this can be an IPv4 address, IPv6 address, a local socket address, or a
     *                 dns name.
     * @param port port to connect to hostName with. For local socket address, this value is ignored.
     * @param socketOptions socketOptions to use.
     * @param tlsContext (optional) tls context to use for using SSL/TLS in the connection.
     * @param bootstrap clientBootstrap object to run the connection on.
     * @param connectionHandler handler to process connection messages and state changes.
     * @return The future will be completed once the connection either succeeds or fails.
     */
    public static CompletableFuture<Void> connect(final String hostName, short port, final SocketOptions socketOptions,
                                           final ClientTlsContext tlsContext, final ClientBootstrap bootstrap,
                                           final ClientConnectionHandler connectionHandler) {
        long tlsContextHandle = tlsContext != null ? tlsContext.getNativeHandle() : 0;

        CompletableFuture<Void> future = new CompletableFuture<>();
        ClientConnectionHandler handlerShim = new ClientConnectionHandler() {
            @Override
            protected void onConnectionSetup(ClientConnection connection, int errorCode) {
                connectionHandler.clientConnection = connection;
                connectionHandler.onConnectionSetup(connection, errorCode);

                if (errorCode == 0) {
                    future.complete(null);
                } else {
                    future.completeExceptionally(new CrtRuntimeException(errorCode, CRT.awsErrorName(errorCode)));
                }
            }

            @Override
            protected void onProtocolMessage(List<Header> headers, byte[] payload, MessageType messageType, int messageFlags) {
                connectionHandler.onProtocolMessage(headers, payload, messageType, messageFlags);
            }

            @Override
            protected void onConnectionClosed(int closeReason) {
                connectionHandler.onConnectionClosed(closeReason);
            }
        };

        int resultCode = clientConnect(hostName.getBytes(StandardCharsets.UTF_8),
                port, socketOptions.getNativeHandle(), tlsContextHandle, bootstrap.getNativeHandle(), handlerShim);

        if (resultCode != 0) {
            int lastError = CRT.awsLastError();
            throw new CrtRuntimeException(lastError, CRT.awsErrorName(lastError));
        }

        return future;
    }

    @Override
    protected void releaseNativeHandle() {
        if (!isNull()) {
            releaseClientConnection(getNativeHandle());
        }
    }

    @Override
    protected boolean canReleaseReferencesImmediately() {
        return true;
    }

    private static native int clientConnect(byte[] hostName, short port, long socketOptions, long tlsContext, long bootstrap, ClientConnectionHandler connectionHandler);
    private static native boolean isClientConnectionClosed(long connection);
    private static native void closeClientConnection(long connection, int errorCode);
    private static native void acquireClientConnection(long connection);
    private static native void releaseClientConnection(long connection);
    private static native int sendProtocolMessage(long connectionPtr, byte[] serialized_headers, byte[] payload, int message_type, int message_flags, MessageFlushCallback callback);
    private static native long newClientStream(long connectionPtr, ClientConnectionContinuationHandler continuationHandler);
}
