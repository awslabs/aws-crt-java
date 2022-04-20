
/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.mqtt;

import software.amazon.awssdk.crt.CleanableCrtResource;
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
public class MqttClient extends CleanableCrtResource {

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
     * Creates an MqttClient with no TLS from the default static {@link ClientBootstrap}
     * 
     * @throws CrtRuntimeException If the system is unable to allocate space for a native MQTT client structure
     */
    public MqttClient() throws CrtRuntimeException {
        this(ClientBootstrap.getOrCreateStaticDefault(), null);
    }

    /**
     * Creates an MqttClient from the provided {@link ClientBootstrap} and {@link TlsContext}
     * @param clientBootstrap The ClientBootstrap to use
     * @param context the tls context to use
     * @throws CrtRuntimeException If the system is unable to allocate space for a native MQTT client structure
     */
    public MqttClient(ClientBootstrap clientBootstrap, TlsContext context) throws CrtRuntimeException {
        acquireNativeHandle(mqttClientNew(clientBootstrap.getNativeHandle()), MqttClient::mqttClientDestroy);
        this.tlsContext = context;
    }

    /**
     * Creates an MqttClient with a default static {@link ClientBootstrap} and provided {@link TlsContext}
     * 
     * @param context the tls context to use
     * @throws CrtRuntimeException If the system is unable to allocate space for a native MQTT client structure
     */
    public MqttClient(TlsContext context) throws CrtRuntimeException {
        this(ClientBootstrap.getOrCreateStaticDefault(), context);
    }

    /**
     * @return the tls context used by all connections associated with this client.
     */
    public TlsContext getTlsContext() { return tlsContext; }
    
    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long mqttClientNew(long bootstrap) throws CrtRuntimeException;

    private static native void mqttClientDestroy(long client);
}
