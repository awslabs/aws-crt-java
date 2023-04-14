/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.test;

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.auth.signing.AwsSigner;
import software.amazon.awssdk.crt.auth.signing.AwsSigningConfig;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.mqtt.WebsocketHandshakeTransformArgs;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * A websocket handshake transformer that adds a sigv4 signature for the handshake to the request.
 * Required in order to connect to Aws IoT via websockets using sigv4 authentication.
 * This is just for making it easier to test Websocket connections with authentication.
 * See AwsSigv4HandshakeTransformer in the Java V2 SDK for more detail and comments.
 */
public class MqttClientConnectionSigv4HandshakeTransformer extends CrtResource implements Consumer<WebsocketHandshakeTransformArgs> {
    AwsSigningConfig signingConfig;

    public MqttClientConnectionSigv4HandshakeTransformer(AwsSigningConfig signingConfig) {
        addReferenceTo(signingConfig);
        this.signingConfig = signingConfig;
    }

    @Override
    protected void releaseNativeHandle() {}

    @Override
    protected boolean canReleaseReferencesImmediately() { return true; }

    public void accept(WebsocketHandshakeTransformArgs handshakeArgs) {
        if (signingConfig.getCredentialsProvider() == null)
        {
            handshakeArgs.complete(handshakeArgs.getHttpRequest());
        }
        else
        {
            try (AwsSigningConfig config = signingConfig.clone()) {
                config.setTime(System.currentTimeMillis());

                CompletableFuture<HttpRequest> signingFuture = AwsSigner.signRequest(handshakeArgs.getHttpRequest(), config);
                signingFuture.whenComplete((HttpRequest request, Throwable error) -> {
                    if (error != null) {
                        handshakeArgs.completeExceptionally(error);
                    } else {
                        handshakeArgs.complete(request);
                    }});
            }
        }
    }
}
