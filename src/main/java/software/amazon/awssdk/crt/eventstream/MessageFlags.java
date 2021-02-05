package software.amazon.awssdk.crt.eventstream;

public enum MessageFlags {
    ConnectionAccepted(1),
    TerminateStream(2);

    private int byteValue;

    MessageFlags(int byteValue) {
        this.byteValue = byteValue;
    }

    public int getByteValue() {
        return this.byteValue;
    }
}
