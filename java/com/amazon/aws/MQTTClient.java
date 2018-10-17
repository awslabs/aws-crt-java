
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
package com.amazon.aws;

import com.amazon.aws.CRT;
import com.amazon.aws.EventLoopGroup;
import com.amazon.aws.RuntimeException;

public final class MQTTClient implements AutoCloseable {
    private EventLoopGroup elg;
    private String clientId;

    public static class ConnectParams {
        public String caPath = "";
        public String keyPath = "";
        public String certificatePath = "";
        public boolean useWebSockets = false;
        public String alpn = "";
        public boolean cleanSession = true;
        public boolean keepAlive = false;

        public ConnectParams() {
        }
    }

    MQTTClient(EventLoopGroup _elg, String _clientId) {
        // This will cause the JNI lib to be loaded the first time a CRT is created
        new CRT();
        elg = _elg;
        clientId = _clientId;
    }

    @Override
    public void close() {
    }

    public void connect() {
        
    }

    private native void mqtt_connect(EventLoopGroup elg, String hostName, short port, ConnectParams params) throws RuntimeException;

    private native void mqtt_disconnect();

    private native void mqtt_subscribe() throws RuntimeException;

    private native void mqtt_unsubscribe();

    private native void mqtt_publish() throws RuntimeException;
};
