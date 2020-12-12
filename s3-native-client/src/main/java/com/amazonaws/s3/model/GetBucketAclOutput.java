// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketAclOutput {
    private Owner owner;

    private List<Grant> grants;

    private GetBucketAclOutput() {
        this.owner = null;
        this.grants = null;
    }

    private GetBucketAclOutput(Builder builder) {
        this.owner = builder.owner;
        this.grants = builder.grants;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(GetBucketAclOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetBucketAclOutput);
    }

    public Owner owner() {
        return owner;
    }

    public void setOwner(final Owner owner) {
        this.owner = owner;
    }

    public List<Grant> grants() {
        return grants;
    }

    public void setGrants(final List<Grant> grants) {
        this.grants = grants;
    }

    static final class Builder {
        private Owner owner;

        private List<Grant> grants;

        private Builder() {
        }

        private Builder(GetBucketAclOutput model) {
            owner(model.owner);
            grants(model.grants);
        }

        public GetBucketAclOutput build() {
            return new com.amazonaws.s3.model.GetBucketAclOutput(this);
        }

        /**
         * <p>Container for the bucket owner's display name and ID.</p>
         */
        public final Builder owner(Owner owner) {
            this.owner = owner;
            return this;
        }

        /**
         * <p>A list of grants.</p>
         */
        public final Builder grants(List<Grant> grants) {
            this.grants = grants;
            return this;
        }
    }
}
