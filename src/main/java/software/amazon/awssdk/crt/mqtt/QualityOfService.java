package software.amazon.awssdk.crt.mqtt;

/**
 * Quality of Service associated with a publish action or subscription [MQTT-4.3].
  */
public enum QualityOfService {
    /**
     * Message will be delivered at most once, or may not be delivered at all. There will be no ACK, and the message
     * will not be stored.
     */
    AT_MOST_ONCE(0),

    /**
     * Message will be delivered at least once. It may be resent multiple times if errors occur before an ACK is
     * returned to the sender. The message will be stored in case it has to be re-sent. This is the most common QualityOfService.
     */
    AT_LEAST_ONCE(1),

    /**
     * The message is always delivered exactly once. This is the safest, but slowest QualityOfService, because multiple levels
     * of handshake must happen to guarantee no duplication of messages.
     */
    EXACTLY_ONCE(2);
    /* reserved = 3 */

    private int qos;

    QualityOfService(int value) {
        qos = value;
    }

    public int getValue() {
        return qos;
    }
}
