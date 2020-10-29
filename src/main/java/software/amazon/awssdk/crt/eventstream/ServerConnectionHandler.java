package software.amazon.awssdk.crt.eventstream;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public abstract class ServerConnectionHandler implements AutoCloseable {
    protected ServerConnection connection;

    protected ServerConnectionHandler(final ServerConnection connection) {
        this.connection = connection;
        // it wasn't really doable to have JNI invoke the function from the ServerConnectionHandler, The ServerListener
        // completes this future, when it's completed, as a convenience go ahead and invoke our own callback which
        // by default cleans up the resources.
        this.connection.getClosedFuture().whenComplete((shutdownReason, ex) -> {
            onConnectionClosed(shutdownReason);
        });
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

    protected void onConnectionClosed(int shutdownReason) {
        this.close();
    }

    @Override
    public void close() {
        if (!connection.isConnectionClosed()) {
            connection.closeConnection(0);
        }
        connection.decRef();
        connection = null;
    }
}
