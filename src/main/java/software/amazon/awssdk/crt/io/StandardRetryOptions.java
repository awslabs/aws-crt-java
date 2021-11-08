/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.io;

/**
 * Top-level configuration for http retries.
 */
public class StandardRetryOptions {

    private ExponentialBackoffRetryOptions backoffRetryOptions;
    private long initialBucketCapacity;

    /**
     * Sets the exponential backoff configuration
     * @param backoffRetryOptions exponential backoff configuration
     * @return this options object
     */
    public StandardRetryOptions withBackoffRetryOptions(ExponentialBackoffRetryOptions backoffRetryOptions) {
        this.backoffRetryOptions = backoffRetryOptions;
        return this;
    }

    /**
     * @return current exponential backoff retry options
     */
    public ExponentialBackoffRetryOptions getBackoffRetryOptions() {
        return this.backoffRetryOptions;
    }

    /**
     * Sets the initial capacity of the token bucket in the standard retry strategy
     * @param initialBucketCapacity initial token bucket capacity
     * @return this options object
     */
    public StandardRetryOptions withInitialBucketCapacity(long initialBucketCapacity) {
        this.initialBucketCapacity = initialBucketCapacity;
        return this;
    }

    /**
     * @return current initial bucket capacity
     */
    public long getInitialBucketCapacity() {
        return this.initialBucketCapacity;
    }
}
