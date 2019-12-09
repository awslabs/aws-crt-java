
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

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * This class wraps aws-c-mqtt to provide the basic MQTT pub/sub functionality
 * via the AWS Common Runtime
 * 
 * MqttClientConnection represents a single connection from one MqttClient to an
 * MQTT service endpoint
 */
public class MqttClientConnection extends CrtResource {
    private final MqttClient client;
    private MqttClientConnectionEvents userConnectionCallbacks;

    private String clientId;
    private String endpoint;
    private int port;

    private SocketOptions socketOptions;
    private boolean cleanSession = true;
    private int keepAliveMs = 0;
    private int pingTimeoutMs = 0;

    private AsyncCallback connectAck;

    /**
     * Wraps the handler provided by the user so that an MqttMessage can be
     * constructed from the published buffer and topic
     */
    private class MessageHandler {
        Consumer<MqttMessage> callback;

        private MessageHandler(Consumer<MqttMessage> callback) {
            this.callback = callback;
        }

        /* called from native when a message is delivered */
        void deliver(String topic, byte[] payload) {
            callback.accept(new MqttMessage(topic, payload));
        }
    }

    /**
     * Constructs a new MqttClientConnection. Connections are reusable after being
     * disconnected.
     * 
     * @param mqttClient Must be non-null
     * @param clientId Must be non-null
     * @param endpoint Must be non-null
     * @param port
     * @throws MqttException If mqttClient is null
     */
    public MqttClientConnection(MqttClient mqttClient, String clientId, String endpoint, int port) throws MqttException {
        if (mqttClient == null) {
            throw new MqttException("mqttClient must not be null");
        }
        if (clientId == null) {
            throw new MqttException("clientId must not be null");
        }
        if (endpoint == null) {
            throw new MqttException("endpoint must not be null");
        }
        if (port <= 0 || port > 65535) {
            throw new MqttException("port must be a positive integer between 1 and 65535");
        }

        try {
            acquireNativeHandle(mqttClientConnectionNew(mqttClient.getNativeHandle(), this));
            addReferenceTo(mqttClient);
            this.client = mqttClient;
            this.clientId = clientId;
            this.endpoint = endpoint;
            this.port = port;
        } catch (CrtRuntimeException ex) {
            throw new MqttException("Exception during mqttClientConnectionNew: " + ex.getMessage());
        }
    }

    /**
     * Frees native resources associated with this connection.
     */
    @Override
    protected void releaseNativeHandle() {
        mqttClientConnectionDestroy(getNativeHandle());
    }

    /**
     * Determines whether a resource releases its dependencies at the same time the
     * native handle is released or if it waits. Resources that wait are responsible
     * for calling releaseReferences() manually.
     */
    @Override
    protected boolean canReleaseReferencesImmediately() {
        return false;
    }

    public void setConnectionCallbacks(MqttClientConnectionEvents connectionCallbacks) {
        this.userConnectionCallbacks = connectionCallbacks;
    }

