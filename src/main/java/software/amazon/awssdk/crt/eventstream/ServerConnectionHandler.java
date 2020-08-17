package software.amazon.awssdk.crt.eventstream;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public abstract class ServerConnectionHandler implements AutoCloseable {
    protected ServerConnection connection;

    protected ServerConnectionHandler(final ServerConnection connection) {
        this.connection = connection;
        connection.addRef();
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

    protected abstract ServerConnectionContinuationHandler onIncomingStream(final ServerConnectionContinuation continuation, String operationName);

    private ServerConnectionContinuationHandler onIncomingStream(final ServerConnectionContinuation continuation, byte[] operationName) {
        String operationNameStr = new String(operationName, StandardCharsets.UTF_8);

        return onIncomingStream(continuation, operationNameStr);
    }

    @Override
    public void close() {
        connection.closeConnection(0);
        connection.decRef();
        connection = null;
    }
}
