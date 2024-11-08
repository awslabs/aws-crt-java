package software.amazon.awssdk.crt.iot;

import software.amazon.awssdk.crt.mqtt.MqttClientConnection;
import software.amazon.awssdk.crt.mqtt5.Mqtt5Client;


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
     * @param maxStreamingSubscriptions aximum number of subscriptions that the client will concurrently use for streaming operations
     * @return the builder instance
     */
    public MqttRequestResponseClientBuilder withMaxStreamingSubscriptions(int maxStreamingSubscriptions) {
        options.setMaxStreamingSubscriptions(maxStreamingSubscriptions);

        return this;
    }

    /**
     * Sets the Duration, in seconds, that a request-response operation will wait for completion before giving up
     *
     * @param operationTimeoutSeconds curation, in seconds, that a request-response operation will wait for completion before giving up
     * @return the builder instance
     */
    public MqttRequestResponseClientBuilder withOperationTimeoutSeconds(int operationTimeoutSeconds) {
        options.setOperationTimeoutSeconds(operationTimeoutSeconds);

        return this;
    }

    public MqttRequestResponseClient build(Mqtt5Client client) {
        return null;
    }

    public MqttRequestResponseClient build(MqttClientConnection client) {
        return null;
    }
}
