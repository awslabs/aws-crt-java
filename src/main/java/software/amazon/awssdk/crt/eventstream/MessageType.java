package software.amazon.awssdk.crt.eventstream;

public enum MessageType {
    ApplicationMessage((byte)0),
    ApplicationError((byte)1),
    Ping((byte)2),
    PingResponse((byte)3),
    Connect((byte)4),
    ConnectAck((byte)5),
    ProtocolError((byte)6),
    ServerError((byte)7);

    private byte enumValue;

    MessageType(byte enumValue) {
        this.enumValue = enumValue;
    }

    public byte getEnumValue() {
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
