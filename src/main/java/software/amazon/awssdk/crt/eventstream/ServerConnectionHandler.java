/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.eventstream;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Handler for EventStream ServerConnections. It's marked AutoClosable.
 * By default onConnectionClosed, calls the close() function on this object.
 */
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

    /**
     * Invoked when a message is received on a connection.
     * @param headers List of EventStream headers for the message received.
     * @param payload Payload for the message received
     * @param messageType message type for the message
     * @param messageFlags message flags for the message
     */
    protected abstract void onProtocolMessage(final List<Header> headers,
                           final byte[] payload, final MessageType messageType, int messageFlags);

    /**
     * Invoked from JNI. Marshals the native data into java objects and calls
     * onProtocolMessage()
     */
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

    /**
     * Invoked upon an incoming stream from a client.
     * @param continuation continuation object for sending continuation events to the client.
     * @param operationName name of the operation the client wishes to invoke.
     * @return a new instance of ServerConnectionContinuationHandler for handling continuation events.
     */
    protected abstract ServerConnectionContinuationHandler onIncomingStream(final ServerConnectionContinuation continuation, String operationName);

    private ServerConnectionContinuationHandler onIncomingStream(final ServerConnectionContinuation continuation, byte[] operationName) {
        String operationNameStr = new String(operationName, StandardCharsets.UTF_8);

        return onIncomingStream(continuation, operationNameStr);
    }

    /**
     * Invoked upon the connection closing. By default, calls close() on this object.
     * @param shutdownReason reason for the shutdown. 0 means clean shutdown.
     */
    protected void onConnectionClosed(int shutdownReason) {
        this.close();
    }

    @Override
    public void close() {
        if (connection.isConnectionOpen()) {
            connection.closeConnection(0);
        }
        connection = null;
    }
}
