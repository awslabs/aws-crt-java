// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class OwnershipControls {
    /**
     * <p>The container element for an ownership control rule.</p>
     */
    List<OwnershipControlsRule> rules;

    OwnershipControls() {
        this.rules = null;
    }

    protected OwnershipControls(BuilderImpl builder) {
        this.rules = builder.rules;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public interface Builder {
        Builder rules(List<OwnershipControlsRule> rules);

        OwnershipControls build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The container element for an ownership control rule.</p>
         */
        List<OwnershipControlsRule> rules;

        protected BuilderImpl() {
        }

        private BuilderImpl(OwnershipControls model) {
            rules(model.rules);
        }

        public OwnershipControls build() {
            return new OwnershipControls(this);
        }

        public final Builder rules(List<OwnershipControlsRule> rules) {
            this.rules = rules;
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

        public List<OwnershipControlsRule> rules() {
            return rules;
        }
    }
}
