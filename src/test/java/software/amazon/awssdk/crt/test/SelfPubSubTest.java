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

import java.util.concurrent.CompletableFuture;
import java.util.function.*;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import software.amazon.awssdk.crt.test.MqttConnectionFixture;

public class SelfPubSubTest extends MqttConnectionFixture {
    public SelfPubSubTest() {
    }

    static final String TEST_TOPIC = "publish/me/senpai";
    static final String TEST_PAYLOAD = "PUBLISH ME! SHINY AND CHROME!";

    int pubsAcked = 0;
    int subsAcked = 0;
    boolean disconnecting = false;

    @Test
    public void testPubSub() {
        connect();

        try {
            Consumer<MqttMessage> messageHandler = (message) -> {
                ByteBuffer payload = message.getPayload();
                assertTrue("Payload buffer is array of bytes", payload.hasArray());
                try {
                    String contents = new String(payload.array(), "UTF-8");
                    assertEquals("Message is intact", TEST_PAYLOAD, contents);
                } catch (UnsupportedEncodingException ex) {
                    fail("Unable to decode payload: " + ex.getMessage());
                }
            };

            CompletableFuture<Integer> subscribed = connection.subscribe(TEST_TOPIC, MqttConnection.QOS.AT_LEAST_ONCE, messageHandler);
            subscribed.thenApply(packetId -> subsAcked++);
            subscribed.get();

            assertEquals("Single subscription", 1, subsAcked);

            ByteBuffer payload = ByteBuffer.allocateDirect(TEST_PAYLOAD.length());
            payload.put(TEST_PAYLOAD.getBytes());
            MqttMessage message = new MqttMessage(TEST_TOPIC, payload);
            CompletableFuture<Integer> published = connection.publish(message, MqttConnection.QOS.AT_LEAST_ONCE, false);
            published.thenApply(packetId -> pubsAcked++);
            published.get();

            assertEquals("Published", 1, pubsAcked);

            CompletableFuture<Integer> unsubscribed = connection.unsubscribe(TEST_TOPIC);
            unsubscribed.thenApply(packetId -> subsAcked--);
            unsubscribed.get();

            assertEquals("No Subscriptions", 0, subsAcked);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }

        disconnect();
    }
};
