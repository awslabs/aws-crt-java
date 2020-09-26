package software.amazon.awssdk.crt.eventstream;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public abstract class ClientConnectionContinuationHandler implements AutoCloseable {
    // this gets set upon creation of the using ClientConnectionContinuation
    protected ClientConnectionContinuation continuation;

    protected abstract void onContinuationMessage(final List<Header> headers,
                          final byte[] payload, final MessageType messageType, int messageFlags);

    private void onContinuationMessageShim(final byte[] headersPayload, final byte[] payload,
                                   int messageType, int messageFlags) {
        List<Header> headers = new ArrayList<>();

        ByteBuffer headersBuffer = ByteBuffer.wrap(headersPayload);
        while (headersBuffer.hasRemaining()) {
            Header header = Header.fromByteBuffer(headersBuffer);
            headers.add(header);
        }

        onContinuationMessage(headers, payload, MessageType.fromEnumValue(messageType), messageFlags);
    }

    protected void onContinuationClosed() {
        this.close();
    }

    @Override
    public void close() {
        if (continuation != null) {
            continuation.decRef();
            continuation = null;
        }
    }
}
