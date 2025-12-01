/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Test;
import static org.junit.Assert.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
        assertThrows(IllegalArgumentException.class, () -> {
            crcc.update(500000);
        });
        assertEquals(crcj.getValue(), crcc.getValue());
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
        assertThrows(IllegalArgumentException.class, () -> {
            crcc.update(500000);
        });
        assertEquals(0x5D6C4A5, crcc.getValue());
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

    @Test
    public void testCrc64NVMEZeroes() {
        byte[] zeroes = new byte[32];
        software.amazon.awssdk.crt.checksums.CRC64NVME crc64 = new software.amazon.awssdk.crt.checksums.CRC64NVME();
        crc64.update(zeroes);
        long expected = 0xCF3473434D4ECF3BL;
        assertEquals(expected, crc64.getValue());
    }

    @Test
    public void testCrc64NVMEZeroesIterated() {
        byte[] zeroes = new byte[32];
        software.amazon.awssdk.crt.checksums.CRC64NVME crc64 = new software.amazon.awssdk.crt.checksums.CRC64NVME();
        for (int i = 0; i < 32; i++) {
            crc64.update(zeroes, i, 1);
        }
        long expected = 0xCF3473434D4ECF3BL;
        assertEquals(expected, crc64.getValue());
    }

    @Test
    public void testCrc64NVMEValues() {
        byte[] values = new byte[32];
        for (byte i = 0; i < 32; i++) {
            values[i] = i;
        }
        software.amazon.awssdk.crt.checksums.CRC64NVME crc64 = new software.amazon.awssdk.crt.checksums.CRC64NVME();
        crc64.update(values);
        long expected = 0xB9D9D4A8492CBD7FL;
        assertEquals(expected, crc64.getValue());
    }

    @Test
    public void testCrc64NVMEValuesIterated() {
        byte[] values = new byte[32];
        for (byte i = 0; i < 32; i++) {
            values[i] = i;
        }
        software.amazon.awssdk.crt.checksums.CRC64NVME crc64 = new software.amazon.awssdk.crt.checksums.CRC64NVME();
        for (int i = 0; i < 32; i++) {
            crc64.update(values, i, 1);
        }
        long expected = 0xB9D9D4A8492CBD7FL;
        assertEquals(expected, crc64.getValue());
    }

    @Test
    public void testCrc64NVMEBench() throws NoSuchAlgorithmException {
        int data_size = 128  * 1024;
        int num_iter = 100;

        long crc64_oneshot_duration_sum = 0;
        long crc64_multi_duration_sum = 0;

        for (int iter = 0; iter < num_iter; ++iter) {
            long startTime2 = System.currentTimeMillis();

            software.amazon.awssdk.crt.checksums.CRC64NVME crc642 = new software.amazon.awssdk.crt.checksums.CRC64NVME();
            byte[] values2 = new byte[4000 * data_size];
            crc642.update(values2);

            long final_val2 = crc642.getValue();

            long endTime2 = System.currentTimeMillis();
            long one_shot_time = endTime2 - startTime2;
            crc64_oneshot_duration_sum += one_shot_time;


            byte[] values = new byte[data_size];
            for (int i = 0; i < data_size; i++) {
                values[i] = (byte)(i % 255);
            }

            long startTime = System.currentTimeMillis();

            software.amazon.awssdk.crt.checksums.CRC64NVME crc64 = new software.amazon.awssdk.crt.checksums.CRC64NVME();
            for (int i = 0; i < 4000; i++) {
                crc64.update(values, 0, data_size);
            }

            long final_val = crc64.getValue();

            long endTime = System.currentTimeMillis();
            long update_time = endTime - startTime;
            crc64_multi_duration_sum += update_time;

             System.out.println("crc64 iter update: " + update_time + " oneshot: " + one_shot_time);
        }

        System.out.println("crc64 oneshot avg time: " + (double)crc64_oneshot_duration_sum / num_iter);
        System.out.println("crc64 multi avg time: " + (double)crc64_multi_duration_sum / num_iter);


        /*
        long startTimeSha256 = System.currentTimeMillis();
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        for (int i = 0; i < 1_000; i++) {
            sha256.update(values, 0, data_size);
        }

        byte[] final_sha = sha256.digest();

        long endTimeSha256 = System.currentTimeMillis();
        long durationSha256 = endTimeSha256 - startTimeSha256;

        System.out.println("sha256 time: " + durationSha256);
        */
    }
}
