package software.amazon.awssdk.crt.test;

import org.junit.Test;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.eventstream.Header;
import software.amazon.awssdk.crt.eventstream.HeaderType;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.*;

public class EventStreamHeaderTest extends CrtTestFixture {
    public EventStreamHeaderTest() {}

    @Test
    public void testStringHeader() {
        Header strHeader = Header.createHeader("testHeader", "testValue");
        assertEquals("testValue", strHeader.getValueAsString());
        assertEquals("testHeader", strHeader.getName());
        assertEquals(HeaderType.String, strHeader.getHeaderType());
    }

    @Test
    public void testStringHeaderSerialization() {
        Header header = Header.createHeader("testHeader", "testValue");
        ByteBuffer serializedBuffer = ByteBuffer.allocate(1024);
        header.writeToByteBuffer(serializedBuffer);
        serializedBuffer.position(0);
        Header deserializedHeader = Header.fromByteBuffer(serializedBuffer);
        assertEquals(header, deserializedHeader);
    }

    @Test(expected = CrtRuntimeException.class)
    public void testStringHeaderValueTooLong() {
        StringBuilder testValue = new StringBuilder();

        while(testValue.length() < Short.MAX_VALUE + 1) {
            testValue.append("a");
        }

        Header.createHeader("testHeader", testValue.toString());
    }

    @Test
    public void testBinaryHeader() {
        byte[] testValue = new byte[] {0,1,2,3,4,5,6,7,8,9,10};
        Header binHeader = Header.createHeader("testHeader", testValue);
        assertArrayEquals(testValue, binHeader.getValueAsBytes());
        assertEquals("testHeader", binHeader.getName());
        assertEquals(HeaderType.ByteBuf, binHeader.getHeaderType());
    }

    @Test
    public void testBinaryHeaderSerialization() {
        byte[] testValue = new byte[] {0,1,2,3,4,5,6,7,8,9,10};
        Header header = Header.createHeader("testHeader", testValue);
        ByteBuffer serializedBuffer = ByteBuffer.allocate(1024);
        header.writeToByteBuffer(serializedBuffer);
        serializedBuffer.position(0);
        Header deserializedHeader = Header.fromByteBuffer(serializedBuffer);
        assertEquals(header, deserializedHeader);
    }

    @Test(expected = CrtRuntimeException.class)
    public void testBinaryHeaderValueTooLong() {
        ByteArrayOutputStream testValue = new ByteArrayOutputStream();

        int written = 0;
        while(written < Short.MAX_VALUE + 1) {
            testValue.write(8);
            written++;
        }

        Header strHeader = Header.createHeader("testHeader", testValue.toByteArray());
    }

    @Test
    public void testBooleanHeader() {
        Header boolHeader = Header.createHeader("testHeader", false);
        assertFalse(boolHeader.getValueAsBoolean());
        assertEquals("testHeader", boolHeader.getName());
        assertEquals(HeaderType.BooleanFalse, boolHeader.getHeaderType());

        boolHeader = Header.createHeader("testHeader", true);
        assertTrue(boolHeader.getValueAsBoolean());
        assertEquals("testHeader", boolHeader.getName());
        assertEquals(HeaderType.BooleanTrue, boolHeader.getHeaderType());
    }

    @Test
    public void testBooleanHeaderSerialization() {
        Header header = Header.createHeader("testHeader", false);
        ByteBuffer serializedBuffer = ByteBuffer.allocate(1024);
        header.writeToByteBuffer(serializedBuffer);
        serializedBuffer.position(0);
        Header deserializedHeader = Header.fromByteBuffer(serializedBuffer);
        assertEquals(header, deserializedHeader);

        header = Header.createHeader("testHeader", true);
        serializedBuffer = ByteBuffer.allocate(1024);
        header.writeToByteBuffer(serializedBuffer);
        serializedBuffer.position(0);
        deserializedHeader = Header.fromByteBuffer(serializedBuffer);
        assertEquals(header, deserializedHeader);
    }

    @Test
    public void testByteHeader() {
        Header byteHeader = Header.createHeader("testHeader", (byte)127);
        assertEquals(127, byteHeader.getValueAsByte());
        assertEquals("testHeader", byteHeader.getName());
        assertEquals(HeaderType.Byte, byteHeader.getHeaderType());
    }

    @Test
    public void testByteHeaderSerialization() {
        Header header = Header.createHeader("testHeader", (byte)127);
        ByteBuffer serializedBuffer = ByteBuffer.allocate(1024);
        header.writeToByteBuffer(serializedBuffer);
        serializedBuffer.position(0);
        Header deserializedHeader = Header.fromByteBuffer(serializedBuffer);
        assertEquals(header, deserializedHeader);
    }

