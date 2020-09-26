package software.amazon.awssdk.crt.eventstream;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public abstract class ClientConnectionHandler implements AutoCloseable {
    protected ClientConnection clientConnection;

    public ClientConnectionHandler() {
    }

    protected abstract void onConnectionSetup(final ClientConnection connection, int errorCode);

    void onConnectionSetupShim(long connectionPtr, int errorCode) {
        if (connectionPtr != 0) {
            clientConnection = new ClientConnection(connectionPtr);
            clientConnection.addRef();
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


    protected abstract void onConnectionClosed(int closeReason);

    @Override
    public void close() {
        if (clientConnection != null) {
            clientConnection.closeConnection(0);
            clientConnection.decRef();
            clientConnection = null;
        }
    }
}

