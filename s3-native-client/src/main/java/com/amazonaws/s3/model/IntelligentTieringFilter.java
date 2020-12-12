// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class IntelligentTieringFilter {
    private String prefix;

    private Tag tag;

    private IntelligentTieringAndOperator and;

    private IntelligentTieringFilter() {
        this.prefix = null;
        this.tag = null;
        this.and = null;
    }

    private IntelligentTieringFilter(Builder builder) {
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
        return Objects.hash(IntelligentTieringFilter.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof IntelligentTieringFilter);
    }

    public String prefix() {
        return prefix;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public Tag tag() {
        return tag;
    }

    public void setTag(final Tag tag) {
        this.tag = tag;
    }

    public IntelligentTieringAndOperator and() {
        return and;
    }

    public void setAnd(final IntelligentTieringAndOperator and) {
        this.and = and;
    }

    static final class Builder {
        private String prefix;

        private Tag tag;

        private IntelligentTieringAndOperator and;

        private Builder() {
        }

        private Builder(IntelligentTieringFilter model) {
            prefix(model.prefix);
            tag(model.tag);
            and(model.and);
        }

        public IntelligentTieringFilter build() {
            return new com.amazonaws.s3.model.IntelligentTieringFilter(this);
        }

        /**
         * <p>An object key name prefix that identifies the subset of objects to which the rule
         *          applies.</p>
         */
        public final Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public final Builder tag(Tag tag) {
            this.tag = tag;
            return this;
        }

        /**
         * <p>A conjunction (logical AND) of predicates, which is used in evaluating a metrics filter.
         *          The operator must have at least two predicates, and an object must match all of the
         *          predicates in order for the filter to apply.</p>
         */
        public final Builder and(IntelligentTieringAndOperator and) {
            this.and = and;
            return this;
        }
    }
}
