/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.iot;

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.mqtt.MqttClientConnection;
import software.amazon.awssdk.crt.mqtt5.Mqtt5Client;

import java.util.concurrent.CompletableFuture;

/**
 * A helper class for AWS service clients that use MQTT as the transport protocol.
 *
 * The class supports orchestrating request-response operations and creating streaming operations.  Used by the
 * IoT SDKs to implement higher-level service clients that provide a good user experience.
 *
 * Not intended to be constructed or used directly; the service client will create one during its construction.
 */
public class MqttRequestResponseClient extends CrtResource {

    /**
     * MQTT5-based constructor for request-response service clients
     *
     * @param client MQTT5 client that the request-response client should use as transport
     * @param options request-response client configuration options
     */
    public MqttRequestResponseClient(Mqtt5Client client, MqttRequestResponseClientBuilder.MqttRequestResponseClientOptions options) {
        acquireNativeHandle(mqttRequestResponseClientNewFrom5(
                this,
                client.getNativeHandle(),
                options.getMaxRequestResponseSubscriptions(),
                options.getMaxStreamingSubscriptions(),
                options.getOperationTimeoutSeconds()
        ));
    }

    /**
     * MQTT311-based constructor for request-response service clients
     *
     * @param client MQTT311 client that the request-response client should use as transport
     * @param options request-response client configuration options
     */
    public MqttRequestResponseClient(MqttClientConnection client, MqttRequestResponseClientBuilder.MqttRequestResponseClientOptions options) {
        acquireNativeHandle(mqttRequestResponseClientNewFrom311(
                this,
                client.getNativeHandle(),
                options.getMaxRequestResponseSubscriptions(),
                options.getMaxStreamingSubscriptions(),
                options.getOperationTimeoutSeconds()
        ));
    }

    /**
     * Submits a request to the request-response client.
     *
     * @param request description of the request to perform
     *                
     * @return future that completes with the result of performing the request
     */
    public CompletableFuture<MqttRequestResponse> submitRequest(RequestResponseOperation request) {
        CompletableFuture<MqttRequestResponse> future = new CompletableFuture<>();

        mqttRequestResponseClientSubmitRequest(getNativeHandle(), request, future);

        return future;
    }
    
    /**
     * Cleans up the native resources associated with this client. The client is unusable after this call
     */
    @Override
    protected void releaseNativeHandle() {
        if (!isNull()) {
            mqttRequestResponseClientDestroy(getNativeHandle());
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

    private static native long mqttRequestResponseClientNewFrom5(
            MqttRequestResponseClient client,
            long protocolClientHandle,
            int maxRequestResponseSubscriptions,
            int maxStreamingSubscriptions,
            int operationTimeoutSeconds
    ) throws CrtRuntimeException;

    private static native long mqttRequestResponseClientNewFrom311(
            MqttRequestResponseClient client,
            long protocolClientHandle,
            int maxRequestResponseSubscriptions,
            int maxStreamingSubscriptions,
            int operationTimeoutSeconds
    ) throws CrtRuntimeException;

    private static native void mqttRequestResponseClientDestroy(long client);

    private static native void mqttRequestResponseClientSubmitRequest(long client, RequestResponseOperation request, CompletableFuture<MqttRequestResponse> future);

}
