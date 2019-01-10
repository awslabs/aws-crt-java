
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
 * functionalities via the AWS Common Runtime
 * 
 * MqttConnection represents a single connection from one MqttClient to an MQTT
 * service endpoint
 */
public class MqttConnection extends CrtResource implements Closeable, MqttConnectionEvents {
    private final MqttClient client;
    private volatile ConnectionState connectionState = ConnectionState.DISCONNECTED;
    private MqttConnectionEvents userConnectionCallbacks;
    private AsyncCallback connectAck;
    private AsyncCallback disconnectAck;

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

        /* called from native when a message is delivered */
        void deliver(byte[] payload) {
            callback.accept(new MqttMessage(topic, ByteBuffer.wrap(payload)));
        }
    }

    /* used to receive the result of an async operation from CRT mqtt */
    interface AsyncCallback {
        public void onSuccess();
        public void onFailure(Throwable reason);
    }

    public MqttConnection(MqttClient mqttClient) throws MqttException {
        this(mqttClient, null);
    }

    public MqttConnection(MqttClient mqttClient, MqttConnectionEvents callbacks) throws MqttException {
        client = mqttClient;
        userConnectionCallbacks = callbacks;
        
        try {
            acquire(mqttConnectionNew(client.native_ptr(), this));
        } catch (CrtRuntimeException ex) {
            throw new MqttException("Exception during mqttConnectionNew: " + ex.getMessage());
        }
    }

    @Override
    public void close() {
        disconnect();
        mqttConnectionDestroy(release());
    }

    public ConnectionState getState() {
        return connectionState;
    }

    public void setLogin(String user, String pass) throws MqttException {
        try {
            mqttConnectionSetLogin(native_ptr(), user, pass);
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
            public void onFailure(Throwable reason) {
                future.completeExceptionally(reason);
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
            public void onFailure(Throwable reason) {
                future.completeExceptionally(reason);
            }
        };
    }

    @Override
    public void onConnectionComplete(int errorCode, boolean sessionPresent) {
        if (errorCode == 0) {
            connectionState = ConnectionState.CONNECTED;
            if (connectAck != null) {
                connectAck.onSuccess();
                connectAck = null;
            }
        } else {
            connectionState = ConnectionState.DISCONNECTED;
            if (connectAck != null) {
                connectAck.onFailure(new MqttException(errorCode));
                connectAck = null;
            }
        }
        if (userConnectionCallbacks != null) {
            userConnectionCallbacks.onConnectionComplete(errorCode, sessionPresent);
        }
    }

    @Override
    public void onConnectionInterrupted(int errorCode) {
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

    @Override
    public void onConnectionResumed(boolean sessionPresent) {
        connectionState = ConnectionState.CONNECTED;
        if (userConnectionCallbacks != null) {
            userConnectionCallbacks.onConnectionResumed(sessionPresent);
        }
    }

    public CompletableFuture<Void> connect(String clientId, String endpointUri, int port) {
        return connect(clientId, endpointUri, port);
    }

    public CompletableFuture<Void> connect(
        String clientId, String endpointUri, int port, 
        SocketOptions socketOptions, TlsContext tls, boolean cleanSession, int keepAliveMs) 
        throws MqttException {
        // Just clamp the keepAlive, no point in throwing
        short keepAlive = (short) Math.max(0, Math.min(keepAliveMs, Short.MAX_VALUE));
        if (port > Short.MAX_VALUE || port <= 0) {
            throw new MqttException("Port must be betweeen 0 and " + Short.MAX_VALUE);
        }
        CompletableFuture<Void> future = new CompletableFuture<>();
        connectAck = wrapVoidFuture(future);
        try {
            connectionState = ConnectionState.CONNECTING;
            mqttConnectionConnect(
                native_ptr(), endpointUri, (short) port, 
                socketOptions != null ? socketOptions.native_ptr() : 0,
                tls != null ? tls.native_ptr() : 0, 
                clientId, cleanSession, keepAlive);

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
        disconnectAck = wrapVoidFuture(future);
        connectionState = ConnectionState.DISCONNECTING;
        mqttConnectionDisconnect(native_ptr());
        return future;
    }

    public CompletableFuture<Integer> subscribe(String topic, QualityOfService qos, Consumer<MqttMessage> handler) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        if (native_ptr() == 0) {
            future.completeExceptionally(new MqttException("Invalid connection during subscribe"));
            return future;
        }

        AsyncCallback subAck = wrapPacketFuture(future);
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

    public CompletableFuture<Integer> unsubscribe(String topic) throws MqttException {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        if (native_ptr() == 0) {
            future.completeExceptionally(new MqttException("Invalid connection during unsubscribe"));
            return future;
        }

        AsyncCallback unsubAck = wrapPacketFuture(future);
        int packetId = mqttConnectionUnsubscribe(native_ptr(), topic, unsubAck);
        // When the future completes, complete the returned future with the packetId
        return future.thenApply(unused -> packetId);
    }

    public CompletableFuture<Integer> publish(MqttMessage message, QualityOfService qos, boolean retain) throws MqttException {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        if (native_ptr() == 0) {
            future.completeExceptionally(new MqttException("Invalid connection during publish"));
        }

        AsyncCallback pubAck = wrapPacketFuture(future);
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
    private static native long mqttConnectionNew(long client, MqttConnectionEvents clientCallbacks) throws CrtRuntimeException;

    private static native void mqttConnectionDestroy(long connection);

    private static native void mqttConnectionConnect(
        long connection, String endpoint, short port, 
        long socketOptions, long tlsContext, String clientId, 
        boolean cleanSession, short keepAliveMs) throws CrtRuntimeException;

    private static native void mqttConnectionDisconnect(long connection);

    private static native short mqttConnectionSubscribe(long connection, String topic, int qos, MessageHandler handler, AsyncCallback ack) throws CrtRuntimeException;

    private static native short mqttConnectionUnsubscribe(long connection, String topic, AsyncCallback ack);

    private static native short mqttConnectionPublish(long connection, String topic, int qos, boolean retain, ByteBuffer payload, AsyncCallback ack) throws CrtRuntimeException;

    private static native boolean mqttConnectionSetWill(long connection, String topic, int qos, boolean retain, ByteBuffer payload) throws CrtRuntimeException;
    
    private static native void mqttConnectionSetLogin(long connection, String username, String password) throws CrtRuntimeException;

    private static native void mqttConnectionPing(long connection) throws CrtRuntimeException;
};
