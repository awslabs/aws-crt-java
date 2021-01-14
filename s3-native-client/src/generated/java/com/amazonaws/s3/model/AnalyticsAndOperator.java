// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class AnalyticsAndOperator {
    /**
     * <p>The prefix to use when evaluating an AND predicate: The prefix that an object must have
     *          to be included in the metrics results.</p>
     */
    String prefix;

    /**
     * <p>The list of tags to use when evaluating an AND predicate.</p>
     */
    List<Tag> tags;

    AnalyticsAndOperator() {
        this.prefix = "";
        this.tags = null;
    }

    protected AnalyticsAndOperator(BuilderImpl builder) {
        this.prefix = builder.prefix;
        this.tags = builder.tags;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(AnalyticsAndOperator.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof AnalyticsAndOperator);
    }

    public String prefix() {
        return prefix;
    }

    public List<Tag> tags() {
        return tags;
    }

    public interface Builder {
        Builder prefix(String prefix);

        Builder tags(List<Tag> tags);

        AnalyticsAndOperator build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The prefix to use when evaluating an AND predicate: The prefix that an object must have
         *          to be included in the metrics results.</p>
         */
        String prefix;

        /**
         * <p>The list of tags to use when evaluating an AND predicate.</p>
         */
        List<Tag> tags;

        protected BuilderImpl() {
        }

        private BuilderImpl(AnalyticsAndOperator model) {
            prefix(model.prefix);
            tags(model.tags);
        }

        public AnalyticsAndOperator build() {
            return new AnalyticsAndOperator(this);
        }

        public final Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public final Builder tags(List<Tag> tags) {
            this.tags = tags;
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

        public List<Tag> tags() {
            return tags;
        }
    }
}
