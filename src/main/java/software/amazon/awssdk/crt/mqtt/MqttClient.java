
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

import software.amazon.awssdk.crt.ClientBootstrap;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.EventLoopGroup;
import software.amazon.awssdk.crt.TlsContext;
import software.amazon.awssdk.crt.mqtt.MqttConnection;

import java.io.Closeable;

/**
 * This class wraps aws-c-mqtt to provide the basic MQTT pub/sub functionalities
 * via the AWS Common Runtime
 * 
 * One MqttClient class is needed per application. It can create any number of connections to
 * any number of MQTT endpoints
 */
public class MqttClient extends CrtResource implements Closeable {
    private final ClientBootstrap bootstrap;
    private final TlsContext tlsContext;

    public MqttClient() throws CrtRuntimeException {
        this(new ClientBootstrap(new EventLoopGroup(1)), null);
    }

    public MqttClient(ClientBootstrap clientBootstrap) throws CrtRuntimeException {
        this(clientBootstrap, null);
    }

    public MqttClient(ClientBootstrap clientBootstrap, TlsContext tlsContext) throws CrtRuntimeException {
        this.bootstrap = clientBootstrap;
        this.tlsContext = tlsContext;
        acquire(mqtt_client_init(bootstrap.native_ptr()));
    }

    @Override
    public void close() {
        if (native_ptr() != 0) {
            mqtt_client_clean_up(release());
        }
    }

    public TlsContext tlsContext() {
        return this.tlsContext;
    }
    
    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long mqtt_client_init(long bootstrap) throws CrtRuntimeException;

    private static native void mqtt_client_clean_up(long client);
}
