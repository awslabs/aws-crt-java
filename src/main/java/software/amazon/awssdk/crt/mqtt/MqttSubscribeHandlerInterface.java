/*
 * Copyright Amazon.com Inc. or its affiliates.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.awssdk.crt.mqtt;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface MqttSubscribeHandlerInterface {
    /**
     * Subscribes to a topic
     *
     * @param topic   The topic to subscribe to
     * @param qos     {@link QualityOfService} for this subscription
     * @param handler A handler which can receive an MqttMessage when a message is
     *                published to the topic
     * @return Future result is the packet/message id associated with the subscribe
     *         operation
     */
    CompletableFuture<Integer> subscribe(String topic, QualityOfService qos, Consumer<MqttMessage> handler);
}
