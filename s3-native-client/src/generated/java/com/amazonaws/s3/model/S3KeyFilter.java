// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class S3KeyFilter {
    List<FilterRule> filterRules;

    S3KeyFilter() {
        this.filterRules = null;
    }

    protected S3KeyFilter(BuilderImpl builder) {
        this.filterRules = builder.filterRules;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(S3KeyFilter.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof S3KeyFilter);
    }

    public List<FilterRule> filterRules() {
        return filterRules;
    }

    public void setFilterRules(final List<FilterRule> filterRules) {
        this.filterRules = filterRules;
    }

    public interface Builder {
        Builder filterRules(List<FilterRule> filterRules);

        S3KeyFilter build();
    }

    protected static class BuilderImpl implements Builder {
        List<FilterRule> filterRules;

        protected BuilderImpl() {
        }

        private BuilderImpl(S3KeyFilter model) {
            filterRules(model.filterRules);
        }

        public S3KeyFilter build() {
            return new S3KeyFilter(this);
        }

        public final Builder filterRules(List<FilterRule> filterRules) {
            this.filterRules = filterRules;
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

        public List<FilterRule> filterRules() {
            return filterRules;
        }

        public void setFilterRules(final List<FilterRule> filterRules) {
            this.filterRules = filterRules;
        }
    }
}
