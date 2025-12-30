/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

 package software.amazon.awssdk.crt.test;

import org.junit.Assume;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;
import software.amazon.awssdk.crt.mqtt.QualityOfService;

 import software.amazon.awssdk.crt.mqtt.*;

/* For environment variable setup, see SetupCrossCICrtEnvironment in the CRT builder */
public class MqttClientConnectionTest extends MqttClientConnectionFixture {
    private final static int MAX_TEST_RETRIES = 3;
    private final static int TEST_RETRY_SLEEP_MILLIS = 2000;

    public MqttClientConnectionTest() {
    }

    private void doConnectDisconnectTest() {
        try (TlsContextOptions contextOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT311_IOT_CORE_RSA_CERT,
                AWS_TEST_MQTT311_IOT_CORE_RSA_KEY);
             TlsContext context = new TlsContext(contextOptions)) {
            connectDirect(
                context,
                AWS_TEST_MQTT311_IOT_CORE_HOST,
                8883,
                null,
                null,
                null,
                true);
            disconnect();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            close();
        }
    }

    @Test
    public void testConnectDisconnect() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT311_IOT_CORE_HOST, AWS_TEST_MQTT311_IOT_CORE_RSA_KEY,
            AWS_TEST_MQTT311_IOT_CORE_RSA_CERT);

        TestUtils.doRetryableTest(this::doConnectDisconnectTest, TestUtils::isRetryableTimeout, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doConnectPublishWaitStatisticsDisconnectTest() {
        try (TlsContextOptions contextOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT311_IOT_CORE_RSA_CERT,
                AWS_TEST_MQTT311_IOT_CORE_RSA_KEY);
             TlsContext context = new TlsContext(contextOptions)) {
            connectDirect(
                context,
                AWS_TEST_MQTT311_IOT_CORE_HOST,
                8883,
                null,
                null,
                null,
                true);

            publish(
                    "test/topic/" + (UUID.randomUUID()).toString(),
                    "hello_world".getBytes(),
                    QualityOfService.AT_LEAST_ONCE).get(60, TimeUnit.SECONDS);

            checkOperationStatistics(0, 0);
            disconnect();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            close();
        }
    }

    @Test
    public void testConnectPublishWaitStatisticsDisconnect() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT311_IOT_CORE_HOST, AWS_TEST_MQTT311_IOT_CORE_RSA_KEY,
            AWS_TEST_MQTT311_IOT_CORE_RSA_CERT);

        TestUtils.doRetryableTest(this::doConnectPublishWaitStatisticsDisconnectTest, TestUtils::isRetryableTimeout, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doConnectPublishStatisticsWaitDisconnectTest() {
        try (TlsContextOptions contextOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT311_IOT_CORE_RSA_CERT,
                AWS_TEST_MQTT311_IOT_CORE_RSA_KEY);
             TlsContext context = new TlsContext(contextOptions)) {
            connectDirect(
                context,
                AWS_TEST_MQTT311_IOT_CORE_HOST,
                8883,
                null,
                null,
                null,
                true);

            String topic = "test/topic/" + (UUID.randomUUID()).toString();
            byte[] payload = "Hello_World".getBytes();
            // Per packet: (The size of the topic, the size of the payload, 2 for the header and 2 for the packet ID)
            Long expectedSize = (topic.length() + payload.length + 4l);

            CompletableFuture<Integer> publishComplete = publish(topic, payload, QualityOfService.AT_LEAST_ONCE);
            checkOperationStatistics(1, expectedSize);

            publishComplete.get(60, TimeUnit.SECONDS);

            checkOperationStatistics(0, 0);
            disconnect();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            close();
        }
    }

    @Test
    public void testConnectPublishStatisticsWaitDisconnect() throws Exception{
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT311_IOT_CORE_HOST, AWS_TEST_MQTT311_IOT_CORE_RSA_KEY,
            AWS_TEST_MQTT311_IOT_CORE_RSA_CERT);

        TestUtils.doRetryableTest(this::doConnectPublishStatisticsWaitDisconnectTest, TestUtils::isRetryableTimeout, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }
    // NOTE: In the future, we will want to test offline publishes, but right now the test fixtures forces a clean session
    // and making publishes while a clean session is set causes an error.

    private void doECCKeyConnectDisconnectTest() {
        try (TlsContextOptions contextOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT311_IOT_CORE_ECC_CERT,
                AWS_TEST_MQTT311_IOT_CORE_ECC_KEY);
             TlsContext context = new TlsContext(contextOptions)) {
            connectDirect(
                    context,
                    AWS_TEST_MQTT311_IOT_CORE_HOST,
                    8883,
                    null,
                    null,
                    null,
                    true);
            disconnect();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            close();
        }
    }

    @Test
    public void testECCKeyConnectDisconnect() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT311_IOT_CORE_HOST, AWS_TEST_MQTT311_IOT_CORE_ECC_KEY,
            AWS_TEST_MQTT311_IOT_CORE_ECC_CERT);

        TestUtils.doRetryableTest(this::doECCKeyConnectDisconnectTest, TestUtils::isRetryableTimeout, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doConnectDisconnectEventsHappyTest() {
        try (TlsContextOptions contextOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT311_IOT_CORE_RSA_CERT,
                AWS_TEST_MQTT311_IOT_CORE_RSA_KEY);
             TlsContext context = new TlsContext(contextOptions)) {
            connectDirect(
                context,
                AWS_TEST_MQTT311_IOT_CORE_HOST,
                8883,
                null,
                null,
                null,
                true);

            OnConnectionSuccessReturn connectionResult = waitForConnectSuccess();
            assertTrue("Connection success callback was empty", connectionResult != null);
            assertTrue("Session present was NOT false", !connectionResult.getSessionPresent());

            disconnect();
            try {
                OnConnectionClosedReturn result = waitForConnectClose();
                assertTrue("Connection close callback was empty", result != null);
            } catch (Exception ex) {
                fail(ex.toString());
            }

            assertEquals("Unexpected onConnectionSuccess call: ", 1, connectionEventsStatistics.onConnectionSuccessCalled);
            assertEquals("Unexpected onConnectionClosed call: ", 1, connectionEventsStatistics.onConnectionClosedCalled);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            close();
        }
    }

    @Test
    public void testConnectDisconnectEventsHappy() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT311_IOT_CORE_HOST, AWS_TEST_MQTT311_IOT_CORE_RSA_KEY,
            AWS_TEST_MQTT311_IOT_CORE_RSA_CERT);

        TestUtils.doRetryableTest(this::doConnectDisconnectEventsHappyTest, TestUtils::isRetryableTimeout, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
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
            TlsContext context = new TlsContext(contextOptions)) {
            try {
                connectDirect(
                    context,
                    AWS_TEST_MQTT311_IOT_CORE_HOST,
                    123,
                    null,
                    null,
                    null,
                    true);
            } catch (Exception ex) {
                // Do nothing with the exception - we expect this to throw since we passed an incorrect port.
            }

            try {
                OnConnectionFailureReturn result = waitForConnectFailure();
                assertTrue("Connection error callback was empty", result != null);
                assertTrue("Error code was success when it should not be", result.getErrorCode() != 0);
            } catch (Exception ex) {
                fail(ex.toString());
            }

            assertEquals("Unexpected onConnectionFailure call: ", 1, connectionEventsStatistics.onConnectionFailureCalled);
            assertEquals("Unexpected onConnectionClosed call: ", 0, connectionEventsStatistics.onConnectionClosedCalled);
        } finally {
            close();
        }
     }
};
