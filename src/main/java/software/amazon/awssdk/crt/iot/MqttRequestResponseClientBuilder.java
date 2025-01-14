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
public class MqttRequestResponseClientBuilder {

    public static class MqttRequestResponseClientOptions {
        int maxRequestResponseSubscriptions = 0;
        int maxStreamingSubscriptions = 0;
        int operationTimeoutSeconds = 0;

        private MqttRequestResponseClientOptions() {}

        private void setMaxRequestResponseSubscriptions(int value) {
            maxRequestResponseSubscriptions = value;
        }

        public int getMaxRequestResponseSubscriptions() { return maxRequestResponseSubscriptions; }

        private void setMaxStreamingSubscriptions(int value) {
            maxStreamingSubscriptions = value;
        }

        public int getMaxStreamingSubscriptions() { return maxStreamingSubscriptions; }

        private void setOperationTimeoutSeconds(int value) {
            operationTimeoutSeconds = value;
        }

        public int getOperationTimeoutSeconds() { return operationTimeoutSeconds; }
    }

    private MqttRequestResponseClientOptions options = new MqttRequestResponseClientOptions();

    public MqttRequestResponseClientBuilder() {
    }

    /**
     * Sets the maximum number of subscriptions that the client will concurrently use for request-response operations
     *
     * @param maxRequestResponseSubscriptions maximum number of subscriptions that the client will concurrently use for request-response operations
     * @return the builder instance
     */
    public MqttRequestResponseClientBuilder withMaxRequestResponseSubscriptions(int maxRequestResponseSubscriptions) {
        options.setMaxRequestResponseSubscriptions(maxRequestResponseSubscriptions);

        return this;
    }

    /**
     * Sets the maximum number of subscriptions that the client will concurrently use for streaming operations
     *
     * @param maxStreamingSubscriptions maximum number of subscriptions that the client will concurrently use for streaming operations
     * @return the builder instance
     */
    public MqttRequestResponseClientBuilder withMaxStreamingSubscriptions(int maxStreamingSubscriptions) {
        options.setMaxStreamingSubscriptions(maxStreamingSubscriptions);

        return this;
    }

    /**
     * Sets the Duration, in seconds, that a request-response operation will wait for completion before giving up
     *
     * @param operationTimeoutSeconds duration, in seconds, that a request-response operation will wait for completion before giving up
     * @return the builder instance
     */
    public MqttRequestResponseClientBuilder withOperationTimeoutSeconds(int operationTimeoutSeconds) {
        options.setOperationTimeoutSeconds(operationTimeoutSeconds);

        return this;
    }

    /**
     * Creates a new MQTT request-response client that uses an MQTT5 client as transport
     *
     * @param client MQTT5 client to use
     * @return a new MQTT request-response client
     */
    public MqttRequestResponseClient build(Mqtt5Client client) {
        return new MqttRequestResponseClient(client, options);
    }

    /**
     * Creates a new MQTT request-response client that uses an MQTT311 client as transport
     *
     * @param client MQTT311 client to use
     * @return a new MQTT request-response client
     */
    public MqttRequestResponseClient build(MqttClientConnection client) {
        return new MqttRequestResponseClient(client, options);
    }
}
