/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import static org.junit.Assert.*;

import software.amazon.awssdk.crt.auth.credentials.CredentialsProvider;
import software.amazon.awssdk.crt.auth.signing.AwsSigningConfig;
import software.amazon.awssdk.crt.http.HttpProxyOptions;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.mqtt.*;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

class MissingCredentialsException extends RuntimeException {
    MissingCredentialsException(String message) {
        super(message);
    }
}

public class MqttClientConnectionFixture extends CrtTestFixture {

    MqttClientConnection connection = null;
    private boolean disconnecting = false;

    static final boolean AWS_TEST_IS_CI = System.getenv("AWS_TEST_IS_CI") != null;
    static final String AWS_TEST_ENDPOINT = System.getenv("AWS_TEST_ENDPOINT");
    static final String AWS_TEST_ROOTCA = System.getenv("AWS_TEST_ROOT_CA");
    // Static credential related
    static final String AWS_TEST_MQTT311_ROLE_CREDENTIAL_ACCESS_KEY = System.getenv("AWS_TEST_MQTT311_ROLE_CREDENTIAL_ACCESS_KEY");
    static final String AWS_TEST_MQTT311_ROLE_CREDENTIAL_SECRET_ACCESS_KEY = System.getenv("AWS_TEST_MQTT311_ROLE_CREDENTIAL_SECRET_ACCESS_KEY");
    static final String AWS_TEST_MQTT311_ROLE_CREDENTIAL_SESSION_TOKEN = System.getenv("AWS_TEST_MQTT311_ROLE_CREDENTIAL_SESSION_TOKEN");
    // Key/Cert connection related
    static final String AWS_TEST_RSA_CERTIFICATE = System.getenv("AWS_TEST_RSA_CERTIFICATE");
    static final String AWS_TEST_RSA_PRIVATEKEY = System.getenv("AWS_TEST_RSA_PRIVATE_KEY");
    static final String AWS_TEST_ECC_CERTIFICATE = System.getenv("AWS_TEST_ECC_CERTIFICATE");
    static final String AWS_TEST_ECC_PRIVATEKEY = System.getenv("AWS_TEST_ECC_PRIVATE_KEY");
    // Custom Key Ops
    static final String AWS_TEST_MQTT311_CUSTOM_KEY_OPS_KEY = System.getenv("AWS_TEST_MQTT311_CUSTOM_KEY_OPS_KEY");
    static final String AWS_TEST_MQTT311_CUSTOM_KEY_OPS_CERT = System.getenv("AWS_TEST_MQTT311_CUSTOM_KEY_OPS_CERT");
    // Cognito
    static final String AWS_TEST_COGNITO_ENDPOINT = System.getenv("AWS_TEST_COGNITO_ENDPOINT");
    static final String AWS_TEST_COGNITO_IDENTITY = System.getenv("AWS_TEST_COGNITO_IDENTITY");
    // MQTT311 Codebuild/Direct connections data
    static final String AWS_TEST_MQTT311_DIRECT_MQTT_HOST = System.getenv("AWS_TEST_MQTT311_DIRECT_MQTT_HOST");
    static final String AWS_TEST_MQTT311_DIRECT_MQTT_PORT = System.getenv("AWS_TEST_MQTT311_DIRECT_MQTT_PORT");
    static final String AWS_TEST_MQTT311_DIRECT_MQTT_BASIC_AUTH_HOST = System.getenv("AWS_TEST_MQTT311_DIRECT_MQTT_BASIC_AUTH_HOST");
    static final String AWS_TEST_MQTT311_DIRECT_MQTT_BASIC_AUTH_PORT = System.getenv("AWS_TEST_MQTT311_DIRECT_MQTT_BASIC_AUTH_PORT");
    static final String AWS_TEST_MQTT311_DIRECT_MQTT_TLS_HOST = System.getenv("AWS_TEST_MQTT311_DIRECT_MQTT_TLS_HOST");
    static final String AWS_TEST_MQTT311_DIRECT_MQTT_TLS_PORT = System.getenv("AWS_TEST_MQTT311_DIRECT_MQTT_TLS_PORT");
    // MQTT311 Codebuild/Websocket connections data
    static final String AWS_TEST_MQTT311_WS_MQTT_HOST = System.getenv("AWS_TEST_MQTT311_WS_MQTT_HOST");
    static final String AWS_TEST_MQTT311_WS_MQTT_PORT = System.getenv("AWS_TEST_MQTT311_WS_MQTT_PORT");
    static final String AWS_TEST_MQTT311_WS_MQTT_BASIC_AUTH_HOST = System.getenv("AWS_TEST_MQTT311_WS_MQTT_BASIC_AUTH_HOST");
    static final String AWS_TEST_MQTT311_WS_MQTT_BASIC_AUTH_PORT = System.getenv("AWS_TEST_MQTT311_WS_MQTT_BASIC_AUTH_PORT");
    static final String AWS_TEST_MQTT311_WS_MQTT_TLS_HOST = System.getenv("AWS_TEST_MQTT311_WS_MQTT_TLS_HOST");
    static final String AWS_TEST_MQTT311_WS_MQTT_TLS_PORT = System.getenv("AWS_TEST_MQTT311_WS_MQTT_TLS_PORT");
    // MQTT311 Codebuild misc connections data
    static final String AWS_TEST_MQTT311_BASIC_AUTH_USERNAME = System.getenv("AWS_TEST_MQTT311_BASIC_AUTH_USERNAME");
    static final String AWS_TEST_MQTT311_BASIC_AUTH_PASSWORD = System.getenv("AWS_TEST_MQTT311_BASIC_AUTH_PASSWORD");
    static final String AWS_TEST_MQTT311_CERTIFICATE_FILE = System.getenv("AWS_TEST_MQTT311_CERTIFICATE_FILE");
    static final String AWS_TEST_MQTT311_KEY_FILE = System.getenv("AWS_TEST_MQTT311_KEY_FILE");
    // MQTT311 IoT Endpoint, Key, Cert
    static final String AWS_TEST_MQTT311_IOT_CORE_HOST = System.getenv("AWS_TEST_MQTT311_IOT_CORE_HOST");
    static final String AWS_TEST_MQTT311_IOT_CORE_RSA_CERT = System.getenv("AWS_TEST_MQTT311_IOT_CORE_RSA_CERT");
    static final String AWS_TEST_MQTT311_IOT_CORE_RSA_KEY = System.getenv("AWS_TEST_MQTT311_IOT_CORE_RSA_KEY");
    static final String AWS_TEST_MQTT311_IOT_CORE_ECC_CERT = System.getenv("AWS_TEST_MQTT311_IOT_CORE_ECC_CERT");
    static final String AWS_TEST_MQTT311_IOT_CORE_ECC_KEY = System.getenv("AWS_TEST_MQTT311_IOT_CORE_ECC_KEY");
    // MQTT311 Proxy
    static final String AWS_TEST_MQTT311_PROXY_HOST = System.getenv("AWS_TEST_MQTT311_PROXY_HOST");
    static final String AWS_TEST_MQTT311_PROXY_PORT = System.getenv("AWS_TEST_MQTT311_PROXY_PORT");
    // MQTT311 Keystore
    static final String AWS_TEST_MQTT311_IOT_CORE_KEYSTORE_FORMAT = System.getenv("AWS_TEST_MQTT311_IOT_CORE_KEYSTORE_FORMAT");
    static final String AWS_TEST_MQTT311_IOT_CORE_KEYSTORE_FILE = System.getenv("AWS_TEST_MQTT311_IOT_CORE_KEYSTORE_FILE");
    static final String AWS_TEST_MQTT311_IOT_CORE_KEYSTORE_PASSWORD = System.getenv("AWS_TEST_MQTT311_IOT_CORE_KEYSTORE_PASSWORD");
    static final String AWS_TEST_MQTT311_IOT_CORE_KEYSTORE_CERT_ALIAS = System.getenv("AWS_TEST_MQTT311_IOT_CORE_KEYSTORE_CERT_ALIAS");
    static final String AWS_TEST_MQTT311_IOT_CORE_KEYSTORE_CERT_PASSWORD = System.getenv("AWS_TEST_MQTT311_IOT_CORE_KEYSTORE_CERT_PASSWORD");
    // MQTT311 PKCS12
    static final String AWS_TEST_MQTT311_IOT_CORE_PKCS12_KEY = System.getenv("AWS_TEST_MQTT311_IOT_CORE_PKCS12_KEY");
    static final String AWS_TEST_MQTT311_IOT_CORE_PKCS12_KEY_PASSWORD = System.getenv("AWS_TEST_MQTT311_IOT_CORE_PKCS12_KEY_PASSWORD");
    // MQTT311 PKCS11
    static final String AWS_TEST_MQTT311_IOT_CORE_PKCS11_LIB = System.getenv("AWS_TEST_MQTT311_IOT_CORE_PKCS11_LIB");
    static final String AWS_TEST_MQTT311_IOT_CORE_PKCS11_TOKEN_LABEL = System.getenv("AWS_TEST_MQTT311_IOT_CORE_PKCS11_TOKEN_LABEL");
    static final String AWS_TEST_MQTT311_IOT_CORE_PKCS11_PIN = System.getenv("AWS_TEST_MQTT311_IOT_CORE_PKCS11_PIN");
    static final String AWS_TEST_MQTT311_IOT_CORE_PKCS11_PKEY_LABEL = System.getenv("AWS_TEST_MQTT311_IOT_CORE_PKCS11_PKEY_LABEL");
    static final String AWS_TEST_MQTT311_IOT_CORE_PKCS11_CERT_FILE = System.getenv("AWS_TEST_MQTT311_IOT_CORE_PKCS11_CERT_FILE");
    // MQTT311 X509
    static final String AWS_TEST_MQTT311_IOT_CORE_X509_CERT = System.getenv("AWS_TEST_MQTT311_IOT_CORE_X509_CERT");
    static final String AWS_TEST_MQTT311_IOT_CORE_X509_KEY = System.getenv("AWS_TEST_MQTT311_IOT_CORE_X509_KEY");
    static final String AWS_TEST_MQTT311_IOT_CORE_X509_ENDPOINT = System.getenv("AWS_TEST_MQTT311_IOT_CORE_X509_ENDPOINT");
    static final String AWS_TEST_MQTT311_IOT_CORE_X509_ROLE_ALIAS = System.getenv("AWS_TEST_MQTT311_IOT_CORE_X509_ROLE_ALIAS");
    static final String AWS_TEST_MQTT311_IOT_CORE_X509_THING_NAME = System.getenv("AWS_TEST_MQTT311_IOT_CORE_X509_THING_NAME");
    // MQTT311 Windows Cert Store
    static final String AWS_TEST_MQTT311_IOT_CORE_WINDOWS_PFX_CERT_NO_PASS = System.getenv("AWS_TEST_MQTT311_IOT_CORE_WINDOWS_PFX_CERT_NO_PASS");
    static final String AWS_TEST_MQTT311_IOT_CORE_WINDOWS_CERT_STORE = System.getenv("AWS_TEST_MQTT311_IOT_CORE_WINDOWS_CERT_STORE");

