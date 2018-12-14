
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

import software.amazon.awssdk.crt.TlsContext;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.SocketOptions;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.nio.ByteBuffer;
import java.io.Closeable;

/**
 * This class wraps aws-c-mqtt to provide the basic MQTT pub/sub
 * functionalities via the AWS Common Runtime
 * 
 * MqttConnection represents a single connection from one MqttClient to an MQTT
 * service endpoint
 */
public class MqttConnection extends CrtResource implements Closeable {
    private final MqttClient client;
    private volatile ConnectionState connectionState = ConnectionState.DISCONNECTED;

    public enum ConnectionState {
        DISCONNECTED, CONNECTING, CONNECTED, DISCONNECTING,
    }


    
    private class MessageHandler {
        String topic;
        Consumer<MqttMessage> callback;

        private MessageHandler(String topic, Consumer<MqttMessage> callback) {
            this.callback = callback;
            this.topic = topic;
        }

        void deliver(byte[] payload) {
            callback.accept(new MqttMessage(topic, ByteBuffer.wrap(payload)));
        }
    }

    /* used to receive the result of an async operation from CRT mqtt */
    interface AsyncCallback {
        public void onSuccess();
        public void onFailure(String reason);
    }
    
    /* used to receive connection events from the CRT */
    interface ClientCallbacks {
        void onConnected();

        /* return true to attempt reconnect if connection is in a recoverable state */
        boolean onDisconnected(boolean recoverable, String reason);
    }

    public MqttConnection(MqttClient mqttClient, String endpoint, int port) throws MqttException {
        this(mqttClient, endpoint, port, null);
    }

    public MqttConnection(MqttClient mqttClient, String endpoint, int port, SocketOptions socketOptions) throws MqttException {
        client = mqttClient;
        if (port > Short.MAX_VALUE || port <= 0) {
            throw new MqttException("Port must be betweeen 0 and " + Short.MAX_VALUE);
        }
        try {
            TlsContext tls = client.tlsContext();
            ClientCallbacks clientCallbacks = new ClientCallbacks() {
                @Override
                public void onConnected() {
                    connectionState = ConnectionState.CONNECTED;
                    onOnline();
                }

                @Override
                public boolean onDisconnected(boolean recoverable, String reason) {
                    connectionState = ConnectionState.DISCONNECTED;
                    return onOffline(recoverable, reason);
                }
            };
            acquire(mqtt_new(client.native_ptr(), endpoint, (short)port, clientCallbacks,
                    socketOptions != null ? socketOptions.native_ptr() : 0,
                    tls != null ? tls.native_ptr() : 0)
            );
        } catch (CrtRuntimeException ex) {
            throw new MqttException("Exception during mqtt_new: " + ex.getMessage());
        }
    }

    @Override
    public void close() {
        disconnect();
        mqtt_clean_up(release());
    }

    public ConnectionState getState() {
        return connectionState;
    }

    public void setLogin(String user, String pass) throws MqttException {
        try {
            mqtt_set_login(native_ptr(), user, pass);
        } catch (CrtRuntimeException ex) {
            throw new MqttException("Failed to set login: " + ex.getMessage());
        }        
    }

    private static AsyncCallback wrapVoidFuture(CompletableFuture<Void> future) {
        return new AsyncCallback() {
            @Override
            public void onSuccess() {
                future.complete(null);
            }

            @Override
            public void onFailure(String reason) {
                Throwable cause = new MqttException(reason);
                future.completeExceptionally(cause);
            }
        };
    }

    private static AsyncCallback wrapPacketFuture(CompletableFuture<Integer> future) {
        return new AsyncCallback() {
            @Override
            public void onSuccess() {
                future.complete(0);
            }

            @Override
            public void onFailure(String reason) {
                Throwable cause = new MqttException(reason);
                future.completeExceptionally(cause);
            }
        };
    }

    public CompletableFuture<Void> connect(String clientId) {
        return connect(clientId, true, 0);
    }

    public CompletableFuture<Void> connect(String clientId, boolean cleanSession) {
        return connect(clientId, cleanSession, 0);
    }

    public CompletableFuture<Void> connect(String clientId, boolean cleanSession, int keepAliveMs) {
        // Just clamp the keepAlive, no point in throwing
        short keepAlive = (short)Math.max(0, Math.min(keepAliveMs, Short.MAX_VALUE));
        CompletableFuture<Void> future = new CompletableFuture<>();
        AsyncCallback connectAck = wrapVoidFuture(future);
        try {
            connectionState = ConnectionState.CONNECTING;
            mqtt_connect(native_ptr(), clientId, cleanSession, keepAlive, connectAck);
        } catch (CrtRuntimeException ex) {
            future.completeExceptionally(ex);
        }
        return future;
    }

