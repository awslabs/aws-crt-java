
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
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.Log;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * This class wraps aws-c-mqtt to provide the basic MQTT pub/sub
 * functionality via the AWS Common Runtime
 * 
 * MqttConnection represents a single connection from one MqttClient to an MQTT
 * service endpoint
 */
public class MqttConnection extends CrtResource {
    private final MqttClient client;
    private MqttConnectionEvents userConnectionCallbacks;
    private AsyncCallback connectAck;

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
        
        try {
            acquireNativeHandle(mqttConnectionNew(mqttClient.getNativeHandle(), this));
            addReferenceTo(mqttClient);
            this.client = mqttClient;
            userConnectionCallbacks = callbacks;
        } catch (CrtRuntimeException ex) {
            throw new MqttException("Exception during mqttConnectionNew: " + ex.getMessage());
        }
    }

    /**
     * Disconnects if necessary, and frees native resources associated with this connection
     */
    @Override
    protected void releaseNativeHandle() {
        mqttConnectionDestroy(getNativeHandle());
    }

    /**
     * Determines whether a resource releases its dependencies at the same time the native handle is released or if it waits.
     * Resources that wait are responsible for calling releaseReferences() manually.
     */
    @Override
    protected boolean canReleaseReferencesImmediately() { return false; }

    /**
     * Sets the login credentials for the connection. Only valid before connect() has been called.
     * @param user Login username
     * @param pass Login password
     * @throws MqttException If the username or password are null
     */
    public void setLogin(String user, String pass) throws MqttException {
        try {
            mqttConnectionSetLogin(getNativeHandle(), user, pass);
        } catch (CrtRuntimeException ex) {
            throw new MqttException("Failed to set login: " + ex.getMessage());
        }        
    }

    // Called from native when the connection is established the first time
    private void onConnectionComplete(int errorCode, boolean sessionPresent) {
        if (connectAck != null) {
            if (errorCode == 0) {
                connectAck.onSuccess(sessionPresent);
            } else {
                connectAck.onFailure(new MqttException(errorCode));
            }
            connectAck = null;
        }
    }

    // Called when the connection drops or is disconnected. If errorCode == 0, the disconnect was intentional.
    private void onConnectionInterrupted(int errorCode, AsyncCallback callback) {
        if (callback != null) {
            if (errorCode == 0) {
                Log.log(Log.LogLevel.Trace, "onConnectionInterrupted with success callback");
                callback.onSuccess();
            } else {
                Log.log(Log.LogLevel.Trace, "onConnectionInterrupted with failure callback");
                callback.onFailure(new MqttException(errorCode));
            }
        }
        if (userConnectionCallbacks != null) {
            userConnectionCallbacks.onConnectionInterrupted(errorCode);
        }
    }

    // Called when a reconnect succeeds, and also on initial connection success.
    private void onConnectionResumed(boolean sessionPresent) {
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
        return connect(clientId, endpoint, port, null, true, 0, 0);
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
        SocketOptions socketOptions, boolean cleanSession, int keepAliveMs, int pingTimeoutMs)
            throws MqttException {

        TlsContext tls = client.getTlsContext();

        // Just clamp the pingTimeout, no point in throwing
        short pingTimeout = (short) Math.max(0, Math.min(pingTimeoutMs, Short.MAX_VALUE));
        if (port > Short.MAX_VALUE || port <= 0) {
            throw new MqttException("Port must be betweeen 0 and " + Short.MAX_VALUE);
        }
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        connectAck = AsyncCallback.wrapFuture(future, null);
        try {
            mqttConnectionConnect(
                getNativeHandle(), endpoint, (short) port,
                socketOptions != null ? socketOptions.getNativeHandle() : 0,
                tls != null ? tls.getNativeHandle() : 0,
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
        if (isNull()) {
            future.complete(null);
            return future;
        }
        AsyncCallback disconnectAck = AsyncCallback.wrapFuture(future, null);
        mqttConnectionDisconnect(getNativeHandle(), disconnectAck);
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
        if (isNull()) {
            future.completeExceptionally(new MqttException("Invalid connection during subscribe"));
            return future;
        }

        AsyncCallback subAck = AsyncCallback.wrapFuture(future, 0);
        try {
            int packetId = mqttConnectionSubscribe(getNativeHandle(), topic, qos.getValue(), new MessageHandler(topic, handler), subAck);
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
        if (isNull()) {
            future.completeExceptionally(new MqttException("Invalid connection during unsubscribe"));
            return future;
        }

        AsyncCallback unsubAck = AsyncCallback.wrapFuture(future, 0);
        int packetId = mqttConnectionUnsubscribe(getNativeHandle(), topic, unsubAck);
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
        if (isNull()) {
            future.completeExceptionally(new MqttException("Invalid connection during publish"));
        }

        AsyncCallback pubAck = AsyncCallback.wrapFuture(future, 0);
        try {
            int packetId = mqttConnectionPublish(getNativeHandle(), message.getTopic(), qos.getValue(), retain, message.getPayloadDirect(), pubAck);
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
        if (isNull()) {
            throw new MqttException("Invalid connection during setWill");
        }

        try {
            mqttConnectionSetWill(getNativeHandle(), message.getTopic(), qos.getValue(), retain, message.getPayloadDirect());
        }
        catch (CrtRuntimeException ex) {
            throw new MqttException("AWS CRT exception: " + ex.toString());
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

    private static native void mqttConnectionDisconnect(long connection, AsyncCallback ack);

    private static native short mqttConnectionSubscribe(long connection, String topic, int qos, MessageHandler handler, AsyncCallback ack) throws CrtRuntimeException;

    private static native short mqttConnectionUnsubscribe(long connection, String topic, AsyncCallback ack);

    private static native short mqttConnectionPublish(long connection, String topic, int qos, boolean retain, ByteBuffer payload, AsyncCallback ack) throws CrtRuntimeException;

    private static native boolean mqttConnectionSetWill(long connection, String topic, int qos, boolean retain, ByteBuffer payload) throws CrtRuntimeException;
    
    private static native void mqttConnectionSetLogin(long connection, String username, String password) throws CrtRuntimeException;
};
