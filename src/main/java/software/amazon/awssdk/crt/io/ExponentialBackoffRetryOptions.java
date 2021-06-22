/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.io;

public class ExponentialBackoffRetryOptions {

    public enum JitterMode {
        Default(0),
        None(1),
        Full(2),
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

    public ExponentialBackoffRetryOptions() {
        this.jitterMode = JitterMode.Default;
    }

    public ExponentialBackoffRetryOptions withEventLoopGroup(EventLoopGroup eventLoopGroup) {
        this.eventLoopGroup = eventLoopGroup;
        return this;
    }

    public EventLoopGroup getEventLoopGroup() {
        return this.eventLoopGroup;
    }

    public ExponentialBackoffRetryOptions withMaxRetries(long maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    public long getMaxRetries() {
        return this.maxRetries;
    }

    public ExponentialBackoffRetryOptions withBackoffScaleFactorMS(long backoffScaleFactorMS) {
        this.backoffScaleFactorMS = backoffScaleFactorMS;
        return this;
    }

    public long getBackoffScaleFactorMS() {
        return this.backoffScaleFactorMS;
    }

    public ExponentialBackoffRetryOptions withJitterMode(JitterMode jitterMode) {
        this.jitterMode = jitterMode;
        return this;
    }

    public JitterMode getJitterMode() {
        return this.jitterMode;
    }
}
