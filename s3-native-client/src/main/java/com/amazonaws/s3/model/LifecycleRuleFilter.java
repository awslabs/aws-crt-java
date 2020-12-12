// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.UnionGenerator")
public class LifecycleRuleFilter {
    private String prefix;

    private Tag tag;

    private LifecycleRuleAndOperator and;

    private LifecycleRuleFilter(Builder builder) {
        this.prefix = builder.prefix;
        this.tag = builder.tag;
        this.and = builder.and;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(LifecycleRuleFilter.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof LifecycleRuleFilter);
    }

    public String prefix() {
        return prefix;
    }

    public Tag tag() {
        return tag;
    }

    public LifecycleRuleAndOperator and() {
        return and;
    }

    static final class Builder {
        private String prefix;

        private Tag tag;

        private LifecycleRuleAndOperator and;

        private Builder() {
        }

        private Builder(LifecycleRuleFilter model) {
            prefix(model.prefix);
            tag(model.tag);
            and(model.and);
        }

        public LifecycleRuleFilter build() {
            return new com.amazonaws.s3.model.LifecycleRuleFilter(this);
        }

        /**
         * <p>Prefix identifying one or more objects to which the rule applies.</p>
         */
        public final Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        /**
         * <p>This tag must exist in the object's tag set in order for the rule to apply.</p>
         */
        public final Builder tag(Tag tag) {
            this.tag = tag;
            return this;
        }

        public final Builder and(LifecycleRuleAndOperator and) {
            this.and = and;
            return this;
        }
    }
}
