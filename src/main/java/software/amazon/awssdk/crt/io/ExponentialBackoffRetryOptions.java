/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.io;

/**
 * Configuration options for the exponential backoff retry strategy for http requests
 */
public class ExponentialBackoffRetryOptions {

    /**
     * What kind of jitter or randomization to apply to the backoff time interval
     *
     * https://aws.amazon.com/blogs/architecture/exponential-backoff-and-jitter/
     */
    public enum JitterMode {

        /** Maps to Full */
        Default(0),

        /** Do not apply any jitter or randomization to the backoff interval */
        None(1),

        /** Choose an actual backoff interval between [0, MaxCurrentBackoff] where MaxCurrentBackoff is the standard
         * exponential backoff value */
        Full(2),

        /** Backoff is taken randomly from the interval between the base backoff
         * interval and a scaling (greater than 1) of the current backoff value */
        Decorrelated(3);

        private int value;

        JitterMode(int value) {
            this.value = value;
        }

        int getValue() {
            return value;
        }
    }

    private EventLoopGroup eventLoopGroup;
    private long maxRetries;
    private long backoffScaleFactorMS;
    private JitterMode jitterMode;

    /**
     * Default constructor
     */
    public ExponentialBackoffRetryOptions() {
        this.jitterMode = JitterMode.Default;
    }

    /**
     * Configure the event loop group to use to schedule the backoff/retry tasks
     * @param eventLoopGroup event loop group to use
     * @return this options object
     */
    public ExponentialBackoffRetryOptions withEventLoopGroup(EventLoopGroup eventLoopGroup) {
        this.eventLoopGroup = eventLoopGroup;
        return this;
    }

    /**
     * @return The event loop group currently configured to do backoff/retry
     */
    public EventLoopGroup getEventLoopGroup() {
        return this.eventLoopGroup;
    }

    /**
     * Configure the maximum number of retries to make while using a strategy sourced from these options
     * @param maxRetries maximum number of retries
     * @return this options object
     */
    public ExponentialBackoffRetryOptions withMaxRetries(long maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    /**
     * @return the maximum number of retries to make while using a strategy sourced from these options
     */
    public long getMaxRetries() {
        return this.maxRetries;
    }

    /**
     * Configures the initial (base) unscaled backoff interval in milliseconds
     * @param backoffScaleFactorMS the initial (base) unscaled backoff interval in milliseconds
     * @return this options object
     */
    public ExponentialBackoffRetryOptions withBackoffScaleFactorMS(long backoffScaleFactorMS) {
        this.backoffScaleFactorMS = backoffScaleFactorMS;
        return this;
    }

    /**
     * @return the initial (base) unscaled backoff interval in milliseconds while using a strategy sourced from
     * these options
     */
    public long getBackoffScaleFactorMS() {
        return this.backoffScaleFactorMS;
    }

    /**
     * Configure the type of jitter to apply to the backoff interval calculations
     * @param jitterMode the type of jitter to apply to the backoff interval calculations
     * @return this options object
     */
    public ExponentialBackoffRetryOptions withJitterMode(JitterMode jitterMode) {
        this.jitterMode = jitterMode;
        return this;
    }

    /**
     * @return the type of jitter to apply to the backoff interval calculations while using a strategy sourced
     * from these options
     */
    public JitterMode getJitterMode() {
        return this.jitterMode;
    }
}
