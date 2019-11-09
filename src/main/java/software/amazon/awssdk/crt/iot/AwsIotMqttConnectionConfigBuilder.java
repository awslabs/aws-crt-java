
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

    public static AwsIotMqttConnectionConfigBuilder newMtlsBuilderFromPath(String certPath, String privateKeyPath) {
        AwsIotMqttConnectionConfigBuilder builder = new AwsIotMqttConnectionConfigBuilder();
        builder.params.port = 8883;
        builder.tlsOptions = TlsContextOptions.createWithMTLSFromPath(certPath, privateKeyPath);
        if (TlsContextOptions.isAlpnSupported()) {
            builder.tlsOptions.withAlpnList("x-amzn-mqtt-ca");
        }
        return builder;
    }

    public static AwsIotMqttConnectionConfigBuilder newMtlsBuilder(String certificate, String privateKey) {
        AwsIotMqttConnectionConfigBuilder builder = new AwsIotMqttConnectionConfigBuilder();
        builder.params.port = 8883;
        builder.tlsOptions = TlsContextOptions.createWithMTLS(certificate, privateKey);
        if (TlsContextOptions.isAlpnSupported()) {
            builder.tlsOptions.withAlpnList("x-amzn-mqtt-ca");
        }
        return builder;
    }

    public static AwsIotMqttConnectionConfigBuilder newMtlsBuilder(byte[] certificate, byte[] privateKey)
            throws UnsupportedEncodingException {
        return newMtlsBuilder(new String(certificate, "UTF8"), new String(privateKey, "UTF8"));
    }

    AwsIotMqttConnectionConfigBuilder withCertificateAuthorityFromPath(String caDirPath, String caFilePath) {
        this.tlsOptions.overrideDefaultTrustStoreFromPath(caDirPath, caFilePath);
        return this;
    }

    AwsIotMqttConnectionConfigBuilder withCertificateAuthority(String caRoot) {
        this.tlsOptions.overrideDefaultTrustStore(caRoot);
        return this;
    }

    AwsIotMqttConnectionConfigBuilder withEndpoint(String endpoint) {
        this.params.endpoint = endpoint;
        return this;
    }

    AwsIotMqttConnectionConfigBuilder withPort(short port) {
        this.params.port = port;
        return this;
    }

    AwsIotMqttConnectionConfigBuilder withClientId(String clientId) {
        this.params.clientId = clientId;
        return this;
    }

    AwsIotMqttConnectionConfigBuilder withCleanSession(boolean cleanSession) {
        this.params.cleanSession = cleanSession;
        return this;
    }

    AwsIotMqttConnectionConfigBuilder withWebsocket() {
        this.params.useWebsocket = true;
        this.tlsOptions.setAlpnList("");
        this.params.port = 443;
        return this;
    }

    AwsIotMqttConnectionConfigBuilder withKeepAliveSeconds(int keepAliveSecs) {
        this.params.keepAliveSecs = keepAliveSecs;
        return this;
    }

    AwsIotMqttConnectionConfigBuilder withTimeoutMs(int timeoutMs) {
        this.params.socketOptions.connectTimeoutMs = timeoutMs;
        return this;
    }

    AwsIotMqttConnectionConfigBuilder withSocketOptions(SocketOptions socketOptions) {
        this.params.socketOptions = socketOptions;
        return this;
    }

    MqttConnectionConfig build() {
        if (tlsOptions != null) {
            params.tlsContext = new ClientTlsContext(tlsOptions);
        }
        
        return params;
    }
}

