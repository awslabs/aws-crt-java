// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class VersioningConfiguration {
    private MFADelete mFADelete;

    private BucketVersioningStatus status;

    private VersioningConfiguration() {
        this.mFADelete = null;
        this.status = null;
    }

    private VersioningConfiguration(Builder builder) {
        this.mFADelete = builder.mFADelete;
        this.status = builder.status;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(VersioningConfiguration.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof VersioningConfiguration);
    }

    public MFADelete mFADelete() {
        return mFADelete;
    }

    public void setMFADelete(final MFADelete mFADelete) {
        this.mFADelete = mFADelete;
    }

    public BucketVersioningStatus status() {
        return status;
    }

    public void setStatus(final BucketVersioningStatus status) {
        this.status = status;
    }

    static final class Builder {
        private MFADelete mFADelete;

        private BucketVersioningStatus status;

        private Builder() {
        }

        private Builder(VersioningConfiguration model) {
            mFADelete(model.mFADelete);
            status(model.status);
        }

        public VersioningConfiguration build() {
            return new com.amazonaws.s3.model.VersioningConfiguration(this);
        }

        /**
         * <p>Specifies whether MFA delete is enabled in the bucket versioning configuration. This
         *          element is only returned if the bucket has been configured with MFA delete. If the bucket
         *          has never been so configured, this element is not returned.</p>
         */
        public final Builder mFADelete(MFADelete mFADelete) {
            this.mFADelete = mFADelete;
            return this;
        }

        /**
         * <p>The versioning state of the bucket.</p>
         */
        public final Builder status(BucketVersioningStatus status) {
            this.status = status;
            return this;
        }
    }
}
