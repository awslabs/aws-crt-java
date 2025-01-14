/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.iot;

/**
 * Class to configure an MQTT-based request response client.
 */
public class MqttRequestResponseClientOptions {
    private int maxRequestResponseSubscriptions = 0;
    private int maxStreamingSubscriptions = 0;
    private int operationTimeoutSeconds = 0;

    public static class MqttRequestResponseClientOptionsBuilder {
        private MqttRequestResponseClientOptions options = new MqttRequestResponseClientOptions();

        private MqttRequestResponseClientOptionsBuilder() {}

        /**
         * Sets the maximum number of subscriptions that the client will concurrently use for request-response operations
         *
         * @param maxRequestResponseSubscriptions maximum number of subscriptions that the client will concurrently use for request-response operations
         * @return the builder instance
         */
        public MqttRequestResponseClientOptionsBuilder withMaxRequestResponseSubscriptions(int maxRequestResponseSubscriptions) {
            this.options.maxRequestResponseSubscriptions = maxRequestResponseSubscriptions;

            return this;
        }

        /**
         * Sets the maximum number of subscriptions that the client will concurrently use for streaming operations
         *
         * @param maxStreamingSubscriptions maximum number of subscriptions that the client will concurrently use for streaming operations
         * @return the builder instance
         */
        public MqttRequestResponseClientOptionsBuilder withMaxStreamingSubscriptions(int maxStreamingSubscriptions) {
            this.options.maxStreamingSubscriptions = maxStreamingSubscriptions;

            return this;
        }

        /**
         * Sets the duration, in seconds, that a request-response operation will wait for completion before giving up
         *
         * @param operationTimeoutSeconds duration, in seconds, that a request-response operation will wait for completion before giving up
         * @return the builder instance
         */
        public MqttRequestResponseClientOptionsBuilder withOperationTimeoutSeconds(int operationTimeoutSeconds) {
            this.options.operationTimeoutSeconds = operationTimeoutSeconds;

            return this;
        }

        /**
         * Creates a new MqttRequestResponseClientOptions instance based on current builder configuration
         * @return
         */
        public MqttRequestResponseClientOptions build() {
            return new MqttRequestResponseClientOptions(this.options);
        }
    }

    private MqttRequestResponseClientOptions() {
    }

    private MqttRequestResponseClientOptions(MqttRequestResponseClientOptions options) {
        this.maxRequestResponseSubscriptions = options.maxRequestResponseSubscriptions;
        this.maxStreamingSubscriptions = options.maxStreamingSubscriptions;
        this.operationTimeoutSeconds = options.operationTimeoutSeconds;
    }

    /**
     * Creates a new builder for MqttRequestResponseClientOptions instances
     *
     * @return a new builder for MqttRequestResponseClientOptions instances
     */
    public static MqttRequestResponseClientOptions.MqttRequestResponseClientOptionsBuilder builder() {
        return new MqttRequestResponseClientOptions.MqttRequestResponseClientOptionsBuilder();
    }

    /**
     * @return the maximum number of subscriptions that the client will concurrently use for request-response operations
     */
    int getMaxRequestResponseSubscriptions() {
        return this.maxRequestResponseSubscriptions;
    }

    /**
     * @return the maximum number of subscriptions that the client will concurrently use for streaming operations
     */
    int getMaxStreamingSubscriptions() {
        return this.maxStreamingSubscriptions;
    }

    /**
     * @return the duration, in seconds, that a request-response operation will wait for completion before giving up
     */
    int getOperationTimeoutSeconds() {
        return this.operationTimeoutSeconds;
    }
}
