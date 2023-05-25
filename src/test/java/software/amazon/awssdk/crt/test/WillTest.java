/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Test;

import java.util.UUID;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.rules.Timeout;

import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;
import software.amazon.awssdk.crt.mqtt.MqttMessage;
import software.amazon.awssdk.crt.mqtt.QualityOfService;


/* For environment variable setup, see SetupCrossCICrtEnvironment in the CRT builder */
public class WillTest extends MqttClientConnectionFixture {
    @Rule

    public Timeout testTimeout = Timeout.seconds(15);

    public WillTest() {
    }

    static final String TEST_TOPIC = "/i/am/ded/" + UUID.randomUUID().toString();
    static final String TEST_WILL = "i am ghost nao";
    static final String TEST_EMPTY_WILL = "";

    @Test
    public void testWill() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT311_IOT_CORE_HOST, AWS_TEST_MQTT311_IOT_CORE_RSA_KEY, AWS_TEST_MQTT311_IOT_CORE_RSA_CERT);
        setConnectionConfigTransformer((config) -> {
            config.setWillMessage(new MqttMessage(TEST_TOPIC, TEST_WILL.getBytes(), QualityOfService.AT_LEAST_ONCE));
        });
        try (TlsContextOptions contextOptions = TlsContextOptions.createWithMtlsFromPath(
            AWS_TEST_MQTT311_IOT_CORE_RSA_CERT,
            AWS_TEST_MQTT311_IOT_CORE_RSA_KEY);
                TlsContext context = new TlsContext(contextOptions);)
            {
                connectDirectWithConfig(
                    context,
                    AWS_TEST_MQTT311_IOT_CORE_HOST,
                    8883,
                    null,
                    null,
                    null);
                disconnect();
                close();
            }
    }

    @Test
    public void testEmptyWill() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT311_IOT_CORE_HOST, AWS_TEST_MQTT311_IOT_CORE_RSA_KEY, AWS_TEST_MQTT311_IOT_CORE_RSA_CERT);
        setConnectionConfigTransformer((config) -> {
            config.setWillMessage(new MqttMessage(TEST_TOPIC, TEST_EMPTY_WILL.getBytes(), QualityOfService.AT_LEAST_ONCE));
        });
        try (TlsContextOptions contextOptions = TlsContextOptions.createWithMtlsFromPath(
            AWS_TEST_MQTT311_IOT_CORE_RSA_CERT,
            AWS_TEST_MQTT311_IOT_CORE_RSA_KEY);
                TlsContext context = new TlsContext(contextOptions);)
            {
                connectDirectWithConfig(
                    context,
                    AWS_TEST_MQTT311_IOT_CORE_HOST,
                    8883,
                    null,
                    null,
                    null);
                disconnect();
                close();
            }
    }

    @Test
    public void testNullWill() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT311_IOT_CORE_HOST, AWS_TEST_MQTT311_IOT_CORE_RSA_KEY, AWS_TEST_MQTT311_IOT_CORE_RSA_CERT);
        setConnectionConfigTransformer((config) -> {
            config.setWillMessage(new MqttMessage(TEST_TOPIC, null, QualityOfService.AT_LEAST_ONCE));
        });
        try (TlsContextOptions contextOptions = TlsContextOptions.createWithMtlsFromPath(
            AWS_TEST_MQTT311_IOT_CORE_RSA_CERT,
            AWS_TEST_MQTT311_IOT_CORE_RSA_KEY);
                TlsContext context = new TlsContext(contextOptions);)
            {
                connectDirectWithConfig(
                    context,
                    AWS_TEST_MQTT311_IOT_CORE_HOST,
                    8883,
                    null,
                    null,
                    null);
                disconnect();
                close();
            }
    }
};
