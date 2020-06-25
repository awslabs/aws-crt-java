/*
 * Copyright Amazon.com Inc. or its affiliates.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.awssdk.crt.mqtt;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface MqttPubSubInterface {
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

    /**
     * Subscribes to a topic without a handler (messages will only be delivered to
     * the OnMessage handler)
     *
     * @param topic The topic to subscribe to
     * @param qos   {@link QualityOfService} for this subscription
     * @return Future result is the packet/message id associated with the subscribe
     *         operation
     */
    CompletableFuture<Integer> subscribe(String topic, QualityOfService qos);

    /**
     * Publishes a message to a topic
     *
     * @param message The message to publish. The message contains the topic to
     *                publish to.
     * @param qos     The {@link QualityOfService} to use for the publish operation
     * @param retain  Whether or not the message should be retained by the broker to
     *                be delivered to future subscribers
     * @return Future value is the packet/message id associated with the publish
     *         operation
     */
    CompletableFuture<Integer> publish(MqttMessage message, QualityOfService qos, boolean retain);
}
