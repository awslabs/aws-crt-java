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
import org.junit.Rule;
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

    static final String TEST_TOPIC = "/i/am/ded";
    static final String TEST_WILL = "i am ghost nao";

    @Override
    protected void modifyConnectionConfiguration(MqttConnectionConfig config) {
        MqttMessage will = new MqttMessage(TEST_TOPIC, TEST_WILL.getBytes());

        config.setWillMessage(will);
        config.setWillQos(QualityOfService.AT_LEAST_ONCE);
        config.setWillRetain(false);
    }

    @Test
    public void testWill() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        connect();
        disconnect();
        close();
    }
};
