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

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;
import software.amazon.awssdk.crt.mqtt.MqttMessage;
import software.amazon.awssdk.crt.mqtt.QualityOfService;


/* For environment variable setup, see SetupCrossCICrtEnvironment in the CRT builder */
public class WillTest extends MqttClientConnectionFixture {
    private final static int MAX_TEST_RETRIES = 3;
    private final static int TEST_RETRY_SLEEP_MILLIS = 2000;

    @Rule
    public Timeout testTimeout = Timeout.seconds(15);

    public WillTest() {
    }

    static final String TEST_TOPIC = "/i/am/ded/" + UUID.randomUUID().toString();
    static final String TEST_WILL = "i am ghost nao";
    static final String TEST_EMPTY_WILL = "";

    private void doWillTest() {
        setConnectionConfigTransformer((config) -> {
            config.setWillMessage(new MqttMessage(TEST_TOPIC, TEST_WILL.getBytes(), QualityOfService.AT_LEAST_ONCE));
        });

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
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            close();
        }
    }

    @Test
    public void testWill() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT311_IOT_CORE_HOST, AWS_TEST_MQTT311_IOT_CORE_RSA_KEY, AWS_TEST_MQTT311_IOT_CORE_RSA_CERT);

        TestUtils.doRetryableTest(this::doWillTest, TestUtils::isRetryableTimeout, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doEmptyWillTest() {
        setConnectionConfigTransformer((config) -> {
            config.setWillMessage(new MqttMessage(TEST_TOPIC, TEST_EMPTY_WILL.getBytes(), QualityOfService.AT_LEAST_ONCE));
        });
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
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            close();
        }
    }

    @Test
    public void testEmptyWill() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT311_IOT_CORE_HOST, AWS_TEST_MQTT311_IOT_CORE_RSA_KEY, AWS_TEST_MQTT311_IOT_CORE_RSA_CERT);

        TestUtils.doRetryableTest(this::doEmptyWillTest, TestUtils::isRetryableTimeout, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doNullWillTest() {
        setConnectionConfigTransformer((config) -> {
            config.setWillMessage(new MqttMessage(TEST_TOPIC, null, QualityOfService.AT_LEAST_ONCE));
        });
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
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            close();
        }
    }

    @Test
    public void testNullWill() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT311_IOT_CORE_HOST, AWS_TEST_MQTT311_IOT_CORE_RSA_KEY, AWS_TEST_MQTT311_IOT_CORE_RSA_CERT);

        TestUtils.doRetryableTest(this::doNullWillTest, TestUtils::isRetryableTimeout, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }
};
