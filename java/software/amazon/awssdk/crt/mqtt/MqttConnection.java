
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

import software.amazon.awssdk.crt.EventLoopGroup;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.mqtt.MqttException;

import java.util.function.Consumer;

/**
 * This class wraps aws-c-mqtt to provide the basic MQTT pub/sub
 * functionalities via the AWS Common Runtime
 * 
 * MqttConnection represents a single connection from one MqttClient to an MQTT
 * service endpoint
 */
public class MqttConnection extends CrtResource implements AutoCloseable {
    private MqttClient client;
    private ConnectOptions options;
    private ConnectionState connectionState = ConnectionState.Disconnected;

    public enum ConnectionState {
        Disconnected, Connecting, Connected, Disconnecting,
    };

    public enum QOS {
        AT_MOST_ONCE(0), AT_LEAST_ONCE(1), EXACTLY_ONCE(2);
        /* reserved = 3 */

        private int qos;

        QOS(int value) {
            qos = value;
        }

        public int getValue() {
            return qos;
        }
    }
    
    class MessageHandler {
        String topic;
        Consumer<MqttMessage> callback;

        public MessageHandler(String _topic, Consumer<MqttMessage> _callback) {
            callback = _callback;
            topic = _topic;
        }

        void deliver(String payload) {
            callback.accept(new MqttMessage(topic, payload));
        }
    }

    public static class ConnectOptions {
        public String endpointUri = ""; /* API endpoint host name */
        public String keyStorePath = "";
        public String certificateFile = ""; /* X.509 based certificate file */
        public String privateKeyFile = ""; /* PKCS#1 or PKCS#8 PEM encoded private key file */
        public boolean useWebSockets = false;
        public String alpn = "";
        public String clientId = "";
        public boolean cleanSession = true;
        public short keepAliveMs = 0;
        public short timeout = 1000;

        public ConnectOptions() {
        }
    }

    /* used to receive the result of an async operation from CRT mqtt */
    interface AsyncCallback {
        public void onSuccess();
        public void onFailure(String reason);
    }
    
    /* used to receive connection events */
    interface ClientCallbacks {
        public void onConnected();
        public void onDisconnected(String reason);
    }

    public MqttConnection(MqttClient _client, ConnectOptions _options) {
        client = _client;
        options = _options;
    }

    @Override
    public void close() {
        disconnect();
    }

    public ConnectionState getState() {
        return connectionState;
    }

    public void updateOptions(ConnectOptions _options) {
        options = _options;
    }

    private static AsyncCallback wrapAck(MqttActionListener ack) {
        AsyncCallback callback = (ack != null) ? new AsyncCallback() {
            @Override
            public void onSuccess() {
                ack.onSuccess();
            }

            @Override
            public void onFailure(String reason) {
                Throwable cause = new MqttException(reason);
                ack.onFailure(cause);
            }
        } : null;
        return callback;
    }

    public boolean connect() {
        return connect(null);
    }

    public boolean connect(MqttActionListener ack) {
        ClientCallbacks clientCallbacks = new ClientCallbacks() {
            @Override
            public void onConnected() {
                connectionState = ConnectionState.Connected;
                onOnline();
            }
            @Override
            public void onDisconnected(String reason) {
                connectionState = ConnectionState.Disconnected;
                onOffline();
            }
        };
        AsyncCallback connectAck = wrapAck(ack);
        try {
            connectionState = ConnectionState.Connecting;
            acquire(mqtt_connect(client.native_ptr(), options, clientCallbacks, connectAck));
        }
        catch (CrtRuntimeException ex) {
            return false;
        }
        return true;
    }

    public void disconnect() {
        disconnect(null);
    }

    public void disconnect(MqttActionListener ack) {
        if (native_ptr() == 0) {
            return;
        }
        AsyncCallback disconnectAck = wrapAck(ack);
        connectionState = ConnectionState.Disconnecting;
        mqtt_disconnect(release(), disconnectAck);
    }

    public short subscribe(String topic, QOS qos, Consumer<MqttMessage> handler) throws MqttException {
        return subscribe(topic, qos, handler, null);
    }

    public short subscribe(String topic, QOS qos, Consumer<MqttMessage> handler, MqttActionListener ack) throws MqttException {
        if (native_ptr() == 0) {
            throw new MqttException("Invalid connection during subscribe");
        }

        AsyncCallback subAck = wrapAck(ack);
        try {
            return mqtt_subscribe(native_ptr(), topic, qos.getValue(), new MessageHandler(topic, handler), subAck);
        }
        catch (CrtRuntimeException ex) {
            throw new MqttException("AWS CRT exception: " + ex.toString());
        }
    }

    public short unsubscribe(String topic) throws MqttException {
        return unsubscribe(topic, null);
    }

    public short unsubscribe(String topic, MqttActionListener ack) throws MqttException {
        if (native_ptr() == 0) {
            throw new MqttException("Invalid connection during unsubscribe");
        }

        AsyncCallback unsubAck = wrapAck(ack);
        return mqtt_unsubscribe(native_ptr(), topic, unsubAck);
    }

    public short publish(MqttMessage message, QOS qos) throws MqttException {
        return publish(message, qos, null);
    }

    public short publish(MqttMessage message, QOS qos, MqttActionListener ack) throws MqttException {
        if (native_ptr() == 0) {
            throw new MqttException("Invalid connection during publish");
        }
        
        AsyncCallback pubAck = wrapAck(ack);
        try {
            return mqtt_publish(native_ptr(), message.getTopic(), qos.getValue(), message.getPayload(), pubAck);
        }
        catch (CrtRuntimeException ex) {
            throw new MqttException("AWS CRT exception: " + ex.toString());
        }
    }

    /*******************************************************************************
     * Overrideable callbacks
     ******************************************************************************/
    public void onOnline() {}
    public void onOffline() {}

    /*******************************************************************************
     * Native methods
     ******************************************************************************/
    private static native long mqtt_connect(long client, ConnectOptions options, ClientCallbacks clientCallbacks, AsyncCallback ack) throws CrtRuntimeException;

    private static native void mqtt_disconnect(long connection, AsyncCallback ack);

    private static native short mqtt_subscribe(long connection, String topic, int qos, MessageHandler handler, AsyncCallback ack) throws CrtRuntimeException;

    private static native short mqtt_unsubscribe(long connection, String topic, AsyncCallback ack);

    private static native short mqtt_publish(long connection, String topic, int qos, String payload, AsyncCallback ack) throws CrtRuntimeException;
};
