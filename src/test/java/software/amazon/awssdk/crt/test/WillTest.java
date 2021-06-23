/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Assume;
import org.junit.Test;
import org.junit.Rule;
import org.junit.BeforeClass;
import org.junit.rules.Timeout;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.mqtt.MqttConnectionConfig;
import software.amazon.awssdk.crt.mqtt.MqttException;
import software.amazon.awssdk.crt.mqtt.MqttMessage;
import software.amazon.awssdk.crt.mqtt.QualityOfService;

import static org.junit.Assert.fail;

public class WillTest extends MqttClientConnectionFixture {
    @Rule

    public Timeout testTimeout = Timeout.seconds(15);

    public WillTest() {
    }

    @BeforeClass
    public static void haveAwsCredentials() {
        Assume.assumeTrue(false);
    }

    static final String TEST_TOPIC = "/i/am/ded";
    static final String TEST_WILL = "i am ghost nao";

    @Override
    protected void modifyConnectionConfiguration(MqttConnectionConfig config) {
        MqttMessage will = new MqttMessage(TEST_TOPIC, TEST_WILL.getBytes(), QualityOfService.AT_LEAST_ONCE);

        config.setWillMessage(will);
    }

    @Test
    public void testWill() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        connect();
        disconnect();
        close();
    }
};
