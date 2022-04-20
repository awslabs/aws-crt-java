/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.eventstream;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Handler interface for responding to continuation events. It's auto closable.
 * By default, onContinuationClosed() releases the underlying resource.
 */
public abstract class ServerConnectionContinuationHandler implements AutoCloseable {
    protected ServerConnectionContinuation continuation;
    private CompletableFuture<Void> completableFuture = new CompletableFuture<>();

    /**
     * Constructor invoked by your subclass.
     * @param continuation continuation to back the handler.
     */
    protected ServerConnectionContinuationHandler(final ServerConnectionContinuation continuation) {
        this.continuation = continuation;
    }

    /**
     * Implement to handle the onContinuationClosed event. By default, releases the underlying
     * resource by calling close(). If you override this function, be sure to either call close()
     * yourself or invoke super.onContinuationClosed().
     */
    protected void onContinuationClosed() {
        this.close();
    }

    /**
     * Invoked when a message is received on a continuation.
     * @param headers List of EventStream headers for the message received.
     * @param payload Payload for the message received
     * @param messageType message type for the message
     * @param messageFlags message flags for the message
     */
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

    /**
     * @return a future that will be completed upon the continuation being closed.
     */
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
