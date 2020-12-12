// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class FilterRule {
    private FilterRuleName name;

    private String value;

    private FilterRule() {
        this.name = null;
        this.value = null;
    }

    private FilterRule(Builder builder) {
        this.name = builder.name;
        this.value = builder.value;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(FilterRule.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof FilterRule);
    }

    public FilterRuleName name() {
        return name;
    }

    public void setName(final FilterRuleName name) {
        this.name = name;
    }

    public String value() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    static final class Builder {
        private FilterRuleName name;

        private String value;

        private Builder() {
        }

        private Builder(FilterRule model) {
            name(model.name);
            value(model.value);
        }

        public FilterRule build() {
            return new com.amazonaws.s3.model.FilterRule(this);
        }

        /**
         * <p>The object key name prefix or suffix identifying one or more objects to which the
         *          filtering rule applies. The maximum length is 1,024 characters. Overlapping prefixes and
         *          suffixes are not supported. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/NotificationHowTo.html">Configuring Event Notifications</a>
         *          in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        public final Builder name(FilterRuleName name) {
            this.name = name;
            return this;
        }

        /**
         * <p>The value that the filter searches for in object key names.</p>
         */
        public final Builder value(String value) {
            this.value = value;
            return this;
        }
    }
}
