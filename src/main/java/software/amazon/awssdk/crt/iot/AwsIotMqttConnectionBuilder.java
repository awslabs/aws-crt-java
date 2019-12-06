
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

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.io.ClientTlsContext;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContextOptions;
import software.amazon.awssdk.crt.mqtt.MqttConnectionConfig;

/*
 * A central class for building Mqtt connections without manually managing a large variety of native objects (some
 * still need to be created though).
 */
public final class AwsIotMqttConnectionBuilder extends CrtResource {
    public String clientId;
    public String endpoint;
    public int port;
    public boolean useWebsocket = false;
    public boolean cleanSession = true;
    public int keepAliveSecs = 0;
    public SocketOptions socketOptions;  // CrtResource
    public MqttMessage will = null;
    public String username;
    public String password;
    public ClientTlsContext tlsContext = null;  // CrtResource
    public HttpProxyOptions proxyOptions = null;

    private TlsContextOptions tlsOptions;  // CrtResource

    private TlsContext tlsContext; // CrtResource
    private MqttClient client; // CrtResource
    private ClientBootstrap bootstrap; // CrtResource

    private AwsIotMqttConnectionBuilder() {
        socketOptions = new SocketOptions();
        addReferenceTo(socketOptions);
    }

    /**
     * Required override method that must begin the release process of the acquired native handle
     */
    protected abstract void releaseNativeHandle() {}

    /**
     * Override that determines whether a resource releases its dependencies at the same time the native handle is released or if it waits.
     * Resources with asynchronous shutdown processes should override this with false, and establish a callback from native code that
     * invokes releaseReferences() when the asynchronous shutdown process has completed.  See HttpClientConnectionManager for an example.
     */
    protected boolean canReleaseReferencesImmediately() { return true; }


    /**
     * This class just tracks native resources: mqtt client, tls context, tls context options, client bootstrap
     */
    protected boolean isNativeResource() { return false; }


    private void swapReferenceTo(CrtResource oldReference, CrtResource newReference) {
        if (oldReference != newReference) {
            addReferenceTo(newReference);
            removeReferenceTo(oldReference);
        }
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
        builder.tlsOptions = TlsContextOptions.createWithMtlsFromPath(certPath, privateKeyPath);
        if (TlsContextOptions.isAlpnSupported()) {
            builder.tlsOptions.withAlpnList("x-amzn-mqtt-ca");
            builder.params.port = 443;
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
        builder.tlsOptions = TlsContextOptions.createWithMtls(certificate, privateKey);
        if (TlsContextOptions.isAlpnSupported()) {
            builder.tlsOptions.withAlpnList("x-amzn-mqtt-ca");
            builder.params.port = 443;
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

    public static AwsIotMqttConnectionConfigBuilder newMtlsPkcs12Builder(String pkcs12Path, String pkcs12Password) {
        AwsIotMqttConnectionConfigBuilder builder = new AwsIotMqttConnectionConfigBuilder();
        builder.tlsOptions = TlsContextOptions.createWithMtlsPkcs12(pkcs12Path, pkcs12Password);
        return builder;
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
        this.endpoint = endpoint;
        return this;
    }

    /**
     * The port to connect to on the IoT endpoint
     * 
     * @param port The port to connect to on the IoT endpoint. Usually 8883 for
     *             MQTT, or 443 for websockets
     */
    AwsIotMqttConnectionConfigBuilder withPort(short port) {
        this.port = port;
        return this;
    }

    /**
     * Configures the client_id to use to connect to the IoT Core service
     * 
     * @param client_id The client id for this connection. Needs to be unique across
     *                  all devices/clients.
     */
    AwsIotMqttConnectionConfigBuilder withClientId(String clientId) {
        this.clientId = clientId;
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
        this.cleanSession = cleanSession;
        return this;
    }

    /**
     * Configures the connection to use MQTT over websockets. Forces the port to
     * 443.
     */
    AwsIotMqttConnectionConfigBuilder withWebsocket() {
        this.useWebsocket = true;
        this.tlsOptions.alpnList.clear();
        this.port = 443;
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
        this.keepAliveSecs = keepAliveSecs;
        return this;
    }

    /**
     * Configures the TCP socket connect timeout (in milliseconds)
     * 
     * @param timeout_ms TCP socket timeout
     */
    AwsIotMqttConnectionConfigBuilder withTimeoutMs(int timeoutMs) {
        this.socketOptions.connectTimeoutMs = timeoutMs;
        return this;
    }

    /**
     * Configures the common settings for the socket to use when opening a
     * connection to the server
     * 
     * @param socketOptions The socket settings
     */
    AwsIotMqttConnectionConfigBuilder withSocketOptions(SocketOptions socketOptions) {
        swapReferenceTo(this.socketOptions, socketOptions);
        this.socketOptions = socketOptions;
        return this;
    }

    AwsIotMqttConnectionConfigBuilder withUsername(String username) {
        this.username = String.format("%s?SDK=JavaV2&Version=%s", username, new PackageInfo().toString());
        return this;
    }

    AwsIotMqttConnectionConfigBuilder withPassword(String password) {
        this.password = password;
        return this;
    }


    CompletableFuture<MqttClientConnection> build(MqttClientConnectionEvents callbacks) {
        // Validate
        if (bootstrap == null) {
            throw new Exception(??);
        }

        // Lazy create
        synchronized(this) {
            if (tlsOptions != null && tlsContext == null) {
                tlsContext = new TlsContext(tlsOptions);
                addReferenceTo(tlsContext);
            }

            if (client == null) {
                client = ??;
                addReferenceTo(client);
            }
        }

        // Connection create
        MqttClientConnection connection = new MqttClientConnection(client, callbacks);
        CompletableFuture<MqttClientConnection> futureConnection = new CompletableFuture<>();
        futureConnection.whenComplete( (conn, error) ->
            if (error) {
                connection.disconnect().get();
                connection.close();
            });

        // Connect
        connection.

        return futureConnection;
    }
}

