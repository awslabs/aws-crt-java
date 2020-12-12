// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketVersioningOutput {
    private BucketVersioningStatus status;

    private MFADeleteStatus mFADelete;

    private GetBucketVersioningOutput() {
        this.status = null;
        this.mFADelete = null;
    }

    private GetBucketVersioningOutput(Builder builder) {
        this.status = builder.status;
        this.mFADelete = builder.mFADelete;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(GetBucketVersioningOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetBucketVersioningOutput);
    }

    public BucketVersioningStatus status() {
        return status;
    }

    public void setStatus(final BucketVersioningStatus status) {
        this.status = status;
    }

    public MFADeleteStatus mFADelete() {
        return mFADelete;
    }

    public void setMFADelete(final MFADeleteStatus mFADelete) {
        this.mFADelete = mFADelete;
    }

    static final class Builder {
        private BucketVersioningStatus status;

        private MFADeleteStatus mFADelete;

        private Builder() {
        }

        private Builder(GetBucketVersioningOutput model) {
            status(model.status);
            mFADelete(model.mFADelete);
        }

        public GetBucketVersioningOutput build() {
            return new com.amazonaws.s3.model.GetBucketVersioningOutput(this);
        }

        /**
         * <p>The versioning state of the bucket.</p>
         */
        public final Builder status(BucketVersioningStatus status) {
            this.status = status;
            return this;
        }

        /**
         * <p>Specifies whether MFA delete is enabled in the bucket versioning configuration. This
         *          element is only returned if the bucket has been configured with MFA delete. If the bucket
         *          has never been so configured, this element is not returned.</p>
         */
        public final Builder mFADelete(MFADeleteStatus mFADelete) {
            this.mFADelete = mFADelete;
            return this;
        }
    }
}
