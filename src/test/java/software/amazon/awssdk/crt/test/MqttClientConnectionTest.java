/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import static org.junit.Assert.fail;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import software.amazon.awssdk.crt.mqtt.QualityOfService;


public class MqttClientConnectionTest extends MqttClientConnectionFixture {
    public MqttClientConnectionTest() {
    }

    @Test
    public void testConnectDisconnect() {
        skipIfNetworkUnavailable();
        connect();
        disconnect();
        close();
    }

    @Test
    public void testConnectPublishWaitStatisticsDisconnect() {
        skipIfNetworkUnavailable();
        connect();
        try {
            publish("test/topic/" + (UUID.randomUUID()).toString(), "hello_world".getBytes(), QualityOfService.AT_LEAST_ONCE).get(60, TimeUnit.SECONDS);
        } catch (Exception ex) {
            fail("Exception ocurred during publish: " + ex.getMessage());
        }
        checkOperationStatistics(0, 0, 0, 0);
        disconnect();
        close();
    }

    @Test
    public void testConnectPublishStatisticsWaitDisconnect() {
        skipIfNetworkUnavailable();

        connect();

        String topic = "test/topic/" + (UUID.randomUUID()).toString();
        byte[] payload = "Hello_World".getBytes();
        CompletableFuture<Integer> puback = null;
        // Per packet: (The size of the topic, the size of the payload, 2 for the header and 2 for the packet ID)
        Long expectedSize = new Long(topic.length() + payload.length + 4);

        puback = publish(topic, payload, QualityOfService.AT_LEAST_ONCE);

        // Note: Unacked will be zero because we are not connected
        checkOperationStatistics(1, expectedSize, 0, 0);

        // Publish
        try {
            puback.get(60, TimeUnit.SECONDS);
        } catch (Exception ex) {
            fail("Exception ocurred during publish: " + ex.getMessage());
        }

        // Make sure it is empty
        checkOperationStatistics(0, 0, 0, 0);
        disconnect();
        close();
    }
    // NOTE: In the future, we will want to test offline publishes, but right now the test fixtures forces a clean session
    // and making publishes while a clean session is set causes an error.

    @Test
    public void testECCKeyConnectDisconnect() {
        skipIfNetworkUnavailable();
        connectECC();
        disconnect();
        close();
    }
};
