/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import software.amazon.awssdk.crt.mqtt.MqttMessage;
import software.amazon.awssdk.crt.mqtt.QualityOfService;

public class WillTest extends MqttClientConnectionFixture {
    @Rule

    public Timeout testTimeout = Timeout.seconds(15);

    public WillTest() {
    }

    static final String TEST_TOPIC = "/i/am/ded";
    static final String TEST_WILL = "i am ghost nao";
    static final String TEST_EMPTY_WILL = "";

    @Test
    public void testWill() {
        skipIfNetworkUnavailable();
        setConnectionConfigTransformer((config) -> {
            config.setWillMessage(new MqttMessage(TEST_TOPIC, TEST_WILL.getBytes(), QualityOfService.AT_LEAST_ONCE));
        });
        connect();
        disconnect();
        close();
    }

    @Test
    public void testEmptyWill() {
        skipIfNetworkUnavailable();
        setConnectionConfigTransformer((config) -> {
            config.setWillMessage(new MqttMessage(TEST_TOPIC, TEST_EMPTY_WILL.getBytes(), QualityOfService.AT_LEAST_ONCE));
        });
        connect();
        disconnect();
        close();
    }

    @Test
    public void testNullWill() {
        skipIfNetworkUnavailable();
        setConnectionConfigTransformer((config) -> {
            config.setWillMessage(new MqttMessage(TEST_TOPIC, null, QualityOfService.AT_LEAST_ONCE));
        });
        connect();
        disconnect();
        close();
    }
};
