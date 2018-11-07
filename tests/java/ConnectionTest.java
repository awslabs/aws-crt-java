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
import java.util.Date;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.*;

public class ConnectionTest {
    public ConnectionTest() {
    }

    static final String TEST_ENDPOINT = "localhost:1883";
    static final int TEST_TIMEOUT = 3000; /* ms */

    @Test
    public void testConnectDisconnect() throws CrtRuntimeException, InterruptedException {
        try {
            MqttClient client = new MqttClient(1);
            assertNotNull(client);
            assertTrue(client.native_ptr() != 0);

            final Semaphore done = new Semaphore(0);

            MqttConnection.ConnectOptions options = new MqttConnection.ConnectOptions();
            options.clientId = "ConnectionTest";
            options.endpointUri = TEST_ENDPOINT;
            MqttConnection connection = new MqttConnection(client, options);
            MqttActionListener connectAck = new MqttActionListener() {
                @Override
                public void onSuccess() {
                    System.out.println("onSuccess");
                    done.release();
                }

                @Override
                public void onFailure(Throwable cause) {
                    fail("Connection failed: " + cause.toString());
                    done.release();
                }
            };
            connection.connect(connectAck);
            done.acquire();
            assertEquals("Connected", MqttConnection.ConnectionState.Connected, connection.getState());

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

            connection.disconnect(disconnectAck);
            done.acquire();
            assertEquals("Disconnected", MqttConnection.ConnectionState.Disconnected, connection.getState());
        } catch (CrtRuntimeException ex) {
            fail(ex.getMessage());
        } catch (InterruptedException interrupted) { 
            fail(interrupted.getMessage());
        } 
    }
};
