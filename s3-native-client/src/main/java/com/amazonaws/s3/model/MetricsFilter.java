// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.UnionGenerator")
public class MetricsFilter {
    private String prefix;

    private Tag tag;

    private MetricsAndOperator and;

    private MetricsFilter(Builder builder) {
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
        return Objects.hash(MetricsFilter.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof MetricsFilter);
    }

    public String prefix() {
        return prefix;
    }

    public Tag tag() {
        return tag;
    }

    public MetricsAndOperator and() {
        return and;
    }

    static final class Builder {
        private String prefix;

        private Tag tag;

        private MetricsAndOperator and;

        private Builder() {
        }

        private Builder(MetricsFilter model) {
            prefix(model.prefix);
            tag(model.tag);
            and(model.and);
        }

        public MetricsFilter build() {
            return new com.amazonaws.s3.model.MetricsFilter(this);
        }

        /**
         * <p>The prefix used when evaluating a metrics filter.</p>
         */
        public final Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        /**
         * <p>The tag used when evaluating a metrics filter.</p>
         */
        public final Builder tag(Tag tag) {
            this.tag = tag;
            return this;
        }

        /**
         * <p>A conjunction (logical AND) of predicates, which is used in evaluating a metrics filter.
         *          The operator must have at least two predicates, and an object must match all of the
         *          predicates in order for the filter to apply.</p>
         */
        public final Builder and(MetricsAndOperator and) {
            this.and = and;
            return this;
        }
    }
}
