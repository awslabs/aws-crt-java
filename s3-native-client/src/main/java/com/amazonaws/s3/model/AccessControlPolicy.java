// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class AccessControlPolicy {
    private List<Grant> grants;

    private Owner owner;

    private AccessControlPolicy() {
        this.grants = null;
        this.owner = null;
    }

    private AccessControlPolicy(Builder builder) {
        this.grants = builder.grants;
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
        return Objects.hash(AccessControlPolicy.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof AccessControlPolicy);
    }

    public List<Grant> grants() {
        return grants;
    }

    public void setGrants(final List<Grant> grants) {
        this.grants = grants;
    }

    public Owner owner() {
        return owner;
    }

    public void setOwner(final Owner owner) {
        this.owner = owner;
    }

    static final class Builder {
        private List<Grant> grants;

        private Owner owner;

        private Builder() {
        }

        private Builder(AccessControlPolicy model) {
            grants(model.grants);
            owner(model.owner);
        }

        public AccessControlPolicy build() {
            return new com.amazonaws.s3.model.AccessControlPolicy(this);
        }

        /**
         * <p>A list of grants.</p>
         */
        public final Builder grants(List<Grant> grants) {
            this.grants = grants;
            return this;
        }

        /**
         * <p>Container for the bucket owner's display name and ID.</p>
         */
        public final Builder owner(Owner owner) {
            this.owner = owner;
            return this;
        }
    }
}
