
/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package software.amazon.awssdk.crt.mqtt;

import software.amazon.awssdk.crt.AsyncCallback;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContext;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.nio.ByteBuffer;
import java.io.Closeable;

/**
 * This class wraps aws-c-mqtt to provide the basic MQTT pub/sub
 * functionality via the AWS Common Runtime
 * 
 * MqttConnection represents a single connection from one MqttClient to an MQTT
 * service endpoint
 */
public class MqttConnection extends CrtResource implements Closeable {
    private final MqttClient client;
    private volatile ConnectionState connectionState = ConnectionState.DISCONNECTED;
    private MqttConnectionEvents userConnectionCallbacks;
    private AsyncCallback connectAck;
    private AsyncCallback disconnectAck;

    public enum ConnectionState {
        DISCONNECTED, CONNECTING, CONNECTED, DISCONNECTING,
    }

    /**
     * Wraps the handler provided by the user so that an MqttMessage can be constructed from the published
     * buffer and topic
     */
    private class MessageHandler {
        String topic;
        Consumer<MqttMessage> callback;

        private MessageHandler(String topic, Consumer<MqttMessage> callback) {
            this.callback = callback;
            this.topic = topic;
        }

        /* called from native when a message is delivered */
        void deliver(byte[] payload) {
            callback.accept(new MqttMessage(topic, ByteBuffer.wrap(payload)));
        }
    }

    /**
     * Constructs a new MqttConnection. Connections are reusable after being disconnected.
     * @param mqttClient Must be non-null
     * @throws MqttException If mqttClient is null
     */
    public MqttConnection(MqttClient mqttClient) throws MqttException {
        this(mqttClient, null);
    }

    /**
     * Constructs a new MqttConnection. Connections are reusable after being disconnected.
     * @param mqttClient Must be non-null
     * @param callbacks Optional handler for connection interruptions/resumptions
     * @throws MqttException If mqttClient is null
     */
    public MqttConnection(MqttClient mqttClient, MqttConnectionEvents callbacks) throws MqttException {
        if (mqttClient == null) {
            throw new MqttException("MqttClient must not be null");
        }

        client = mqttClient;
        userConnectionCallbacks = callbacks;
        
        try {
            acquire(mqttConnectionNew(client.native_ptr(), this));
        } catch (CrtRuntimeException ex) {
            throw new MqttException("Exception during mqttConnectionNew: " + ex.getMessage());
        }
    }

    /**
     * Disconnects if necessary, and frees native resources associated with this connection
     */
    @Override
    public void close() {
        disconnect();
        mqttConnectionDestroy(release());
    }

    /**
     * Returns the current connection state. This function should not be used often, it is much better to respond
     * to events delivered via the MqttConnectionEvents interface provided at construction.
     * @return The current connection state
     */
    public ConnectionState getState() {
        return connectionState;
    }

    /**
     * Sets the login credentials for the connection. Only valid before connect() has been called.
     * @param user Login username
     * @param pass Login password
     * @throws MqttException If the username or password are null
     */
    public void setLogin(String user, String pass) throws MqttException {
        try {
            mqttConnectionSetLogin(native_ptr(), user, pass);
        } catch (CrtRuntimeException ex) {
            throw new MqttException("Failed to set login: " + ex.getMessage());
        }        
    }

    // Called from native when the connection is established the first time
    private void onConnectionComplete(int errorCode, boolean sessionPresent) {
        if (errorCode == 0) {
            connectionState = ConnectionState.CONNECTED;
            if (connectAck != null) {
                connectAck.onSuccess(sessionPresent);
                connectAck = null;
            }
        } else {
            connectionState = ConnectionState.DISCONNECTED;
            if (connectAck != null) {
                connectAck.onFailure(new MqttException(errorCode));
                connectAck = null;
            }
        }
    }

    // Called when the connection drops or is disconnected. If errorCode == 0, the disconnect was intentional.
    private void onConnectionInterrupted(int errorCode) {
        connectionState = ConnectionState.DISCONNECTED;
        if (disconnectAck != null) {
            if (errorCode == 0) {
                disconnectAck.onSuccess();
            } else {
                disconnectAck.onFailure(new MqttException(errorCode));
            }
            disconnectAck = null;
        }
        if (userConnectionCallbacks != null) {
            userConnectionCallbacks.onConnectionInterrupted(errorCode);
        }
    }

