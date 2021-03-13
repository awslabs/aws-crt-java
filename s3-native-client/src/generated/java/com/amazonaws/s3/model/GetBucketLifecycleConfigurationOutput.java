// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketLifecycleConfigurationOutput {
    /**
     * <p>Container for a lifecycle rule.</p>
     */
    List<LifecycleRule> rules;

    GetBucketLifecycleConfigurationOutput() {
        this.rules = null;
    }

    protected GetBucketLifecycleConfigurationOutput(BuilderImpl builder) {
        this.rules = builder.rules;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public interface Builder {
        Builder rules(List<LifecycleRule> rules);

        GetBucketLifecycleConfigurationOutput build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Container for a lifecycle rule.</p>
         */
        List<LifecycleRule> rules;

        protected BuilderImpl() {
        }

        private BuilderImpl(GetBucketLifecycleConfigurationOutput model) {
            rules(model.rules);
        }

        public GetBucketLifecycleConfigurationOutput build() {
            return new GetBucketLifecycleConfigurationOutput(this);
        }

        public final Builder rules(List<LifecycleRule> rules) {
            this.rules = rules;
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

        public List<LifecycleRule> rules() {
            return rules;
        }
    }
}
