/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.http;

/**
 * This class provides access to basic http connection monitoring controls in lieu of the more traditional
 * timeouts.
 *
 * The user can set a throughput threshold (in bytes per second) for the a connection to be considered healthy.  If
 * the connection falls below this threshold for a configurable amount of time, then the connection is considered
 * unhealthy and shut down.  Throughput/health is only measured when the connection has work (read or write) that
 * needs to be done.
 */
public class HttpMonitoringOptions {

    /**
     * minimum amount of throughput, in bytes per second, for a connection to be considered healthy.
     */
    private long minThroughputBytesPerSecond;

    /**
     * How long, in seconds, a connection is allowed to be unhealthy before getting shut down.  Must be at least
     * two.
     */
    private int allowableThroughputFailureIntervalSeconds;

    /**
     * Creates a new set of monitoring options
     */
    public HttpMonitoringOptions() {
    }

    /**
     * Sets a throughput threshold for connections.  Throughput below this value will be considered unhealthy.
     * @param minThroughputBytesPerSecond minimum amount of throughput, in bytes per second, for a connection to be
     *                                    considered healthy.
     */
    public void setMinThroughputBytesPerSecond(long minThroughputBytesPerSecond) {
        if (minThroughputBytesPerSecond < 0) {
            throw new IllegalArgumentException("Http monitoring minimum throughput must be non-negative");
        }
        this.minThroughputBytesPerSecond = minThroughputBytesPerSecond;
    }

    /**
     * @return minimum amount of throughput, in bytes per second, for a connection to be considered healthy.
     */
    public long getMinThroughputBytesPerSecond() { return minThroughputBytesPerSecond; }

    /**
     * Sets how long, in seconds, a connection is allowed to be unhealthy before getting shut down.  Must be at
     * least two.
     * @param allowableThroughputFailureIntervalSeconds How long, in seconds, a connection is allowed to be unhealthy
     *                                                  before getting shut down.
     */
    public void setAllowableThroughputFailureIntervalSeconds(int allowableThroughputFailureIntervalSeconds) {
        if (allowableThroughputFailureIntervalSeconds < 2) {
            throw new IllegalArgumentException("Http monitoring failure interval must be at least two");
        }
        this.allowableThroughputFailureIntervalSeconds = allowableThroughputFailureIntervalSeconds;
    }

    /**
     * @return How long, in seconds, a connection is allowed to be unhealthy before getting shut down.  Must be at
     * least two.
     */
    public int getAllowableThroughputFailureIntervalSeconds() { return allowableThroughputFailureIntervalSeconds; }


}
