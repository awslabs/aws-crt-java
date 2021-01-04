// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketOwnershipControlsOutput {
    /**
     * <p>The <code>OwnershipControls</code> (BucketOwnerPreferred or ObjectWriter) currently in
     *          effect for this Amazon S3 bucket.</p>
     */
    OwnershipControls ownershipControls;

    GetBucketOwnershipControlsOutput() {
        this.ownershipControls = null;
    }

    protected GetBucketOwnershipControlsOutput(BuilderImpl builder) {
        this.ownershipControls = builder.ownershipControls;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public interface Builder {
        Builder ownershipControls(OwnershipControls ownershipControls);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The <code>OwnershipControls</code> (BucketOwnerPreferred or ObjectWriter) currently in
         *          effect for this Amazon S3 bucket.</p>
         */
        OwnershipControls ownershipControls;

        protected BuilderImpl() {
        }

        private BuilderImpl(GetBucketOwnershipControlsOutput model) {
            ownershipControls(model.ownershipControls);
        }

        public GetBucketOwnershipControlsOutput build() {
            return new GetBucketOwnershipControlsOutput(this);
        }

        public final Builder ownershipControls(OwnershipControls ownershipControls) {
            this.ownershipControls = ownershipControls;
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

        public OwnershipControls ownershipControls() {
            return ownershipControls;
        }

        public void setOwnershipControls(final OwnershipControls ownershipControls) {
            this.ownershipControls = ownershipControls;
        }
    }
}
