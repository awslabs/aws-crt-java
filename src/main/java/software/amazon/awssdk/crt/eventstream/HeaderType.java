package software.amazon.awssdk.crt.eventstream;

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

    public int getWireBytesOverhead() {
        return overhead;
    }

    public int getEnumIntValue() {
        return intValue;
    }

    public boolean isVariableLength() {
        return isVariableLength;
    }
}
