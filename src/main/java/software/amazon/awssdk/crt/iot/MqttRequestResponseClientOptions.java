/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.iot;

import software.amazon.awssdk.crt.mqtt.MqttClientConnection;
import software.amazon.awssdk.crt.mqtt5.Mqtt5Client;

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
         * Sets the Duration, in seconds, that a request-response operation will wait for completion before giving up
         *
         * @param operationTimeoutSeconds duration, in seconds, that a request-response operation will wait for completion before giving up
         * @return the builder instance
         */
        public MqttRequestResponseClientOptionsBuilder withOperationTimeoutSeconds(int operationTimeoutSeconds) {
            this.options.operationTimeoutSeconds = operationTimeoutSeconds;

            return this;
        }

        /**
         *
         * @return
         */
        public MqttRequestResponseClientOptions build() {
            return new MqttRequestResponseClientOptions(this.options);
        }
    }

    MqttRequestResponseClientOptions() {
    }

    MqttRequestResponseClientOptions(MqttRequestResponseClientOptions options) {
        this.maxRequestResponseSubscriptions = options.maxRequestResponseSubscriptions;
        this.maxStreamingSubscriptions = options.maxStreamingSubscriptions;
        this.operationTimeoutSeconds = options.operationTimeoutSeconds;
    }

    public static MqttRequestResponseClientOptions.MqttRequestResponseClientOptionsBuilder builder() {
        return new MqttRequestResponseClientOptions.MqttRequestResponseClientOptionsBuilder();
    }

    int getMaxRequestResponseSubscriptions() {
        return this.maxRequestResponseSubscriptions;
    }

    int getMaxStreamingSubscriptions() {
        return this.maxStreamingSubscriptions;
    }

    int getOperationTimeoutSeconds() {
        return this.operationTimeoutSeconds;
    }
}
