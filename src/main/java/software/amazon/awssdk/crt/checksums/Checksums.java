/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.checksums;

import software.amazon.awssdk.crt.CrtResource;
import java.nio.ByteBuffer;

public abstract class Checksums {

    /* java built in implementation of crc32 and crc32c returns a long, this can cause some confusion if not handled
       properly as crc32 and crc32c only result in 32 bits. be careful when mixing the built in implementation with this
       one */
    public static int crc32(byte[] input) {
        return crc32(input, 0, 0, input.length);
    }
    public static int crc32c(byte[] input) {
        return crc32c(input, 0, 0, input.length);
    }

    public static int crc32(int input, int prev) {
        byte[] buf = ByteBuffer.allocate(4).putInt(input).array();
        return crc32(buf, prev, 0, buf.length);
    }
    public static int crc32c(int input, int prev) {
        byte[] buf = ByteBuffer.allocate(4).putInt(input).array();
        return crc32c(buf, prev, 0, buf.length);
    }

    public static int crc32(int input) {
        byte[] buf = ByteBuffer.allocate(4).putInt(input).array();
        return crc32(buf, 0, 0, buf.length);
    }
    public static int crc32c(int input) {
        byte[] buf = ByteBuffer.allocate(4).putInt(input).array();
        return crc32c(buf, 0, 0, buf.length);
    }

    public static int crc32(byte[] input, int previous) {
        return crc32(input, previous, 0, input.length);
    }
    public static int crc32c(byte[] input, int previous) {
        return crc32c(input, previous, 0, input.length);
    }

    public static native int crc32(byte[] input, int previous, int offset, int length);
    public static native int crc32c(byte[] input, int previous, int offset, int length);
}
