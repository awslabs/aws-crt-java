/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import software.amazon.awssdk.crt.mqtt.MqttMessage;
import software.amazon.awssdk.crt.mqtt.QualityOfService;;import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class MqttClientConnectionTest extends MqttClientConnectionFixture {
    public MqttClientConnectionTest() {
    }

    @Test
    public void testConnectDisconnect() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        connect();
        disconnect();
        close();
    }

    @Test
    public void testRetainedMessage() throws InterruptedException, TimeoutException, ExecutionException {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        final String topic = "sdk/test/java";
        final String messagePayload = "payload-" + UUID.randomUUID().toString();
        boolean publishedMessage = false;
        try {
            connect();
            //publish a message on a topic with the retained flag set
            final MqttMessage retainedMessage = new MqttMessage(topic, messagePayload.getBytes(StandardCharsets.UTF_8),
                    QualityOfService.AT_LEAST_ONCE, true);
            connection.publish(retainedMessage).get(); //wait for publish
            publishedMessage = true;
            Thread.sleep(5000); //wait for brief period before subscribing to the topic at all
            final CompletableFuture<Boolean> messageMatchFuture = new CompletableFuture<>();
            connection.subscribe(topic, QualityOfService.AT_LEAST_ONCE, message -> {
                messageMatchFuture.complete(message.getQos() == QualityOfService.AT_LEAST_ONCE &&
                        new String(message.getPayload(), StandardCharsets.UTF_8).equals(messagePayload));
            });
            //wait for 15 seconds max to get message sent
            Assert.assertTrue(messageMatchFuture.get(15, TimeUnit.SECONDS));
        }
        finally {
            if (publishedMessage) {
                //publish message to clear retained
                final MqttMessage clearRetained = new MqttMessage(topic, new byte[] {}, QualityOfService.AT_LEAST_ONCE);
                connection.publish(clearRetained); //not critical to wait for this
            }
            disconnect();
            close();
        }
    }
};
