/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Test;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Rule;
import org.junit.rules.Timeout;

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
        skipIfNetworkUnavailable();

        connect();

        try {
            CompletableFuture<MqttMessage> receivedFuture = new CompletableFuture<>();
            Consumer<MqttMessage> messageHandler = (message) -> {
                receivedFuture.complete(message);
            };

            CompletableFuture<Integer> subscribed = connection.subscribe(TEST_TOPIC, QualityOfService.AT_LEAST_ONCE,
                    messageHandler);
            subscribed.thenApply(unused -> subsAcked++);
            int packetId = subscribed.get();

            assertNotSame(0, packetId);
            assertEquals("Single subscription", 1, subsAcked);

            MqttMessage message = new MqttMessage(TEST_TOPIC, TEST_PAYLOAD.getBytes(), QualityOfService.AT_LEAST_ONCE,
                    false);
            CompletableFuture<Integer> published = connection.publish(message);
            published.thenApply(unused -> pubsAcked++);
            packetId = published.get();

            assertNotSame(0, packetId);
            assertEquals("Published", 1, pubsAcked);

            published = connection.publish(message);
            published.thenApply(unused -> pubsAcked++);
            packetId = published.get();

            assertNotSame(0, packetId);
            assertEquals("Published", 2, pubsAcked);

            MqttMessage received = receivedFuture.get();
            assertEquals("Received", message.getTopic(), received.getTopic());
            assertArrayEquals("Received", message.getPayload(), received.getPayload());
            assertEquals("Received", message.getQos(), received.getQos());
            assertEquals("Received", message.getRetain(), received.getRetain());

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
        skipIfNetworkUnavailable();

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

        connect(messageHandler);

        try {
            CompletableFuture<Integer> subscribed = connection.subscribe(TEST_TOPIC, QualityOfService.AT_LEAST_ONCE);
            subscribed.thenApply(unused -> subsAcked++);
            int packetId = subscribed.get();

            assertNotSame(0, packetId);
            assertEquals("Single subscription", 1, subsAcked);

            MqttMessage message = new MqttMessage(TEST_TOPIC, TEST_PAYLOAD.getBytes(), QualityOfService.AT_LEAST_ONCE);
            CompletableFuture<Integer> published = connection.publish(message);
            published.thenApply(unused -> pubsAcked++);
            packetId = published.get();

            assertNotSame(0, packetId);
            assertEquals("Published", 1, pubsAcked);

            published = connection.publish(message);
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
