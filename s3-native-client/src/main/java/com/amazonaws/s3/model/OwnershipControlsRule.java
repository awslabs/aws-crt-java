// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class OwnershipControlsRule {
    private ObjectOwnership objectOwnership;

    private OwnershipControlsRule() {
        this.objectOwnership = null;
    }

    private OwnershipControlsRule(Builder builder) {
        this.objectOwnership = builder.objectOwnership;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setObjectOwnership(final ObjectOwnership objectOwnership) {
        this.objectOwnership = objectOwnership;
    }

    static final class Builder {
        private ObjectOwnership objectOwnership;

        private Builder() {
        }

        private Builder(OwnershipControlsRule model) {
            objectOwnership(model.objectOwnership);
        }

        public OwnershipControlsRule build() {
            return new com.amazonaws.s3.model.OwnershipControlsRule(this);
        }

        public final Builder objectOwnership(ObjectOwnership objectOwnership) {
            this.objectOwnership = objectOwnership;
            return this;
        }
    }
}
