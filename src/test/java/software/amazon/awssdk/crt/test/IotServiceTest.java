/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Assume;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;
import software.amazon.awssdk.crt.mqtt.MqttMessage;
import software.amazon.awssdk.crt.mqtt.QualityOfService;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;

public class IotServiceTest extends MqttClientConnectionFixture {
    private final static int MAX_TEST_RETRIES = 3;
    private final static int TEST_RETRY_SLEEP_MILLIS = 2000;

    public IotServiceTest() {
    }

    static final String TEST_TOPIC = "sdk/test/java/" + UUID.randomUUID().toString();
    int subsAcked = 0;

    private void doIotServiceTest() {
        Consumer<MqttMessage> messageHandler = (message) -> {};
        int port = 8883;

        try (TlsContextOptions contextOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT311_IOT_CORE_RSA_CERT,
                AWS_TEST_MQTT311_IOT_CORE_RSA_KEY);
            TlsContext context = new TlsContext(contextOptions)) {
            connectDirect(
                    context,
                    AWS_TEST_MQTT311_IOT_CORE_HOST,
                    port,
                    null,
                    null,
                    null);

            CompletableFuture<Integer> subscribed = connection.subscribe(TEST_TOPIC, QualityOfService.AT_LEAST_ONCE, messageHandler);
            subscribed.thenApply(packetId -> subsAcked++);
            subscribed.get();

            assertEquals("Single subscription", 1, subsAcked);

            CompletableFuture<Integer> unsubscribed = connection.unsubscribe(TEST_TOPIC);
            unsubscribed.thenApply(packetId -> subsAcked--);
            unsubscribed.get();

            assertEquals("No Subscriptions", 0, subsAcked);
            disconnect();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            close();
        }
    }

    @Test
    public void testIotService() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT311_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT311_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT311_IOT_CORE_RSA_KEY != null);

        TestUtils.doRetryableTest(this::doIotServiceTest, TestUtils::isRetryableTimeout, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }
};
