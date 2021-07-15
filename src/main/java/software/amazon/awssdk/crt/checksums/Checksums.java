/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.checksums;

import software.amazon.awssdk.crt.CrtResource;

public abstract class Checksums {

    /* java built in implementation of crc32 and crc32c returns a long, this can cause some confusion if not handled
       properly as crc32 and crc32c only result in 32 bits. be careful when mixing the built in implementation with this
       one */
    public static int crc32(byte[] input) {
        return crc32(input, 0);
    }
    public static int crc32c(byte[] input) {
        return crc32c(input, 0);
    }

    public static native int crc32(byte[] input, int previous);
    public static native int crc32c(byte[] input, int previous);
}
