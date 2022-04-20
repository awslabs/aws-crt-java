/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.eventstream;

import software.amazon.awssdk.crt.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Event-stream header. This object can be represented in many types, so before using
 * the getValueAs*() functions, check the value of getHeaderType() and then decide
 * which getValueAs*() function to call based on the returned type.
 */
public class Header {
    private String headerName;
    private HeaderType headerType;
    private byte[] headerValue;

    private Header() {
    }

    /**
     * Create a header with name of boolean value
     * @param name name for the header.
     * @param value value for the header.
     * @return new Header instance
     */
    public static Header createHeader(final String name, boolean value) {
        Header header = new Header();
        checkHeaderNameLen(name);
        header.headerName = name;
        header.setValue(value);
        return header;
    }

    /**
     * Create a header with name of byte or int8 value
     * @param name name for the header
     * @param value value for the header
     * @return new Header instance
     */
    public static Header createHeader(final String name, byte value) {
        Header header = new Header();
        checkHeaderNameLen(name);
        header.headerName = name;
        header.setValue(value);
        return header;
    }

    /**
     * Create a header with name of String value
     * @param name name for the header
     * @param value value for the header
     * @return new Header instance
     */
    public static Header createHeader(final String name, final String value) {
        Header header = new Header();
        checkHeaderNameLen(name);
        header.headerName = name;
        header.setValue(value);
        return header;
    }

    /**
     * Create a header with name of short or int16 value
     * @param name name for the header
     * @param value value for the header
     * @return new Header instance
     */
    public static Header createHeader(final String name, short value) {
        Header header = new Header();
        checkHeaderNameLen(name);
        header.headerName = name;
        header.setValue(value);
        return header;
    }

    /**
     * Create a header with name of int or int32 value
     * @param name name for the header
     * @param value value for the header
     * @return new Header instance
     */
    public static Header createHeader(final String name, int value) {
        Header header = new Header();
        checkHeaderNameLen(name);
        header.headerName = name;
        header.setValue(value);
        return header;
    }

    /**
     * Create a header with name of long or int64 value
     * @param name name for the header
     * @param value value for the header
     * @return new Header instance
     */
    public static Header createHeader(final String name, long value) {
        Header header = new Header();
        checkHeaderNameLen(name);
        header.headerName = name;
        header.setValue(value);
        return header;
    }

    /**
     * Create a header with name of Date (assumed to be UTC) value
     * @param name name for the header
     * @param value value for the header
     * @return new Header instance
     */
    public static Header createHeader(final String name, final Date value) {
        Header header = new Header();
        checkHeaderNameLen(name);
        header.headerName = name;
        header.setValue(value);
        return header;
    }

    /**
     * Create a header with name of byte[] value
     * @param name name for the header
     * @param value value for the header
     * @return new Header instance
     */
    public static Header createHeader(final String name, final byte[] value) {
        Header header = new Header();
        checkHeaderNameLen(name);
        header.headerName = name;
        header.setValue(value);
        return header;
    }

    /**
     * Create a header with name of UUID value
     * @param name name for the header
     * @param value value for the header
     * @return new Header instance
     */
    public static Header createHeader(final String name, final UUID value) {
        Header header = new Header();
        checkHeaderNameLen(name);
        header.headerName = name;
        header.setValue(value);
        return header;
    }

