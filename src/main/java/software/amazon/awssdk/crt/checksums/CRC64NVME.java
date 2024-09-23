/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.checksums;

import software.amazon.awssdk.crt.CRT;
import java.util.zip.Checksum;

/**
 * CRT implementation of the Java Checksum interface for making CRC64NVME checksum calculations
 */
public class CRC64NVME implements Checksum, Cloneable {
    static {
        new CRT();
    };

    private long value = 0;

    /**
     * Default constructor
     */
    public CRC64NVME() {
    }

    private CRC64NVME(int value) {
        this.value = value;
    }

    @Override
    public Object clone() {
        return new CRC64NVME(value);
    }

    /**
     * Returns the current checksum value.
     *
     * @return the current checksum value.
     */
    @Override
    public long getValue() {
        return value;
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
        value = crc64nvme(b, value, off, len);
    }

    /**
     * Updates the current checksum with the specified array of bytes.
     *
     * @param b the byte array to update the checksum with
     */
    public void update(byte[] b) {
        update(b, 0, b.length);
    }

    /**
     * Updates the current checksum with the specified byte.
     *
     * @param b the byte to update the checksum with
     */
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
    private static native long crc64nvme(byte[] input, long previous, int offset, int length);;
}
