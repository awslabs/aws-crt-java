
/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
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
        acquireNativeHandle(mqttClientNew(clientBootstrap.getNativeHandle()));
        addReferenceTo(clientBootstrap);
    }

    /**
     * Creates an MqttClient with no TLS from the default static {@link ClientBootstrap}
     * 
     * Note: If you are calling this manually, you will need to release the static ClientBootstrap using
     * "ClientBootstrap.releaseStaticDefault()" when you are done using the MQTT client to free the static
     * Client Bootstrap from memory.
     * 
     * @throws CrtRuntimeException If the system is unable to allocate space for a native MQTT client structure
     */
    public MqttClient() throws CrtRuntimeException {
        ClientBootstrap defaultBootstrap = ClientBootstrap.getOrCreateStaticDefault();
        acquireNativeHandle(mqttClientNew(defaultBootstrap.getNativeHandle()));
        addReferenceTo(defaultBootstrap);
    }

    /**
     * Creates an MqttClient from the provided {@link ClientBootstrap} and {@link TlsContext}
     * @param clientBootstrap The ClientBootstrap to use
     * @param context the tls context to use
     * @throws CrtRuntimeException If the system is unable to allocate space for a native MQTT client structure
     */
    public MqttClient(ClientBootstrap clientBootstrap, TlsContext context) throws CrtRuntimeException {
        acquireNativeHandle(mqttClientNew(clientBootstrap.getNativeHandle()));
        addReferenceTo(clientBootstrap);
        addReferenceTo(context);
        this.tlsContext = context;
    }

    /**
     * Creates an MqttClient with a default static {@link ClientBootstrap} and provided {@link TlsContext}
     * 
     * Note: If you are calling this manually, you will need to release the static ClientBootstrap using
     * "ClientBootstrap.releaseStaticDefault()" when you are done using the MQTT client to free the static
     * Client Bootstrap from memory.
     * 
     * @param context the tls context to use
     * @throws CrtRuntimeException If the system is unable to allocate space for a native MQTT client structure
     */
    public MqttClient(TlsContext context) throws CrtRuntimeException {
        ClientBootstrap defaultBootstrap = ClientBootstrap.getOrCreateStaticDefault();
        acquireNativeHandle(mqttClientNew(defaultBootstrap.getNativeHandle()));
        addReferenceTo(defaultBootstrap);
        addReferenceTo(context);
        this.tlsContext = context;
    }

    /**
     * @return the tls context used by all connections associated with this client.
     */
    public TlsContext getTlsContext() { return tlsContext; }

    /**
     * Cleans up the native resources associated with this client. The client is unusable after this call
     */
    @Override
    protected void releaseNativeHandle() {
        if (!isNull()) {
            mqttClientDestroy(getNativeHandle());
        }
    }

    /**
     * Determines whether a resource releases its dependencies at the same time the native handle is released or if it waits.
     * Resources that wait are responsible for calling releaseReferences() manually.
     */
    @Override
    protected boolean canReleaseReferencesImmediately() { return true; }
    
    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long mqttClientNew(long bootstrap) throws CrtRuntimeException;

    private static native void mqttClientDestroy(long client);
}
