// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketLifecycleConfigurationOutput {
    private List<LifecycleRule> rules;

    private GetBucketLifecycleConfigurationOutput() {
        this.rules = null;
    }

    private GetBucketLifecycleConfigurationOutput(Builder builder) {
        this.rules = builder.rules;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(GetBucketLifecycleConfigurationOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetBucketLifecycleConfigurationOutput);
    }

    public List<LifecycleRule> rules() {
        return rules;
    }

    public void setRules(final List<LifecycleRule> rules) {
        this.rules = rules;
    }

    static final class Builder {
        private List<LifecycleRule> rules;

        private Builder() {
        }

        private Builder(GetBucketLifecycleConfigurationOutput model) {
            rules(model.rules);
        }

        public GetBucketLifecycleConfigurationOutput build() {
            return new com.amazonaws.s3.model.GetBucketLifecycleConfigurationOutput(this);
        }

        /**
         * <p>Container for a lifecycle rule.</p>
         */
        public final Builder rules(List<LifecycleRule> rules) {
            this.rules = rules;
            return this;
        }
    }
}
