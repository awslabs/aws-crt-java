// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.UnionGenerator")
public class AnalyticsFilter {
    private String prefix;

    private Tag tag;

    private AnalyticsAndOperator and;

    private AnalyticsFilter(Builder builder) {
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
        return Objects.hash(AnalyticsFilter.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof AnalyticsFilter);
    }

    public String prefix() {
        return prefix;
    }

    public Tag tag() {
        return tag;
    }

    public AnalyticsAndOperator and() {
        return and;
    }

    static final class Builder {
        private String prefix;

        private Tag tag;

        private AnalyticsAndOperator and;

        private Builder() {
        }

        private Builder(AnalyticsFilter model) {
            prefix(model.prefix);
            tag(model.tag);
            and(model.and);
        }

        public AnalyticsFilter build() {
            return new com.amazonaws.s3.model.AnalyticsFilter(this);
        }

        /**
         * <p>The prefix to use when evaluating an analytics filter.</p>
         */
        public final Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        /**
         * <p>The tag to use when evaluating an analytics filter.</p>
         */
        public final Builder tag(Tag tag) {
            this.tag = tag;
            return this;
        }

        /**
         * <p>A conjunction (logical AND) of predicates, which is used in evaluating an analytics
         *          filter. The operator must have at least two predicates.</p>
         */
        public final Builder and(AnalyticsAndOperator and) {
            this.and = and;
            return this;
        }
    }
}
