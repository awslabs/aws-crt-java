package software.amazon.awssdk.crt.iot;

import java.util.ArrayList;

public class MqttRequest {

    public static class MqttRequestBuilder {

        private MqttRequest request = new MqttRequest();

        private MqttRequestBuilder() {}

        public MqttRequestBuilder withResponsePath(MqttRequestResponsePath path) {
            request.responsePaths.add(path);
            return this;
        }

        public MqttRequestBuilder withSubscription(String topicFilter) {
            request.subscriptions.add(topicFilter);
            return this;
        }

        public MqttRequestBuilder withPublishTopic(String publishTopic) {
            request.publishTopic = publishTopic;
            return this;
        }

        public MqttRequestBuilder withPayload(byte[] payload) {
            request.payload = payload;
            return this;
        }


        public MqttRequestBuilder withCorrelationToken(String correlationToken) {
            request.correlationToken = correlationToken;
            return this;
        }

        public MqttRequest build() {
            return new MqttRequest(request);
        }
    }

    private ArrayList<MqttRequestResponsePath> responsePaths = new ArrayList<>();
    private ArrayList<String> subscriptions = new ArrayList<>();
    private String publishTopic;
    private String correlationToken;
    private byte[] payload;

    private MqttRequest() {
    }

    private MqttRequest(MqttRequest request) {
        this.responsePaths.addAll(request.responsePaths);
        this.subscriptions.addAll(request.subscriptions);
        this.publishTopic = request.publishTopic;
        this.correlationToken = request.correlationToken;
        this.payload = request.payload;
    }

    public MqttRequestBuilder builder() {
        return new MqttRequestBuilder();
    }

}
