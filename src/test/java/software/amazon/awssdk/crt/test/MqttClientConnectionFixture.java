/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.rules.DisableOnDebug;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

import static org.junit.Assert.*;

import software.amazon.awssdk.crt.*;
import software.amazon.awssdk.crt.auth.credentials.CredentialsProvider;
import software.amazon.awssdk.crt.auth.signing.AwsSigningConfig;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;
import software.amazon.awssdk.crt.io.TlsContextCustomKeyOperationOptions;
import software.amazon.awssdk.crt.mqtt.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    static final String AWS_TEST_ACCESS_KEY = System.getenv("AWS_TEST_ROLE_CREDENTIAL_ACCESS_KEY");
    static final String AWS_TEST_SECRET_ACCESS_KEY = System.getenv("AWS_TEST_ROLE_CREDENTIAL_SECRET_ACCESS_KEY");
    static final String AWS_TEST_SESSION_TOKEN = System.getenv("AWS_TEST_ROLE_CREDENTIAL_SESSION_TOKEN");
    // Key/Cert connection related
    static final String AWS_TEST_RSA_CERTIFICATE = System.getenv("AWS_TEST_RSA_CERTIFICATE");
    static final String AWS_TEST_RSA_PRIVATEKEY = System.getenv("AWS_TEST_RSA_PRIVATE_KEY");
    static final String AWS_TEST_RSA_PKCS8_PRIVATEKEY = System.getenv("AWS_TEST_PKCS8_RSA_PRIVATE_KEY");
    static final String AWS_TEST_ECC_CERTIFICATE = System.getenv("AWS_TEST_ECC_CERTIFICATE");
    static final String AWS_TEST_ECC_PRIVATEKEY = System.getenv("AWS_TEST_ECC_PRIVATE_KEY");
    // Cognito
    static final String AWS_TEST_COGNITO_ENDPOINT = System.getenv("AWS_TEST_COGNITO_ENDPOINT");
    static final String AWS_TEST_COGNITO_IDENTITY = System.getenv("AWS_TEST_COGNITO_IDENTITY");

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

    enum AUTH_KEY_TYPE {
        RSA,
        ECC
    }

    Consumer<MqttConnectionConfig> connectionConfigTransformer = null;

    public void skipIfCredentialsMissingRSA()
    {
        Assume.assumeTrue(AWS_TEST_RSA_CERTIFICATE != null);
        Assume.assumeTrue(AWS_TEST_RSA_PRIVATEKEY != null);
    }
    public void skipIfCredentialsMissingECC()
    {
        Assume.assumeTrue(AWS_TEST_ECC_CERTIFICATE != null);
        Assume.assumeTrue(AWS_TEST_ECC_PRIVATEKEY != null);
    }
    public void skipIfCredentialsMissingStatic()
    {
        Assume.assumeTrue(AWS_TEST_ACCESS_KEY != null);
        Assume.assumeTrue(AWS_TEST_SECRET_ACCESS_KEY != null);
        Assume.assumeTrue(AWS_TEST_SESSION_TOKEN != null);
    }

    protected void setConnectionConfigTransformer(Consumer<MqttConnectionConfig> connectionConfigTransformer) {
        this.connectionConfigTransformer = connectionConfigTransformer;
    }

    private boolean findCredentials(AUTH_KEY_TYPE key_type) {
        CrtTestContext ctx = getContext();

        // For each parameter, check the context first, then check the file system/system properties
        try {
            if (ctx.iotCARoot == null) {
                pathToCa = AWS_TEST_ROOTCA != null ? Paths.get(AWS_TEST_ROOTCA) : null;
                if (pathToCa == null || !pathToCa.toFile().exists()) {
                    throw new MissingCredentialsException("Root CA could not be found at " + pathToCa);
                }
                ctx.iotCARoot = Files.readAllBytes(pathToCa);
            }
            caRoot = new String(ctx.iotCARoot);

            if (ctx.iotEndpoint == null && AWS_TEST_ENDPOINT != null) {
                ctx.iotEndpoint = AWS_TEST_ENDPOINT;
            }
            iotEndpoint = ctx.iotEndpoint;

            if (key_type == AUTH_KEY_TYPE.RSA && ctx.iotClientCertificate == null) {
                pathToCert = AWS_TEST_RSA_CERTIFICATE != null? Paths.get(AWS_TEST_RSA_CERTIFICATE) : null;
                if (pathToCert == null || pathToCert.toString().equals("")) {
                    throw new MissingCredentialsException("Certificate not provided");
                }
                if (!pathToCert.toFile().exists()) {
                    throw new MissingCredentialsException("Certificate could not be found at " + pathToCert);
                }
                ctx.iotClientCertificate = Files.readAllBytes(pathToCert);
            }
            else if (key_type == AUTH_KEY_TYPE.ECC && ctx.iotClientEccCertificate == null) {
                pathToCert = AWS_TEST_ECC_CERTIFICATE != null? Paths.get(AWS_TEST_ECC_CERTIFICATE) : null;
                if (pathToCert == null || pathToCert.toString().equals("")) {
                    throw new MissingCredentialsException("Certificate not provided");
                }
                if (!pathToCert.toFile().exists()) {
                    throw new MissingCredentialsException("Certificate could not be found at " + pathToCert);
                }
                ctx.iotClientEccCertificate = Files.readAllBytes(pathToCert);
            }

            if (key_type == AUTH_KEY_TYPE.RSA && ctx.iotClientPrivateKey == null) {
                pathToKey = AWS_TEST_RSA_PRIVATEKEY != null? Paths.get(AWS_TEST_RSA_PRIVATEKEY) : null;
                if (pathToKey == null || pathToKey.toString().equals("")) {
                    throw new MissingCredentialsException("Private key not provided");
                }
                if (!pathToKey.toFile().exists()) {
                    throw new MissingCredentialsException("Private key could not be found at " + pathToKey);
                }
                ctx.iotClientPrivateKey = Files.readAllBytes(pathToKey);
            }
            else if (key_type == AUTH_KEY_TYPE.ECC && ctx.iotClientEccPrivateKey == null) {
                pathToKey = AWS_TEST_ECC_PRIVATEKEY != null? Paths.get(AWS_TEST_ECC_PRIVATEKEY) : null;
                if (pathToKey == null || pathToKey.toString().equals("")) {
                    throw new MissingCredentialsException("Private key not provided");
                }
                if (!pathToKey.toFile().exists()) {
                    throw new MissingCredentialsException("Private key could not be found at " + pathToKey);
                }
                ctx.iotClientEccPrivateKey = Files.readAllBytes(pathToKey);
            }

            if( key_type == AUTH_KEY_TYPE.ECC)
            {
                certificatePem = new String(ctx.iotClientEccCertificate);
                privateKeyPem = new String(ctx.iotClientEccPrivateKey);
            }
            else if(key_type == AUTH_KEY_TYPE.RSA)
            {
                certificatePem = new String(ctx.iotClientCertificate);
                privateKeyPem = new String(ctx.iotClientPrivateKey);
            }

            return true;
        } catch (InvalidPathException ex) {
            return false;
        } catch (MissingCredentialsException ex) {
            if (AWS_TEST_IS_CI) {
                throw ex;
            }
            return false;
        } catch (IOException ex) {
            return false;
        }
    }

    public TlsContext createIotClientTlsContext() {
        return createTlsContextOptions(getContext().iotCARoot);
    }

    public TlsContext createIotClientTlsContext(TlsContextOptions tlsOpts) {
        return new TlsContext(configureTlsContextOptions(tlsOpts, getContext().iotCARoot));
    }

    MqttClientConnectionFixture() {
    }

    boolean connect() {
        return connect(false, 0, 0, null);
    }

    boolean connect(Consumer<MqttMessage> anyMessageHandler) {
        return connect(false, 0, 0, anyMessageHandler);
    }

    boolean connect(boolean cleanSession, int keepAliveSecs, int protocolOperationTimeout) {
        return connect(cleanSession, keepAliveSecs, protocolOperationTimeout, null);
    }

    boolean connectECC() {
        return connectWithKeyType(false, 0, 0, null, AUTH_KEY_TYPE.ECC);
    }

    boolean connectDirect(boolean cleanSession, int keepAliveSecs, int protocolOperationTimeout, Consumer<MqttMessage> anyMessageHandler)
    {
        MqttClientConnectionEvents events = new MqttClientConnectionEvents() {
            @Override
            public void onConnectionResumed(boolean sessionPresent) {
                System.out.println("Connection resumed");
            }

            @Override
            public void onConnectionInterrupted(int errorCode) {
                if (!disconnecting) {
                    System.out.println(
                            "Connection interrupted: error: " + errorCode + " " + CRT.awsErrorString(errorCode));
                }
            }
        };

        try(EventLoopGroup elg = new EventLoopGroup(1);
            HostResolver hr = new HostResolver(elg);
            ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtls(certificatePem, privateKeyPem);) {

            int port = TEST_PORT;
            if (caRoot != null) {
                tlsOptions.overrideDefaultTrustStore(caRoot);
            }
            if (TlsContextOptions.isAlpnSupported()) {
                tlsOptions.withAlpnList("x-amzn-mqtt-ca");
                port = TEST_PORT_ALPN;
            }

            cleanSession = true; // only true is supported right now
            String clientId = TEST_CLIENTID + (UUID.randomUUID()).toString();
            try (TlsContext tls = new TlsContext(tlsOptions);
                 MqttClient client = new MqttClient(bootstrap, tls);
                 MqttConnectionConfig config = new MqttConnectionConfig()) {

                config.setMqttClient(client);
                config.setClientId(clientId);
                config.setEndpoint(iotEndpoint);
                config.setPort(port);
                config.setCleanSession(cleanSession);
                config.setKeepAliveSecs(keepAliveSecs);
                config.setProtocolOperationTimeoutMs(protocolOperationTimeout);

                if (connectionConfigTransformer != null) {
                    connectionConfigTransformer.accept(config);
                }

                connection = new MqttClientConnection(config);
                if (anyMessageHandler != null) {
                    connection.onMessage(anyMessageHandler);
                }

                CompletableFuture<Boolean> connected = connection.connect();
                connected.get();
                return true;
            }
        } catch (Exception ex) {
            fail("Exception during connect: " + ex.toString());
        }
        return false;
    }

    boolean connect(boolean cleanSession, int keepAliveSecs, int protocolOperationTimeout, Consumer<MqttMessage> anyMessageHandler)
    {
        Assume.assumeTrue(findCredentials(AUTH_KEY_TYPE.RSA));
        return connectDirect(cleanSession,keepAliveSecs,protocolOperationTimeout, anyMessageHandler);
    }

    boolean connectWithKeyType(boolean cleanSession, int keepAliveSecs, int protocolOperationTimeout, Consumer<MqttMessage> anyMessageHandler, AUTH_KEY_TYPE type) {
        Assume.assumeTrue(findCredentials(type));
        return connectDirect(cleanSession,keepAliveSecs,protocolOperationTimeout, anyMessageHandler);
    }

    boolean connectWebsocketsWithCredentialsProvider(CredentialsProvider credentialsProvider)
    {
        // Return result
        boolean result = false;
        // Default settings
        boolean cleanSession = true; // only true is supported right now
        int keepAliveSecs = 0;
        int protocolOperationTimeout = 60000;
        String clientId = TEST_CLIENTID + (UUID.randomUUID()).toString();
        int port = TEST_PORT_ALPN;
        // Other setup
        iotEndpoint = AWS_TEST_ENDPOINT;

        try (EventLoopGroup elg = new EventLoopGroup(1);
            HostResolver hr = new HostResolver(elg);
            ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
            TlsContextOptions tlsOptions = TlsContextOptions.createDefaultClient();)
        {
            try (TlsContext tls = new TlsContext(tlsOptions);
                 MqttClient client = new MqttClient(bootstrap, tls);
                 MqttConnectionConfig config = new MqttConnectionConfig();
                 AwsSigningConfig signingConfig = new AwsSigningConfig();) {

                config.setMqttClient(client);
                config.setClientId(clientId);
                config.setEndpoint(iotEndpoint);
                config.setPort(port);
                config.setCleanSession(cleanSession);
                config.setKeepAliveSecs(keepAliveSecs);
                config.setProtocolOperationTimeoutMs(protocolOperationTimeout);
                config.setUseWebsockets(true);

                // Make the websocket transformer
                signingConfig.setAlgorithm(AwsSigningConfig.AwsSigningAlgorithm.SIGV4);
                signingConfig.setSignatureType(AwsSigningConfig.AwsSignatureType.HTTP_REQUEST_VIA_QUERY_PARAMS);
                signingConfig.setRegion(TEST_REGION);
                signingConfig.setService("iotdevicegateway");
                signingConfig.setCredentialsProvider(credentialsProvider);
                signingConfig.setOmitSessionToken(true);

                try (MqttClientConnectionSigv4HandshakeTransformer transformer = new MqttClientConnectionSigv4HandshakeTransformer(signingConfig);)
                {
                    config.setWebsocketHandshakeTransform(transformer);
                    // config.addReferenceTo(transformer);

                    connection = new MqttClientConnection(config);

                    CompletableFuture<Boolean> connected = connection.connect();
                    connected.get();
                    result = true;
                }
            } catch (Exception ex) {
                fail("Exception during connect: " + ex.toString());
            }
        }
        return result;
    }

    boolean connectCustomKeyOps(TlsContextCustomKeyOperationOptions keyOperationOptions) throws Exception {
        return connectWithCustomKeyOps(true, 0, 60000, keyOperationOptions);
    }

    boolean connectWithCustomKeyOps(boolean cleanSession, int keepAliveSecs, int protocolOperationTimeout, TlsContextCustomKeyOperationOptions keyOperationOptions) throws Exception {
        Assume.assumeTrue(findCredentials(AUTH_KEY_TYPE.RSA));

        // Add the certificate
        keyOperationOptions.withCertificateFilePath(pathToCert.toString());

        try(EventLoopGroup elg = new EventLoopGroup(1);
            HostResolver hr = new HostResolver(elg);
            ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsCustomKeyOperations(keyOperationOptions);) {

            int port = TEST_PORT;
            if (caRoot != null) {
                tlsOptions.overrideDefaultTrustStore(caRoot);
            }
            if (TlsContextOptions.isAlpnSupported()) {
                tlsOptions.withAlpnList("x-amzn-mqtt-ca");
                port = TEST_PORT_ALPN;
            }

            cleanSession = true; // only true is supported right now
            String clientId = TEST_CLIENTID + (UUID.randomUUID()).toString();
            try (TlsContext tls = new TlsContext(tlsOptions);
                 MqttClient client = new MqttClient(bootstrap, tls);
                 MqttConnectionConfig config = new MqttConnectionConfig()) {

                config.setMqttClient(client);
                config.setClientId(clientId);
                config.setEndpoint(iotEndpoint);
                config.setPort(port);
                config.setCleanSession(cleanSession);
                config.setKeepAliveSecs(keepAliveSecs);
                config.setProtocolOperationTimeoutMs(protocolOperationTimeout);

                connection = new MqttClientConnection(config);

                CompletableFuture<Boolean> connected = connection.connect();
                connected.get();
                return true;
            }
        } catch (Exception ex) {
            connection.close();
            throw ex;
        }
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
