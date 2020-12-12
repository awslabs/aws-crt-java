// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketOwnershipControlsOutput {
    private OwnershipControls ownershipControls;

    private GetBucketOwnershipControlsOutput() {
        this.ownershipControls = null;
    }

    private GetBucketOwnershipControlsOutput(Builder builder) {
        this.ownershipControls = builder.ownershipControls;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(GetBucketOwnershipControlsOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetBucketOwnershipControlsOutput);
    }

    public OwnershipControls ownershipControls() {
        return ownershipControls;
    }

    public void setOwnershipControls(final OwnershipControls ownershipControls) {
        this.ownershipControls = ownershipControls;
    }

    static final class Builder {
        private OwnershipControls ownershipControls;

        private Builder() {
        }

        private Builder(GetBucketOwnershipControlsOutput model) {
            ownershipControls(model.ownershipControls);
        }

        public GetBucketOwnershipControlsOutput build() {
            return new com.amazonaws.s3.model.GetBucketOwnershipControlsOutput(this);
        }

        /**
         * <p>The <code>OwnershipControls</code> (BucketOwnerPreferred or ObjectWriter) currently in
         *          effect for this Amazon S3 bucket.</p>
         */
        public final Builder ownershipControls(OwnershipControls ownershipControls) {
            this.ownershipControls = ownershipControls;
            return this;
        }
    }
}
