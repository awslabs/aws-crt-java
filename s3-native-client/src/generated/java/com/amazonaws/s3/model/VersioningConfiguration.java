// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class VersioningConfiguration {
    /**
     * <p>Specifies whether MFA delete is enabled in the bucket versioning configuration. This
     *          element is only returned if the bucket has been configured with MFA delete. If the bucket
     *          has never been so configured, this element is not returned.</p>
     */
    MFADelete mFADelete;

    /**
     * <p>The versioning state of the bucket.</p>
     */
    BucketVersioningStatus status;

    VersioningConfiguration() {
        this.mFADelete = null;
        this.status = null;
    }

    protected VersioningConfiguration(BuilderImpl builder) {
        this.mFADelete = builder.mFADelete;
        this.status = builder.status;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public BucketVersioningStatus status() {
        return status;
    }

    public interface Builder {
        Builder mFADelete(MFADelete mFADelete);

        Builder status(BucketVersioningStatus status);

        VersioningConfiguration build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Specifies whether MFA delete is enabled in the bucket versioning configuration. This
         *          element is only returned if the bucket has been configured with MFA delete. If the bucket
         *          has never been so configured, this element is not returned.</p>
         */
        MFADelete mFADelete;

        /**
         * <p>The versioning state of the bucket.</p>
         */
        BucketVersioningStatus status;

        protected BuilderImpl() {
        }

        private BuilderImpl(VersioningConfiguration model) {
            mFADelete(model.mFADelete);
            status(model.status);
        }

        public VersioningConfiguration build() {
            return new VersioningConfiguration(this);
        }

        public final Builder mFADelete(MFADelete mFADelete) {
            this.mFADelete = mFADelete;
            return this;
        }

        public final Builder status(BucketVersioningStatus status) {
            this.status = status;
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

        public MFADelete mFADelete() {
            return mFADelete;
        }

        public BucketVersioningStatus status() {
            return status;
        }
    }
}
