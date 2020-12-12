// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketLocationOutput {
    private BucketLocationConstraint locationConstraint;

    private GetBucketLocationOutput() {
        this.locationConstraint = null;
    }

    private GetBucketLocationOutput(Builder builder) {
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

    public void setLocationConstraint(final BucketLocationConstraint locationConstraint) {
        this.locationConstraint = locationConstraint;
    }

    static final class Builder {
        private BucketLocationConstraint locationConstraint;

        private Builder() {
        }

        private Builder(GetBucketLocationOutput model) {
            locationConstraint(model.locationConstraint);
        }

        public GetBucketLocationOutput build() {
            return new com.amazonaws.s3.model.GetBucketLocationOutput(this);
        }

        /**
         * <p>Specifies the Region where the bucket resides. For a list of all the Amazon S3 supported
         *          location constraints by Region, see <a href="https://docs.aws.amazon.com/general/latest/gr/rande.html#s3_region">Regions and Endpoints</a>.
         *          Buckets in Region <code>us-east-1</code> have a LocationConstraint of
         *          <code>null</code>.</p>
         */
        public final Builder locationConstraint(BucketLocationConstraint locationConstraint) {
            this.locationConstraint = locationConstraint;
            return this;
        }
    }
}
