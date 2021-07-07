/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.io;

public class StandardRetryOptions {

    private ExponentialBackoffRetryOptions backoffRetryOptions;
    private long initialBucketCapacity;

    public StandardRetryOptions withBackoffRetryOptions(ExponentialBackoffRetryOptions backoffRetryOptions) {
        this.backoffRetryOptions = backoffRetryOptions;
        return this;
    }

    public ExponentialBackoffRetryOptions getBackoffRetryOptions() {
        return this.backoffRetryOptions;
    }

    public StandardRetryOptions withInitialBucketCapacity(long initialBucketCapacity) {
        this.initialBucketCapacity = initialBucketCapacity;
        return this;
    }

    public long getInitialBucketCapacity() {
        return this.initialBucketCapacity;
    }
}
