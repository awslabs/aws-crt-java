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
import java.util.Random;

public class CrcBenchmark extends CrtTestFixture {
    public CrcBenchmark() {}

    @Test
    public void crcBenchmark() {
        // Checksum crcc = new software.amazon.awssdk.crt.checksums.CRC32C();
        java.util.zip.CRC32 crcc = new java.util.zip.CRC32();
        System.out.println("crc32");
        int[] chunk_sizes = new int[] {1 << 22, 1 << 20, 1 << 10, 1 << 9, 1 << 8, 1 << 7};
        profile(1 << 22, chunk_sizes, 10, 1,  crcc);
        // int[] chunk_sizes = new int[] {1 << 19, 1 << 15, 1 << 10, 1 << 9, 1 << 8, 1 << 7};
        // profile(1 << 19, chunk_sizes, 10, 1,  crcc);
        assertEquals(1, 1);
    }

    private double[] update_summary(long count, double mean, double M2, double my_min, double my_max, double new_value) {
        double delta = new_value - mean;
        mean += delta / count;
        double delta2 = new_value - mean;
        M2 += delta * delta2;
        my_min = Math.min(my_min, new_value);
        my_max = Math.max(my_max, new_value);
        return new double[] {mean, M2, my_min, my_max};
    }
    
    private double finalize_summary(int count, double M2) {
        return M2 / count;
    }
    
    private void print_stats(double[] means, double [] variances, double[] mins, double[] maxs, int[] chunk_sizes){
        for (int i = 0; i < means.length; i++){
            System.out.println(String.format("chunk size: %d, min: %,.2f, max: %,.2f, mean: %,.2f, variance: %,.2f", chunk_sizes[i], mins[i], maxs[i], means[i], variances[i]));
        }
    }
    
    
    private double profile_sequence_chunks(byte[] to_hash, int chunk_size, int iterations, Checksum checksum_fn){
        double mean = 0;
        double M2 = 0;
        double min = Double.MAX_VALUE;
        double max = 0;
        for (int x = 0; x < iterations; x++){
            int i = 0;
            long start = System.nanoTime();
            while(i + chunk_size < to_hash.length){
                checksum_fn.update(to_hash, i, chunk_size);
                i = i + chunk_size;
            }
            checksum_fn.update(to_hash, i, to_hash.length - i);
            long end =  System.nanoTime();
            double[] stats = update_summary(x + 1, mean, M2, min, max, (end - start));
            mean = stats[0];
            M2 = stats[1];
            min = stats[2];
            max = stats[3];
        }
        return mean;
    }
    
    private double[] profile_sequence(byte[] to_hash, int[] chunk_sizes, int iterations_per_sequence, Checksum checksum_fn) {
        double[] times = new double[chunk_sizes.length];
        for(int i = 0; i < chunk_sizes.length; i++) {
            profile_sequence_chunks(to_hash, chunk_sizes[i], iterations_per_sequence, checksum_fn);
            System.out.println("profile_sequence");
            checksum_fn.reset();
            times[i] = (profile_sequence_chunks(to_hash, chunk_sizes[i], iterations_per_sequence, checksum_fn));
        }
        return times;
    }
    
    private void profile(int size, int[] chunk_sizes, int num_sequences, int iterations_per_sequence, Checksum checksum_fn){
        double[] means = new double[chunk_sizes.length];
        double [] variances = new double[chunk_sizes.length];
        double[] mins = new double[chunk_sizes.length];
        for (int i = 0; i < mins.length; i++){
            mins[i] = Double.MAX_VALUE;
        }
        double[] maxs = new double[chunk_sizes.length];
        Random rnd = new Random();
        for(int x = 0; x < num_sequences; x++){
            byte[] buffer = new byte[size];
            rnd.nextBytes(buffer);
            // if(x % 100 == 0) {
            System.out.println(String.format("count: %d", x));
            // }
            double[] times = profile_sequence(buffer, chunk_sizes, iterations_per_sequence, checksum_fn);
            for(int i = 0; i < chunk_sizes.length; i++) {
                double[] stats = update_summary(x + 1, means[i], variances[i], mins[i], maxs[i], times[i]);
                means[i] = stats[0];
                variances[i] = stats[1];
                mins[i] = stats[2];
                maxs[i] = stats[3];
            }
        }
        for (int i = 0; i < variances.length; i++){
            variances[i] = finalize_summary(num_sequences, variances[i]);
        }
        print_stats(means, variances, mins, maxs, chunk_sizes);
    }
}
