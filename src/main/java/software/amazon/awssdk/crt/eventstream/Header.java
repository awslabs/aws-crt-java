package software.amazon.awssdk.crt.eventstream;

import software.amazon.awssdk.crt.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Header {
    private String headerName;
    private HeaderType headerType;
    private byte[] headerValue;

    private Header() {
    }

    public static Header createHeader(final String name, boolean value) {
        Header header = new Header();
        checkHeaderNameLen(name);
        header.headerName = name;
        header.setValue(value);
        return header;
    }

    public static Header createHeader(final String name, byte value) {
        Header header = new Header();
        checkHeaderNameLen(name);
        header.headerName = name;
        header.setValue(value);
        return header;
    }

    public static Header createHeader(final String name, final String value) {
        Header header = new Header();
        checkHeaderNameLen(name);
        header.headerName = name;
        header.setValue(value);
        return header;
    }

    public static Header createHeader(final String name, short value) {
        Header header = new Header();
        checkHeaderNameLen(name);
        header.headerName = name;
        header.setValue(value);
        return header;
    }

    public static Header createHeader(final String name, int value) {
        Header header = new Header();
        checkHeaderNameLen(name);
        header.headerName = name;
        header.setValue(value);
        return header;
    }

    public static Header createHeader(final String name, long value) {
        Header header = new Header();
        checkHeaderNameLen(name);
        header.headerName = name;
        header.setValue(value);
        return header;
    }

    public static Header createHeader(final String name, final Date value) {
        Header header = new Header();
        checkHeaderNameLen(name);
        header.headerName = name;
        header.setValue(value);
        return header;
    }

    public static Header createHeader(final String name, final byte[] value) {
        Header header = new Header();
        checkHeaderNameLen(name);
        header.headerName = name;
        header.setValue(value);
        return header;
    }

    public static Header createHeader(final String name, final UUID value) {
        Header header = new Header();
        checkHeaderNameLen(name);
        header.headerName = name;
        header.setValue(value);
        return header;
    }

    public static Header fromByteBuffer(final ByteBuffer buffer) {
        Header header = new Header();

        int nameLen = buffer.get();
        byte[] nameBuffer = new byte[nameLen];
        buffer.get(nameBuffer);
        header.headerName = new String(nameBuffer, StandardCharsets.UTF_8);

        int type = buffer.get();

        switch (type) {
            case 0:
                header.headerType = HeaderType.BooleanTrue;
                break;
            case 1:
                header.headerType = HeaderType.BooleanFalse;
                break;
            case 2:
                header.headerType = HeaderType.Byte;
                header.headerValue = new byte[1];
                buffer.get(header.headerValue);
                break;
            case 3:
                header.headerType = HeaderType.Int16;
                header.headerValue = new byte[2];
                buffer.get(header.headerValue);
                break;
            case 4:
                header.headerType = HeaderType.Int32;
                header.headerValue = new byte[4];
                buffer.get(header.headerValue);
                break;
            case 5:
                header.headerType = HeaderType.Int64;
                header.headerValue = new byte[8];
                buffer.get(header.headerValue);
                break;
            case 6:
                short bufLen = buffer.getShort();
                byte[] bufValue = new byte[bufLen];
                buffer.get(bufValue);
                header.setValue(bufValue);
                break;
            case 7:
                short strLen = buffer.getShort();
                byte[] strValue = new byte[strLen];
                buffer.get(strValue);
                header.setValue(new String(strValue, StandardCharsets.UTF_8));
                break;
            case 8:
                header.headerType = HeaderType.TimeStamp;
                header.headerValue = new byte[8];
                buffer.get(header.headerValue);
                break;
            case 9:
                header.headerType = HeaderType.UUID;
                header.headerValue = new byte[16];
                buffer.get(header.headerValue);
                break;
            default:
                throw new CrtRuntimeException("Invalid event-stream header buffer.");
        }

        return header;
    }

    public void writeToByteBuffer(ByteBuffer buffer) {
        buffer.put((byte)headerName.length());
        buffer.put(headerName.getBytes(StandardCharsets.UTF_8));
        buffer.put((byte)headerType.getEnumIntValue());

        if (headerType != HeaderType.BooleanFalse && headerType != HeaderType.BooleanTrue) {
            buffer.put(headerValue);
        }
    }

    public String getName() {
        return this.headerName;
    }

    public HeaderType getHeaderType() {
        return this.headerType;
    }

    public boolean getValueAsBoolean() {
        if (!(headerType == HeaderType.BooleanTrue || headerType == HeaderType.BooleanFalse)) {
            throw new CrtRuntimeException("Invalid Event-stream header type");
        }

        return headerType == HeaderType.BooleanTrue;
    }

    private void setValue(boolean value) {
        if (value) {
            this.headerType = HeaderType.BooleanTrue;
        } else {
            this.headerType = HeaderType.BooleanFalse;
        }
    }

    public byte getValueAsByte() {
        checkType(HeaderType.Byte);
        return headerValue[0];
    }

    private void setValue(byte value) {
        headerType = HeaderType.Byte;
        headerValue = new byte[] { value};
    }

    public short getValueAsShort() {
        checkType(HeaderType.Int16);
        ByteBuffer valueBuffer = ByteBuffer.wrap(headerValue);
        return valueBuffer.getShort();
    }

    private void setValue(short value) {
        headerType = HeaderType.Int16;
        ByteBuffer valueBuffer = ByteBuffer.allocate(2);
        valueBuffer.putShort(value);
        headerValue = valueBuffer.array();
    }

    public int getValueAsInt() {
        checkType(HeaderType.Int32);
        ByteBuffer valueBuffer = ByteBuffer.wrap(headerValue);
        return valueBuffer.getInt();
    }

    private void setValue(int value) {
        headerType = HeaderType.Int32;
        ByteBuffer valueBuffer = ByteBuffer.allocate(4);
        valueBuffer.putInt(value);
        headerValue = valueBuffer.array();
    }

    public long getValueAsLong() {
        checkType(HeaderType.Int64);
        ByteBuffer valueBuffer = ByteBuffer.wrap(headerValue);
        return valueBuffer.getLong();
    }

    private void setValue(long value) {
        headerType = HeaderType.Int64;
        ByteBuffer valueBuffer = ByteBuffer.allocate(8);
        valueBuffer.putLong(value);
        headerValue = valueBuffer.array();
    }

    public Date getValueAsTimestamp() {
        checkType(HeaderType.TimeStamp);
        ByteBuffer valueBuffer = ByteBuffer.wrap(headerValue);
        return new Date(valueBuffer.getLong());
    }

    private void setValue(Date value) {
        headerType = HeaderType.TimeStamp;
        ByteBuffer valueBuffer = ByteBuffer.allocate(8);
        valueBuffer.putLong(value.getTime());
        headerValue = valueBuffer.array();
    }

    public byte[] getValueAsBytes() {
        checkType(HeaderType.ByteBuf);
        ByteBuffer valueBuffer = ByteBuffer.wrap(headerValue);
        short len = valueBuffer.getShort();
        byte[] bufferVal = new byte[len];
        valueBuffer.get(bufferVal);
        return bufferVal;
    }

    private void setValue(final byte[] value) {
        if (value.length > Short.MAX_VALUE) {
            throw new CrtRuntimeException("The max length for a ByteBuf Header value is Short.MAX_VALUE");
        }

        headerType = HeaderType.ByteBuf;
        ByteBuffer valueBuffer = ByteBuffer.allocate(headerType.getWireBytesOverhead() + value.length);
        valueBuffer.putShort((short)value.length);
        valueBuffer.put(value);
        headerValue = valueBuffer.array();
    }

    public String getValueAsString() {
        checkType(HeaderType.String);
        ByteBuffer valueBuffer = ByteBuffer.wrap(headerValue);
        short len = valueBuffer.getShort();
        byte[] bufferVal = new byte[len];
        valueBuffer.get(bufferVal);
        return new String(bufferVal, StandardCharsets.UTF_8);
    }

    private void setValue(final String value) {
        if (value.length() > Short.MAX_VALUE) {
            throw new CrtRuntimeException("The max length for a String Header value is Short.MAX_VALUE");
        }

        headerType = HeaderType.String;
        ByteBuffer valueBuffer = ByteBuffer.allocate(headerType.getWireBytesOverhead() + value.length());
        valueBuffer.putShort((short)value.length());
        valueBuffer.put(value.getBytes(StandardCharsets.UTF_8));
        headerValue = valueBuffer.array();
    }

    public UUID getValueAsUUID() {
        checkType(HeaderType.UUID);

        // I straight up stole this from the private constructor for UUID.
        // A POX on whomever made it private.
        long msb = 0;
        long lsb = 0;

        int i;
        for(i = 0; i < 8; ++i) {
            msb = msb << 8 | (long)(headerValue[i] & 255);
        }

        for(i = 8; i < 16; ++i) {
            lsb = lsb << 8 | (long)(headerValue[i] & 255);
        }
        return new UUID(msb, lsb);
    }

    private void setValue(final UUID value) {
        headerType = HeaderType.UUID;
        ByteBuffer valueBuffer = ByteBuffer.allocate(16);
        valueBuffer.putLong(value.getMostSignificantBits());
        valueBuffer.putLong(value.getLeastSignificantBits());
        headerValue = valueBuffer.array();
    }

    public int getTotalByteLength() {
        int length = 1 + headerName.length() + 1;
        length += headerType.getWireBytesOverhead();
        length += headerValue != null ? headerValue.length : 0;
        return length;
    }

    public static byte[] marshallHeadersForJNI(List<Header> headers) {
        int totalWireLength = 0;
        for(Header header: headers) {
            totalWireLength += header.getTotalByteLength();
        }

        byte[] marshalledData = new byte[totalWireLength];
        ByteBuffer marshalledBuf = ByteBuffer.wrap(marshalledData);

        for(Header header: headers) {
            header.writeToByteBuffer(marshalledBuf);
        }

        return marshalledData;
    }

    private void checkType(HeaderType headerType) {
        if (this.headerType != headerType) {
            throw new CrtRuntimeException("Invalid Event-stream header type");
        }
    }

    private static void checkHeaderNameLen(final String headerName) {
        if (headerName.length() > Byte.MAX_VALUE) {
            throw new CrtRuntimeException("Header name must be less than 127 bytes.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Header header = (Header) o;
        return headerName.equals(header.headerName) &&
                headerType == header.headerType &&
                Arrays.equals(headerValue, header.headerValue);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(headerName, headerType);
        result = 31 * result + Arrays.hashCode(headerValue);
        return result;
    }
}
