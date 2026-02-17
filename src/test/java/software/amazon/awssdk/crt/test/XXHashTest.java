/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Test;
import static org.junit.Assert.*;
import software.amazon.awssdk.crt.checksums.XXHash;

public class XXHashTest extends CrtTestFixture {
    public XXHashTest() {
    }

    @Test
    public void testXXHash64Piping() {
        byte[] input = "Hello world".getBytes();

        byte[] out = XXHash.computeXXHash64(input);

        byte[] expected = {(byte)0xc5, (byte)0x00, (byte)0xb0, (byte)0xc9, (byte)0x12, (byte)0xb3, (byte)0x76, (byte)0xd8};

        assertArrayEquals(out, expected);

        byte[] input2 = "llo world".getBytes();
        try(XXHash hash = XXHash.newXXHash64()) {
            hash.update('H'.getBytes(););
            hash.update(('e'.getBytes());
            hash.update(input2);
            byte[] out2 = hash.digest();

            assertArrayEquals(out2, expected);
        }
    }

    @Test
    public void testXXHash3_64Piping() {
        byte[] input = "Hello world".getBytes();

        byte[] out = XXHash.computeXXHash3_64(input);

        byte[] expected = {(byte)0xb6, (byte)0xac, (byte)0xb9, (byte)0xd8, (byte)0x4a, (byte)0x38, (byte)0xff, (byte)0x74};

        assertArrayEquals(out, expected);

        try(XXHash hash = XXHash.newXXHash3_64()) {
            hash.update(input, 0, 11);
            byte[] out2 = hash.digest();

            assertArrayEquals(out2, expected);
        }
    }

    @Test
    public void testXXHash3_128Piping() {
        byte[] input = "Hello world".getBytes();

        byte[] out = XXHash.computeXXHash3_128(input);

        byte[] expected = {(byte)0x73, (byte)0x51, (byte)0xf8, (byte)0x98, (byte)0x12, (byte)0xf9, (byte)0x73, (byte)0x82, 
            (byte)0xb9, (byte)0x1d, (byte)0x05, (byte)0xb3, (byte)0x1e, (byte)0x04, (byte)0xdd, (byte)0x7f};

        assertArrayEquals(out, expected);

        try(XXHash hash = XXHash.newXXHash3_128()) {
            hash.update(input);
            byte[] out2 = hash.digest();

            assertArrayEquals(out2, expected);
        }
    }
}
