// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutBucketLifecycleConfigurationRequest {
    private String bucket;

    private BucketLifecycleConfiguration lifecycleConfiguration;

    private String expectedBucketOwner;

    private PutBucketLifecycleConfigurationRequest() {
        this.bucket = null;
        this.lifecycleConfiguration = null;
        this.expectedBucketOwner = null;
    }

    private PutBucketLifecycleConfigurationRequest(Builder builder) {
        this.bucket = builder.bucket;
        this.lifecycleConfiguration = builder.lifecycleConfiguration;
        this.expectedBucketOwner = builder.expectedBucketOwner;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(PutBucketLifecycleConfigurationRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof PutBucketLifecycleConfigurationRequest);
    }

    public String bucket() {
        return bucket;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public BucketLifecycleConfiguration lifecycleConfiguration() {
        return lifecycleConfiguration;
    }

    public void setLifecycleConfiguration(
            final BucketLifecycleConfiguration lifecycleConfiguration) {
        this.lifecycleConfiguration = lifecycleConfiguration;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    static final class Builder {
        private String bucket;

        private BucketLifecycleConfiguration lifecycleConfiguration;

        private String expectedBucketOwner;

        private Builder() {
        }

        private Builder(PutBucketLifecycleConfigurationRequest model) {
            bucket(model.bucket);
            lifecycleConfiguration(model.lifecycleConfiguration);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public PutBucketLifecycleConfigurationRequest build() {
            return new com.amazonaws.s3.model.PutBucketLifecycleConfigurationRequest(this);
        }

        /**
         * <p>The name of the bucket for which to set the configuration.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>Container for lifecycle rules. You can add as many as 1,000 rules.</p>
         */
        public final Builder lifecycleConfiguration(
                BucketLifecycleConfiguration lifecycleConfiguration) {
            this.lifecycleConfiguration = lifecycleConfiguration;
            return this;
        }

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        public final Builder expectedBucketOwner(String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
            return this;
        }
    }
}