    public CompletableFuture<Void> disconnect() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        if (native_ptr() == 0) {
            future.complete(null);
            return future;
        }
        AsyncCallback disconnectAck = wrapVoidFuture(future);
        connectionState = ConnectionState.DISCONNECTING;
        mqtt_disconnect(native_ptr(), disconnectAck);
        return future;
    }

    public CompletableFuture<Integer> subscribe(String topic, QoS qos, Consumer<MqttMessage> handler) throws MqttException {
        if (native_ptr() == 0) {
            throw new MqttException("Invalid connection during subscribe");
        }

        CompletableFuture<Integer> future = new CompletableFuture<>();
        AsyncCallback subAck = wrapPacketFuture(future);
        try {
            int packetId = mqtt_subscribe(native_ptr(), topic, qos.getValue(), new MessageHandler(topic, handler), subAck);
            // When the future completes, complete the returned future with the packetId
            return future.thenApply(unused -> packetId);
        }
        catch (CrtRuntimeException ex) {
            throw new MqttException("AWS CRT exception: " + ex.toString());
        }
    }

    public CompletableFuture<Integer> unsubscribe(String topic) throws MqttException {
        if (native_ptr() == 0) {
            throw new MqttException("Invalid connection during unsubscribe");
        }

        CompletableFuture<Integer> future = new CompletableFuture<>();
        AsyncCallback unsubAck = wrapPacketFuture(future);
        int packetId = mqtt_unsubscribe(native_ptr(), topic, unsubAck);
        // When the future completes, complete the returned future with the packetId
        return future.thenApply(unused -> packetId);
    }

    public CompletableFuture<Integer> publish(MqttMessage message, QoS qos, boolean retain) throws MqttException {
        if (native_ptr() == 0) {
            throw new MqttException("Invalid connection during publish");
        }

        CompletableFuture<Integer> future = new CompletableFuture<>();
        AsyncCallback pubAck = wrapPacketFuture(future);
        try {
            int packetId = mqtt_publish(native_ptr(), message.getTopic(), qos.getValue(), retain, message.getPayloadDirect(), pubAck);
            // When the future completes, complete the returned future with the packetId
            return future.thenApply(unused -> packetId);
        }
        catch (CrtRuntimeException ex) {
            throw new MqttException("AWS CRT exception: " + ex.toString());
        }
    }

    public boolean setWill(MqttMessage message, QoS qos, boolean retain) throws MqttException {
        if (native_ptr() == 0) {
            throw new MqttException("Invalid connection during setWill");
        }

        try {
            return mqtt_set_will(native_ptr(), message.getTopic(), qos.getValue(), retain, message.getPayloadDirect());
        }
        catch (CrtRuntimeException ex) {
            throw new MqttException("AWS CRT exception: " + ex.toString());
        }
    }

    public void ping() throws MqttException {
        if (native_ptr() == 0) {
            throw new MqttException("Invalid connection during ping");
        }
        try {
            mqtt_ping(native_ptr());
        } catch (CrtRuntimeException ex) {
            throw new MqttException("Failed to send ping: " + ex.getMessage());
        }
    }

    /*******************************************************************************
     * Overrideable callbacks
     ******************************************************************************/
    public void onOnline() {}
    public boolean onOffline(boolean recoverable, String reason) { return false; }

    /*******************************************************************************
     * Native methods
     ******************************************************************************/
    private static native long mqtt_new(long client, String endpoint, short port, ClientCallbacks clientCallbacks, long socketOptions, long tlsCtx) throws CrtRuntimeException;

    private static native void mqtt_clean_up(long connection);

    private static native void mqtt_connect(long connection, String clientId, boolean cleanSession, short keepAliveMs, AsyncCallback ack) throws CrtRuntimeException;

    private static native void mqtt_disconnect(long connection, AsyncCallback ack);

    private static native short mqtt_subscribe(long connection, String topic, int qos, MessageHandler handler, AsyncCallback ack) throws CrtRuntimeException;

    private static native short mqtt_unsubscribe(long connection, String topic, AsyncCallback ack);

    private static native short mqtt_publish(long connection, String topic, int qos, boolean retain, ByteBuffer payload, AsyncCallback ack) throws CrtRuntimeException;

    private static native boolean mqtt_set_will(long connection, String topic, int qos, boolean retain, ByteBuffer payload) throws CrtRuntimeException;
    
    private static native void mqtt_set_login(long connection, String username, String password) throws CrtRuntimeException;

    private static native void mqtt_ping(long connection) throws CrtRuntimeException;
};
