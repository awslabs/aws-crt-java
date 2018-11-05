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
import java.util.function.*;

public class SubscribeTest {
    public SubscribeTest() {
    }

    static final String TEST_ENDPOINT = "localhost:1883";
    static final int TEST_TIMEOUT = 3000; /* ms */
    static final String TEST_TOPIC = "test";

    int subsAcked = 0;

    @Test
    public void testConnectDisconnect() {
        try (MqttClient client = new MqttClient(1)) {
            assertNotNull(client);
            assertTrue(client.native_ptr() != 0);

            MqttConnection.ConnectOptions options = new MqttConnection.ConnectOptions();
            options.clientId = "SubscribeTest";
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

                Consumer<MqttMessage> messageHandler = new Consumer<MqttMessage>() {
                    @Override
                    public void accept(MqttMessage message) {
                        
                    }
                };

                MqttActionListener subAck = new MqttActionListener(){
                    @Override
                    public void onSuccess() {
                        subsAcked++;
                        connection.notify();
                    }
                
                    @Override
                    public void onFailure(Throwable cause) {
                        fail("Subscription failed: " + cause.getMessage());
                        connection.notify();
                    }
                };

                connection.subscribe(TEST_TOPIC, MqttConnection.QOS.AT_LEAST_ONCE, messageHandler, subAck);
                connection.wait(TEST_TIMEOUT);

                assertEquals("Single subscription", 1, subsAcked);

                MqttActionListener unsubAck = new MqttActionListener() {
                    @Override
                    public void onSuccess() {
                        subsAcked--;
                        connection.notify();
                    }

                    @Override
                    public void onFailure(Throwable cause) {
                        fail("Subscription failed: " + cause.getMessage());
                        connection.notify();
                    }
                };
                connection.unsubscribe(TEST_TOPIC, unsubAck);
                connection.wait(TEST_TIMEOUT);

                assertEquals("No Subscriptions", 0, subsAcked);

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
        } catch (MqttException mqttEx) {
            fail(mqttEx.getMessage());
        }
    }
};
