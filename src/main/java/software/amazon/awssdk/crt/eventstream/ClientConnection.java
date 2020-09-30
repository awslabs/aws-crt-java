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

public class ClientConnection extends CrtResource {

    ClientConnection(long clientConnection) {
        acquireClientConnection(clientConnection);
    }

    public void closeConnection(int shutdownErrorCode) {
        closeClientConnection(getNativeHandle(), shutdownErrorCode);
    }

    public boolean isClose() {
        return isClientConnectionClosed(getNativeHandle());
    }

    public CompletableFuture<Void> sendProtocolMessage(final List<Header> headers, final byte[] payload,
                                                       final MessageType messsageType, int messageFlags) {
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

    public void sendProtocolMessage(final List<Header> headers, final byte[] payload,
                                    final MessageType messsageType, int messageFlags, MessageFlushCallback callback) {
        byte[] headersBuf = headers != null ? Header.marshallHeadersForJNI(headers) : null;

        int result = sendProtocolMessage(getNativeHandle(), headersBuf, payload, messsageType.getEnumValue(), messageFlags, callback);

        if (result != 0) {
            int errorCode = CRT.awsLastError();
            throw new CrtRuntimeException(errorCode, CRT.awsErrorString(errorCode));
        }
    }

    public ClientConnectionContinuation newStream(final ClientConnectionContinuationHandler continuationHandler) {
        long continuationHandle = newClientStream(getNativeHandle(), continuationHandler);

        if (continuationHandle == 0) {
            int lastError = CRT.awsLastError();
            throw new CrtRuntimeException(lastError, CRT.awsErrorString(lastError));
        }

        ClientConnectionContinuation connectionContinuation = new ClientConnectionContinuation(continuationHandle);
        continuationHandler.continuation = connectionContinuation;
        continuationHandler.continuation.addRef();

        return connectionContinuation;
    }

    public static CompletableFuture<Void> connect(final String hostName, short port, final SocketOptions socketOptions,
                                           final ClientTlsContext tlsContext, final ClientBootstrap bootstrap,
                                           final ClientConnectionHandler connectionHandler) {
        long tlsContextHandle = tlsContext != null ? tlsContext.getNativeHandle() : 0;

        CompletableFuture<Void> future = new CompletableFuture<>();
        ClientConnectionHandler handlerShim = new ClientConnectionHandler() {
            @Override
            protected void onConnectionSetup(ClientConnection connection, int errorCode) {
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
