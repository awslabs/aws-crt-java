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

import org.junit.Test;
import static org.junit.Assert.*;
import software.amazon.awssdk.crt.*;
import software.amazon.awssdk.crt.mqtt.*;
import java.util.function.*;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

import software.amazon.awssdk.crt.test.MqttConnectionFixture;

public class PublishTest extends MqttConnectionFixture {
    public PublishTest() {
    }

    static final String TEST_TOPIC = "publish/me/senpai";
    static final String TEST_PAYLOAD = "PUBLISH ME! SHINY AND CHROME!";

    int pubsAcked = 0;

    @Test
    public void testPublish() {
        connect();
        
        try {
            MqttActionListener pubAck = new MqttActionListener() {
                @Override
                public void onSuccess() {
                    pubsAcked++;
                    done.release();
                }

                @Override
                public void onFailure(Throwable cause) {
                    fail("Publish failed: " + cause.getMessage());
                    done.release();
                }
            };

            ByteBuffer payload = ByteBuffer.allocateDirect(TEST_PAYLOAD.length());
            payload.put(TEST_PAYLOAD.getBytes());
            MqttMessage message = new MqttMessage(TEST_TOPIC, payload);
            connection.publish(message, MqttConnection.QOS.AT_LEAST_ONCE, false, pubAck);
            done.acquire();

            assertEquals("Published", 1, pubsAcked);
        } catch (InterruptedException interrupted) { /* wait() can be interrupted */
            fail(interrupted.getMessage());
        } catch (MqttException mqttEx) {
            fail(mqttEx.getMessage());
        }

        disconnect();
    }
};
