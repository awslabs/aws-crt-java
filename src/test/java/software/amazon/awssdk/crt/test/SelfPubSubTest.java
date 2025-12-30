/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Assume;
import org.junit.Test;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Rule;
import org.junit.rules.Timeout;

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;
import software.amazon.awssdk.crt.mqtt.MqttMessage;
import software.amazon.awssdk.crt.mqtt.QualityOfService;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;
import java.io.UnsupportedEncodingException;

public class SelfPubSubTest extends MqttClientConnectionFixture {
    private final static int MAX_TEST_RETRIES = 3;
    private final static int TEST_RETRY_SLEEP_MILLIS = 2000;

    @Rule
    public Timeout testTimeout = Timeout.seconds(15);

    public SelfPubSubTest() {
    }

    static final String TEST_TOPIC = "publish/me/senpai/" + UUID.randomUUID().toString();
    static final String TEST_PAYLOAD = "PUBLISH ME! SHINY AND CHROME!";

    private void doPubSubTest() {
        try (TlsContextOptions contextOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT311_IOT_CORE_RSA_CERT,
                AWS_TEST_MQTT311_IOT_CORE_RSA_KEY);
             TlsContext context = new TlsContext(contextOptions)) {
            connectDirect(
                    context,
                    AWS_TEST_MQTT311_IOT_CORE_HOST,
                    8883,
                    null,
                    null,
                    null,
                    true);

            CompletableFuture<MqttMessage> receivedFuture = new CompletableFuture<>();
            Consumer<MqttMessage> messageHandler = (message) -> {
                receivedFuture.complete(message);
            };

            CompletableFuture<Integer> subscribed = connection.subscribe(TEST_TOPIC, QualityOfService.AT_LEAST_ONCE,
                    messageHandler);
            int packetId = subscribed.get();

            assertNotSame(0, packetId);

            MqttMessage message = new MqttMessage(TEST_TOPIC, TEST_PAYLOAD.getBytes(), QualityOfService.AT_LEAST_ONCE,
                    false);
            CompletableFuture<Integer> published = connection.publish(message);
            packetId = published.get();

            assertNotSame(0, packetId);

            published = connection.publish(message);
            packetId = published.get();

            assertNotSame(0, packetId);

            MqttMessage received = receivedFuture.get();
            assertEquals("Received", message.getTopic(), received.getTopic());
            assertArrayEquals("Received", message.getPayload(), received.getPayload());
            assertEquals("Received", message.getQos(), received.getQos());
            assertEquals("Received", message.getRetain(), received.getRetain());

            CompletableFuture<Integer> unsubscribed = connection.unsubscribe(TEST_TOPIC);
            packetId = unsubscribed.get();

            assertNotSame(0, packetId);

            disconnect();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            close();
        }
    }

    @Test
    public void testPubSub() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT311_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT311_IOT_CORE_RSA_KEY != null);
        Assume.assumeTrue(AWS_TEST_MQTT311_IOT_CORE_RSA_CERT != null);

        TestUtils.doRetryableTest(this::doPubSubTest, TestUtils::isRetryableTimeout, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doPubSubOnMessageTest() {
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

        try (TlsContextOptions contextOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT311_IOT_CORE_RSA_CERT,
                AWS_TEST_MQTT311_IOT_CORE_RSA_KEY);
             TlsContext context = new TlsContext(contextOptions)) {
            setConnectionMessageTransformer(messageHandler);
            connectDirect(
                context,
                AWS_TEST_MQTT311_IOT_CORE_HOST,
                8883,
                null,
                null,
                null,
                true);


            CompletableFuture<Integer> subscribed = connection.subscribe(TEST_TOPIC, QualityOfService.AT_LEAST_ONCE);
            int packetId = subscribed.get();

            assertNotSame(0, packetId);

            MqttMessage message = new MqttMessage(TEST_TOPIC, TEST_PAYLOAD.getBytes(), QualityOfService.AT_LEAST_ONCE);
            CompletableFuture<Integer> published = connection.publish(message);
            packetId = published.get();

            assertNotSame(0, packetId);

            published = connection.publish(message);
            packetId = published.get();

            assertNotSame(0, packetId);

            CompletableFuture<Integer> unsubscribed = connection.unsubscribe(TEST_TOPIC);
            packetId = unsubscribed.get();

            assertNotSame(0, packetId);

            disconnect();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            close();
        }
    }

    @Test
    public void testPubSubOnMessage() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT311_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT311_IOT_CORE_RSA_KEY != null);
        Assume.assumeTrue(AWS_TEST_MQTT311_IOT_CORE_RSA_CERT != null);

        TestUtils.doRetryableTest(this::doPubSubOnMessageTest, TestUtils::isRetryableTimeout, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }
};
