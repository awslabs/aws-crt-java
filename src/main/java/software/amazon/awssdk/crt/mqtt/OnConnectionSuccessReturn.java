package software.amazon.awssdk.crt.mqtt;

/**
 * The data returned when the
 * {@link software.amazon.awssdk.crt.mqtt.MqttClientConnectedEvents}
 * connection success callback is invoked
 */
public class OnConnectionSuccessReturn {

    private boolean sessionPresent;

    /**
     * Returns whether a session was present and resumed for this successful connection.
     * Will be set to true if the connection resumed an already present MQTT connection session.
     * @return whether a session was present and resumed
     */
    public boolean getSessionPresent() {
        return sessionPresent;
    }

    /**
     * Constructs a new OnConnectionSuccessReturn with a session present.
     * @param sessionPresent whether a session was present and resumed
     */
    protected OnConnectionSuccessReturn(boolean sessionPresent) {
        this.sessionPresent = sessionPresent;
    }
}
