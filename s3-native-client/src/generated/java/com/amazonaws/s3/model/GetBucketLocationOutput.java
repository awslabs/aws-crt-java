// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketLocationOutput {
    /**
     * <p>Specifies the Region where the bucket resides. For a list of all the Amazon S3 supported
     *          location constraints by Region, see <a href="https://docs.aws.amazon.com/general/latest/gr/rande.html#s3_region">Regions and Endpoints</a>.
     *          Buckets in Region <code>us-east-1</code> have a LocationConstraint of
     *          <code>null</code>.</p>
     */
    BucketLocationConstraint locationConstraint;

    GetBucketLocationOutput() {
        this.locationConstraint = null;
    }

    protected GetBucketLocationOutput(BuilderImpl builder) {
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
        return Objects.hash(GetBucketLocationOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetBucketLocationOutput);
    }

    public BucketLocationConstraint locationConstraint() {
        return locationConstraint;
    }

    public interface Builder {
        Builder locationConstraint(BucketLocationConstraint locationConstraint);

        GetBucketLocationOutput build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Specifies the Region where the bucket resides. For a list of all the Amazon S3 supported
         *          location constraints by Region, see <a href="https://docs.aws.amazon.com/general/latest/gr/rande.html#s3_region">Regions and Endpoints</a>.
         *          Buckets in Region <code>us-east-1</code> have a LocationConstraint of
         *          <code>null</code>.</p>
         */
        BucketLocationConstraint locationConstraint;

        protected BuilderImpl() {
        }

        private BuilderImpl(GetBucketLocationOutput model) {
            locationConstraint(model.locationConstraint);
        }

        public GetBucketLocationOutput build() {
            return new GetBucketLocationOutput(this);
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
    }
}
