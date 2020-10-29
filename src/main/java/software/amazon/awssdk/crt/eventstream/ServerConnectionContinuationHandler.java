package software.amazon.awssdk.crt.eventstream;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class ServerConnectionContinuationHandler implements AutoCloseable {
    protected ServerConnectionContinuation continuation;
    private CompletableFuture<Void> completableFuture = new CompletableFuture<>();

    protected ServerConnectionContinuationHandler(final ServerConnectionContinuation continuation) {
        this.continuation = continuation;
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

    void onContinuationClosedShim() {
        onContinuationClosed();
        completableFuture.complete(null);
    }

    public CompletableFuture<Void> getContinuationClosedFuture() {
        return completableFuture;
    }

    @Override
    public void close() {
        if (continuation != null) {
            continuation.decRef();
            continuation = null;
        }
    }
}
