// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketIntelligentTieringConfigurationOutput {
    /**
     * <p>Container for S3 Intelligent-Tiering configuration.</p>
     */
    IntelligentTieringConfiguration intelligentTieringConfiguration;

    GetBucketIntelligentTieringConfigurationOutput() {
        this.intelligentTieringConfiguration = null;
    }

    protected GetBucketIntelligentTieringConfigurationOutput(BuilderImpl builder) {
        this.intelligentTieringConfiguration = builder.intelligentTieringConfiguration;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(GetBucketIntelligentTieringConfigurationOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetBucketIntelligentTieringConfigurationOutput);
    }

    public IntelligentTieringConfiguration intelligentTieringConfiguration() {
        return intelligentTieringConfiguration;
    }

    public interface Builder {
        Builder intelligentTieringConfiguration(
                IntelligentTieringConfiguration intelligentTieringConfiguration);

        GetBucketIntelligentTieringConfigurationOutput build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Container for S3 Intelligent-Tiering configuration.</p>
         */
        IntelligentTieringConfiguration intelligentTieringConfiguration;

        protected BuilderImpl() {
        }

        private BuilderImpl(GetBucketIntelligentTieringConfigurationOutput model) {
            intelligentTieringConfiguration(model.intelligentTieringConfiguration);
        }

        public GetBucketIntelligentTieringConfigurationOutput build() {
            return new GetBucketIntelligentTieringConfigurationOutput(this);
        }

        public final Builder intelligentTieringConfiguration(
                IntelligentTieringConfiguration intelligentTieringConfiguration) {
            this.intelligentTieringConfiguration = intelligentTieringConfiguration;
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

        public IntelligentTieringConfiguration intelligentTieringConfiguration() {
            return intelligentTieringConfiguration;
        }
    }
}
