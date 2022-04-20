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
 * Handler for EventStream ClientConnections. It's marked AutoClosable.
 * By default onConnectionClosed, calls the close() function on this object.
 */
public abstract class ClientConnectionHandler implements AutoCloseable {
    protected ClientConnection clientConnection;

    public ClientConnectionHandler() {
    }

    /**
     * Invoked upon completion of the Connection attempt
     * @param connection if the setup was successful, connection is non-null. On error, errorCode
     *                   will be non-zero
     * @param errorCode Error representing any error that occurred during connect.
     */
    protected abstract void onConnectionSetup(final ClientConnection connection, int errorCode);

    /**
     * Invoked from JNI. Constructs usable ClientConnection object and invokes:
     * onConnectionSetup()
     */
    void onConnectionSetupShim(long connectionPtr, int errorCode) {
        if (connectionPtr != 0) {
            // don't add ref, this is a private constructor and the only reference lives in the handler.
            clientConnection = new ClientConnection(connectionPtr);
        }

        onConnectionSetup(clientConnection, errorCode);
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
     * Invoked from JNI. Marshalls the native data into usable java objects and invokes
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
     * Invoked from JNI. Invokes onConnectionClosed()
     */
    private void onConnectionClosedShim(int closeReason) {
        onConnectionClosed(closeReason);
    }

    /**
     * @return a future for syncing on Connection closed.
     */
    public CompletableFuture<Integer> getConnectionClosedFuture() {
        return clientConnection.getClosedFuture();
    }

    /**
     * Invoked upon the connection closed event. By default it calls close()
     * on this object.
     * @param closeReason The reason the connection was closed. 0 means a clean shutdown.
     */
    protected void onConnectionClosed(int closeReason) {
        this.close();
    }

    @Override
    public void close() {
        clientConnection = null;
    }
}