    static final short TEST_PORT = 8883;
    static final short TEST_PORT_ALPN = 443;
    static final String TEST_CLIENTID = "aws-crt-java-";
    static final String TEST_REGION = "us-east-1";

    Path pathToCert = null;
    Path pathToKey = null;
    Path pathToCa = null;

    String certificatePem = null;
    String privateKeyPem = null;
    String caRoot = null;
    String iotEndpoint = null;

    Consumer<MqttConnectionConfig> connectionConfigTransformer = null;
    protected void setConnectionConfigTransformer(Consumer<MqttConnectionConfig> connectionConfigTransformer) {
        this.connectionConfigTransformer = connectionConfigTransformer;
    }

    Consumer<MqttMessage> connectionMessageTransfomer = null;
    protected void setConnectionMessageTransformer(Consumer<MqttMessage> connectionMessageTransfomer) {
        this.connectionMessageTransfomer = connectionMessageTransfomer;
    }

    MqttClientConnectionFixture() {
    }

    boolean connectDirectWithConfig(TlsContext tlsContext, String endpoint, int port, String username, String password, HttpProxyOptions httpProxyOptions)
    {
        try {
            return connectDirectWithConfigThrows(tlsContext, endpoint, port, username, password, httpProxyOptions);
        } catch (Exception ex) {
            fail("Exception during connect: " + ex.toString());
        }
        return false;
    }

