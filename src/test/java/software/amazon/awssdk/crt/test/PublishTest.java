/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.fail;
import org.junit.Rule;
import org.junit.rules.Timeout;
import software.amazon.awssdk.crt.mqtt.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PublishTest extends MqttClientConnectionFixture {
    @Rule
    public Timeout testTimeout = Timeout.seconds(15);

    public PublishTest() {
    }

    final String TEST_TOPIC = "publish/me/senpai" + (UUID.randomUUID()).toString();
    static final String TEST_PAYLOAD = "PUBLISH ME! SHINY AND CHROME!";
    static final String EMPTY_PAYLOAD = "";

    final Lock receivedLock = new ReentrantLock();
    final Condition receivedSignal  = receivedLock.newCondition();

    ArrayList<MqttMessage> receivedMessages = new ArrayList<>();

    private void onPublishHandler(MqttMessage message) {
        receivedLock.lock();
        receivedMessages.add(message);
        receivedSignal.signal();
        receivedLock.unlock();
    }

    private void subscribe() {
        try {
            CompletableFuture<Integer> subscribed = connection.subscribe(TEST_TOPIC, QualityOfService.AT_LEAST_ONCE,
                    this::onPublishHandler);

            subscribed.get();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    private void publishAndCheck(byte[] payload) {
        try {
            MqttMessage message = new MqttMessage(TEST_TOPIC, payload, QualityOfService.AT_LEAST_ONCE);
            CompletableFuture<Integer> published = connection.publish(message);
            published.get();

            // test time out will break us out of this on failure
            receivedLock.lock();
            while(receivedMessages.size() == 0) {
                receivedSignal.await();
            }

            MqttMessage received = receivedMessages.get(0);
            Assert.assertEquals(
                    Arrays.toString(message.getPayload()),
                    Arrays.toString(payload));

            receivedLock.unlock();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void testRoundTrip() {
        skipIfNetworkUnavailable();
        connect();
        subscribe();
        publishAndCheck(TEST_PAYLOAD.getBytes());
        disconnect();
        close();
    }

    @Test
    public void testEmptyRoundTrip() {
        skipIfNetworkUnavailable();
        connect();
        subscribe();
        publishAndCheck(EMPTY_PAYLOAD.getBytes());
        disconnect();
        close();
    }

    @Test
    public void testNullRoundTrip() {
        skipIfNetworkUnavailable();
        connect();
        subscribe();
        publishAndCheck(null);
        disconnect();
        close();
    }
};
