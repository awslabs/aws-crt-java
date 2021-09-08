/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.zip.*;
import software.amazon.awssdk.crt.checksums.*;

public class CrcTest extends CrtTestFixture {
    public CrcTest() {
    }

    @Test
    public void testCrc32Zeroes() {
        byte[] zeroes = new byte[32];
        java.util.zip.CRC32 crcj = new java.util.zip.CRC32();
        software.amazon.awssdk.crt.checksums.CRC32 crcc = new software.amazon.awssdk.crt.checksums.CRC32();
        crcj.update(zeroes);
        crcc.update(zeroes);
        int expected = 0x190A55AD;
        assertEquals(crcj.getValue(), crcc.getValue());
        assertEquals(expected, (int) crcc.getValue());
    }

    @Test
    public void testCrc32Ints() {
        java.util.zip.CRC32 crcj = new java.util.zip.CRC32();
        software.amazon.awssdk.crt.checksums.CRC32 crcc = new software.amazon.awssdk.crt.checksums.CRC32();
        crcj.update(0x000000ff);
        crcc.update(0x000000ff);
        crcj.update(234);
        crcc.update(234);
        crcj.update(160);
        crcc.update(160);
        crcj.update(0x000000ab);
        crcc.update(0x000000ab);
        assertEquals(crcj.getValue(), crcc.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCrc32ThrowsException() {
        software.amazon.awssdk.crt.checksums.CRC32 crcc = new software.amazon.awssdk.crt.checksums.CRC32();
        crcc.update(500000);
    }

    @Test
    public void testCrc32ZeroesIterated() {
        byte[] zeroes = new byte[32];
        java.util.zip.CRC32 crcj = new java.util.zip.CRC32();
        software.amazon.awssdk.crt.checksums.CRC32 crcc = new software.amazon.awssdk.crt.checksums.CRC32();
        for (int i = 0; i < 32; i++) {
            crcc.update(zeroes, i, 1);
            crcj.update(zeroes, i, 1);
        }
        int expected = 0x190A55AD;
        assertEquals(crcj.getValue(), crcc.getValue());
        assertEquals(expected, (int) crcc.getValue());
    }

    @Test
    public void testCrc32Values() {
        byte[] values = new byte[32];
        for (byte i = 0; i < 32; i++) {
            values[i] = i;
        }
        java.util.zip.CRC32 crcj = new java.util.zip.CRC32();
        software.amazon.awssdk.crt.checksums.CRC32 crcc = new software.amazon.awssdk.crt.checksums.CRC32();
        crcj.update(values);
        crcc.update(values);
        int expected = 0x91267E8A;
        assertEquals(crcj.getValue(), crcc.getValue());
        assertEquals(expected, (int) crcc.getValue());
    }

    @Test
    public void testCrc32ValuesIterated() {
        byte[] values = new byte[32];
        for (byte i = 0; i < 32; i++) {
            values[i] = i;
        }
        java.util.zip.CRC32 crcj = new java.util.zip.CRC32();
        software.amazon.awssdk.crt.checksums.CRC32 crcc = new software.amazon.awssdk.crt.checksums.CRC32();
        for (int i = 0; i < 32; i++) {
            crcc.update(values, i, 1);
            crcj.update(values, i, 1);
        }
        int expected = 0x91267E8A;
        assertEquals(crcj.getValue(), crcc.getValue());
        assertEquals(expected, (int) crcc.getValue());
    }

    @Test
    public void testCrc32LargeBuffer() {
        byte[] zeroes = new byte[25 * (1 << 20)];
        java.util.zip.CRC32 crcj = new java.util.zip.CRC32();
        software.amazon.awssdk.crt.checksums.CRC32 crcc = new software.amazon.awssdk.crt.checksums.CRC32();
        crcj.update(zeroes);
        crcc.update(zeroes);
        int expected = 0x72103906;
        assertEquals(crcj.getValue(), crcc.getValue());
        assertEquals(expected, (int) crcc.getValue());
    }

    @Test
    public void testCrc32CZeroes() {
        byte[] zeroes = new byte[32];
        software.amazon.awssdk.crt.checksums.CRC32C crcc = new software.amazon.awssdk.crt.checksums.CRC32C();
        crcc.update(zeroes);
        int expected = 0x8A9136AA;
        assertEquals(expected, (int) crcc.getValue());
    }

    @Test
    public void testCrc32CInts() {
        software.amazon.awssdk.crt.checksums.CRC32C crcc = new software.amazon.awssdk.crt.checksums.CRC32C();
        crcc.update(0x000000ff);
        crcc.update(234);
        crcc.update(160);
        crcc.update(0x000000ab);
        assertEquals(0x5D6C4A5, crcc.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCrc32CThrowsException() {
        software.amazon.awssdk.crt.checksums.CRC32C crcc = new software.amazon.awssdk.crt.checksums.CRC32C();
        crcc.update(500000);
    }

    @Test
    public void testCrc32CZeroesIterated() {
        byte[] zeroes = new byte[32];
        software.amazon.awssdk.crt.checksums.CRC32C crcc = new software.amazon.awssdk.crt.checksums.CRC32C();
        for (int i = 0; i < 32; i++) {
            crcc.update(zeroes, i, 1);
        }
        int expected = 0x8A9136AA;
        assertEquals(expected, (int) crcc.getValue());
    }

    @Test
    public void testCrc32CValues() {
        byte[] values = new byte[32];
        for (byte i = 0; i < 32; i++) {
            values[i] = i;
        }
        software.amazon.awssdk.crt.checksums.CRC32C crcc = new software.amazon.awssdk.crt.checksums.CRC32C();
        crcc.update(values);
        int expected = 0x46DD794E;
        assertEquals(expected, (int) crcc.getValue());
    }

    @Test
    public void testCrc32CValuesIterated() {
        byte[] values = new byte[32];
        for (byte i = 0; i < 32; i++) {
            values[i] = i;
        }
        software.amazon.awssdk.crt.checksums.CRC32C crcc = new software.amazon.awssdk.crt.checksums.CRC32C();
        for (int i = 0; i < 32; i++) {
            crcc.update(values, i, 1);
        }
        int expected = 0x46DD794E;
        assertEquals(expected, (int) crcc.getValue());
    }

    @Test
    public void testCrc32CLargeBuffer() {
        byte[] zeroes = new byte[25 * (1 << 20)];
        software.amazon.awssdk.crt.checksums.CRC32C crcc = new software.amazon.awssdk.crt.checksums.CRC32C();
        crcc.update(zeroes);
        int expected = 0xfb5b991d;
        assertEquals(expected, (int) crcc.getValue());
    }
}
