/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.s3;

/**
 * Controls how client performs file I/O operations. Only applies to file-based workloads.
 */
public class FileIoOptions {

    /**
     * Skip buffering the part in memory before sending the request.
     * If set, set the {@code diskThroughputGbps} to reasonably align with the available disk throughput.
     * Otherwise, the transfer may fail with connection starvation.
     * Defaults to false.
     */
    private boolean shouldStream;

    /**
     * The estimated disk throughput in gigabits per second (Gbps).
     * Only applied when {@code shouldStream} is true.
     *
     * When doing upload with streaming, it's important to set the disk throughput to prevent connection starvation.
     * Note: There are possibilities that cannot reach all available disk throughput:
     * 1. Disk is busy with other applications
     * 2. OS Cache may cap the throughput, use {@code directIo} to get around this.
     */
    private double diskThroughputGbps;

    /**
     * Enable direct I/O to bypass the OS cache. Helpful when the disk I/O outperforms the kernel cache.
     * Notes:
     * - Only supported on Linux for now.
     * - Only supports upload for now.
     * - Uses it as a potentially powerful tool that should be used with caution. Read NOTES for O_DIRECT
     *   for additional info https://man7.org/linux/man-pages/man2/openat.2.html
     */
    private boolean directIo;

    /**
     * Constructor with all parameters.
     *
     * @param shouldStream Whether to skip buffering the part in memory before sending the request
     * @param diskThroughputGbps The estimated disk throughput in gigabits per second (Gbps)
     * @param directIo Whether to enable direct I/O to bypass the OS cache
     */
    public FileIoOptions(boolean shouldStream, double diskThroughputGbps, boolean directIo) {
        this.shouldStream = shouldStream;
        this.diskThroughputGbps = diskThroughputGbps;
        this.directIo = directIo;
    }

    /**
     * Gets whether to skip buffering the part in memory before sending the request.
     *
     * @return true if streaming is enabled, false otherwise
     */
    public boolean getShouldStream() {
        return shouldStream;
    }

    /**
     * Sets whether to skip buffering the part in memory before sending the request.
     * If set to true, set the {@code diskThroughputGbps} to reasonably align with the available disk throughput.
     * Otherwise, the transfer may fail with connection starvation.
     *
     * @param shouldStream true to enable streaming, false otherwise
     */
    public void setShouldStream(boolean shouldStream) {
        this.shouldStream = shouldStream;
    }

    /**
     * Gets the estimated disk throughput in gigabits per second (Gbps).
     *
     * @return the estimated disk throughput in Gbps
     */
    public double getDiskThroughputGbps() {
        return diskThroughputGbps;
    }

    /**
     * Sets the estimated disk throughput in gigabits per second (Gbps).
     * Only applied when {@code shouldStream} is true.
     *
     * @param diskThroughputGbps the estimated disk throughput in Gbps
     */
    public void setDiskThroughputGbps(double diskThroughputGbps) {
        this.diskThroughputGbps = diskThroughputGbps;
    }

    /**
     * Gets whether direct I/O is enabled to bypass the OS cache.
     *
     * @return true if direct I/O is enabled, false otherwise
     */
    public boolean getDirectIo() {
        return directIo;
    }

    /**
     * Sets whether to enable direct I/O to bypass the OS cache.
     * Helpful when the disk I/O outperforms the kernel cache.
     * Notes:
     * - Only supported on Linux for now.
     * - Only supports upload for now.
     *
     * @param directIo true to enable direct I/O, false otherwise
     */
    public void setDirectIo(boolean directIo) {
        this.directIo = directIo;
    }
}
