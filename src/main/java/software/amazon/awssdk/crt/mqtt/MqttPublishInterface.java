/*
 * Copyright Amazon.com Inc. or its affiliates.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.awssdk.crt.mqtt;

import java.util.concurrent.CompletableFuture;

public interface MqttPublishInterface {
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
