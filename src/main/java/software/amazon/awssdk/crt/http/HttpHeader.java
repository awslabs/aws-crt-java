/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.http;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HttpHeader {
    private final static int BUFFER_INT_SIZE = 4;
    private final static Charset UTF8 = StandardCharsets.UTF_8;
    private byte[] name; /* Not final, Native will manually set name after calling empty Constructor. */
    private byte[] value; /* Not final, Native will manually set value after calling empty Constructor. */

    /**
     * Called by Native to create a new HttpHeader. This is so that Native doesn't
     * have to worry about UTF8 encoding/decoding issues. The user thread will deal
     * with them when they call getName() or getValue()
     **/
    private HttpHeader() {
    }

    public HttpHeader(String name, String value) {
        this.name = name.getBytes(UTF8);
        this.value = value.getBytes(UTF8);
    }

    public HttpHeader(byte[] name, byte[] value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        if (name == null) {
            return "";
        }
        return new String(name, UTF8);
    }

    public byte[] getNameBytes() {
        return name;
    }

    public String getValue() {
        if (value == null) {
            return "";
        }
        return new String(value, UTF8);
    }

    public byte[] getValueBytes() {
        return value;
    }

    @Override
    public String toString() {
        return getName() + ":" + getValue();
    }

    /**
     * Each header is marshalled as [4-bytes BE name length] [variable length name
     * value] [4-bytes BE value length] [variable length value value]
     * 
     * @param headersBlob Blob of encoded headers
     * @return array of decoded headers
     */
    public static List<HttpHeader> loadHeadersListFromMarshalledHeadersBlob(ByteBuffer headersBlob) {
        List<HttpHeader> headers = new ArrayList<>(16);

        while (headersBlob.hasRemaining()) {
            int nameLen = headersBlob.getInt();

            // we want to protect against 0 length header names, 0 length values are fine.
            // the marshalling layer will make sure that even if a length is 0, the 0 will
            // still be stored in the byte array.
            if (nameLen > 0) {
                byte[] nameBuf = new byte[nameLen];
                headersBlob.get(nameBuf);
                int valLen = headersBlob.getInt();
                byte[] valueBuf = new byte[valLen];
                headersBlob.get(valueBuf);
                headers.add(new HttpHeader(nameBuf, valueBuf));
            }
        }

        return headers;
    }

    /**
     * Lists of headers are marshalled as follows:
     *
     * each string field is: [4-bytes BE] [variable length bytes specified by the
     * previous field]
     *
     * [header name-value pairs]
     * 
     * @return encoded blob of headers
     */

    public static byte[] marshalHeadersForJni(List<HttpHeader> headers) {
        int size = 0;
        size += (BUFFER_INT_SIZE * 2) * headers.size();

        for (HttpHeader header : headers) {
            if (header.getNameBytes().length > 0) {
                size += header.getNameBytes().length + header.getValueBytes().length;
            }
        }

        ByteBuffer buffer = ByteBuffer.allocate(size);
        ;
        for (HttpHeader header : headers) {
            if (header.getNameBytes().length > 0) {
                buffer.putInt(header.getNameBytes().length);
                buffer.put(header.getNameBytes());
                buffer.putInt(header.getValueBytes().length);
                buffer.put(header.getValueBytes());
            }
        }

        return buffer.array();
    }

    /**
     * @param headersBlob encoded headers blob
     * @return array of headers
     * @see #loadHeadersListFromMarshalledHeadersBlob
     */
    public static HttpHeader[] loadHeadersFromMarshalledHeadersBlob(ByteBuffer headersBlob) {
        List<HttpHeader> headers = loadHeadersListFromMarshalledHeadersBlob(headersBlob);
        HttpHeader[] headersArray = new HttpHeader[headers.size()];
        return headers.toArray(headersArray);
    }
}