    @Test
    public void testShortHeader() {
        Header shortHeader = Header.createHeader("testHeader", (short)8001);
        assertEquals(8001, shortHeader.getValueAsShort());
        assertEquals("testHeader", shortHeader.getName());
        assertEquals(HeaderType.Int16, shortHeader.getHeaderType());
    }

    @Test
    public void testShortHeaderSerialization() {
        Header header = Header.createHeader("testHeader", (short)8001);
        ByteBuffer serializedBuffer = ByteBuffer.allocate(1024);
        header.writeToByteBuffer(serializedBuffer);
        serializedBuffer.position(0);
        Header deserializedHeader = Header.fromByteBuffer(serializedBuffer);
        assertEquals(header, deserializedHeader);
    }

    @Test
    public void testIntHeader() {
        Header intHeader = Header.createHeader("testHeader", 123456789);
        assertEquals(123456789, intHeader.getValueAsInt());
        assertEquals("testHeader", intHeader.getName());
        assertEquals(HeaderType.Int32, intHeader.getHeaderType());
    }

    @Test
    public void testIntHeaderSerialization() {
        Header header = Header.createHeader("testHeader", 123456789);
        ByteBuffer serializedBuffer = ByteBuffer.allocate(1024);
        header.writeToByteBuffer(serializedBuffer);
        serializedBuffer.position(0);
        Header deserializedHeader = Header.fromByteBuffer(serializedBuffer);
        assertEquals(header, deserializedHeader);
    }

    @Test
    public void testLongHeader() {
        Header longHeader = Header.createHeader("testHeader", (long)Integer.MAX_VALUE + 1);
        assertEquals((long)Integer.MAX_VALUE + 1, longHeader.getValueAsLong());
        assertEquals("testHeader", longHeader.getName());
        assertEquals(HeaderType.Int64, longHeader.getHeaderType());
    }

    @Test
    public void testLongHeaderSerialization() {
        Header header = Header.createHeader("testHeader", (long)Integer.MAX_VALUE + 1);
        ByteBuffer serializedBuffer = ByteBuffer.allocate(1024);
        header.writeToByteBuffer(serializedBuffer);
        serializedBuffer.position(0);
        Header deserializedHeader = Header.fromByteBuffer(serializedBuffer);
        assertEquals(header, deserializedHeader);
    }

    @Test
    public void testDateHeader() {
        Date headerValue = new Date();
        Header dateHeader = Header.createHeader("testHeader", headerValue);
        assertEquals(headerValue, dateHeader.getValueAsTimestamp());
        assertEquals("testHeader", dateHeader.getName());
        assertEquals(HeaderType.TimeStamp, dateHeader.getHeaderType());
    }

    @Test
    public void testDateHeaderSerialization() {
        Date headerValue = new Date();
        Header header = Header.createHeader("testHeader", headerValue);
        ByteBuffer serializedBuffer = ByteBuffer.allocate(1024);
        header.writeToByteBuffer(serializedBuffer);
        serializedBuffer.position(0);
        Header deserializedHeader = Header.fromByteBuffer(serializedBuffer);
        assertEquals(header, deserializedHeader);
    }

    @Test
    public void testUUIDHeader() {
        UUID headerValue = UUID.randomUUID();
        Header uuidHeader = Header.createHeader("testHeader", headerValue);
        assertEquals(headerValue, uuidHeader.getValueAsUUID());
        assertEquals("testHeader", uuidHeader.getName());
        assertEquals(HeaderType.UUID, uuidHeader.getHeaderType());
    }

    @Test
    public void testUUIDHeaderSerialization() {
        UUID headerValue = UUID.randomUUID();
        Header header = Header.createHeader("testHeader", headerValue);
        ByteBuffer serializedBuffer = ByteBuffer.allocate(1024);
        header.writeToByteBuffer(serializedBuffer);
        serializedBuffer.position(0);
        Header deserializedHeader = Header.fromByteBuffer(serializedBuffer);
        assertEquals(header, deserializedHeader);
    }

    @Test(expected = CrtRuntimeException.class)
    public void testHeaderNameTooLong() {
        StringBuilder headerName = new StringBuilder();

        while(headerName.length() < Byte.MAX_VALUE + 1) {
            headerName.append("a");
        }

        Header.createHeader(headerName.toString(), "this should fail");
    }
}
