// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class CreateBucketConfiguration {
    private BucketLocationConstraint locationConstraint;

    private CreateBucketConfiguration() {
        this.locationConstraint = null;
    }

    private CreateBucketConfiguration(Builder builder) {
        this.locationConstraint = builder.locationConstraint;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(CreateBucketConfiguration.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof CreateBucketConfiguration);
    }

    public BucketLocationConstraint locationConstraint() {
        return locationConstraint;
    }

    public void setLocationConstraint(final BucketLocationConstraint locationConstraint) {
        this.locationConstraint = locationConstraint;
    }

    static final class Builder {
        private BucketLocationConstraint locationConstraint;

        private Builder() {
        }

        private Builder(CreateBucketConfiguration model) {
            locationConstraint(model.locationConstraint);
        }

        public CreateBucketConfiguration build() {
            return new com.amazonaws.s3.model.CreateBucketConfiguration(this);
        }

        /**
         * <p>Specifies the Region where the bucket will be created. If you don't specify a Region,
         *          the bucket is created in the US East (N. Virginia) Region (us-east-1).</p>
         */
        public final Builder locationConstraint(BucketLocationConstraint locationConstraint) {
            this.locationConstraint = locationConstraint;
            return this;
        }
    }
}
