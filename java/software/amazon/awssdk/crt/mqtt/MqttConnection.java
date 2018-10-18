
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
    private String clientId;
    private ConnectOptions options;

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

    static {
        // This will cause the JNI lib to be loaded the first time a CRT is created
        new CRT();
    }

    MqttConnection(EventLoopGroup _elg, ConnectOptions _options) {
        elg = _elg;
        options = _options;
    }

    @Override
    public void close() {
    }

    public void updateOptions(ConnectOptions _options) {
        options = _options;
    }

    public void connect(MqttConnectionListener _listener) {

    }

    public void disconnect() {

    }

    public void subscribe() {

    }

    public void unsubscribe() {

    }

    public void publish() {

    }

    private native void mqtt_connect(EventLoopGroup elg, ConnectOptions params, MqttConnectionListener callback) throws CrtRuntimeException;

    private native void mqtt_disconnect();

    private native void mqtt_subscribe() throws CrtRuntimeException;

    private native void mqtt_unsubscribe();

    private native void mqtt_publish() throws CrtRuntimeException;
};
