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
import software.amazon.awssdk.crt.io.*;
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

public class CustomKeyOpsFixture extends CrtTestFixture {

    MqttClientConnection connection = null;
    private boolean disconnecting = false;

    static final String TEST_ENDPOINT = System.getProperty("endpoint");
    static final String TEST_CERTIFICATE = System.getProperty("certificate");
    static final String TEST_PRIVATEKEY = System.getProperty("privatekey_p8");
    static final String TEST_ROOTCA = System.getProperty("rootca");
    static final short TEST_PORT = 8883;
    static final short TEST_PORT_ALPN = 443;
    static final String TEST_CLIENTID = "aws-crt-java-";

    Path pathToCert = null;
    Path pathToKey = null;
    Path pathToCa = null;

    String certificatePath = null;
    String privateKeyPkcs8Path = null;
    String caRoot = null;
    String iotEndpoint = null;

    CustomKeyOpsFixture() {
    }

    protected boolean findCredentials() {
        CrtTestContext ctx = getContext();

        // For each parameter, check the file system/system properties
        try {
            if (ctx.iotCARoot == null) {
                pathToCa = TEST_ROOTCA != null ? Paths.get(TEST_ROOTCA) : null;
                if (pathToCa == null || !pathToCa.toFile().exists()) {
                    throw new MissingCredentialsException("Root CA could not be found at " + pathToCa);
                }
                ctx.iotCARoot = Files.readAllBytes(pathToCa);
            }
            caRoot = new String(ctx.iotCARoot);

            iotEndpoint = TEST_ENDPOINT;

            pathToCert = TEST_CERTIFICATE != null? Paths.get(TEST_CERTIFICATE) : null;
            if (pathToCert == null || pathToCert.toString().equals("")) {
                throw new MissingCredentialsException("Certificate not provided");
            }
            if (!pathToCert.toFile().exists()) {
                throw new MissingCredentialsException("Certificate could not be found at " + pathToCert);
            }
            certificatePath = new String(pathToCert.toString());

            pathToKey = TEST_PRIVATEKEY != null ? Paths.get(TEST_PRIVATEKEY) : null;
            if (pathToKey == null || pathToKey.toString().equals("")) {
                throw new MissingCredentialsException("Private PKCS8 key not provided");
            }
            if (!pathToKey.toFile().exists()) {
                throw new MissingCredentialsException("Private PKCS8 key could not be found at " + pathToKey);
            }
            privateKeyPkcs8Path = new String(pathToKey.toString());

            return true;
        } catch (InvalidPathException ex) {
            return false;
        } catch (MissingCredentialsException ex) {
            return false;
        } catch (IOException ex) {
            return false;
        }
    }

    boolean connect(TlsContextCustomKeyOperationOptions keyOperationOptions) throws Exception {
        return connect(true, 0, 60000, keyOperationOptions);
    }

    boolean connect(boolean cleanSession, int keepAliveSecs, int protocolOperationTimeout, TlsContextCustomKeyOperationOptions keyOperationOptions) throws Exception {
        Assume.assumeTrue(findCredentials());

        // Add the certificate
        keyOperationOptions.withCertificateFilePath(certificatePath);

        MqttClientConnectionEvents events = new MqttClientConnectionEvents() {
            @Override
            public void onConnectionResumed(boolean sessionPresent) {
                System.out.println("Connection resumed");
            }

            @Override
            public void onConnectionInterrupted(int errorCode) {
                if (!disconnecting) {
                    System.out.println("Connection interrupted: error: " + errorCode + " " + CRT.awsErrorString(errorCode));
                }
            }
        };

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
                connected.get(5, TimeUnit.SECONDS);
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
}
