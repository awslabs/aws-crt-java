package software.amazon.awssdk.crt.eventstream;

import software.amazon.awssdk.crt.CrtRuntimeException;

/**
 * Java mirror of the native aws_event_stream_header_value_type enum, specifying properties of
 * the type of a header's value
 */
public enum HeaderType {
    BooleanTrue(0, 0,false),
    BooleanFalse(1, 0,false),
    Byte(2, 0,false),
    Int16(3, 0,false),
    Int32(4, 0,false),
    Int64(5, 0,false),
    ByteBuf(6, 2,true),
    String(7, 2,true),
    TimeStamp(8, 0,false),
    UUID(9, 0,false);

    private int intValue;
    private boolean isVariableLength;
    private int overhead;

    HeaderType(int intValue, int overhead, boolean isVariableLength) {
        this.intValue = intValue;
        this.overhead = overhead;
        this.isVariableLength = isVariableLength;
    }

    /**
     *
     * @return additional bytes needed to serialize the header's value, beyond the value's data itself
     */
    public int getWireBytesOverhead() {
        return overhead;
    }

    /**
     *
     * @return the native integer value associated with this Java enum value
     */
    public int getEnumIntValue() {
        return intValue;
    }

    /**
     *
     * @return true if encoding this type requires a variable number of bytes, false if a fixed number of bytes
     */
    public boolean isVariableLength() {
        return isVariableLength;
    }

    /**
     * Creates a Java header type enum from an associated native integer value
     * @param intValue native integer value
     * @return a new Java header type value
     */
    public static HeaderType getValueFromInt(int intValue) {
        for (HeaderType type : HeaderType.values()) {
            if (type.intValue == intValue) {
                return type;
            }
        }

        throw new CrtRuntimeException("Invalid event-stream header int value.");
    }
}
