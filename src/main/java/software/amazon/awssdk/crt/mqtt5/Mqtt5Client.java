/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.mqtt5;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.http.HttpProxyOptions;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.mqtt5.packets.ConnectPacket;
import software.amazon.awssdk.crt.mqtt5.packets.DisconnectPacket;
import software.amazon.awssdk.crt.mqtt5.packets.PublishPacket;
import software.amazon.awssdk.crt.mqtt5.packets.SubAckPacket;
import software.amazon.awssdk.crt.mqtt5.packets.SubscribePacket;
import software.amazon.awssdk.crt.mqtt5.packets.UnsubAckPacket;
import software.amazon.awssdk.crt.mqtt5.packets.UnsubscribePacket;
import software.amazon.awssdk.crt.mqtt5.packets.ConnectPacket.ConnectPacketBuilder;


 /**
 * This class wraps the aws-c-mqtt MQTT5 client to provide the basic MQTT5 pub/sub functionalities
 * via the AWS Common Runtime
 *
 * One Mqtt5Client class creates one connection.
 *
 * MQTT5 support is currently in <b>developer preview</b>.  We encourage feedback at all times, but feedback during the
 * preview window is especially valuable in shaping the final product.  During the preview period we may make
 * backwards-incompatible changes to the public API, but in general, this is something we will try our best to avoid.
 */
public class Mqtt5Client extends CrtResource implements Serializable {

    /**
     * A private reference to the websocket handshake from the MQTT5 client options
     */
    private Consumer<Mqtt5WebsocketHandshakeTransformArgs> websocketHandshakeTransform;

    /**
     * A boolean that holds whether the client's current state is connected or not
     */
    private boolean isConnected;

    /**
     * Creates a Mqtt5Client instance using the provided Mqtt5ClientOptions. Once the Mqtt5Client is created,
     * changing the settings will not cause a change in already created Mqtt5Client's.
     *
     * @param options The Mqtt5Options class to use to configure the new Mqtt5Client.
     * @throws CrtRuntimeException If the system is unable to allocate space for a native MQTT5 client structure
     */
    public Mqtt5Client(Mqtt5ClientOptions options) throws CrtRuntimeException {
        ClientBootstrap bootstrap = options.getBootstrap();
        SocketOptions socketOptions = options.getSocketOptions();
        TlsContext tlsContext = options.getTlsContext();
        HttpProxyOptions proxyOptions = options.getHttpProxyOptions();
        ConnectPacket connectionOptions = options.getConnectOptions();
        this.websocketHandshakeTransform = options.getWebsocketHandshakeTransform();

        if (bootstrap == null) {
            bootstrap = ClientBootstrap.getOrCreateStaticDefault();
        }

        if (connectionOptions == null) {
            ConnectPacketBuilder connectBuilder = new ConnectPacketBuilder();
            connectionOptions = connectBuilder.build();
        }

        acquireNativeHandle(mqtt5ClientNew(
            options,
            connectionOptions,
            bootstrap,
            this
        ));

        if (bootstrap != null) {
            addReferenceTo(bootstrap);
        }
        if (socketOptions != null) {
            addReferenceTo(socketOptions);
        }
        if (tlsContext != null) {
            addReferenceTo(tlsContext);
        }
        if (proxyOptions != null) {
            if (proxyOptions.getTlsContext() != null) {
                addReferenceTo(proxyOptions.getTlsContext());
            }
        }
        isConnected = false;
    }

    /**
     * Cleans up the native resources associated with this client. The client is unusable after this call
     */
    @Override
    protected void releaseNativeHandle() {
        if (!isNull()) {
            mqtt5ClientDestroy(getNativeHandle());
        }
    }

    /**
     * Determines whether a resource releases its dependencies at the same time the native handle is released or if it waits.
     * Resources that wait are responsible for calling releaseReferences() manually.
     */
    @Override
    protected boolean canReleaseReferencesImmediately() { return false; }

    /**
     * Notifies the Mqtt5Client that you want it maintain connectivity to the configured endpoint.
     * The client will attempt to stay connected using the properties of the reconnect-related parameters
     * in the Mqtt5Client configuration.
     *
     * This is an asynchronous operation.
     *
     * @throws CrtRuntimeException If the native client returns an error when starting
     */
    public void start() throws CrtRuntimeException {
        mqtt5ClientInternalStart(getNativeHandle());
    }

    /**
     * Notifies the Mqtt5Client that you want it to end connectivity to the configured endpoint, disconnecting any
     * existing connection and halting any reconnect attempts.
     *
     * This is an asynchronous operation.
     *
     * @param disconnectPacket (optional) Properties of a DISCONNECT packet to send as part of the shutdown process
     * @throws CrtRuntimeException If the native client is unable to initialize the stop process.
     */
    public void stop(DisconnectPacket disconnectPacket) throws CrtRuntimeException {
        mqtt5ClientInternalStop(getNativeHandle(), disconnectPacket);
    }

