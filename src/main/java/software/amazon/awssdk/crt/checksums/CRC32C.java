/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.checksums;

import software.amazon.awssdk.crt.CRT;
import java.util.zip.Checksum;

/**
 * CRT implementation of the Java Checksum interface for making Crc32c checksum calculations
 */
public class CRC32C implements Checksum, Cloneable {
    static {
        new CRT();
    };
    private int value = 0;

    /**
     * Default constructor
     */
    public CRC32C() {
    }

    private CRC32C(int value) {
        this.value = value;
    }

    @Override
    public Object clone() {
        return new CRC32C(value);
    }

    /**
     * Returns the current checksum value.
     *
     * @return the current checksum value.
     */
    @Override
    public long getValue() {
        return (long) value & 0xffffffffL;
    }

    /**
     * Resets the checksum to its initial value.
     */
    @Override
    public void reset() {
        value = 0;
    }

    /**
     * Updates the current checksum with the specified array of bytes.
     *
     * @param b the byte array to update the checksum with
     * @param off the starting offset within b of the data to use
     * @param len the number of bytes to use in the update
     */
    @Override
    public void update(byte[] b, int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        }
        if (off < 0 || len < 0 || off > b.length - len) {
            throw new ArrayIndexOutOfBoundsException();
        }
        value = crc32c(b, value, off, len);
    }

    public void update(byte[] b) {
        value = crc32c(b, value, 0, b.length);
    }

    @Override
    public void update(int b) {
        if (b < 0 || b > 0xff) {
            throw new IllegalArgumentException();
        }
        byte[] buf = { (byte) (b & 0x000000ff) };
        this.update(buf);
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native int crc32c(byte[] input, int previous, int offset, int length);
}
