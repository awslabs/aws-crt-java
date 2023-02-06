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
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;
import software.amazon.awssdk.crt.mqtt.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

class MissingCredentialsException extends RuntimeException {
    MissingCredentialsException(String message) {
        super(message);
    }
}

public class MqttClientConnectionFixture extends CrtTestFixture {

    static final boolean IS_CI = System.getProperty("aws.crt.ci") != null;

    MqttClientConnection connection = null;
    private boolean disconnecting = false;

    static final String TEST_ENDPOINT = System.getProperty("endpoint");
    static final String TEST_CERTIFICATE = System.getProperty("certificate");
    static final String TEST_PRIVATEKEY = System.getProperty("privatekey");
    static final String TEST_ECC_CERTIFICATE = System.getProperty("ecc_certificate");
    static final String TEST_ECC_PRIVATEKEY = System.getProperty("ecc_privatekey");
    static final String TEST_ROOTCA = System.getProperty("rootca");
    static final short TEST_PORT = 8883;
    static final short TEST_PORT_ALPN = 443;
    static final String TEST_CLIENTID = "aws-crt-java-";

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

    protected void setConnectionConfigTransformer(Consumer<MqttConnectionConfig> connectionConfigTransformer) {
        this.connectionConfigTransformer = connectionConfigTransformer;
    }

    private boolean findCredentials(AUTH_KEY_TYPE key_type) {
        CrtTestContext ctx = getContext();

        // For each parameter, check the context first, then check the file system/system properties
        try {
            if (ctx.iotCARoot == null) {
                pathToCa = TEST_ROOTCA != null ? Paths.get(TEST_ROOTCA) : null;
                if (pathToCa == null || !pathToCa.toFile().exists()) {
                    throw new MissingCredentialsException("Root CA could not be found at " + pathToCa);
                }
                ctx.iotCARoot = Files.readAllBytes(pathToCa);
            }
            caRoot = new String(ctx.iotCARoot);

            if (ctx.iotEndpoint == null && TEST_ENDPOINT != null) {
                ctx.iotEndpoint = TEST_ENDPOINT;
            }
            iotEndpoint = ctx.iotEndpoint;

            if (key_type == AUTH_KEY_TYPE.RSA && ctx.iotClientCertificate == null) {
                pathToCert = TEST_CERTIFICATE != null? Paths.get(TEST_CERTIFICATE) : null;
                if (pathToCert == null || pathToCert.toString().equals("")) {
                    throw new MissingCredentialsException("Certificate not provided");
                }
                if (!pathToCert.toFile().exists()) {
                    throw new MissingCredentialsException("Certificate could not be found at " + pathToCert);
                }
                ctx.iotClientCertificate = Files.readAllBytes(pathToCert);
            }
            else if (key_type == AUTH_KEY_TYPE.ECC && ctx.iotClientEccCertificate == null) {
                pathToCert = TEST_ECC_CERTIFICATE != null? Paths.get(TEST_ECC_CERTIFICATE) : null;
                if (pathToCert == null || pathToCert.toString().equals("")) {
                    throw new MissingCredentialsException("Certificate not provided");
                }
                if (!pathToCert.toFile().exists()) {
                    throw new MissingCredentialsException("Certificate could not be found at " + pathToCert);
                }
                ctx.iotClientEccCertificate = Files.readAllBytes(pathToCert);
            }

            if (key_type == AUTH_KEY_TYPE.RSA && ctx.iotClientPrivateKey == null) {
                pathToKey = TEST_PRIVATEKEY != null? Paths.get(TEST_PRIVATEKEY) : null;
                if (pathToKey == null || pathToKey.toString().equals("")) {
                    throw new MissingCredentialsException("Private key not provided");
                }
                if (!pathToKey.toFile().exists()) {
                    throw new MissingCredentialsException("Private key could not be found at " + pathToKey);
                }
                ctx.iotClientPrivateKey = Files.readAllBytes(pathToKey);
            }
            else if (key_type == AUTH_KEY_TYPE.ECC && ctx.iotClientEccPrivateKey == null) {
                pathToKey = TEST_ECC_PRIVATEKEY != null? Paths.get(TEST_ECC_PRIVATEKEY) : null;
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
            if (IS_CI) {
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

            @Override
            public void onConnectionFailure(OnConnectionFailureReturn data) {
                System.out.println("Connection failed with error: " + data.getErrorCode() + " " + CRT.awsErrorString(data.getErrorCode()));
            }

            @Override
            public void onConnectionSuccess(OnConnectionSuccessReturn data) {
                System.out.println("Connection success. Session present: " + data.getSessionPresent());
            }

            @Override
            public void onConnectionClosed(OnConnectionClosedReturn data) {
                System.out.println("Connection disconnected successfully");
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
                config.setConnectionCallbacks(events);

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
        long expectedUnackedOperationCount, long expectedUnackedOperationSize, boolean equalExact) {
        try {
            MqttClientConnectionOperationStatistics statistics = connection.getOperationStatistics();

            long incomplete_ops_count = statistics.getIncompleteOperationCount();
            long incomplete_ops_size = statistics.getIncompleteOperationSize();
            long unacked_ops_count = statistics.getUnackedOperationCount();
            long unacked_ops_size = statistics.getUnackedOperationSize();

            if (equalExact == true) {
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
            }
            else {
                if (incomplete_ops_count <= expectedIncompleteOperationCount) {
                    fail("Incomplete operations count:" + incomplete_ops_count + " did not <= expected value:" + expectedIncompleteOperationCount);
                }
                if (incomplete_ops_size <= expectedIncompleteOperationSize) {
                    fail("Incomplete operations size:" + incomplete_ops_size + " did not <= expected value:" + expectedIncompleteOperationSize);
                }
                if (unacked_ops_count <= expectedUnackedOperationCount) {
                    fail("Unacked operations count:" + unacked_ops_count + " did not <= expected value:" + expectedUnackedOperationCount);
                }
                if (unacked_ops_size <= expectedUnackedOperationSize) {
                    fail("Unacked operations size:" + unacked_ops_size + " did not <= expected value:" + expectedUnackedOperationSize);
                }
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
