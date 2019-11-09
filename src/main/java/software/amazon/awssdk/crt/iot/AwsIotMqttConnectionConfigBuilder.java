
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
package software.amazon.awssdk.crt.iot;

import software.amazon.awssdk.crt.utils.PackageInfo;

import java.io.UnsupportedEncodingException;

import software.amazon.awssdk.crt.io.ClientTlsContext;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContextOptions;
import software.amazon.awssdk.crt.mqtt.MqttConnectionConfig;

public final class AwsIotMqttConnectionConfigBuilder {
    private MqttConnectionConfig params = new MqttConnectionConfig();
    private TlsContextOptions tlsOptions;

    private AwsIotMqttConnectionConfigBuilder() {
        this.params.username = String.format("?SDK=JavaV2&Version=%s", new PackageInfo().toString());
    }

    /**
     * Create a new builder with mTLS file paths
     * 
     * @param certPath       - Path to certificate, in PEM format
     * @param privateKeyPath - Path to private key, in PEM format
     */
    public static AwsIotMqttConnectionConfigBuilder newMtlsBuilderFromPath(String certPath, String privateKeyPath) {
        AwsIotMqttConnectionConfigBuilder builder = new AwsIotMqttConnectionConfigBuilder();
        builder.params.port = 8883;
        builder.tlsOptions = TlsContextOptions.createWithMTLSFromPath(certPath, privateKeyPath);
        if (TlsContextOptions.isAlpnSupported()) {
            builder.tlsOptions.withAlpnList("x-amzn-mqtt-ca");
        }
        return builder;
    }

    /**
     * Create a new builder with mTLS cert pair in memory
     * 
     * @param certificate - Certificate, in PEM format
     * @param privateKey  - Private key, in PEM format
     */
    public static AwsIotMqttConnectionConfigBuilder newMtlsBuilder(String certificate, String privateKey) {
        AwsIotMqttConnectionConfigBuilder builder = new AwsIotMqttConnectionConfigBuilder();
        builder.params.port = 8883;
        builder.tlsOptions = TlsContextOptions.createWithMTLS(certificate, privateKey);
        if (TlsContextOptions.isAlpnSupported()) {
            builder.tlsOptions.withAlpnList("x-amzn-mqtt-ca");
        }
        return builder;
    }

    /**
     * Create a new builder with mTLS cert pair in memory
     * 
     * @param certificate - Certificate, in PEM format
     * @param privateKey  - Private key, in PEM format
     */
    public static AwsIotMqttConnectionConfigBuilder newMtlsBuilder(byte[] certificate, byte[] privateKey)
            throws UnsupportedEncodingException {
        return newMtlsBuilder(new String(certificate, "UTF8"), new String(privateKey, "UTF8"));
    }

    /**
     * Overrides the default system trust store.
     * 
     * @param caDirPath  - Only used on Unix-style systems where all trust anchors
     *                    are stored in a directory (e.g. /etc/ssl/certs).
     * @param caFilePath - Single file containing all trust CAs, in PEM format
     */
    AwsIotMqttConnectionConfigBuilder withCertificateAuthorityFromPath(String caDirPath, String caFilePath) {
        this.tlsOptions.overrideDefaultTrustStoreFromPath(caDirPath, caFilePath);
        return this;
    }

    /**
     * Overrides the default system trust store.
     * 
     * @param caRoot - Buffer containing all trust CAs, in PEM format
     */
    AwsIotMqttConnectionConfigBuilder withCertificateAuthority(String caRoot) {
        this.tlsOptions.overrideDefaultTrustStore(caRoot);
        return this;
    }

    /**
     * Configures the IoT endpoint for this connection
     * 
     * @param endpoint The IoT endpoint to connect to
     */
    AwsIotMqttConnectionConfigBuilder withEndpoint(String endpoint) {
        this.params.endpoint = endpoint;
        return this;
    }

    /**
     * The port to connect to on the IoT endpoint
     * 
     * @param port The port to connect to on the IoT endpoint. Usually 8883 for
     *             MQTT, or 443 for websockets
     */
    AwsIotMqttConnectionConfigBuilder withPort(short port) {
        this.params.port = port;
        return this;
    }

    /**
     * Configures the client_id to use to connect to the IoT Core service
     * 
     * @param client_id The client id for this connection. Needs to be unique across
     *                  all devices/clients.
     */
    AwsIotMqttConnectionConfigBuilder withClientId(String clientId) {
        this.params.clientId = clientId;
        return this;
    }

    /**
     * Determines whether or not the service should try to resume prior
     * subscriptions, if it has any
     * 
     * @param clean_session true if the session should drop prior subscriptions when
     *                      this client connects, false to resume the session
     */
    AwsIotMqttConnectionConfigBuilder withCleanSession(boolean cleanSession) {
        this.params.cleanSession = cleanSession;
        return this;
    }

    /**
     * Configures the connection to use MQTT over websockets. Forces the port to
     * 443.
     */
    AwsIotMqttConnectionConfigBuilder withWebsocket() {
        this.params.useWebsocket = true;
        this.tlsOptions.alpnList.clear();
        this.params.port = 443;
        return this;
    }

    /**
     * Configures MQTT keep-alive via PING messages. Note that this is not TCP
     * keepalive.
     * 
     * @param keep_alive How often in seconds to send an MQTT PING message to the
     *                   service to keep the connection alive
     */
    AwsIotMqttConnectionConfigBuilder withKeepAliveSeconds(int keepAliveSecs) {
        this.params.keepAliveSecs = keepAliveSecs;
        return this;
    }

    /**
     * Configures the TCP socket connect timeout (in milliseconds)
     * 
     * @param timeout_ms TCP socket timeout
     */
    AwsIotMqttConnectionConfigBuilder withTimeoutMs(int timeoutMs) {
        this.params.socketOptions.connectTimeoutMs = timeoutMs;
        return this;
    }

    /**
     * Configures the common settings for the socket to use when opening a
     * connection to the server
     * 
     * @param socketOptions The socket settings
     */
    AwsIotMqttConnectionConfigBuilder withSocketOptions(SocketOptions socketOptions) {
        this.params.socketOptions = socketOptions;
        return this;
    }

    /**
     * Returns the configured MqttConnectionConfig
     * 
     * @returns The configured MqttConnectionConfig
     */
    MqttConnectionConfig build() {
        if (tlsOptions != null) {
            params.tlsContext = new ClientTlsContext(tlsOptions);
        }
        
        return params;
    }
}

