package software.amazon.awssdk.crt.eventstream;

public enum MessageType {
    ApplicationMessage(0),
    ApplicationError(1),
    Ping(2),
    PingResponse(3),
    Connect(4),
    ConnectAck(5),
    ProtocolError(6),
    ServerError(7);

    private int enumValue;

    MessageType(int enumValue) {
        this.enumValue = enumValue;
    }

    public int getEnumValue() {
        return this.enumValue;
    }

    public static MessageType fromEnumValue(int enumValue) {
        for (MessageType type : MessageType.values()) {
            if (type.enumValue == enumValue) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown MessageType enum value: " + enumValue);
    }
}
