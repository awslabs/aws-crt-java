// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class AccessControlTranslation {
    private OwnerOverride owner;

    private AccessControlTranslation() {
        this.owner = null;
    }

    private AccessControlTranslation(Builder builder) {
        this.owner = builder.owner;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    static final class Builder {
        private OwnerOverride owner;

        private Builder() {
        }

        private Builder(AccessControlTranslation model) {
            owner(model.owner);
        }

        public AccessControlTranslation build() {
            return new com.amazonaws.s3.model.AccessControlTranslation(this);
        }

        /**
         * <p>Specifies the replica ownership. For default and valid values, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/RESTBucketPUTreplication.html">PUT bucket
         *             replication</a> in the <i>Amazon Simple Storage Service API Reference</i>.</p>
         */
        public final Builder owner(OwnerOverride owner) {
            this.owner = owner;
            return this;
        }
    }
}
