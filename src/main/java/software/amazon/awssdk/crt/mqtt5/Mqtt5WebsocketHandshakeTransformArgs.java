/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.mqtt5;

import java.util.concurrent.CompletableFuture;

import software.amazon.awssdk.crt.http.HttpRequest;

/**
 * Arguments to a websocket handshake transform operation.
 * The transform may modify the http request before it is sent to the server.
 * The transform MUST call complete() or completeExceptionally() when the transform is complete,
 * failure to do so will stall the mqtt connection indefinitely.
 * The transform operation may be asynchronous.
 *
 * The default websocket handshake http request uses path "/mqtt".
 * All required headers for a websocket handshake are present,
 * plus the optional header "Sec-WebSocket-Protocol: mqtt".
 */
public final class Mqtt5WebsocketHandshakeTransformArgs {
    private Mqtt5Client mqttClient;
    private HttpRequest httpRequest;
    private CompletableFuture<HttpRequest> future;

    /**
     * @param mqttClient Mqtt5Client that is establishing a websocket connection
     * @param httpRequest http request that may be modified by the transform operation
     * @param future Future to complete when the transform is complete.
     */
    public Mqtt5WebsocketHandshakeTransformArgs(Mqtt5Client mqttClient, HttpRequest httpRequest,
            CompletableFuture<HttpRequest> future) {

        this.mqttClient = mqttClient;
        this.httpRequest = httpRequest;
        this.future = future;
    }

    /**
     * Queries the Mqtt5Client.
     *
     * @return the Mqtt5Client.
     */
    public Mqtt5Client getMqttClient() {
        return mqttClient;
    }

    /**
     * Get the http request that will be used to perform the websocket handshake.
     * The transform operation may modify this request.
     *
     * @return The http request that will be used to perform the websocket handshake
     */
    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    /**
     * Mark the transform operation as successfully completed.
     * The websocket connection will proceed, using the http request.
     * @param signedRequest completed request
     */
    public void complete(HttpRequest signedRequest) {
        future.complete(signedRequest);
    }

    /**
     * Mark the transform operation as unsuccessfully completed.
     * The websocket connection attempt will be canceled.
     *
     * @param ex the exception
     */
    public void completeExceptionally(Throwable ex) {
        future.completeExceptionally(ex);
    }
}
