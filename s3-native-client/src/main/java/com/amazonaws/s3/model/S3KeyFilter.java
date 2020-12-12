// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class S3KeyFilter {
    private List<FilterRule> filterRules;

    private S3KeyFilter() {
        this.filterRules = null;
    }

    private S3KeyFilter(Builder builder) {
        this.filterRules = builder.filterRules;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    static final class Builder {
        private List<FilterRule> filterRules;

        private Builder() {
        }

        private Builder(S3KeyFilter model) {
            filterRules(model.filterRules);
        }

        public S3KeyFilter build() {
            return new com.amazonaws.s3.model.S3KeyFilter(this);
        }

        public final Builder filterRules(List<FilterRule> filterRules) {
            this.filterRules = filterRules;
            return this;
        }
    }
}
