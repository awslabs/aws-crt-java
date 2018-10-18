
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

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.EventLoopGroup;
import software.amazon.awssdk.crt.CrtRuntimeException;

public final class MqttConnection implements AutoCloseable {
    private EventLoopGroup elg;
    private ConnectOptions options;
    private long connection;

    public static class ConnectOptions {
        public String endpointUri = ""; // API endpoint host name
        public String keyStorePath = "";
        public String certificateFile = ""; // X.509 based certificate file
        public String privateKeyFile = ""; // PKCS#1 or PKCS#8 PEM encoded private key file
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
    
    interface ClientCallbacks {
        public void onConnected();
        public void onDisconnected(String reason);
    }

    static {
        // This will cause the JNI lib to be loaded the first time a CRT is created
        new CRT();
    }

    MqttConnection(EventLoopGroup _elg, ConnectOptions _options) {
        elg = _elg;
        options = _options;
        connection = 0;
    }

    @Override
    public void close() {
        disconnect();
    }

    public long native_ptr() {
        return connection;
    }

    public void updateOptions(ConnectOptions _options) {
        options = _options;
    }

    public boolean connect(MqttConnectionListener _listener) {
        MqttToken connectToken = new MqttToken(options.clientId);
        ClientCallbacks clientCallbacks = new ClientCallbacks() {
            @Override
            public void onConnected() {

            }
            @Override
            public void onDisconnected(String reason) {

            }
        };
        AsyncCallback connectCallback = new AsyncCallback() {
            @Override
            public void onSuccess() {
                _listener.onSuccess(connectToken);
            }
            @Override
            public void onFailure(String reason) {
                Throwable cause = new Exception(reason);
                _listener.onFailure(connectToken, cause);
            }
        };
        try {
            connection = mqtt_connect(elg.native_ptr(), options, clientCallbacks, connectCallback);
        }
        catch (CrtRuntimeException ex) {
            return false;
        }
        return true;
    }

    public void disconnect() {
        if (connection != 0) {
            mqtt_disconnect(connection);
            connection = 0;
        }
    }

    public void subscribe() {

    }

    public void unsubscribe() {

    }

    public void publish() {

    }

    private static native long mqtt_connect(long elg, ConnectOptions options, ClientCallbacks clientCallbacks, AsyncCallback connectCallback) throws CrtRuntimeException;

    private static native void mqtt_disconnect(long connection);

    private static native void mqtt_subscribe(long connection) throws CrtRuntimeException;

    private static native void mqtt_unsubscribe(long connection);

    private static native void mqtt_publish(long connection) throws CrtRuntimeException;
};
