// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class BucketLifecycleConfiguration {
    /**
     * <p>A lifecycle rule for individual objects in an Amazon S3 bucket.</p>
     */
    List<LifecycleRule> rules;

    BucketLifecycleConfiguration() {
        this.rules = null;
    }

    protected BucketLifecycleConfiguration(BuilderImpl builder) {
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
        return Objects.hash(BucketLifecycleConfiguration.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof BucketLifecycleConfiguration);
    }

    public List<LifecycleRule> rules() {
        return rules;
    }

    public void setRules(final List<LifecycleRule> rules) {
        this.rules = rules;
    }

    public interface Builder {
        Builder rules(List<LifecycleRule> rules);

        BucketLifecycleConfiguration build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>A lifecycle rule for individual objects in an Amazon S3 bucket.</p>
         */
        List<LifecycleRule> rules;

        protected BuilderImpl() {
        }

        private BuilderImpl(BucketLifecycleConfiguration model) {
            rules(model.rules);
        }

        public BucketLifecycleConfiguration build() {
            return new BucketLifecycleConfiguration(this);
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

        public void setRules(final List<LifecycleRule> rules) {
            this.rules = rules;
        }
    }
}
