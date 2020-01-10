
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

    private TlsContext tlsContext;

    /**
     * Creates an MqttClient with no TLS from the provided {@link ClientBootstrap}
     * @param clientBootstrap The ClientBootstrap to use
     * @throws CrtRuntimeException If the system is unable to allocate space for a native MQTT client structure
     */
    public MqttClient(ClientBootstrap clientBootstrap) throws CrtRuntimeException {
        this(clientBootstrap, null);
    }

    /**
     * Creates an MqttClient from the provided {@link ClientBootstrap} and {@link TlsContext}
     * @param clientBootstrap The ClientBootstrap to use
     * @param tlsContext the tls context to use
     * @throws CrtRuntimeException If the system is unable to allocate space for a native MQTT client structure
     */
    public MqttClient(ClientBootstrap clientBootstrap, TlsContext context) throws CrtRuntimeException {
        acquireNativeHandle(mqttClientNew(clientBootstrap, context), (x)->mqttClientDestroy(x));
        this.tlsContext = context;
    }

    /**
     * Gets the tls context used by all connections associated with this client.
     */
    public TlsContext getTlsContext() { return tlsContext; }
    
    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long mqttClientNew(ClientBootstrap bootstrap, TlsContext context) throws CrtRuntimeException;

    private static native void mqttClientDestroy(long client);
}
