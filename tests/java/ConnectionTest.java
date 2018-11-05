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

public class ConnectionTest {
    public ConnectionTest() {
    }

    static final String TEST_ENDPOINT = "localhost:1883";
    static final int TEST_TIMEOUT = 3000; /* ms */

    @Test
    public void testConnectDisconnect() {
        try (MqttClient client = new MqttClient(1)) {
            assertNotNull(client);
            assertTrue(client.native_ptr() != 0);

            MqttConnection.ConnectOptions options = new MqttConnection.ConnectOptions();
            options.clientId = "ConnectionTest";
            options.endpointUri = TEST_ENDPOINT;
            MqttConnection connection = client.createConnection(options);
            synchronized (connection) {
                MqttActionListener connectAck = new MqttActionListener() {
                    @Override
                    public void onSuccess() {
                        connection.notify();
                    }

                    @Override
                    public void onFailure(Throwable cause) {
                        fail("Connection failed: " + cause.toString());
                        connection.notify();
                    }
                };
                connection.connect(connectAck);
                connection.wait(TEST_TIMEOUT);
                assertEquals("Connected", MqttConnection.ConnectionState.Connected, connection.getState());

                MqttActionListener disconnectAck = new MqttActionListener() {
                    @Override
                    public void onSuccess() {
                        connection.notify();
                    }

                    @Override
                    public void onFailure(Throwable cause) {
                        fail("Disconnect failed: " + cause.toString());
                        connection.notify();
                    }
                };
                connection.disconnect();
                connection.wait(TEST_TIMEOUT);
                assertEquals("Disconnected", MqttConnection.ConnectionState.Disconnected, connection.getState());
            }

        } catch (CrtRuntimeException ex) {
            fail(ex.getMessage());
        } catch (InterruptedException interrupted) { /* wait() can be interrupted */
            fail(interrupted.getMessage());
        }
    }
};
