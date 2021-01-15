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
public class ReplicationRuleAndOperator {
    /**
     * <p>An object key name prefix that identifies the subset of objects to which the rule
     *          applies.</p>
     */
    String prefix;

    /**
     * <p>An array of tags containing key and value pairs.</p>
     */
    List<Tag> tags;

    ReplicationRuleAndOperator() {
        this.prefix = "";
        this.tags = null;
    }

    protected ReplicationRuleAndOperator(BuilderImpl builder) {
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
        return Objects.hash(ReplicationRuleAndOperator.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ReplicationRuleAndOperator);
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

        ReplicationRuleAndOperator build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>An object key name prefix that identifies the subset of objects to which the rule
         *          applies.</p>
         */
        String prefix;

        /**
         * <p>An array of tags containing key and value pairs.</p>
         */
        List<Tag> tags;

        protected BuilderImpl() {
        }

        private BuilderImpl(ReplicationRuleAndOperator model) {
            prefix(model.prefix);
            tags(model.tags);
        }

        public ReplicationRuleAndOperator build() {
            return new ReplicationRuleAndOperator(this);
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
