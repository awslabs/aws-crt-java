// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class OwnershipControls {
    private List<OwnershipControlsRule> rules;

    private OwnershipControls() {
        this.rules = null;
    }

    private OwnershipControls(Builder builder) {
        this.rules = builder.rules;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(OwnershipControls.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof OwnershipControls);
    }

    public List<OwnershipControlsRule> rules() {
        return rules;
    }

    public void setRules(final List<OwnershipControlsRule> rules) {
        this.rules = rules;
    }

    static final class Builder {
        private List<OwnershipControlsRule> rules;

        private Builder() {
        }

        private Builder(OwnershipControls model) {
            rules(model.rules);
        }

        public OwnershipControls build() {
            return new com.amazonaws.s3.model.OwnershipControls(this);
        }

        /**
         * <p>The container element for an ownership control rule.</p>
         */
        public final Builder rules(List<OwnershipControlsRule> rules) {
            this.rules = rules;
            return this;
        }
    }
}
