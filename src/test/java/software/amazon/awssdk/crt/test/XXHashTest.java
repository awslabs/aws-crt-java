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
        byte[] input = "Hello world";

        byte[] out = XXHash.computeXXHash64(input);

        byte[] expected = {0xc5, 0x00, 0xb0, 0xc9, 0x12, 0xb3, 0x76, 0xd8};

        assertEquals(out, expected);

        XXHash hash = XXHash.newXXHash64();
        hash.update(input);
        byte[] out2 = hash.finalize();

        assertEquals(out2, expected);
    }

    @Test
    public void testXXHash3_64Piping() {
        byte[] input = "Hello world";

        byte[] out = XXHash.computeXXHash3_64(input);

        byte[] expected = {0xb6, 0xac, 0xb9, 0xd8, 0x4a, 0x38, 0xff, 0x74};

        assertEquals(out, expected);

        XXHash hash = XXHash.newXXHash3_64();
        hash.update(input);
        byte[] out2 = hash.finalize();

        assertEquals(out2, expected);
    }

    @Test
    public void testXXHash3_128Piping() {
        byte[] input = "Hello world";

        byte[] out = XXHash.computeXXHash3_128(input);

        byte[] expected = {0x73, 0x51, 0xf8, 0x98, 0x12, 0xf9, 0x73, 0x82, 
            0xb9, 0x1d, 0x05, 0xb3, 0x1e, 0x04, 0xdd, 0x7f};

        assertEquals(out, expected);

        XXHash hash = XXHash.newXXHash3_128();
        hash.update(input);
        byte[] out2 = hash.finalize();

        assertEquals(out2, expected);
    }
}
