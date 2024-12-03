package software.amazon.awssdk.crt.iot;

import java.util.ArrayList;

public class MqttRequestResponsePath {

    public static class MqttRequestResponsePathBuilder {

        private MqttRequestResponsePath path = new MqttRequestResponsePath();

        private MqttRequestResponsePathBuilder() {}

        public MqttRequestResponsePathBuilder withResponseTopic(String responseTopic) {
            path.responseTopic = responseTopic;
            return this;
        }

        public MqttRequestResponsePathBuilder withCorrelationTokenJsonPath(String correlationTokenJsonpath) {
            path.correlationTokenJsonpath = correlationTokenJsonpath;
            return this;
        }

        public MqttRequestResponsePath build() {
            return new MqttRequestResponsePath(path);
        }
    }

    private String responseTopic;
    private String correlationTokenJsonpath;

    private MqttRequestResponsePath() {
    }

    private MqttRequestResponsePath(MqttRequestResponsePath path) {
        this.responseTopic = path.responseTopic;
        this.correlationTokenJsonpath = path.correlationTokenJsonpath;
    }

    public static MqttRequestResponsePathBuilder builder() {
        return new MqttRequestResponsePathBuilder();
    }
}
