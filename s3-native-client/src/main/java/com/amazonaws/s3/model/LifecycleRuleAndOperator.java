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
public class LifecycleRuleAndOperator {
    private String prefix;

    private List<Tag> tags;

    private LifecycleRuleAndOperator() {
        this.prefix = null;
        this.tags = null;
    }

    private LifecycleRuleAndOperator(Builder builder) {
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
        return Objects.hash(LifecycleRuleAndOperator.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof LifecycleRuleAndOperator);
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

        private Builder(LifecycleRuleAndOperator model) {
            prefix(model.prefix);
            tags(model.tags);
        }

        public LifecycleRuleAndOperator build() {
            return new com.amazonaws.s3.model.LifecycleRuleAndOperator(this);
        }

        /**
         * <p>Prefix identifying one or more objects to which the rule applies.</p>
         */
        public final Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        /**
         * <p>All of these tags must exist in the object's tag set in order for the rule to
         *          apply.</p>
         */
        public final Builder tags(List<Tag> tags) {
            this.tags = tags;
            return this;
        }
    }
}
