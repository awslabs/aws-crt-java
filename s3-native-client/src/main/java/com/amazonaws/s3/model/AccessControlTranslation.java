// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class AccessControlTranslation {
    /**
     * <p>Specifies the replica ownership. For default and valid values, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/RESTBucketPUTreplication.html">PUT bucket
     *             replication</a> in the <i>Amazon Simple Storage Service API Reference</i>.</p>
     */
    OwnerOverride owner;

    AccessControlTranslation() {
        this.owner = null;
    }

    protected AccessControlTranslation(BuilderImpl builder) {
        this.owner = builder.owner;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(AccessControlTranslation.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof AccessControlTranslation);
    }

    public OwnerOverride owner() {
        return owner;
    }

    public void setOwner(final OwnerOverride owner) {
        this.owner = owner;
    }

    public interface Builder {
        Builder owner(OwnerOverride owner);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Specifies the replica ownership. For default and valid values, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/RESTBucketPUTreplication.html">PUT bucket
         *             replication</a> in the <i>Amazon Simple Storage Service API Reference</i>.</p>
         */
        OwnerOverride owner;

        protected BuilderImpl() {
        }

        private BuilderImpl(AccessControlTranslation model) {
            owner(model.owner);
        }

        public AccessControlTranslation build() {
            return new AccessControlTranslation(this);
        }

        public final Builder owner(OwnerOverride owner) {
            this.owner = owner;
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

        public OwnerOverride owner() {
            return owner;
        }

        public void setOwner(final OwnerOverride owner) {
            this.owner = owner;
        }
    }
}
