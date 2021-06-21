// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class FilterRule {
    /**
     * <p>The object key name prefix or suffix identifying one or more objects to which the
     *          filtering rule applies. The maximum length is 1,024 characters. Overlapping prefixes and
     *          suffixes are not supported. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/NotificationHowTo.html">Configuring Event Notifications</a>
     *          in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     */
    FilterRuleName name;

    /**
     * <p>The value that the filter searches for in object key names.</p>
     */
    String value;

    FilterRule() {
        this.name = null;
        this.value = "";
    }

    protected FilterRule(BuilderImpl builder) {
        this.name = builder.name;
        this.value = builder.value;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public String value() {
        return value;
    }

    public interface Builder {
        Builder name(FilterRuleName name);

        Builder value(String value);

        FilterRule build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The object key name prefix or suffix identifying one or more objects to which the
         *          filtering rule applies. The maximum length is 1,024 characters. Overlapping prefixes and
         *          suffixes are not supported. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/NotificationHowTo.html">Configuring Event Notifications</a>
         *          in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        FilterRuleName name;

        /**
         * <p>The value that the filter searches for in object key names.</p>
         */
        String value;

        protected BuilderImpl() {
        }

        private BuilderImpl(FilterRule model) {
            name(model.name);
            value(model.value);
        }

        public FilterRule build() {
            return new FilterRule(this);
        }

        public final Builder name(FilterRuleName name) {
            this.name = name;
            return this;
        }

        public final Builder value(String value) {
            this.value = value;
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

        public FilterRuleName name() {
            return name;
        }

        public String value() {
            return value;
        }
    }
}