    // Called when a reconnect succeeds, and also on initial connection success.
    private void onConnectionResumed(boolean sessionPresent) {
        connectionState = ConnectionState.CONNECTED;
        if (userConnectionCallbacks != null) {
            userConnectionCallbacks.onConnectionResumed(sessionPresent);
        }
    }

    /**
     * Connect to the service endpoint and start a session without TLS.
     * @param clientId The clientId provided to the service. Must be unique across all connected Things on the endpoint.
     * @param endpoint The hostname of the service endpoint
     * @param port The port to connect to on the service endpoint host
     * @return Future result is true if resuming a session, false if clean session
     */
    public CompletableFuture<Boolean> connect(String clientId, String endpoint, int port) {
        return connect(clientId, endpoint, port, null, null, true, 0, 0);
    }

    /**
     * Connect to the service endpoint and start a session
     * @param clientId The clientId provided to the service. Must be unique across all connected Things on the endpoint.
     * @param endpoint The hostname of the service endpoint
     * @param port     The port to connect to on the service endpoint host
     * @param socketOptions Optional SocketOptions instance with options for this connection
     * @param tls Optional TLS context. If this is null, a TLS connection will not be attempted.
     * @param cleanSession Whether or not to completely restart the session. If false, topics that were already
     *                     subscribed by this clientId will be resumed
     * @param keepAliveMs 0 = no keepalive, non-zero = ms between keepalive packets
     * @return Future result is true if resuming a session, false if clean session
     * @throws MqttException If the port is out of range
     */
    public CompletableFuture<Boolean> connect(
        String clientId, String endpoint, int port, 
        SocketOptions socketOptions, TlsContext tls, boolean cleanSession, int keepAliveMs, int pingTimeoutMs) 
            throws MqttException {
            
        // Just clamp the pingTimeout, no point in throwing
        short pingTimeout = (short) Math.max(0, Math.min(pingTimeoutMs, Short.MAX_VALUE));
        if (port > Short.MAX_VALUE || port <= 0) {
            throw new MqttException("Port must be betweeen 0 and " + Short.MAX_VALUE);
        }
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        connectAck = AsyncCallback.wrapFuture(future, null);
        try {
            connectionState = ConnectionState.CONNECTING;
            mqttConnectionConnect(
                native_ptr(), endpoint, (short) port, 
                socketOptions != null ? socketOptions.native_ptr() : 0,
                tls != null ? tls.native_ptr() : 0, 
                clientId, cleanSession, keepAliveMs, pingTimeout);

        } catch (CrtRuntimeException ex) {
            future.completeExceptionally(ex);
        }
        return future;
    }