    boolean connectDirectWithConfigThrows(TlsContext tlsContext, String endpoint, int port, String username, String password, HttpProxyOptions httpProxyOptions) throws Exception
    {
        try(EventLoopGroup elg = new EventLoopGroup(1);
            HostResolver hr = new HostResolver(elg);
            ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);) {

            // Default settings
            boolean cleanSession = true; // only true is supported right now
            int keepAliveSecs = 0;
            int protocolOperationTimeout = 60000;
            String clientId = TEST_CLIENTID + (UUID.randomUUID()).toString();

            try (MqttConnectionConfig config = new MqttConnectionConfig()) {

                MqttClient client = null;
                if (tlsContext != null)
                {
                    client = new MqttClient(bootstrap, tlsContext);
                }
                else
                {
                    client = new MqttClient(bootstrap);
                }

                config.setMqttClient(client);
                config.setClientId(clientId);
                config.setEndpoint(endpoint);
                config.setPort(port);
                config.setCleanSession(cleanSession);
                config.setKeepAliveSecs(keepAliveSecs);
                config.setProtocolOperationTimeoutMs(protocolOperationTimeout);

                if (httpProxyOptions != null) {
                    config.setHttpProxyOptions(httpProxyOptions);
                }
                if (username != null) {
                    config.setUsername(username);
                }
                if (password != null)
                {
                    config.setPassword(password);
                }

                if (connectionConfigTransformer != null) {
                    connectionConfigTransformer.accept(config);
                }

                try {
                    connection = new MqttClientConnection(config);
                    if (connectionMessageTransfomer != null) {
                        connection.onMessage(connectionMessageTransfomer);
                    }
                    CompletableFuture<Boolean> connected = connection.connect();
                    connected.get();
                } finally {
                    client.close();
                }
                return true;
            }
        }
    }

    boolean connectWebsocketsWithCredentialsProvider(CredentialsProvider credentialsProvider, String endpoint, int port, TlsContext tlsContext, String username, String password, HttpProxyOptions httpProxyOptions)
    {
        // Return result
        boolean result = false;
        // Default settings
        boolean cleanSession = true; // only true is supported right now
        int keepAliveSecs = 0;
        int protocolOperationTimeout = 60000;
        String clientId = TEST_CLIENTID + (UUID.randomUUID()).toString();

        try (EventLoopGroup elg = new EventLoopGroup(1);
            HostResolver hr = new HostResolver(elg);
            ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);)
        {
            try (MqttConnectionConfig config = new MqttConnectionConfig();
                 AwsSigningConfig signingConfig = new AwsSigningConfig();) {

                MqttClient client = null;
                if (tlsContext != null)
                {
                    client = new MqttClient(bootstrap, tlsContext);
                }
                else
                {
                    client = new MqttClient(bootstrap);
                }

                config.setMqttClient(client);
                config.setClientId(clientId);
                config.setEndpoint(endpoint);
                config.setPort(port);
                config.setCleanSession(cleanSession);
                config.setKeepAliveSecs(keepAliveSecs);
                config.setProtocolOperationTimeoutMs(protocolOperationTimeout);
                config.setUseWebsockets(true);

                if (username != null) {
                    config.setUsername(username);
                }
                if (password != null) {
                    config.setPassword(password);
                }
                if (httpProxyOptions != null) {
                    config.setHttpProxyOptions(httpProxyOptions);
                }

                if (connectionConfigTransformer != null) {
                    connectionConfigTransformer.accept(config);
                }

                // Make the websocket transformer
                if (credentialsProvider != null) {
                    signingConfig.setAlgorithm(AwsSigningConfig.AwsSigningAlgorithm.SIGV4);
                    // NOTE: Missing a credentials provider gives a non-helpful error. This needs to be changed in Java V2...
                    signingConfig.setCredentialsProvider(credentialsProvider);
                }
                signingConfig.setSignatureType(AwsSigningConfig.AwsSignatureType.HTTP_REQUEST_VIA_QUERY_PARAMS);
                signingConfig.setRegion(TEST_REGION);
                signingConfig.setService("iotdevicegateway");
                signingConfig.setOmitSessionToken(true);
                try (MqttClientConnectionSigv4HandshakeTransformer transformer = new MqttClientConnectionSigv4HandshakeTransformer(signingConfig);)
                {
                    config.setWebsocketHandshakeTransform(transformer);
                    connection = new MqttClientConnection(config);
                    if (connectionMessageTransfomer != null) {
                        connection.onMessage(connectionMessageTransfomer);
                    }
                    CompletableFuture<Boolean> connected = connection.connect();
                    connected.get();
                    result = true;
                }
                client.close();

            } catch (Exception ex) {
                fail("Exception during connect: " + ex.toString());
            }
        }
        return result;
    }

    void disconnect() {
        disconnecting = true;
        try {
            CompletableFuture<Void> disconnected = connection.disconnect();
            disconnected.get();
        } catch (Exception ex) {
            fail("Exception during disconnect: " + ex.getMessage());
        }

    }

    void close() {
        connection.close();
    }

    CompletableFuture<Integer> publish(String topic, byte[] payload, QualityOfService qos) {
        try {
            MqttMessage messageToSend = new MqttMessage(topic, payload, qos);
            return connection.publish(messageToSend);
        } catch (Exception ex) {
            fail("Exception during publish: " + ex.getMessage());
        }
        return null;
    }

    void checkOperationStatistics(
        long expectedIncompleteOperationCount, long expectedIncompleteOperationSize,
        long expectedUnackedOperationCount, long expectedUnackedOperationSize) {
        try {
            MqttClientConnectionOperationStatistics statistics = connection.getOperationStatistics();

            long incomplete_ops_count = statistics.getIncompleteOperationCount();
            long incomplete_ops_size = statistics.getIncompleteOperationSize();
            long unacked_ops_count = statistics.getUnackedOperationCount();
            long unacked_ops_size = statistics.getUnackedOperationSize();

            if (incomplete_ops_count != expectedIncompleteOperationCount) {
                fail("Incomplete operations count:" + incomplete_ops_count + " did not equal expected value:" + expectedIncompleteOperationCount);
            }

            if (incomplete_ops_size != expectedIncompleteOperationSize) {
                fail("Incomplete operations size:" + incomplete_ops_size + " did not equal expected value:" + expectedIncompleteOperationSize);
            }

            if (unacked_ops_count != expectedUnackedOperationCount) {
                fail("Unacked operations count:" + unacked_ops_count + " did not equal expected value:" + expectedUnackedOperationCount);
            }

            if (unacked_ops_size != expectedUnackedOperationSize) {
                fail("Unacked operations size:" + unacked_ops_size + " did not equal expected value:" + expectedUnackedOperationSize);
            }
        } catch (Exception ex) {
            fail("Exception during operation statistics check: " + ex.getMessage());
        }
    }

    void sleepForMilliseconds(long secondsToSleep) {
        try {
            Thread.sleep(secondsToSleep);
        } catch (Exception ex) {
            fail("Exception during sleep: " + ex.getMessage());
        }
    }
}
