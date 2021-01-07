// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketAclOutput {
    /**
     * <p>Container for the bucket owner's display name and ID.</p>
     */
    Owner owner;

    /**
     * <p>A list of grants.</p>
     */
    List<Grant> grants;

    GetBucketAclOutput() {
        this.owner = null;
        this.grants = null;
    }

    protected GetBucketAclOutput(BuilderImpl builder) {
        this.owner = builder.owner;
        this.grants = builder.grants;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public List<Grant> grants() {
        return grants;
    }

    public void setOwner(final Owner owner) {
        this.owner = owner;
    }

    public void setGrants(final List<Grant> grants) {
        this.grants = grants;
    }

    public interface Builder {
        Builder owner(Owner owner);

        Builder grants(List<Grant> grants);

        GetBucketAclOutput build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Container for the bucket owner's display name and ID.</p>
         */
        Owner owner;

        /**
         * <p>A list of grants.</p>
         */
        List<Grant> grants;

        protected BuilderImpl() {
        }

        private BuilderImpl(GetBucketAclOutput model) {
            owner(model.owner);
            grants(model.grants);
        }

        public GetBucketAclOutput build() {
            return new GetBucketAclOutput(this);
        }

        public final Builder owner(Owner owner) {
            this.owner = owner;
            return this;
        }

        public final Builder grants(List<Grant> grants) {
            this.grants = grants;
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

        public Owner owner() {
            return owner;
        }

        public List<Grant> grants() {
            return grants;
        }

        public void setOwner(final Owner owner) {
            this.owner = owner;
        }

        public void setGrants(final List<Grant> grants) {
            this.grants = grants;
        }
    }
}
