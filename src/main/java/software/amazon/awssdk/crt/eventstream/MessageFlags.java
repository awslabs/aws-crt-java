package software.amazon.awssdk.crt.eventstream;

/**
 * Java mirror of the native aws_event_stream_rpc_message_flag enum, specifying rpc message-related flags
 */
public enum MessageFlags {
    ConnectionAccepted(1),
    TerminateStream(2);

    private int byteValue;

    MessageFlags(int byteValue) {
        this.byteValue = byteValue;
    }

    /**
     * @return the native enum value associated with this Java enum value
     */
    public int getByteValue() {
        return this.byteValue;
    }
}
