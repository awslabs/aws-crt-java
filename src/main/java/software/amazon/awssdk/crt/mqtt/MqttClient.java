
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

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.TlsContext;

/**
 * This class wraps aws-c-mqtt to provide the basic MQTT pub/sub functionalities
 * via the AWS Common Runtime
 * 
 * One MqttClient class is needed per application. It can create any number of connections to
 * any number of MQTT endpoints
 */
public class MqttClient extends CrtResource {
    private final ClientBootstrap bootstrap;
    private final TlsContext tlsContext;

    /**
     * Creates a default MqttClient with no TLS and a {@link ClientBootstrap} constructed with default settings
     * @throws CrtRuntimeException @see software.amazon.awssdk.crt.io.ClientBootstrap#constructor(EventLoopGroup) @see software.amazon.awssdk.crt.io.EventLoopGroup#constructor(int)
     */
    public MqttClient() throws CrtRuntimeException {
        this(new ClientBootstrap(1), null);
        own(bootstrap);
    }

    /**
     * Creates an MqttClient from the provided {@link ClientBootstrap} with no TLS
     * @param clientBootstrap The ClientBootstrap to use
     * @throws CrtRuntimeException If the system is unable to allocate space for a native MQTT client structure
     */
    public MqttClient(ClientBootstrap clientBootstrap) throws CrtRuntimeException {
        this(clientBootstrap, null);
    }

    /**
     * Creates an MqttClient from the provided {@link ClientBootstrap} and {@link TlsContext}
     * @param clientBootstrap The ClientBootstrap to use
     * @param tlsContext The TlsContext to use
     * @throws CrtRuntimeException If the system is unable to allocate space for a native MQTT client structure
     */
    public MqttClient(ClientBootstrap clientBootstrap, TlsContext tlsContext) throws CrtRuntimeException {
        this.bootstrap = clientBootstrap;
        this.tlsContext = tlsContext;
        acquire(mqttClientNew(bootstrap.native_ptr()));
    }

    /**
     * Cleans up the native resources associated with this client. The client is unusable after this call
     */
    @Override
    public void close() {
        if (!isNull()) {
            mqttClientDestroy(release());
        }
        super.close();
    }

    /**
     * Get the {@link TlsContext} associated with this client
     * @return the TlsContext provided to this client at construction. May be null.
     */
    public TlsContext tlsContext() {
        return this.tlsContext;
    }
    
    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long mqttClientNew(long bootstrap) throws CrtRuntimeException;

    private static native void mqttClientDestroy(long client);
}
