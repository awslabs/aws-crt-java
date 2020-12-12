// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class MetricsAndOperator {
    private String prefix;

    private List<Tag> tags;

    private MetricsAndOperator() {
        this.prefix = null;
        this.tags = null;
    }

    private MetricsAndOperator(Builder builder) {
        this.prefix = builder.prefix;
        this.tags = builder.tags;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(MetricsAndOperator.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof MetricsAndOperator);
    }

    public String prefix() {
        return prefix;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public List<Tag> tags() {
        return tags;
    }

    public void setTags(final List<Tag> tags) {
        this.tags = tags;
    }

    static final class Builder {
        private String prefix;

        private List<Tag> tags;

        private Builder() {
        }

        private Builder(MetricsAndOperator model) {
            prefix(model.prefix);
            tags(model.tags);
        }

        public MetricsAndOperator build() {
            return new com.amazonaws.s3.model.MetricsAndOperator(this);
        }

        /**
         * <p>The prefix used when evaluating an AND predicate.</p>
         */
        public final Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        /**
         * <p>The list of tags used when evaluating an AND predicate.</p>
         */
        public final Builder tags(List<Tag> tags) {
            this.tags = tags;
            return this;
        }
    }
}
