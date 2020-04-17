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
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR connectionS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
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

class MissingCredentialsException extends RuntimeException {
    MissingCredentialsException(String message) {
        super(message);
    }
}

public class MqttClientConnectionFixture extends CrtTestFixture {

    MqttClientConnection connection = null;
    private boolean disconnecting = false;

    static final String TEST_ENDPOINT = System.getProperty("endpoint");
    static final String TEST_CERTIFICATE = System.getProperty("certificate");
    static final String TEST_PRIVATEKEY = System.getProperty("privatekey");
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

    protected void modifyConnectionConfiguration(MqttConnectionConfig config) {}

    private boolean findCredentials() {
        CrtTestContext ctx = getContext();

        // For each parameter, check the context first, then check the file system/system properties
        try {
            if (ctx.iotCARoot == null) {
                pathToCa = Paths.get(TEST_ROOTCA);
                if (pathToCa != null && !pathToCa.toFile().exists()) {
                    throw new MissingCredentialsException("Root CA could not be found at " + pathToCa);
                }
                ctx.iotCARoot = Files.readAllBytes(pathToCa);
            }
            caRoot = new String(ctx.iotCARoot);

            if (ctx.iotEndpoint == null) {
                ctx.iotEndpoint = TEST_ENDPOINT;
            }
            iotEndpoint = ctx.iotEndpoint;

            if (ctx.iotClientCertificate == null) {
                pathToCert = Paths.get(TEST_CERTIFICATE);
                if (pathToCert == null || pathToCert.toString().equals("")) {
                    throw new MissingCredentialsException("Certificate not provided");
                }
                if (!pathToCert.toFile().exists()) {
                    throw new MissingCredentialsException("Certificate could not be found at " + pathToCert);
                }
                ctx.iotClientCertificate = Files.readAllBytes(pathToCert);
            }
            certificatePem = new String(ctx.iotClientCertificate);

            if (ctx.iotClientPrivateKey == null) {
                pathToKey = Paths.get(TEST_PRIVATEKEY);
                if (pathToKey == null || pathToKey.toString().equals("")) {
                    throw new MissingCredentialsException("Private key not provided");
                }
                if (!pathToKey.toFile().exists()) {
                    throw new MissingCredentialsException("Private key could not be found at " + pathToKey);
                }
                ctx.iotClientPrivateKey = Files.readAllBytes(pathToKey);
            }
            privateKeyPem = new String(ctx.iotClientPrivateKey);

            return true;
        } catch (InvalidPathException ex) {
            return false;
        } catch (MissingCredentialsException ex) {
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
        return connect(false, 0);
    }

    boolean connect(boolean cleanSession, int keepAliveMs) {
        Assume.assumeTrue(findCredentials());

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
                config.setKeepAliveMs(keepAliveMs);

                modifyConnectionConfiguration(config);

                connection = new MqttClientConnection(config);

                CompletableFuture<Boolean> connected = connection.connect();
                connected.get();
                return true;
            }
        } catch (Exception ex) {
            fail("Exception during connect: " + ex.toString());
        }
        return false;
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
