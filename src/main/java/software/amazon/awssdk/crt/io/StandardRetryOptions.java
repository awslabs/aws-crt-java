/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.io;

import software.amazon.awssdk.crt.CrtResource;

public class StandardRetryOptions extends CrtResource {

    /**
     * Determines whether a resource releases its dependencies at the same time the native handle is released or if it waits.
     * Resources that wait are responsible for calling releaseReferences() manually.
     */
    @Override
    protected boolean canReleaseReferencesImmediately() { return true; }

    /**
     * Frees the native resources associated with this instance
     */
    @Override
    protected void releaseNativeHandle() {
        // It is perfectly acceptable for this to have never created a native resource
        if (!isNull()) {
            standardRetryOptionsDestroy(getNativeHandle());
        }
    }

    private StandardRetryOptions(Builder builder) {
        super();

        ExponentialBackoffRetryOptions backoffRetryOptions = builder.backoffRetryOptions;

        if(backoffRetryOptions == null) {
            backoffRetryOptions = new ExponentialBackoffRetryOptions.Builder().build();
        }

        acquireNativeHandle(standardRetryOptionsNew(
                backoffRetryOptions.getNativeHandle(),
                builder.initialBucketCapacity));

        addReferenceTo(backoffRetryOptions);
    }

    static public class Builder {

        private ExponentialBackoffRetryOptions backoffRetryOptions;
        private long initialBucketCapacity;

        public Builder() {
        }

        public Builder withBackoffRetryOptions(ExponentialBackoffRetryOptions backoffRetryOptions) {
            this.backoffRetryOptions = backoffRetryOptions;
            return this;
        }

        public ExponentialBackoffRetryOptions getBackoffRetryOptions() {
            return this.backoffRetryOptions;
        }

        public Builder withInitialBucketCapcity(long initialBucketCapacity) {
            this.initialBucketCapacity = initialBucketCapacity;
            return this;
        }

        public long getInitialBucketCapacity() {
            return this.initialBucketCapacity;
        }

        public StandardRetryOptions build() {
            return new StandardRetryOptions(this);
        }
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long standardRetryOptionsNew(long backoffRetryOptions, long initialBucketCapacity);

    private static native void standardRetryOptionsDestroy(long retryOptions);
}
