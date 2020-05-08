/*
 * Copyright 2010-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
public final class WebsocketHandshakeTransformArgs {
    private MqttClientConnection mqttConnection;
    private HttpRequest httpRequest;
    private CompletableFuture<HttpRequest> future;

    /**
     * @param mqttConnection mqtt client connection that is establishing a websocket connection
     * @param httpRequest http request that may be modified by the transform operation
     * @param future Future to complete when the transform is complete.
     */
    public WebsocketHandshakeTransformArgs(MqttClientConnection mqttConnection, HttpRequest httpRequest,
            CompletableFuture<HttpRequest> future) {

        this.mqttConnection = mqttConnection;
        this.httpRequest = httpRequest;
        this.future = future;
    }

    /**
     * Queries the mqtt client connection.
     *
     * @return the mqtt client connection.
     */
    public MqttClientConnection getMqttClientConnection() {
        return mqttConnection;
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
