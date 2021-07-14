/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Assume;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.zip.*;
import software.amazon.awssdk.crt.checksums.*;

public class CrcTest extends CrtTestFixture {
    public CrcTest() {}

    @Test
    public void testCrc32Zeroes() {
        byte[] zeroes = new byte[32];
        int res = Crc.crc32(zeroes);
        int expected = 0x190A55AD;
        assertEquals(expected, res);
    }
    @Test
    public void testCrc32ZeroesIterated() {
        int res = 0;
        for (int i = 0; i < 32; i++) {
            res = Crc.crc32(new byte[1], res);
        }
        int expected = 0x190A55AD;
        assertEquals(expected, res);
    }
    @Test
    public void testCrc32Values() {
        byte[] values = new byte[32];
        for (byte i = 0; i < 32; i++) {
            values[i] = i;
        }
        int res = Crc.crc32(values);
        int expected = 0x91267E8A;
        assertEquals(expected, res);
    }
    @Test
    public void testCrc32ValuesIterated() {
        int res = 0;
        for (byte i = 0; i < 32; i++) {
            byte[] buf = {i};
            res = Crc.crc32(buf, res);
        }
        int expected = 0x91267E8A;
        assertEquals(expected, res);
    }
    @Test
    public void testCrc32LargeBuffer() {
        byte[] zeroes = new byte[25 * (1 << 20)];
        int res = Crc.crc32(zeroes);
        int expected = 0x72103906;
        assertEquals(expected, res);
    }
    @Test
    public void testCrc32cZeroes() {
        byte[] zeroes = new byte[32];
        int res = Crc.crc32c(zeroes);
        int expected = 0x8A9136AA;
        assertEquals(expected, res);
    }
    @Test
    public void testCrc32cZeroesIterated() {
        int res = 0;
        for (int i = 0; i < 32; i++) {
            res = Crc.crc32c(new byte[1], res);
        }
        int expected = 0x8A9136AA;
        assertEquals(expected, res);
    }
    @Test
    public void testCrc32cValues() {
        byte[] values = new byte[32];
        for (byte i = 0; i < 32; i++) {
            values[i] = i;
        }
        int res = Crc.crc32c(values);
        int expected = 0x46DD794E;
        assertEquals(expected, res);
    }
    @Test
    public void testCrc32cValuesIterated() {
        int res = 0;
        for (byte i = 0; i < 32; i++) {
            byte[] buf = {i};
            res = Crc.crc32c(buf, res);
        }
        int expected = 0x46DD794E;
        assertEquals(expected, res);
    }
    @Test
    public void testCrc32cLargeBuffer() {
        byte[] zeroes = new byte[25 * (1 << 20)];
        int res = Crc.crc32c(zeroes);
        int expected = 0xfb5b991d;
        assertEquals(expected, res);
    }
}