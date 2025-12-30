
/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
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
import software.amazon.awssdk.crt.mqtt5.Mqtt5Client;
import software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions;
import software.amazon.awssdk.crt.mqtt5.packets.ConnectPacket;
import software.amazon.awssdk.crt.internal.IoTDeviceSDKMetrics;

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

    private static final int MAX_PORT = 65535;

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
        void deliver(String topic, byte[] payload, boolean dup, int qos, boolean retain) {
            QualityOfService qosEnum = QualityOfService.getEnumValueFromInteger(qos);
            callback.accept(new MqttMessage(topic, payload, qosEnum, retain, dup));
        }
    }

    /**
     * Static help function to create a MqttConnectionConfig from a
     * Mqtt5ClientOptions
     */
    private static MqttConnectionConfig s_toMqtt3ConnectionConfig(Mqtt5ClientOptions mqtt5options) {
        MqttConnectionConfig options = new MqttConnectionConfig();
        options.setEndpoint(mqtt5options.getHostName());
        options.setPort(mqtt5options.getPort() != null ? Math.toIntExact(mqtt5options.getPort()) : 0);
        options.setSocketOptions(mqtt5options.getSocketOptions());
        if (mqtt5options.getConnectOptions() != null) {
            options.setClientId(mqtt5options.getConnectOptions().getClientId());
            options.setKeepAliveSecs(
                    mqtt5options.getConnectOptions().getKeepAliveIntervalSeconds() != null
                            ? Math.toIntExact(mqtt5options.getConnectOptions().getKeepAliveIntervalSeconds())
                            : 0);
        }
        options.setCleanSession(
                mqtt5options.getSessionBehavior().compareTo(Mqtt5ClientOptions.ClientSessionBehavior.CLEAN) <= 0);
        options.setPingTimeoutMs(
                mqtt5options.getPingTimeoutMs() != null ? Math.toIntExact(mqtt5options.getPingTimeoutMs()) : 0);
        options.setProtocolOperationTimeoutMs(mqtt5options.getAckTimeoutSeconds() != null
                ? Math.toIntExact(mqtt5options.getAckTimeoutSeconds()) * 1000
                : 0);
        return options;
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
        if (config.getPort() <= 0 || config.getPort() > MAX_PORT) {
            throw new MqttException("port must be a positive integer between 1 and 65535");
        }

        try {
            acquireNativeHandle(mqttClientConnectionNewFrom311Client(config.getMqttClient().getNativeHandle(), this));
            SetupConfig(config);

        } catch (CrtRuntimeException ex) {
            throw new MqttException("Exception during mqttClientConnectionNew: " + ex.getMessage());
        }
    }

    /**
     * Constructs a new MqttClientConnection from a Mqtt5Client. Connections are
     * reusable after being
     * disconnected.
     *
     * @param mqtt5client the mqtt5 client to setup from
     * @param callbacks   connection callbacks triggered when receive connection
     *                    events
     *
     * @throws MqttException If mqttClient is null
     */
    public MqttClientConnection(Mqtt5Client mqtt5client, MqttClientConnectionEvents callbacks) throws MqttException {
        if (mqtt5client == null) {
            throw new MqttException("mqttClient must not be null");
        }

        try (MqttConnectionConfig config = s_toMqtt3ConnectionConfig(mqtt5client.getClientOptions())) {
            config.setMqtt5Client(mqtt5client);
            if (callbacks != null) {
                config.setConnectionCallbacks(callbacks);
            }

            if (config.getClientId() == null) {
                throw new MqttException("clientId must not be null");
            }
            if (config.getEndpoint() == null) {
                throw new MqttException("endpoint must not be null");
            }
            if (config.getPort() <= 0 || config.getPort() > MAX_PORT) {
                throw new MqttException("port must be a positive integer between 1 and 65535");
            }

            try {
                acquireNativeHandle(
                        mqttClientConnectionNewFrom5Client(config.getMqtt5Client().getNativeHandle(), this));
                SetupConfig(config);

            } catch (CrtRuntimeException ex) {
                throw new MqttException("Exception during mqttClientConnectionNew: " + ex.getMessage());
            }
        } catch (Exception e) {
            throw new MqttException("Failed to setup mqtt3 connection : " + e.getMessage());
        }

    }

    private void SetupConfig(MqttConnectionConfig config) throws MqttException {
        try {
            if (config.getUsername() != null) {
                mqttClientConnectionSetLogin(getNativeHandle(), config.getUsername(), config.getPassword());
            }

            if (config.getMetricsEnabled()) {
                mqttClientConnectionSetMetrics(getNativeHandle(), new IoTDeviceSDKMetrics());
            }

            if (config.getMinReconnectTimeoutSecs() != 0L && config.getMaxReconnectTimeoutSecs() != 0L) {
                mqttClientConnectionSetReconnectTimeout(getNativeHandle(), config.getMinReconnectTimeoutSecs(),
                        config.getMaxReconnectTimeoutSecs());
            }

            MqttMessage message = config.getWillMessage();
            if (message != null) {
                mqttClientConnectionSetWill(getNativeHandle(), message.getTopic(), message.getQos().getValue(),
                        message.getRetain(), message.getPayload());
            }

            if (config.getUseWebsockets()) {
                mqttClientConnectionUseWebsockets(getNativeHandle());
            }

            if (config.getHttpProxyOptions() != null) {
                HttpProxyOptions options = config.getHttpProxyOptions();
                TlsContext proxyTlsContext = options.getTlsContext();
                mqttClientConnectionSetHttpProxyOptions(getNativeHandle(),
                        options.getConnectionType().getValue(),
                        options.getHost(),
                        options.getPort(),
                        proxyTlsContext != null ? proxyTlsContext.getNativeHandle() : 0,
                        options.getAuthorizationType().getValue(),
                        options.getAuthorizationUsername(),
                        options.getAuthorizationPassword(),
                        options.getNoProxyHosts());
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

    // called when the connection or reconnection is successful
    private void onConnectionSuccess(boolean sessionPresent) {
        MqttClientConnectionEvents callbacks = config.getConnectionCallbacks();
        if (callbacks != null) {
            OnConnectionSuccessReturn returnData = new OnConnectionSuccessReturn(sessionPresent);
            callbacks.onConnectionSuccess(returnData);
        }
    }

    // called when the connection drops
    private void onConnectionFailure(int errorCode) {
        MqttClientConnectionEvents callbacks = config.getConnectionCallbacks();
        if (callbacks != null) {
            OnConnectionFailureReturn returnData = new OnConnectionFailureReturn(errorCode);
            callbacks.onConnectionFailure(returnData);
        }
    }

    // Called when a reconnect succeeds, and also on initial connection success.
    private void onConnectionResumed(boolean sessionPresent) {
        MqttClientConnectionEvents callbacks = config.getConnectionCallbacks();
        if (callbacks != null) {
            callbacks.onConnectionResumed(sessionPresent);
            OnConnectionSuccessReturn returnData = new OnConnectionSuccessReturn(sessionPresent);
            callbacks.onConnectionSuccess(returnData);
        }
    }

    // Called when the connection is disconnected successfully and intentionally.
    private void onConnectionClosed() {
        if (config != null) {
            MqttClientConnectionEvents callbacks = config.getConnectionCallbacks();
            if (callbacks != null) {
                OnConnectionClosedReturn returnData = new OnConnectionClosedReturn();
                callbacks.onConnectionClosed(returnData);
            }
        }
    }

    /**
     * Connect to the service endpoint and start a session
     *
     * @return Future result is true if resuming a session, false if clean session
     * @throws MqttException If the port is out of range
     */
    public CompletableFuture<Boolean> connect() throws MqttException {

        TlsContext tls = null;
        if (config.getMqttClient() != null) {
            tls = config.getMqttClient().getTlsContext();
        } else if (config.getMqtt5Client() != null) {
            tls = config.getMqtt5Client().getClientOptions().getTlsContext();
        }

        // Just clamp the pingTimeout, no point in throwing
        short pingTimeout = (short) Math.max(0, Math.min(config.getPingTimeoutMs(), Short.MAX_VALUE));

        int port = config.getPort();
        if (port > MAX_PORT || port <= 0) {
            throw new MqttException("Port must be betweeen 0 and 65535");
        }
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        connectAck = AsyncCallback.wrapFuture(future, null);
        SocketOptions socketOptions = config.getSocketOptions();

        try {
            mqttClientConnectionConnect(getNativeHandle(), config.getEndpoint(), port,
                    socketOptions != null ? socketOptions.getNativeHandle() : 0,
                    tls != null ? tls.getNativeHandle() : 0, config.getClientId(), config.getCleanSession(),
                    config.getKeepAliveSecs(), pingTimeout, config.getProtocolOperationTimeoutMs());

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
     * @param handler A handler which can receive an MqttMessage when a message is
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
     * Publishes a message to a topic.
     *
     * @param message The message to publish.
     *
     * @return Future value is the packet/message id associated with the publish
     *         operation
     */
    public CompletableFuture<Integer> publish(MqttMessage message) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        if (isNull()) {
            future.completeExceptionally(new MqttException("Invalid connection during publish"));
        }

        AsyncCallback pubAck = AsyncCallback.wrapFuture(future, 0);
        try {
            int packetId = mqttClientConnectionPublish(getNativeHandle(), message.getTopic(),
                    message.getQos().getValue(), message.getRetain(), message.getPayload(), pubAck);
            // When the future completes, complete the returned future with the packetId
            return future.thenApply(unused -> packetId);
        } catch (CrtRuntimeException ex) {
            future.completeExceptionally(ex);
            return future;
        }
    }

    @Deprecated
    public CompletableFuture<Integer> publish(MqttMessage message, QualityOfService qos, boolean retain) {
        return publish(new MqttMessage(message.getTopic(), message.getPayload(), qos, retain));
    }

    // Called from native when a websocket handshake request is being prepared.
    private void onWebsocketHandshake(HttpRequest handshakeRequest, long nativeUserData) {
        CompletableFuture<HttpRequest> future = new CompletableFuture<>();
        future.whenComplete((x, throwable) -> {
            mqttClientConnectionWebsocketHandshakeComplete(getNativeHandle(), x != null ? x.marshalForJni() : null,
                    throwable, nativeUserData);
        });

        WebsocketHandshakeTransformArgs args = new WebsocketHandshakeTransformArgs(this, handshakeRequest, future);

        Consumer<WebsocketHandshakeTransformArgs> transform = config.getWebsocketHandshakeTransform();
        if (transform != null) {
            transform.accept(args);
        } else {
            args.complete(handshakeRequest);
        }
    }

    /**
     * Returns statistics about the current state of the MqttClientConnection's
     * queue of operations.
     *
     * @return Current state of the connection's queue of operations.
     */
    public MqttClientConnectionOperationStatistics getOperationStatistics() {
        return mqttClientConnectionGetOperationStatistics(getNativeHandle());
    }

    /*******************************************************************************
     * Native methods
     ******************************************************************************/
    private static native long mqttClientConnectionNewFrom311Client(long client, MqttClientConnection thisObj)
            throws CrtRuntimeException;

    private static native long mqttClientConnectionNewFrom5Client(long client, MqttClientConnection thisObj)
            throws CrtRuntimeException;

    private static native void mqttClientConnectionDestroy(long connection);

    private static native void mqttClientConnectionConnect(long connection, String endpoint, int port,
            long socketOptions, long tlsContext, String clientId, boolean cleanSession, int keepAliveMs,
            short pingTimeoutMs, int protocolOperationTimeoutMs) throws CrtRuntimeException;

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

    private static native void mqttClientConnectionSetMetrics(long connection, IoTDeviceSDKMetrics metrics)
            throws CrtRuntimeException;

    private static native void mqttClientConnectionSetReconnectTimeout(long connection, long minTimeout,
            long maxTimeout)
            throws CrtRuntimeException;

    private static native void mqttClientConnectionUseWebsockets(long connection) throws CrtRuntimeException;

    private static native void mqttClientConnectionWebsocketHandshakeComplete(long connection, byte[] marshalledRequest,
            Throwable throwable,
            long nativeUserData) throws CrtRuntimeException;

    private static native void mqttClientConnectionSetHttpProxyOptions(long connection,
            int proxyConnectionType,
            String proxyHost,
            int proxyPort,
            long proxyTlsContext,
            int proxyAuthorizationType,
            String proxyAuthorizationUsername,
            String proxyAuthorizationPassword,
            String noProxyHosts) throws CrtRuntimeException;

    private static native MqttClientConnectionOperationStatistics mqttClientConnectionGetOperationStatistics(
            long connection);

};