    public MqttClientConnectionEvents getConnectionCallbacks() {
        return userConnectionCallbacks;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setSocketOptions(SocketOptions socketOptions) {
        swapReferenceTo(this.socketOptions, socketOptions);
        this.socketOptions = socketOptions;
    }

    public SocketOptions getSocketOptions() {
        return socketOptions;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    public boolean getCleanSession() {
        return cleanSession;
    }

    public void setKeepAliveMs(int keepAliveMs) {
        this.keepAliveMs = keepAliveMs;
    }

    public int getKeepAliveMs() {
        return keepAliveMs;
    }

    public void setPingTimeoutMs(int pingTimeoutMs) {
        this.pingTimeoutMs = pingTimeoutMs;
    }

    public int getPingTimeoutMs() {
        return pingTimeoutMs;
    }

    /**
     * Sets the login credentials for the connection. Only valid before connect()
     * has been called.
     * 
     * @param user Login username
     * @param pass Login password
     * @throws MqttException If the username or password are null
     */
    public void setLogin(String user, String pass) throws MqttException {
        try {
            mqttClientConnectionSetLogin(getNativeHandle(), user, pass);
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

    // Called when the connection drops or is disconnected. If errorCode == 0, the
    // disconnect was intentional.
    private void onConnectionInterrupted(int errorCode, AsyncCallback callback) {
        if (callback != null) {
            if (errorCode == 0) {
                callback.onSuccess();
            } else {
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
     * Connect to the service endpoint and start a session
     *
     * @return Future result is true if resuming a session, false if clean session
     * @throws MqttException If the port is out of range
     */
    public CompletableFuture<Boolean> connect() throws MqttException {

        TlsContext tls = client.getTlsContext();

        // Just clamp the pingTimeout, no point in throwing
        short pingTimeout = (short) Math.max(0, Math.min(pingTimeoutMs, Short.MAX_VALUE));
        if (port > Short.MAX_VALUE || port <= 0) {
            throw new MqttException("Port must be betweeen 0 and " + Short.MAX_VALUE);
        }
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        connectAck = AsyncCallback.wrapFuture(future, null);
        try {
            mqttClientConnectionConnect(getNativeHandle(), endpoint, (short) port,
                    socketOptions != null ? socketOptions.getNativeHandle() : 0,
                    tls != null ? tls.getNativeHandle() : 0, clientId, cleanSession, keepAliveMs, pingTimeout);

        } catch (CrtRuntimeException ex) {
            future.completeExceptionally(ex);
        }
        return future;
    }

    /**
     * Disconnects the current session
     * 
     * @return When this future completes, the disconnection is complete
     */
    public CompletableFuture<Void> disconnect() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        if (isNull()) {
            future.complete(null);
            return future;
        }
        AsyncCallback disconnectAck = AsyncCallback.wrapFuture(future, null);
        mqttClientConnectionDisconnect(getNativeHandle(), disconnectAck);
        return future;
    }

    /**
     * Subscribes to a topic
     * 
     * @param topic   The topic to subscribe to
     * @param qos     {@link QualityOfService} for this subscription
     * @param handler A handler which can recieve an MqttMessage when a message is
     *                published to the topic
     * @return Future result is the packet/message id associated with the subscribe
     *         operation
     */
    public CompletableFuture<Integer> subscribe(String topic, QualityOfService qos, Consumer<MqttMessage> handler) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        if (isNull()) {
            future.completeExceptionally(new MqttException("Invalid connection during subscribe"));
            return future;
        }

        AsyncCallback subAck = AsyncCallback.wrapFuture(future, 0);
        try {
            int packetId = mqttClientConnectionSubscribe(getNativeHandle(), topic, qos.getValue(),
                    handler != null ? new MessageHandler(handler) : null, subAck);
            // When the future completes, complete the returned future with the packetId
            return future.thenApply(unused -> packetId);
        } catch (CrtRuntimeException ex) {
            future.completeExceptionally(ex);
            return future;
        }
    }

    /**
     * Subscribes to a topic without a handler (messages will only be delivered to the OnMessage handler)
     * 
     * @param topic   The topic to subscribe to
     * @param qos     {@link QualityOfService} for this subscription
     * @return Future result is the packet/message id associated with the subscribe
     *         operation
     */
    public CompletableFuture<Integer> subscribe(String topic, QualityOfService qos) {
        return subscribe(topic, qos, null);
    }

    /**
     * Sets a handler to be invoked whenever a message arrives, subscription or not
     * @param handler A handler which can receive any MqttMessage
     */
    public void onMessage(Consumer<MqttMessage> handler) {
        mqttClientConnectionOnMessage(getNativeHandle(), new MessageHandler(handler));
    }

    /**
     * Unsubscribes from a topic
     * 
     * @param topic The topic to unsubscribe from
     * @return Future result is the packet/message id associated with the
     *         unsubscribe operation
     */
    public CompletableFuture<Integer> unsubscribe(String topic) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        if (isNull()) {
            future.completeExceptionally(new MqttException("Invalid connection during unsubscribe"));
            return future;
        }

        AsyncCallback unsubAck = AsyncCallback.wrapFuture(future, 0);
        int packetId = mqttClientConnectionUnsubscribe(getNativeHandle(), topic, unsubAck);
        // When the future completes, complete the returned future with the packetId
        return future.thenApply(unused -> packetId);
    }

    /**
     * Publishes a message to a topic
     * 
     * @param message The message to publish. The message contains the topic to
     *                publish to.
     * @param qos     The {@link QualityOfService} to use for the publish operation
     * @param retain  Whether or not the message should be retained by the broker to
     *                be delivered to future subscribers
     * @return Future value is the packet/message id associated with the publish
     *         operation
     */
    public CompletableFuture<Integer> publish(MqttMessage message, QualityOfService qos, boolean retain) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        if (isNull()) {
            future.completeExceptionally(new MqttException("Invalid connection during publish"));
        }

        AsyncCallback pubAck = AsyncCallback.wrapFuture(future, 0);
        try {
            int packetId = mqttClientConnectionPublish(getNativeHandle(), message.getTopic(), qos.getValue(), retain,
                    message.getPayload(), pubAck);
            // When the future completes, complete the returned future with the packetId
            return future.thenApply(unused -> packetId);
        } catch (CrtRuntimeException ex) {
            future.completeExceptionally(ex);
            return future;
        }
    }

    /**
     * Sets the last will and testament message to be delivered to a topic when this
     * client disconnects
     * 
     * @param message The message to publish as the will. The message contains the
     *                topic that the message will be published to on disconnect.
     * @param qos     The {@link QualityOfService} of the will message
     * @param retain  Whether or not the message should be retained by the broker to
     *                be delivered to future subscribers
     * @throws MqttException If the connection is already connected, or is otherwise
     *                       unable to set the will
     */
    public void setWill(MqttMessage message, QualityOfService qos, boolean retain) throws MqttException {
        if (isNull()) {
            throw new MqttException("Invalid connection during setWill");
        }

        try {
            mqttClientConnectionSetWill(getNativeHandle(), message.getTopic(), qos.getValue(), retain,
                    message.getPayload());
        } catch (CrtRuntimeException ex) {
            throw new MqttException("AWS CRT exception: " + ex.toString());
        }
    }

    /*******************************************************************************
     * Native methods
     ******************************************************************************/
    private static native long mqttClientConnectionNew(long client, MqttClientConnection thisObj)
            throws CrtRuntimeException;

    private static native void mqttClientConnectionDestroy(long connection);

    private static native void mqttClientConnectionConnect(long connection, String endpoint, short port,
            long socketOptions, long tlsContext, String clientId, boolean cleanSession, int keepAliveMs,
            short pingTimeoutMs) throws CrtRuntimeException;

    private static native void mqttClientConnectionDisconnect(long connection, AsyncCallback ack);

    private static native short mqttClientConnectionSubscribe(long connection, String topic, int qos,
            MessageHandler handler, AsyncCallback ack) throws CrtRuntimeException;

    private static native void mqttClientConnectionOnMessage(long connection, MessageHandler handler)
            throws CrtRuntimeException;

    private static native short mqttClientConnectionUnsubscribe(long connection, String topic, AsyncCallback ack);

    private static native short mqttClientConnectionPublish(long connection, String topic, int qos, boolean retain,
            byte[] payload, AsyncCallback ack) throws CrtRuntimeException;

    private static native boolean mqttClientConnectionSetWill(long connection, String topic, int qos, boolean retain,
            byte[] payload) throws CrtRuntimeException;

    private static native void mqttClientConnectionSetLogin(long connection, String username, String password)
            throws CrtRuntimeException;
};