    /**
     * Marshals buffer into a Header instance
     * @param buffer buffer to read the header data from
     * @return New instance of Header
     */
    public static Header fromByteBuffer(final ByteBuffer buffer) {
        Header header = new Header();

        int nameLen = buffer.get();
        byte[] nameBuffer = new byte[nameLen];
        buffer.get(nameBuffer);
        header.headerName = new String(nameBuffer, StandardCharsets.UTF_8);

        int type = buffer.get();
        HeaderType headerType = HeaderType.getValueFromInt(type);
        header.headerType = headerType;

        switch (headerType) {
            case BooleanFalse:
            case BooleanTrue:
                break;
            case Byte:
                header.headerValue = new byte[1];
                buffer.get(header.headerValue);
                break;
            case Int16:
                header.headerValue = new byte[2];
                buffer.get(header.headerValue);
                break;
            case Int32:
                header.headerValue = new byte[4];
                buffer.get(header.headerValue);
                break;
            case Int64:
                header.headerValue = new byte[8];
                buffer.get(header.headerValue);
                break;
            case ByteBuf:
                short bufLen = buffer.getShort();
                byte[] bufValue = new byte[bufLen];
                buffer.get(bufValue);
                header.setValue(bufValue);
                break;
            case String:
                short strLen = buffer.getShort();
                byte[] strValue = new byte[strLen];
                buffer.get(strValue);
                header.setValue(new String(strValue, StandardCharsets.UTF_8));
                break;
            case TimeStamp:
                header.headerValue = new byte[8];
                buffer.get(header.headerValue);
                break;
            case UUID:
                header.headerValue = new byte[16];
                buffer.get(header.headerValue);
                break;
            default:
                throw new CrtRuntimeException("Invalid event-stream header buffer.");
        }

        return header;
    }

    /**
     * Writes the value of this header into a buffer, using the wire representation of
     * the header.
     * @param buffer buffer to write this header into
     */
    public void writeToByteBuffer(ByteBuffer buffer) {
        buffer.put((byte)headerName.length());
        buffer.put(headerName.getBytes(StandardCharsets.UTF_8));
        buffer.put((byte)headerType.getEnumIntValue());

        if (headerType != HeaderType.BooleanFalse && headerType != HeaderType.BooleanTrue) {
            buffer.put(headerValue);
        }
    }

    /**
     * Gets the name of the header as a (UTF-8) string
     * @return utf-8 encoded string for the header name
     */
    public String getName() {
        return this.headerName;
    }

    /**
     * Gets the header type of the value.
     * @return HeaderType for this header
     */
    public HeaderType getHeaderType() {
        return this.headerType;
    }

    /**
     * Gets the value as a boolean. This assumes you've already checked getHeaderType()
     * returns BooleanTrue or BooleanFalse
     * @return the value as a boolean
     */
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

    /**
     * Gets the value as a byte or int8. This assumes you've already checked getHeaderType()
     * returns Byte
     * @return the value as a byte
     */
    public byte getValueAsByte() {
        checkType(HeaderType.Byte);
        return headerValue[0];
    }

    private void setValue(byte value) {
        headerType = HeaderType.Byte;
        headerValue = new byte[] { value};
    }

    /**
     * Gets the value as a short or int16. This assumes you've already checked getHeaderType()
     * returns Int16
     * @return the value as a short
     */
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

    /**
     * Gets the value as an int or int32. This assumes you've already checked getHeaderType()
     * returns Int32
     * @return the value as a int
     */
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

    /**
     * Gets the value as a long or int64. This assumes you've already checked getHeaderType()
     * returns Int64
     * @return the value as a long
     */
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

    /**
     * Gets the value as a Date. This assumes you've already checked getHeaderType()
     * returns TimeStamp
     * @return the value as a Date
     */
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

    /**
     * Gets the value as a byte[]. This assumes you've already checked getHeaderType()
     * returns ByteBuf
     * @return the value as a byte[]
     */
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

    /**
     * Gets the value as a utf-8 encoded string.
     * This assumes you've already checked getHeaderType()
     * returns String
     * @return the value as a utf-8 encoded string
     */
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

    /**
     * Gets the value as a UUID. This assumes you've already checked getHeaderType()
     * returns UUID
     * @return the value as a UUID
     */
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

    /**
     * @return the total binary wire representation length of this header.
     */
    public int getTotalByteLength() {
        // name len (1 byte) + header name + type (1 byte)
        int length = 1 + headerName.length() + 1;
        // optional variable length specifier
        length += headerValue != null ? headerValue.length : 0;
        return length;
    }

    /**
     * Marshals a list of headers into a usable headers block for an event-stream message.
     * Used for sending headers across the JNI boundary more efficiently
     * @param headers list of headers to write to the headers block
     * @return a byte[] that matches the event-stream header-block format.
     */
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
        if (headerName.length() >= Byte.MAX_VALUE) {
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
