package software.amazon.awssdk.crt.eventstream;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class ClientConnectionHandler implements AutoCloseable {
    protected ClientConnection clientConnection;
    private CompletableFuture<Integer> closeFuture = new CompletableFuture<>();

    public ClientConnectionHandler() {
    }

    protected abstract void onConnectionSetup(final ClientConnection connection, int errorCode);

    void onConnectionSetupShim(long connectionPtr, int errorCode) {
        if (connectionPtr != 0) {
            // don't add ref, this is a private constructor and the only reference lives in the handler.
            clientConnection = new ClientConnection(connectionPtr);
        }

        onConnectionSetup(clientConnection, errorCode);
    }

    protected abstract void onProtocolMessage(final List<Header> headers,
                                              final byte[] payload, final MessageType messageType, int messageFlags);


    private void onProtocolMessage(final byte[] headersPayload, final byte[] payload,
                                   int messageType, int messageFlags) {
        List<Header> headers = new ArrayList<>();

        ByteBuffer headersBuffer = ByteBuffer.wrap(headersPayload);
        while (headersBuffer.hasRemaining()) {
            Header header = Header.fromByteBuffer(headersBuffer);
            headers.add(header);
        }

        onProtocolMessage(headers, payload, MessageType.fromEnumValue(messageType), messageFlags);
    }

    private void onConnectionClosedShim(int closeReason) {
        onConnectionClosed(closeReason);
        closeFuture.complete(closeReason);
    }

    public CompletableFuture<Integer> getConnectionClosedFuture() {
        return closeFuture;
    }

    protected abstract void onConnectionClosed(int closeReason);

    @Override
    public void close() {
        if (clientConnection != null) {
            if (!clientConnection.isClosed()) {
                clientConnection.closeConnection(0);
            }
            clientConnection.decRef();
            clientConnection = null;
        }
    }
}

