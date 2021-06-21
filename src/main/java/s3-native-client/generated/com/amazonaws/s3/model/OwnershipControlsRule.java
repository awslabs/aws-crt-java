// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class OwnershipControlsRule {
    ObjectOwnership objectOwnership;

    OwnershipControlsRule() {
        this.objectOwnership = null;
    }

    protected OwnershipControlsRule(BuilderImpl builder) {
        this.objectOwnership = builder.objectOwnership;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(OwnershipControlsRule.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof OwnershipControlsRule);
    }

    public ObjectOwnership objectOwnership() {
        return objectOwnership;
    }

    public interface Builder {
        Builder objectOwnership(ObjectOwnership objectOwnership);

        OwnershipControlsRule build();
    }

    protected static class BuilderImpl implements Builder {
        ObjectOwnership objectOwnership;

        protected BuilderImpl() {
        }

        private BuilderImpl(OwnershipControlsRule model) {
            objectOwnership(model.objectOwnership);
        }

        public OwnershipControlsRule build() {
            return new OwnershipControlsRule(this);
        }

        public final Builder objectOwnership(ObjectOwnership objectOwnership) {
            this.objectOwnership = objectOwnership;
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

        public ObjectOwnership objectOwnership() {
            return objectOwnership;
        }
    }
}
