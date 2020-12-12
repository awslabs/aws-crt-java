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
public class IntelligentTieringAndOperator {
    private String prefix;

    private List<Tag> tags;

    private IntelligentTieringAndOperator() {
        this.prefix = null;
        this.tags = null;
    }

    private IntelligentTieringAndOperator(Builder builder) {
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
        return Objects.hash(IntelligentTieringAndOperator.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof IntelligentTieringAndOperator);
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

        private Builder(IntelligentTieringAndOperator model) {
            prefix(model.prefix);
            tags(model.tags);
        }

        public IntelligentTieringAndOperator build() {
            return new com.amazonaws.s3.model.IntelligentTieringAndOperator(this);
        }

        /**
         * <p>An object key name prefix that identifies the subset of objects to which the
         *          configuration applies.</p>
         */
        public final Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        /**
         * <p>All of these tags must exist in the object's tag set in order for the configuration to
         *          apply.</p>
         */
        public final Builder tags(List<Tag> tags) {
            this.tags = tags;
            return this;
        }
    }
}
