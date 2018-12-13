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

import com.sun.xml.internal.ws.util.CompletedFuture;
import org.junit.Test;
import static org.junit.Assert.*;
import software.amazon.awssdk.crt.mqtt.*;

import java.util.concurrent.CompletableFuture;
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

        try {
            CompletableFuture<Integer> subscribed = connection.subscribe(TEST_TOPIC, MqttConnection.QOS.AT_LEAST_ONCE, messageHandler);
            subscribed.thenAccept(packetId -> subsAcked++);
            subscribed.get();

            assertEquals("Single subscription", 1, subsAcked);

            CompletableFuture<Integer> unsubscribed = connection.unsubscribe(TEST_TOPIC);
            unsubscribed.thenAccept(packetId -> subsAcked--);
            unsubscribed.get();

            assertEquals("No Subscriptions", 0, subsAcked);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
        
        disconnect();       
    }
};
