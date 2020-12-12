// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.UnionGenerator")
public class ReplicationRuleFilter {
    private String prefix;

    private Tag tag;

    private ReplicationRuleAndOperator and;

    private ReplicationRuleFilter(Builder builder) {
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
        return Objects.hash(ReplicationRuleFilter.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ReplicationRuleFilter);
    }

    public String prefix() {
        return prefix;
    }

    public Tag tag() {
        return tag;
    }

    public ReplicationRuleAndOperator and() {
        return and;
    }

    static final class Builder {
        private String prefix;

        private Tag tag;

        private ReplicationRuleAndOperator and;

        private Builder() {
        }

        private Builder(ReplicationRuleFilter model) {
            prefix(model.prefix);
            tag(model.tag);
            and(model.and);
        }

        public ReplicationRuleFilter build() {
            return new com.amazonaws.s3.model.ReplicationRuleFilter(this);
        }

        /**
         * <p>An object key name prefix that identifies the subset of objects to which the rule
         *          applies.</p>
         */
        public final Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        /**
         * <p>A container for specifying a tag key and value. </p>
         *          <p>The rule applies only to objects that have the tag in their tag set.</p>
         */
        public final Builder tag(Tag tag) {
            this.tag = tag;
            return this;
        }

        /**
         * <p>A container for specifying rule filters. The filters determine the subset of objects to
         *          which the rule applies. This element is required only if you specify more than one filter.
         *          For example: </p>
         *          <ul>
         *             <li>
         *                <p>If you specify both a <code>Prefix</code> and a <code>Tag</code> filter, wrap
         *                these filters in an <code>And</code> tag.</p>
         *             </li>
         *             <li>
         *                <p>If you specify a filter based on multiple tags, wrap the <code>Tag</code> elements
         *                in an <code>And</code> tag.</p>
         *             </li>
         *          </ul>
         */
        public final Builder and(ReplicationRuleAndOperator and) {
            this.and = and;
            return this;
        }
    }
}