    /**
     * Disconnects the current session
     * @return When this future completes, the disconnection is complete
     */
    public CompletableFuture<Void> disconnect() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        if (native_ptr() == 0) {
            future.complete(null);
            return future;
        }
        disconnectAck = AsyncCallback.wrapFuture(future, null);
        connectionState = ConnectionState.DISCONNECTING;
        mqttConnectionDisconnect(native_ptr());
        return future;
    }

    /**
     * Subscribes to a topic
     * @param topic The topic to subscribe to
     * @param qos {@link QualityOfService} for this subscription
     * @param handler A handler which can recieve an MqttMessage when a message is published to the topic
     * @return Future result is the packet/message id associated with the subscribe operation
     */
    public CompletableFuture<Integer> subscribe(String topic, QualityOfService qos, Consumer<MqttMessage> handler) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        if (native_ptr() == 0) {
            future.completeExceptionally(new MqttException("Invalid connection during subscribe"));
            return future;
        }

        AsyncCallback subAck = AsyncCallback.wrapFuture(future, 0);
        try {
            int packetId = mqttConnectionSubscribe(native_ptr(), topic, qos.getValue(), new MessageHandler(topic, handler), subAck);
            // When the future completes, complete the returned future with the packetId
            return future.thenApply(unused -> packetId);
        }
        catch (CrtRuntimeException ex) {
            future.completeExceptionally(ex);
            return future;
        }
    }

    /**
     * Unsubscribes from a topic
     * @param topic The topic to unsubscribe from
     * @return Future result is the packet/message id associated with the unsubscribe operation
     */
    public CompletableFuture<Integer> unsubscribe(String topic) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        if (native_ptr() == 0) {
            future.completeExceptionally(new MqttException("Invalid connection during unsubscribe"));
            return future;
        }

        AsyncCallback unsubAck = AsyncCallback.wrapFuture(future, 0);
        int packetId = mqttConnectionUnsubscribe(native_ptr(), topic, unsubAck);
        // When the future completes, complete the returned future with the packetId
        return future.thenApply(unused -> packetId);
    }

    /**
     * Publishes a message to a topic
     * @param message The message to publish. The message contains the topic to publish to.
     * @param qos The {@link QualityOfService} to use for the publish operation
     * @param retain Whether or not the message should be retained by the broker to be delivered to future subscribers
     * @return Future value is the packet/message id associated with the publish operation
     */
    public CompletableFuture<Integer> publish(MqttMessage message, QualityOfService qos, boolean retain) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        if (native_ptr() == 0) {
            future.completeExceptionally(new MqttException("Invalid connection during publish"));
        }

        AsyncCallback pubAck = AsyncCallback.wrapFuture(future, 0);
        try {
            int packetId = mqttConnectionPublish(native_ptr(), message.getTopic(), qos.getValue(), retain, message.getPayloadDirect(), pubAck);
            // When the future completes, complete the returned future with the packetId
            return future.thenApply(unused -> packetId);
        }
        catch (CrtRuntimeException ex) {
            future.completeExceptionally(ex);
            return future;
        }
    }

    /**
     * Sets the last will and testament message to be delivered to a topic when this client disconnects
     * @param message The message to publish as the will. The message contains the topic that the message will be
     *                published to on disconnect.
     * @param qos The {@link QualityOfService} of the will message
     * @param retain Whether or not the message should be retained by the broker to be delivered to future subscribers
     * @throws MqttException If the connection is already connected, or is otherwise unable to set the will
     */
    public void setWill(MqttMessage message, QualityOfService qos, boolean retain) throws MqttException {
        if (native_ptr() == 0) {
            throw new MqttException("Invalid connection during setWill");
        }

        try {
            mqttConnectionSetWill(native_ptr(), message.getTopic(), qos.getValue(), retain, message.getPayloadDirect());
        }
        catch (CrtRuntimeException ex) {
            throw new MqttException("AWS CRT exception: " + ex.toString());
        }
    }

    /**
     * Sends a MQTT ping to the endpoint.
     * @throws MqttException If the connection is not connected, or the ping operation is otherwise unable to be attempted
     */
    public void ping() throws MqttException {
        if (native_ptr() == 0) {
            throw new MqttException("Invalid connection during ping");
        }
        try {
            mqttConnectionPing(native_ptr());
        } catch (CrtRuntimeException ex) {
            throw new MqttException("Failed to send ping: " + ex.getMessage());
        }
    }

    /*******************************************************************************
     * Native methods
     ******************************************************************************/
    private static native long mqttConnectionNew(long client, MqttConnection thisObj) throws CrtRuntimeException;

    private static native void mqttConnectionDestroy(long connection);

    private static native void mqttConnectionConnect(
        long connection, String endpoint, short port, 
        long socketOptions, long tlsContext, String clientId, 
        boolean cleanSession, int keepAliveMs, short pingTimeoutMs) throws CrtRuntimeException;

    private static native void mqttConnectionDisconnect(long connection);

    private static native short mqttConnectionSubscribe(long connection, String topic, int qos, MessageHandler handler, AsyncCallback ack) throws CrtRuntimeException;

    private static native short mqttConnectionUnsubscribe(long connection, String topic, AsyncCallback ack);

    private static native short mqttConnectionPublish(long connection, String topic, int qos, boolean retain, ByteBuffer payload, AsyncCallback ack) throws CrtRuntimeException;

    private static native boolean mqttConnectionSetWill(long connection, String topic, int qos, boolean retain, ByteBuffer payload) throws CrtRuntimeException;
    
    private static native void mqttConnectionSetLogin(long connection, String username, String password) throws CrtRuntimeException;

    private static native void mqttConnectionPing(long connection) throws CrtRuntimeException;
};
