/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Assume;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Rule;
import org.junit.rules.Timeout;

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;
import software.amazon.awssdk.crt.mqtt.MqttMessage;
import software.amazon.awssdk.crt.mqtt.QualityOfService;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;


/* For environment variable setup, see SetupCrossCICrtEnvironment in the CRT builder */
public class SubscribeTest extends MqttClientConnectionFixture {
    private final static int MAX_TEST_RETRIES = 3;
    private final static int TEST_RETRY_SLEEP_MILLIS = 2000;

    @Rule
    public Timeout testTimeout = Timeout.seconds(15);

    public SubscribeTest() {
    }

    static final String TEST_TOPIC = "suback/me/senpai/" + UUID.randomUUID().toString();

    int subsAcked = 0;

    private void doSubscribeUnsubscribeTest() {
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
            Consumer<MqttMessage> messageHandler = (message) -> {};

            CompletableFuture<Integer> subscribed = connection.subscribe(TEST_TOPIC, QualityOfService.AT_LEAST_ONCE,
                    messageHandler);
            subscribed.thenAccept(packetId -> subsAcked++);
            subscribed.get();

            assertEquals("Single subscription", 1, subsAcked);

            CompletableFuture<Integer> unsubscribed = connection.unsubscribe(TEST_TOPIC);
            unsubscribed.thenAccept(packetId -> subsAcked--);
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
    public void testSubscribeUnsubscribe() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT311_IOT_CORE_HOST, AWS_TEST_MQTT311_IOT_CORE_RSA_KEY, AWS_TEST_MQTT311_IOT_CORE_RSA_CERT);

        TestUtils.doRetryableTest(this::doSubscribeUnsubscribeTest, TestUtils::isRetryableTimeout, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }
};
