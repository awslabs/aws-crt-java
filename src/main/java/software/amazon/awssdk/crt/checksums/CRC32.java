/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.checksums;

import software.amazon.awssdk.crt.CrtResource;
import java.nio.ByteBuffer;
import java.util.zip.Checksum;

public class CRC32 implements Checksum, Clone{

    private int value = 0;

    public CRC32() {
    }

    private CRC32(int value) {
        this.value = value;
    }

    @Override
    public Object Clone() {
        return new CRC32(value);
    }

    @Override
    public long getValue() {
        return (long)value & 0xffffffffL;
    }

    public void reset() {
        value = 0;
    }

    public void update(byte[] b, int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        }
        if (off < 0 || len < 0 || off > b.length - len) {
            throw new ArrayIndexOutOfBoundsException();
        }
        value = crc32(b, value, off, len);
    }

    public void update(byte[] b) {
        value = crc32(b, value, 0, b.length);
    }

    public void update(int b) {
        byte[] buf = {(byte)(b & 0x000000ff)};
        this.update(buf);
    }

    private static native int crc32(byte[] input, int previous, int offset, int length);;
}
