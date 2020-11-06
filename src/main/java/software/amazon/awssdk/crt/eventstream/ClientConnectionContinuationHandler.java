package software.amazon.awssdk.crt.eventstream;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Handler interface for responding to continuation events. It's auto closable.
 * By default, onContinuationClosed() releases the underlying resource.
 */
public abstract class ClientConnectionContinuationHandler implements AutoCloseable {
    // this gets set upon creation of the using ClientConnectionContinuation
    protected ClientConnectionContinuation continuation;
    private CompletableFuture<Void> closedFuture = new CompletableFuture<>();

    /**
     * Invoked when a message is received on a continuation.
     * @param headers List of EventStream headers for the message received.
     * @param payload Payload for the message received
     * @param messageType message type for the message
     * @param messageFlags message flags for the message
     */
    protected abstract void onContinuationMessage(final List<Header> headers,
                          final byte[] payload, final MessageType messageType, int messageFlags);

    /**
     * Invoked from JNI. Converts the native data into usable java objects and invokes
     * onContinuationMessage().
     */
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

    /**
     * By default closes the underlying resource. If you override this function, be sure to
     * either call close() manually or invoke super.onContinuationClosed() before returning.
     */
    protected void onContinuationClosed() {
        this.close();
    }

    /**
     * Invoked from JNI. Calls onContinuationClosed().
     */
    void onContinuationClosedShim() {
        onContinuationClosed();
        closedFuture.complete(null);
    }

    /**
     * Returns a future that will be completed upon the continuation being closed.
     */
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
