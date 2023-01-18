/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import java.util.UUID;
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
    public void testConnectPublishStatisticsDisconnect() {
        skipIfNetworkUnavailable();
        connect();
        publish("test/topic/" + (UUID.randomUUID()).toString(), "hello_world".getBytes(), QualityOfService.AT_LEAST_ONCE);
        sleepForMilliseconds(2000);
        checkOperationStatistics(0, 0, 0, 0);
        disconnect();
        close();
    }

    @Test
    public void testPublishStatisticsConnectDisconnect() {
        skipIfNetworkUnavailable();

        long publish_count = 5;
        String randomTopic = "test/topic/" + (UUID.randomUUID()).toString();
        byte[] randomPayload = "Hello_World".getBytes();
        for (int i = 0; i < publish_count; i++) {
            publish(randomTopic, randomPayload, QualityOfService.AT_LEAST_ONCE);
        }
        sleepForMilliseconds(2000);
        // Per packet: (The size of the topic, the size of the payload, 2 for the header and 2 for the packet ID)
        Long expectedSize = (randomTopic.length() + randomPayload.length + 4) * publish_count;
        // Note: Unacked will be zero because we are not connected
        checkOperationStatistics(publish_count, expectedSize, 0, 0);
        connect();
        sleepForMilliseconds(2000);
        checkOperationStatistics(0, 0, 0, 0);
        disconnect();
        close();
    }

    @Test
    public void testECCKeyConnectDisconnect() {
        skipIfNetworkUnavailable();
        connectECC();
        disconnect();
        close();
    }
};
