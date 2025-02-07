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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A helper class for AWS service clients that use MQTT as the transport protocol.
 *
 * The class supports orchestrating request-response operations and creating streaming operations.  Used by the
 * IoT SDKs to implement higher-level service clients that provide a good user experience.
 *
 * Not intended to be constructed or used directly; the service client will create one during its construction.
 */
public class MqttRequestResponseClient extends CrtResource {

    /*
     * Using a read-write lock to protect the native handle on Java -> Native calls is a new approach to handle
     * accidental misuse of CrtResource objects.  The current method (no protection) works as long as the user
     * follows the rules, but race conditions can lead to crashes or undefined behavior if the user breaks the
     * rules (uses the CrtResource after or while the final close() call is in progress).
     *
     * For this new method to be correct, it must not be possible that Java -> Native calls ever call back
     * native -> Java in the same call stack.  This is true for both the request response client and streaming
     * operations, allowing us to add this layer of safety.
     */
    private final ReentrantReadWriteLock handleLock = new ReentrantReadWriteLock();
    private final Lock handleReadLock = handleLock.readLock();
    private final Lock handleWriteLock = handleLock.writeLock();

    /**
     * MQTT5-based constructor for request-response service clients
     *
     * @param client MQTT5 client that the request-response client should use as transport
     * @param options request-response client configuration options
     */
    public MqttRequestResponseClient(Mqtt5Client client, MqttRequestResponseClientOptions options) {
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
    public MqttRequestResponseClient(MqttClientConnection client, MqttRequestResponseClientOptions options) {
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

        this.handleReadLock.lock();
        try {
            long handle = getNativeHandle();
            if (handle != 0) {
                mqttRequestResponseClientSubmitRequest(getNativeHandle(), request, future);
            } else {
                future.completeExceptionally(new CrtRuntimeException("Client already closed"));
            }
        } finally {
            this.handleReadLock.unlock();
        }

        return future;
    }

    /**
     * Creates a new streaming operation from a set of configuration options.  A streaming operation provides a
     * mechanism for listening to a specific event stream from an AWS MQTT-based service.
     *
     * @param options configuration options for the streaming operation
     *
     * @return a new streaming operation instance
     */
    public StreamingOperation createStream(StreamingOperationOptions options) {
        this.handleReadLock.lock();
        try {
            long handle = getNativeHandle();
            if (handle != 0) {
                return new StreamingOperation(this, options);
            } else {
                throw new CrtRuntimeException("Client already closed");
            }
        } finally {
            this.handleReadLock.unlock();
        }
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

    @Override
    public void close() {
        this.handleWriteLock.lock();
        try {
            super.close();
        } finally {
            this.handleWriteLock.unlock();
        }
    }

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
