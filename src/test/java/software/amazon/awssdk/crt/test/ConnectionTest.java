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

import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;

import org.junit.rules.ExpectedException;
import software.amazon.awssdk.crt.*;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;
import software.amazon.awssdk.crt.mqtt.*;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

class MissingCredentialsException extends RuntimeException {
    MissingCredentialsException(String message) {
        super(message);
    }
}

class MqttConnectionFixture {
    EventLoopGroup elg = null;
    ClientBootstrap bootstrap = null;
    MqttClient client = null;
    MqttConnection connection = null;
    private boolean disconnecting = false;

    static final String TEST_ENDPOINT = System.getProperty("endpoint");
    static final String TEST_CERTIFICATE = System.getProperty("certificate");
    static final String TEST_PRIVATEKEY = System.getProperty("privatekey");
    static final String TEST_ROOTCA = System.getProperty("rootca");
    static final short TEST_PORT = 8883;
    static final short TEST_PORT_ALPN = 443;
    static final String TEST_CLIENTID = "sdk-java-v2-" + UUID.randomUUID();

    Path pathToCert = null;
    Path pathToKey = null;
    Path pathToCa = null;

    private void findCredentials() {
        try {
            pathToCert = Paths.get(TEST_CERTIFICATE);
            pathToKey = Paths.get(TEST_PRIVATEKEY);
            pathToCa = Paths.get(TEST_ROOTCA);
            if (pathToCert == null || pathToCert.toString().equals("")) {
                throw new MissingCredentialsException("Certificate not provided");
            }
            if (!pathToCert.toFile().exists()) {
                throw new MissingCredentialsException("Certificate could not be found at " + pathToCert);
            }
            if (pathToKey == null || pathToKey.toString().equals("")) {
                throw new MissingCredentialsException("Private key not provided");
            }
            if (!pathToKey.toFile().exists()) {
                throw new MissingCredentialsException("Private key could not be found at " + pathToKey);
            }
            if (pathToCa != null && !pathToCa.toFile().exists()) {
                throw new MissingCredentialsException("Root CA could not be found at " + pathToCa);
            }
        } catch (InvalidPathException ex) {
            throw new MissingCredentialsException("Exception thrown during credential resolve: " + ex);
        }
    }

    MqttConnectionFixture() {
    }

    boolean connect() {
        return connect(false, 0);
    }

    boolean connect(boolean cleanSession, int keepAliveMs) {
        findCredentials();

        try {
            elg = new EventLoopGroup(1);
            bootstrap = new ClientBootstrap(elg);
        } catch (CrtRuntimeException ex) {
            fail("Exception during bootstrapping: " + ex.toString());
        }
        try {
            int port = TEST_PORT;
            TlsContextOptions tlsOptions = TlsContextOptions.createWithMTLS(pathToCert.toString(), pathToKey.toString());
            if (!pathToCa.toString().equals("")) {
                tlsOptions.overrideDefaultTrustStore(null, pathToCa.toString());
            }
            if (TlsContextOptions.isAlpnSupported()) {
                tlsOptions.setAlpnList("x-amzn-mqtt-ca");
                port = TEST_PORT_ALPN;
            }
            TlsContext tls = new TlsContext(tlsOptions);
            client = new MqttClient(bootstrap, tls);
            assertNotNull(client);
            assertTrue(client.native_ptr() != 0);

            MqttConnectionEvents events = new MqttConnectionEvents(){
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

            connection = new MqttConnection(client, events);
            assertNotNull(connection);
            cleanSession = true; // only true is supported right now
            CompletableFuture<Boolean> connected = connection.connect(TEST_CLIENTID, TEST_ENDPOINT, port, null, tls, cleanSession, keepAliveMs);
            connected.get();
            assertEquals("CONNECTED", MqttConnection.ConnectionState.CONNECTED, connection.getState());
            return true;
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
        }
        catch (Exception ex) {
            fail("Exception during disconnect: " + ex.getMessage());
        }
        
        assertEquals("DISCONNECTED", MqttConnection.ConnectionState.DISCONNECTED, connection.getState());
    }
}
public class ConnectionTest extends MqttConnectionFixture {
    public ConnectionTest() {
    }

    @Test
    public void testConnectDisconnect() {
        connect();
        disconnect(); 
    }
};
