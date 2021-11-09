/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Assume;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.Log.LogLevel;
import software.amazon.awssdk.crt.mqtt.MqttMessage;
import software.amazon.awssdk.crt.mqtt.QualityOfService;

import java.util.concurrent.CompletableFuture;
import java.util.function.*;

public class IotServiceTest extends MqttClientConnectionFixture {
    public IotServiceTest() {
    }

    static final String TEST_TOPIC = "sdk/test/java";

    int subsAcked = 0;

    @Test
    public void testIotService() {
        skipIfNetworkUnavailable();
        connect( true, (short)0, 0, null);

        Consumer<MqttMessage> messageHandler = (message) -> {};

        try {
            CompletableFuture<Integer> subscribed = connection.subscribe(TEST_TOPIC, QualityOfService.AT_LEAST_ONCE, messageHandler);
            subscribed.thenApply(packetId -> subsAcked++);
            subscribed.get();

            assertEquals("Single subscription", 1, subsAcked);

            CompletableFuture<Integer> unsubscribed = connection.unsubscribe(TEST_TOPIC);
            unsubscribed.thenApply(packetId -> subsAcked--);
            unsubscribed.get();

            assertEquals("No Subscriptions", 0, subsAcked);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
        
        disconnect();
        close();
    }
};
