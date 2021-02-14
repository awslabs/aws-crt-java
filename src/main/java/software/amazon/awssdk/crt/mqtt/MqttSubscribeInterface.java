/*
 * Copyright Amazon.com Inc. or its affiliates.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.awssdk.crt.mqtt;

import java.util.concurrent.CompletableFuture;

public interface MqttSubscribeInterface {
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
}
