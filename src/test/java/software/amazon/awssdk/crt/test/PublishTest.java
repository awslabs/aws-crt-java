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
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import org.junit.Rule;
import org.junit.rules.Timeout;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.mqtt.*;

import java.util.concurrent.CompletableFuture;

public class PublishTest extends MqttClientConnectionFixture {
    @Rule
    public Timeout testTimeout = Timeout.seconds(15);

    public PublishTest() {
    }

    static final String TEST_TOPIC = "publish/me/senpai";
    static final String TEST_PAYLOAD = "PUBLISH ME! SHINY AND CHROME!";

    int pubsAcked = 0;

    @Test
    public void testPublish() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        connect();
        
        try {
            MqttMessage message = new MqttMessage(TEST_TOPIC, TEST_PAYLOAD.getBytes());
            CompletableFuture<Integer> published = connection.publish(message, QualityOfService.AT_LEAST_ONCE, false);
            published.thenApply(packetId -> pubsAcked++);
            published.get();

            assertEquals("Published", 1, pubsAcked);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }

        disconnect();
        close();
    }
};
