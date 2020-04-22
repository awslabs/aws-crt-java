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
import org.junit.Test;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Rule;
import org.junit.rules.Timeout;

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.mqtt.MqttMessage;
import software.amazon.awssdk.crt.mqtt.QualityOfService;

import java.util.concurrent.CompletableFuture;
import java.util.function.*;
import java.io.UnsupportedEncodingException;

public class SelfPubSubTest extends MqttClientConnectionFixture {
    @Rule
    public Timeout testTimeout = Timeout.seconds(15);
    
    public SelfPubSubTest() {
    }

    static final String TEST_TOPIC = "publish/me/senpai";
    static final String TEST_PAYLOAD = "PUBLISH ME! SHINY AND CHROME!";

    int pubsAcked = 0;
    int subsAcked = 0;

    @Test
    public void testPubSub() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        connect();

        try {
            Consumer<MqttMessage> messageHandler = (message) -> {
                byte[] payload = message.getPayload();
                try {
                    String contents = new String(payload, "UTF-8");
                    assertEquals("Message is intact", TEST_PAYLOAD, contents);
                } catch (UnsupportedEncodingException ex) {
                    fail("Unable to decode payload: " + ex.getMessage());
                }
            };

            CompletableFuture<Integer> subscribed = connection.subscribe(TEST_TOPIC, QualityOfService.AT_LEAST_ONCE,
                    messageHandler);
            subscribed.thenApply(unused -> subsAcked++);
            int packetId = subscribed.get();

            assertNotSame(0, packetId);
            assertEquals("Single subscription", 1, subsAcked);

            MqttMessage message = new MqttMessage(TEST_TOPIC, TEST_PAYLOAD.getBytes());
            CompletableFuture<Integer> published = connection.publish(message, QualityOfService.AT_LEAST_ONCE, false);
            published.thenApply(unused -> pubsAcked++);
            packetId = published.get();

            assertNotSame(0, packetId);
            assertEquals("Published", 1, pubsAcked);

            published = connection.publish(message, QualityOfService.AT_LEAST_ONCE, false);
            published.thenApply(unused -> pubsAcked++);
            packetId = published.get();

            assertNotSame(0, packetId);
            assertEquals("Published", 2, pubsAcked);

            CompletableFuture<Integer> unsubscribed = connection.unsubscribe(TEST_TOPIC);
            unsubscribed.thenApply(unused -> subsAcked--);
            packetId = unsubscribed.get();

            assertNotSame(0, packetId);
            assertEquals("No Subscriptions", 0, subsAcked);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }

        disconnect();
        close();
    }
    
    @Test
    public void testPubSubOnMessage() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        connect();

        try {
            Consumer<MqttMessage> messageHandler = (message) -> {
                byte[] payload = message.getPayload();
                try {
                    assertEquals(TEST_TOPIC, message.getTopic());
                    String contents = new String(payload, "UTF-8");
                    assertEquals("Message is intact", TEST_PAYLOAD, contents);
                } catch (UnsupportedEncodingException ex) {
                    fail("Unable to decode payload: " + ex.getMessage());
                }
            };

            connection.onMessage(messageHandler);

            CompletableFuture<Integer> subscribed = connection.subscribe(TEST_TOPIC, QualityOfService.AT_LEAST_ONCE);
            subscribed.thenApply(unused -> subsAcked++);
            int packetId = subscribed.get();

            assertNotSame(0, packetId);
            assertEquals("Single subscription", 1, subsAcked);

            MqttMessage message = new MqttMessage(TEST_TOPIC, TEST_PAYLOAD.getBytes());
            CompletableFuture<Integer> published = connection.publish(message, QualityOfService.AT_LEAST_ONCE, false);
            published.thenApply(unused -> pubsAcked++);
            packetId = published.get();

            assertNotSame(0, packetId);
            assertEquals("Published", 1, pubsAcked);

            published = connection.publish(message, QualityOfService.AT_LEAST_ONCE, false);
            published.thenApply(unused -> pubsAcked++);
            packetId = published.get();

            assertNotSame(0, packetId);
            assertEquals("Published", 2, pubsAcked);

            CompletableFuture<Integer> unsubscribed = connection.unsubscribe(TEST_TOPIC);
            unsubscribed.thenApply(unused -> subsAcked--);
            packetId = unsubscribed.get();

            assertNotSame(0, packetId);
            assertEquals("No Subscriptions", 0, subsAcked);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }

        disconnect();
        close();
    }
};
