// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class IntelligentTieringFilter {
    /**
     * <p>An object key name prefix that identifies the subset of objects to which the rule
     *          applies.</p>
     */
    String prefix;

    Tag tag;

    /**
     * <p>A conjunction (logical AND) of predicates, which is used in evaluating a metrics filter.
     *          The operator must have at least two predicates, and an object must match all of the
     *          predicates in order for the filter to apply.</p>
     */
    IntelligentTieringAndOperator and;

    IntelligentTieringFilter() {
        this.prefix = "";
        this.tag = null;
        this.and = null;
    }

    protected IntelligentTieringFilter(BuilderImpl builder) {
        this.prefix = builder.prefix;
        this.tag = builder.tag;
        this.and = builder.and;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public Tag tag() {
        return tag;
    }

    public IntelligentTieringAndOperator and() {
        return and;
    }

    public interface Builder {
        Builder prefix(String prefix);

        Builder tag(Tag tag);

        Builder and(IntelligentTieringAndOperator and);

        IntelligentTieringFilter build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>An object key name prefix that identifies the subset of objects to which the rule
         *          applies.</p>
         */
        String prefix;

        Tag tag;

        /**
         * <p>A conjunction (logical AND) of predicates, which is used in evaluating a metrics filter.
         *          The operator must have at least two predicates, and an object must match all of the
         *          predicates in order for the filter to apply.</p>
         */
        IntelligentTieringAndOperator and;

        protected BuilderImpl() {
        }

        private BuilderImpl(IntelligentTieringFilter model) {
            prefix(model.prefix);
            tag(model.tag);
            and(model.and);
        }

        public IntelligentTieringFilter build() {
            return new IntelligentTieringFilter(this);
        }

        public final Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public final Builder tag(Tag tag) {
            this.tag = tag;
            return this;
        }

        public final Builder and(IntelligentTieringAndOperator and) {
            this.and = and;
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

        public String prefix() {
            return prefix;
        }

        public Tag tag() {
            return tag;
        }

        public IntelligentTieringAndOperator and() {
            return and;
        }
    }
}
