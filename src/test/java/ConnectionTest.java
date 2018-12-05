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

package crt.test;

import org.junit.Test;
import static org.junit.Assert.*;
import software.amazon.awssdk.crt.*;
import software.amazon.awssdk.crt.mqtt.*;
import java.util.concurrent.Semaphore;


class MqttConnectionFixture {
    EventLoopGroup elg = null;
    ClientBootstrap bootstrap = null;
    MqttClient client = null;
    MqttConnection connection = null;
    final Semaphore done = new Semaphore(0);
    private boolean disconnecting = false;

    static final String TEST_ENDPOINT = "localhost";
    static final short TEST_PORT = 1883;
    static final String TEST_CLIENTID = "AwsCrtJavaMqttTest";

    MqttConnectionFixture() {

    }

    boolean connect() {
        return connect(false, (short)0);
    }

    boolean connect(boolean cleanSession, short keepAliveMs) {
        return connect(cleanSession, keepAliveMs, null);
    }
    
    boolean connect(boolean cleanSession, short keepAliveMs, TlsContext tls) {
        return connect(TEST_ENDPOINT, TEST_PORT, TEST_CLIENTID, cleanSession, keepAliveMs, tls);
    }

    boolean connect(String endpoint, short port, String clientId, boolean cleanSession, short keepAliveMs, TlsContext tls) {
        try {
            elg = new EventLoopGroup(1);
            bootstrap = new ClientBootstrap(elg);
        } catch (CrtRuntimeException ex) {
            fail("Exception during bootstrapping: " + ex.toString());
        }
        try {
            client = new MqttClient(bootstrap, tls);
            assertNotNull(client);
            assertTrue(client.native_ptr() != 0);

            connection = new MqttConnection(client, endpoint, port) {
                @Override
                public void onOnline() { }

                @Override
                public boolean onOffline(boolean recoverable, String reason) {
                    if (!disconnecting && !recoverable) {
                        System.out.println("Lost connection to server: " + reason);
                    }
                    return recoverable;
                }
            };
            assertNotNull(connection);
            MqttActionListener connectAck = new MqttActionListener() {
                @Override
                public void onSuccess() {
                    done.release();
                }

                @Override
                public void onFailure(Throwable cause) {
                    System.out.println("Connect failed: " + cause.toString());
                    done.release();
                }
            };
            cleanSession = true; // only true is supported right now
            connection.connect(clientId, cleanSession, keepAliveMs, connectAck);
            done.acquire();
            assertEquals("Connected", MqttConnection.ConnectionState.Connected, connection.getState());
            return true;
        } catch (Exception ex) {
            fail("Exception during connect: " + ex.toString());
        }
        return false;
    }

    void disconnect() {
        MqttActionListener disconnectAck = new MqttActionListener() {
            @Override
            public void onSuccess() {
                done.release();
            }

            @Override
            public void onFailure(Throwable cause) {
                fail("Disconnect failed: " + cause.toString());
                done.release();
            }
        };

        disconnecting = true;
        try {
            connection.disconnect(disconnectAck);
            done.acquire();
        }
        catch (Exception ex) {
            fail("Exception during disconnect: " + ex.getMessage());
        }
        
        assertEquals("Disconnected", MqttConnection.ConnectionState.Disconnected, connection.getState());
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