    /**
     * Tells the Mqtt5Client to attempt to send a PUBLISH packet.
     *
     * Will return a future containing a PublishPacket if the publish is successful.
     * The data in the PublishPacket varies depending on the QoS of the Publish. For QoS 0, the PublishPacket
     * will not contain data. For QoS 1, the PublishPacket will contain a PubAckPacket.
     * See PublishPacket class documentation for more info.
     *
     * @param publishPacket PUBLISH packet to send to the server
     * @return A future that will be rejected with an error or resolved with a PublishResult response
     */
    public CompletableFuture<PublishResult> publish(PublishPacket publishPacket) {
        CompletableFuture<PublishResult> publishFuture = new CompletableFuture<>();
        mqtt5ClientInternalPublish(getNativeHandle(), publishPacket, publishFuture);
        return publishFuture;
    }

    /**
     * Tells the Mqtt5Client to attempt to subscribe to one or more topic filters.
     *
     * @param subscribePacket SUBSCRIBE packet to send to the server
     * @return a future that will be rejected with an error or resolved with the SUBACK response
     */
    public CompletableFuture<SubAckPacket> subscribe(SubscribePacket subscribePacket) {
        CompletableFuture<SubAckPacket> subscribeFuture = new CompletableFuture<>();
        mqtt5ClientInternalSubscribe(getNativeHandle(), subscribePacket, subscribeFuture);
        return subscribeFuture;
    }

    /**
     * Tells the Mqtt5Client to attempt to unsubscribe from one or more topic filters.
     *
     * @param unsubscribePacket UNSUBSCRIBE packet to send to the server
     * @return a future that will be rejected with an error or resolved with the UNSUBACK response
     */
    public CompletableFuture<UnsubAckPacket> unsubscribe(UnsubscribePacket unsubscribePacket) {
        CompletableFuture<UnsubAckPacket> unsubscribeFuture = new CompletableFuture<>();
        mqtt5ClientInternalUnsubscribe(getNativeHandle(), unsubscribePacket, unsubscribeFuture);
        return unsubscribeFuture;
    }

    /**
     * Returns statistics about the current state of the Mqtt5Client's queue of operations.
     * @return Current state of the client's queue of operations.
     */
    public Mqtt5ClientOperationStatistics getOperationStatistics() {
        return mqtt5ClientInternalGetOperationStatistics(getNativeHandle());
    }

    /**
     * Returns the connectivity state for the Mqtt5Client.
     * @return True if the client is connected, false otherwise
     */
    public synchronized boolean getIsConnected() {
        return isConnected;
    }

    /**
     * Sets the connectivity state of the Mqtt5Client. Is used by JNI.
     * @param connected The current connectivity state of the Mqtt5Client
     */
    private synchronized void setIsConnected(boolean connected) {
        isConnected = connected;
    }

    /*******************************************************************************
     * websocket methods
     ******************************************************************************/

    /**
     * Called from native when a websocket handshake request is being prepared.
     * @param handshakeRequest The HttpRequest being prepared
     * @param nativeUserData Native data
     */
    private void onWebsocketHandshake(HttpRequest handshakeRequest, long nativeUserData) {
        CompletableFuture<HttpRequest> future = new CompletableFuture<>();
        future.whenComplete((x, throwable) -> {
            mqtt5ClientInternalWebsocketHandshakeComplete(getNativeHandle(), x != null ? x.marshalForJni() : null,
                    throwable, nativeUserData);
        });

        Mqtt5WebsocketHandshakeTransformArgs args = new Mqtt5WebsocketHandshakeTransformArgs(this, handshakeRequest, future);

        Consumer<Mqtt5WebsocketHandshakeTransformArgs> transform = this.websocketHandshakeTransform;
        if (transform != null) {
            transform.accept(args);
        } else {
            args.complete(handshakeRequest);
        }
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long mqtt5ClientNew(
        Mqtt5ClientOptions options,
        ConnectPacket connect_options,
        ClientBootstrap bootstrap,
        Mqtt5Client client
    ) throws CrtRuntimeException;
    private static native void mqtt5ClientDestroy(long client);
    private static native void mqtt5ClientInternalStart(long client);
    private static native void mqtt5ClientInternalStop(long client, DisconnectPacket disconnect_options);
    private static native void mqtt5ClientInternalPublish(long client, PublishPacket publish_options, CompletableFuture<PublishResult> publish_result);
    private static native void mqtt5ClientInternalSubscribe(long client, SubscribePacket subscribe_options, CompletableFuture<SubAckPacket> subscribe_suback);
    private static native void mqtt5ClientInternalUnsubscribe(long client, UnsubscribePacket unsubscribe_options, CompletableFuture<UnsubAckPacket> unsubscribe_suback);
    private static native void mqtt5ClientInternalWebsocketHandshakeComplete(long connection, byte[] marshalledRequest, Throwable throwable, long nativeUserData) throws CrtRuntimeException;
    private static native Mqtt5ClientOperationStatistics mqtt5ClientInternalGetOperationStatistics(long client);
}
