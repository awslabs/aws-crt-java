/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.iot;

import java.util.function.Consumer;

/**
 * Configuration options for an MQTT-based streaming operation.
 */
public class StreamingOperationOptions {

    String topic;
    Consumer<SubscriptionStatusEvent> subscriptionStatusEventCallback;
    Consumer<IncomingPublishEvent> incomingPublishEventCallback;

    public static class StreamingOperationOptionsBuilder {

        private final StreamingOperationOptions options = new StreamingOperationOptions();

        StreamingOperationOptionsBuilder() {}

        /**
         * Sets the MQTT topic that a streaming operation will listen for messages on
         *
         * @param topic MQTT topic to listen to
         *
         * @return this builder object
         */
        public StreamingOperationOptionsBuilder withTopic(String topic) {
            this.options.topic = topic;
            return this;
        }

        /**
         * Sets the callback function a streaming operation should invoke whenever the underlying subscription changes
         * status.
         *
         * @param callback function to invoke on streaming subscription status changes
         *
         * @return this builder object
         */
        public StreamingOperationOptionsBuilder withSubscriptionStatusEventCallback(Consumer<SubscriptionStatusEvent> callback) {
            this.options.subscriptionStatusEventCallback = callback;
            return this;
        }

        /**
         * Sets the callback function a streaming operation should invoke every time a publish message arrives on
         * the operation's topic.
         *
         * @param callback function to invoke whenever a publish messages arrives that matches the operation's topic
         *
         * @return this builder object
         */
        public StreamingOperationOptionsBuilder withIncomingPublishEventCallback(Consumer<IncomingPublishEvent> callback) {
            this.options.incomingPublishEventCallback = callback;
            return this;
        }

        /**
         * Creates a StreamingOperationOptions instance from the builder's configuration.
         *
         * @return a new StreamingOperationOptions instance
         */
        public StreamingOperationOptions build() {
            return new StreamingOperationOptions(this.options);
        }
    }

    /**
     * Creates a new StreamingOperationOptionsBuilder instance
     *
     * @return a new StreamingOperationOptionsBuilder instance
     */
    public static StreamingOperationOptionsBuilder builder() {
        return new StreamingOperationOptionsBuilder();
    }

    private StreamingOperationOptions() {
    }

    private StreamingOperationOptions(StreamingOperationOptions options) {
        this.topic = options.topic;
        this.subscriptionStatusEventCallback = options.subscriptionStatusEventCallback;
        this.incomingPublishEventCallback = options.incomingPublishEventCallback;
    }
}
