/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Assume;
import org.junit.Test;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import org.junit.Rule;
import org.junit.rules.Timeout;
import software.amazon.awssdk.crt.mqtt.*;

import java.util.concurrent.CompletableFuture;

public class PublishTest extends MqttClientConnectionFixture {
    @Rule
    public Timeout testTimeout = Timeout.seconds(15);

    public PublishTest() {
    }

    static final String TEST_TOPIC = "publish/me/senpai";
    static final String TEST_PAYLOAD = "PUBLISH ME! SHINY AND CHROME!";

    int pubsAcked = 0;

    @Test
    public void testPublish() {
        skipIfNetworkUnavailable();
        connect();

        try {
            MqttMessage message = new MqttMessage(TEST_TOPIC, TEST_PAYLOAD.getBytes(), QualityOfService.AT_LEAST_ONCE);
            CompletableFuture<Integer> published = connection.publish(message);
            published.thenApply(packetId -> pubsAcked++);
            published.get();

            assertEquals("Published", 1, pubsAcked);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }

        disconnect();
        close();
    }
};
