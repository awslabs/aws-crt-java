// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketIntelligentTieringConfigurationOutput {
    private IntelligentTieringConfiguration intelligentTieringConfiguration;

    private GetBucketIntelligentTieringConfigurationOutput() {
        this.intelligentTieringConfiguration = null;
    }

    private GetBucketIntelligentTieringConfigurationOutput(Builder builder) {
        this.intelligentTieringConfiguration = builder.intelligentTieringConfiguration;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setIntelligentTieringConfiguration(
            final IntelligentTieringConfiguration intelligentTieringConfiguration) {
        this.intelligentTieringConfiguration = intelligentTieringConfiguration;
    }

    static final class Builder {
        private IntelligentTieringConfiguration intelligentTieringConfiguration;

        private Builder() {
        }

        private Builder(GetBucketIntelligentTieringConfigurationOutput model) {
            intelligentTieringConfiguration(model.intelligentTieringConfiguration);
        }

        public GetBucketIntelligentTieringConfigurationOutput build() {
            return new com.amazonaws.s3.model.GetBucketIntelligentTieringConfigurationOutput(this);
        }

        /**
         * <p>Container for S3 Intelligent-Tiering configuration.</p>
         */
        public final Builder intelligentTieringConfiguration(
                IntelligentTieringConfiguration intelligentTieringConfiguration) {
            this.intelligentTieringConfiguration = intelligentTieringConfiguration;
            return this;
        }
    }
}
