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
import software.amazon.awssdk.crt.mqtt.*;
import java.util.function.*;

import software.amazon.awssdk.crt.test.MqttConnectionFixture;

public class SubscribeTest extends MqttConnectionFixture {
    public SubscribeTest() {
    }

    static final String TEST_TOPIC = "suback/me/senpai";

    int subsAcked = 0;

    @Test
    public void testSubscribeUnsubscribe() {
        connect();

        Consumer<MqttMessage> messageHandler = new Consumer<MqttMessage>() {
            @Override
            public void accept(MqttMessage message) {

            }
        };

        MqttActionListener subAck = new MqttActionListener() {
            @Override
            public void onSuccess() {
                subsAcked++;
                done.release();
            }

            @Override
            public void onFailure(Throwable cause) {
                fail("Subscription failed: " + cause.getMessage());
                done.release();
            }
        };

        try {
            connection.subscribe(TEST_TOPIC, MqttConnection.QOS.AT_LEAST_ONCE, messageHandler, subAck);
            done.acquire();

            assertEquals("Single subscription", 1, subsAcked);

            MqttActionListener unsubAck = new MqttActionListener() {
                @Override
                public void onSuccess() {
                    subsAcked--;
                    done.release();
                }

                @Override
                public void onFailure(Throwable cause) {
                    fail("Unsubscription failed: " + cause.getMessage());
                    done.release();
                }
            };
            connection.unsubscribe(TEST_TOPIC, unsubAck);
            done.acquire();

            assertEquals("No Subscriptions", 0, subsAcked);
        } catch (InterruptedException interrupted) { /* wait() can be interrupted */
            fail(interrupted.getMessage());
        } catch (MqttException mqttEx) {
            fail(mqttEx.getMessage());
        }
        
        disconnect();       
    }
};
