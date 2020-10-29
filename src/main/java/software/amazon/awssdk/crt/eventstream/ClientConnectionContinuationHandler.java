package software.amazon.awssdk.crt.eventstream;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class ClientConnectionContinuationHandler implements AutoCloseable {
    // this gets set upon creation of the using ClientConnectionContinuation
    protected ClientConnectionContinuation continuation;
    private CompletableFuture<Void> closedFuture = new CompletableFuture<>();

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

    void onContinuationClosedShim() {
        onContinuationClosed();
        closedFuture.complete(null);
    }

    public CompletableFuture<Void> getContinuationClosedFuture() {
        return closedFuture;
    }

    @Override
    public void close() {
        if (continuation != null) {
            continuation.decRef();
            continuation = null;
        }
    }
}
