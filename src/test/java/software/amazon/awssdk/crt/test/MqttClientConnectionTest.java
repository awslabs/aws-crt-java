/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

 package software.amazon.awssdk.crt.test;

import org.junit.Assume;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;
import software.amazon.awssdk.crt.mqtt.QualityOfService;

 import org.junit.Test;
 import software.amazon.awssdk.crt.mqtt.*;

/* For environment variable setup, see SetupCrossCICrtEnvironment in the CRT builder */
public class MqttClientConnectionTest extends MqttClientConnectionFixture {
    public MqttClientConnectionTest() {
    }

    @Test
    public void testConnectDisconnect() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT311_IOT_CORE_HOST, AWS_TEST_MQTT311_IOT_CORE_RSA_KEY,
            AWS_TEST_MQTT311_IOT_CORE_RSA_CERT);
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
    public void testConnectPublishWaitStatisticsDisconnect() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT311_IOT_CORE_HOST, AWS_TEST_MQTT311_IOT_CORE_RSA_KEY,
            AWS_TEST_MQTT311_IOT_CORE_RSA_CERT);
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
                try {
                    publish(
                        "test/topic/" + (UUID.randomUUID()).toString(),
                        "hello_world".getBytes(),
                        QualityOfService.AT_LEAST_ONCE).get(60, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    fail("Exception ocurred during publish: " + ex.getMessage());
                }
                checkOperationStatistics(0, 0);
                disconnect();
                close();
            }
    }

    @Test
    public void testConnectPublishStatisticsWaitDisconnect() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT311_IOT_CORE_HOST, AWS_TEST_MQTT311_IOT_CORE_RSA_KEY,
            AWS_TEST_MQTT311_IOT_CORE_RSA_CERT);
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

                String topic = "test/topic/" + (UUID.randomUUID()).toString();
                byte[] payload = "Hello_World".getBytes();
                // Per packet: (The size of the topic, the size of the payload, 2 for the header and 2 for the packet ID)
                Long expectedSize = (topic.length() + payload.length + 4l);

                CompletableFuture<Integer> puback = publish(topic, payload, QualityOfService.AT_LEAST_ONCE);
                checkOperationStatistics(1, expectedSize);

                // Publish
                try {
                    puback.get(60, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    fail("Exception ocurred during publish: " + ex.getMessage());
                }

                checkOperationStatistics(0, 0);
                disconnect();
                close();
            }
    }
    // NOTE: In the future, we will want to test offline publishes, but right now the test fixtures forces a clean session
    // and making publishes while a clean session is set causes an error.

    @Test
    public void testECCKeyConnectDisconnect() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT311_IOT_CORE_HOST, AWS_TEST_MQTT311_IOT_CORE_ECC_KEY,
            AWS_TEST_MQTT311_IOT_CORE_ECC_CERT);
        try (TlsContextOptions contextOptions = TlsContextOptions.createWithMtlsFromPath(
            AWS_TEST_MQTT311_IOT_CORE_ECC_CERT,
            AWS_TEST_MQTT311_IOT_CORE_ECC_KEY);
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
    public void testConnectDisconnectEventsHappy() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT311_IOT_CORE_HOST, AWS_TEST_MQTT311_IOT_CORE_RSA_KEY,
            AWS_TEST_MQTT311_IOT_CORE_RSA_CERT);
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
                try {
                    OnConnectionSuccessReturn result = waitForConnectSuccess();
                    assertTrue("Connection success callback was empty", result != null);
                    assertTrue("Session present was NOT false", result.getSessionPresent() == false);
                } catch (Exception ex) {
                    fail(ex.toString());
                }
                disconnect();
                try {
                    OnConnectionClosedReturn result = waitForConnectClose();
                    assertTrue("Connection close callback was empty", result != null);
                } catch (Exception ex) {
                    fail(ex.toString());
                }
                close();
            }
     }

     @Test
     public void testConnectDisconnectEventsUnhappy() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT311_IOT_CORE_HOST, AWS_TEST_MQTT311_IOT_CORE_RSA_KEY,
            AWS_TEST_MQTT311_IOT_CORE_RSA_CERT);
        try (TlsContextOptions contextOptions = TlsContextOptions.createWithMtlsFromPath(
            AWS_TEST_MQTT311_IOT_CORE_RSA_CERT,
            AWS_TEST_MQTT311_IOT_CORE_RSA_KEY);
                TlsContext context = new TlsContext(contextOptions);)
            {
                connectDirectWithConfig(
                    context,
                    AWS_TEST_MQTT311_IOT_CORE_HOST,
                    123,
                    null,
                    null,
                    null);
                try {
                    OnConnectionFailureReturn result = waitForConnectFailure();
                    assertTrue("Connection error callback was empty", result != null);
                    assertTrue("Error code was success when it should not be", result.getErrorCode() != 0);
                } catch (Exception ex) {
                    fail(ex.toString());
                }
                close();
            }
     }
};
