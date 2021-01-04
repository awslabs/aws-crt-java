// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class CreateBucketConfiguration {
    /**
     * <p>Specifies the Region where the bucket will be created. If you don't specify a Region,
     *          the bucket is created in the US East (N. Virginia) Region (us-east-1).</p>
     */
    BucketLocationConstraint locationConstraint;

    CreateBucketConfiguration() {
        this.locationConstraint = null;
    }

    protected CreateBucketConfiguration(BuilderImpl builder) {
        this.locationConstraint = builder.locationConstraint;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public interface Builder {
        Builder locationConstraint(BucketLocationConstraint locationConstraint);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Specifies the Region where the bucket will be created. If you don't specify a Region,
         *          the bucket is created in the US East (N. Virginia) Region (us-east-1).</p>
         */
        BucketLocationConstraint locationConstraint;

        protected BuilderImpl() {
        }

        private BuilderImpl(CreateBucketConfiguration model) {
            locationConstraint(model.locationConstraint);
        }

        public CreateBucketConfiguration build() {
            return new CreateBucketConfiguration(this);
        }

        public final Builder locationConstraint(BucketLocationConstraint locationConstraint) {
            this.locationConstraint = locationConstraint;
            return this;
        }

        @Override
        public int hashCode() {
            return Objects.hash(BuilderImpl.class);
        }

        @Override
        public boolean equals(Object rhs) {
            if (rhs == null) return false;
            return (rhs instanceof BuilderImpl);
        }

        public BucketLocationConstraint locationConstraint() {
            return locationConstraint;
        }

        public void setLocationConstraint(final BucketLocationConstraint locationConstraint) {
            this.locationConstraint = locationConstraint;
        }
    }
}
