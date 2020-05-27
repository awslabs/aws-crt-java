
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
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpProxyOptions;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.mqtt.MqttConnectionConfig;

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

    private MqttConnectionConfig config;

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
     * @param config Configuration to use
     * @throws MqttException If mqttClient is null
     */
    public MqttClientConnection(MqttConnectionConfig config) throws MqttException {
        if (config.getMqttClient() == null) {
            throw new MqttException("mqttClient must not be null");
        }
        if (config.getClientId() == null) {
            throw new MqttException("clientId must not be null");
        }
        if (config.getEndpoint() == null) {
            throw new MqttException("endpoint must not be null");
        }
        if (config.getPort() <= 0 || config.getPort() > 65535) {
            throw new MqttException("port must be a positive integer between 1 and 65535");
        }

        try {
            acquireNativeHandle(mqttClientConnectionNew(config.getMqttClient().getNativeHandle(), this));

            if (config.getUsername() != null) {
                mqttClientConnectionSetLogin(getNativeHandle(), config.getUsername(), config.getPassword());
            }

            MqttMessage message = config.getWillMessage();
            if (message != null) {
                mqttClientConnectionSetWill(getNativeHandle(), message.getTopic(), config.getWillQos().getValue(), config.getWillRetain(),
                        message.getPayload());
            }

            if (config.getUseWebsockets()) {
                mqttClientConnectionUseWebsockets(getNativeHandle());
                if (config.getWebsocketProxyOptions() != null) {
                    HttpProxyOptions options = config.getWebsocketProxyOptions();
                    TlsContext proxyTlsContext = options.getTlsContext();
                    mqttClientConnectionSetWebsocketProxyOptions(getNativeHandle(),
                        options.getHost(),
                        options.getPort(),
                        proxyTlsContext != null ? proxyTlsContext.getNativeHandle() : 0,
                        options.getAuthorizationType().getValue(),
                        options.getAuthorizationUsername(),
                        options.getAuthorizationPassword());
                }
            }

            addReferenceTo(config);
            this.config = config;

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
        MqttClientConnectionEvents callbacks = config.getConnectionCallbacks();
        if (callbacks != null) {
            callbacks.onConnectionInterrupted(errorCode);
        }
    }

    // Called when a reconnect succeeds, and also on initial connection success.
    private void onConnectionResumed(boolean sessionPresent) {
        MqttClientConnectionEvents callbacks = config.getConnectionCallbacks();
        if (callbacks != null) {
            callbacks.onConnectionResumed(sessionPresent);
        }
    }

    /**
     * Connect to the service endpoint and start a session
     *
     * @return Future result is true if resuming a session, false if clean session
     * @throws MqttException If the port is out of range
     */
    public CompletableFuture<Boolean> connect() throws MqttException {

        TlsContext tls = config.getMqttClient().getTlsContext();

        // Just clamp the pingTimeout, no point in throwing
        short pingTimeout = (short) Math.max(0, Math.min(config.getPingTimeoutMs(), Short.MAX_VALUE));

        short port = (short)config.getPort();
        if (port > Short.MAX_VALUE || port <= 0) {
            throw new MqttException("Port must be betweeen 0 and " + Short.MAX_VALUE);
        }
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        connectAck = AsyncCallback.wrapFuture(future, null);
        SocketOptions socketOptions = config.getSocketOptions();

        try {
            mqttClientConnectionConnect(getNativeHandle(), config.getEndpoint(), port,
                    socketOptions != null ? socketOptions.getNativeHandle() : 0,
                    tls != null ? tls.getNativeHandle() : 0, config.getClientId(), config.getCleanSession(),
                    config.getKeepAliveMs(), pingTimeout);

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
     * Subscribes to a topic without a handler (messages will only be delivered to
     * the OnMessage handler)
     *
     * @param topic The topic to subscribe to
     * @param qos   {@link QualityOfService} for this subscription
     * @return Future result is the packet/message id associated with the subscribe
     *         operation
     */
    public CompletableFuture<Integer> subscribe(String topic, QualityOfService qos) {
        return subscribe(topic, qos, null);
    }

    /**
     * Sets a handler to be invoked whenever a message arrives, subscription or not
     *
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

    // Called from native when a websocket handshake request is being prepared.
    private void onWebsocketHandshake(HttpRequest handshakeRequest, long nativeUserData) {
        CompletableFuture<HttpRequest> future = new CompletableFuture<>();
        future.whenComplete((x, throwable) -> {
            mqttClientConnectionWebsocketHandshakeComplete(getNativeHandle(), x.marshalForJni(), throwable, nativeUserData);
        });

        WebsocketHandshakeTransformArgs args = new WebsocketHandshakeTransformArgs(this, handshakeRequest, future);

        Consumer<WebsocketHandshakeTransformArgs> transform = config.getWebsocketHandshakeTransform();
        if (transform != null) {
            transform.accept(args);
        } else {
            args.complete(handshakeRequest);
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

    private static native void mqttClientConnectionUseWebsockets(long connection) throws CrtRuntimeException;

    private static native void mqttClientConnectionWebsocketHandshakeComplete(long connection, byte[] marshalledRequest, Throwable throwable,
            long nativeUserData) throws CrtRuntimeException;

    private static native void mqttClientConnectionSetWebsocketProxyOptions(long connection,
                                                                    String proxyHost,
                                                                    int proxyPort,
                                                                    long proxyTlsContext,
                                                                    int proxyAuthorizationType,
                                                                    String proxyAuthorizationUsername,
                                                                    String proxyAuthorizationPassword) throws CrtRuntimeException;
};
