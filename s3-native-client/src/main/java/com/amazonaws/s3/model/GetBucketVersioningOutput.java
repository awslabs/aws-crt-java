// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketVersioningOutput {
    /**
     * <p>The versioning state of the bucket.</p>
     */
    BucketVersioningStatus status;

    /**
     * <p>Specifies whether MFA delete is enabled in the bucket versioning configuration. This
     *          element is only returned if the bucket has been configured with MFA delete. If the bucket
     *          has never been so configured, this element is not returned.</p>
     */
    MFADeleteStatus mFADelete;

    GetBucketVersioningOutput() {
        this.status = null;
        this.mFADelete = null;
    }

    protected GetBucketVersioningOutput(BuilderImpl builder) {
        this.status = builder.status;
        this.mFADelete = builder.mFADelete;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public MFADeleteStatus mFADelete() {
        return mFADelete;
    }

    public void setStatus(final BucketVersioningStatus status) {
        this.status = status;
    }

    public void setMFADelete(final MFADeleteStatus mFADelete) {
        this.mFADelete = mFADelete;
    }

    public interface Builder {
        Builder status(BucketVersioningStatus status);

        Builder mFADelete(MFADeleteStatus mFADelete);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The versioning state of the bucket.</p>
         */
        BucketVersioningStatus status;

        /**
         * <p>Specifies whether MFA delete is enabled in the bucket versioning configuration. This
         *          element is only returned if the bucket has been configured with MFA delete. If the bucket
         *          has never been so configured, this element is not returned.</p>
         */
        MFADeleteStatus mFADelete;

        protected BuilderImpl() {
        }

        private BuilderImpl(GetBucketVersioningOutput model) {
            status(model.status);
            mFADelete(model.mFADelete);
        }

        public GetBucketVersioningOutput build() {
            return new GetBucketVersioningOutput(this);
        }

        public final Builder status(BucketVersioningStatus status) {
            this.status = status;
            return this;
        }

        public final Builder mFADelete(MFADeleteStatus mFADelete) {
            this.mFADelete = mFADelete;
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

        public BucketVersioningStatus status() {
            return status;
        }

        public MFADeleteStatus mFADelete() {
            return mFADelete;
        }

        public void setStatus(final BucketVersioningStatus status) {
            this.status = status;
        }

        public void setMFADelete(final MFADeleteStatus mFADelete) {
            this.mFADelete = mFADelete;
        }
    }
}
