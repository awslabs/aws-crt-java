// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class AccessControlPolicy {
    /**
     * <p>A list of grants.</p>
     */
    List<Grant> grants;

    /**
     * <p>Container for the bucket owner's display name and ID.</p>
     */
    Owner owner;

    AccessControlPolicy() {
        this.grants = null;
        this.owner = null;
    }

    protected AccessControlPolicy(BuilderImpl builder) {
        this.grants = builder.grants;
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

    public Owner owner() {
        return owner;
    }

    public interface Builder {
        Builder grants(List<Grant> grants);

        Builder owner(Owner owner);

        AccessControlPolicy build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>A list of grants.</p>
         */
        List<Grant> grants;

        /**
         * <p>Container for the bucket owner's display name and ID.</p>
         */
        Owner owner;

        protected BuilderImpl() {
        }

        private BuilderImpl(AccessControlPolicy model) {
            grants(model.grants);
            owner(model.owner);
        }

        public AccessControlPolicy build() {
            return new AccessControlPolicy(this);
        }

        public final Builder grants(List<Grant> grants) {
            this.grants = grants;
            return this;
        }

        public final Builder owner(Owner owner) {
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

        public List<Grant> grants() {
            return grants;
        }

        public Owner owner() {
            return owner;
        }
    }
}
