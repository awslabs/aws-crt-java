package software.amazon.awssdk.crt.eventstream;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public abstract class ServerConnectionContinuationHandler implements AutoCloseable {
    protected ServerConnectionContinuation continuation;

    protected ServerConnectionContinuationHandler(final ServerConnectionContinuation continuation) {
        this.continuation = continuation;
        this.continuation.addRef();
    }

    protected abstract void onContinuationClosed();

    protected abstract void onContinuationMessage(final List<Header> headers,
                                             final byte[] payload, final MessageType messageType, int messageFlags);

    void onContinuationMessageShim(final byte[] headersPayload, final byte[] payload,
                                   int messageType, int messageFlags) {
        List<Header> headers = new ArrayList<>();

        ByteBuffer headersBuffer = ByteBuffer.wrap(headersPayload);
        while (headersBuffer.hasRemaining()) {
            Header header = Header.fromByteBuffer(headersBuffer);
            headers.add(header);
        }

        onContinuationMessage(headers, payload, MessageType.fromEnumValue(messageType), messageFlags);
    }

    @Override
    public void close() {
        continuation.decRef();
        continuation = null;
    }
}
