// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketAnalyticsConfigurationOutput {
    private AnalyticsConfiguration analyticsConfiguration;

    private GetBucketAnalyticsConfigurationOutput() {
        this.analyticsConfiguration = null;
    }

    private GetBucketAnalyticsConfigurationOutput(Builder builder) {
        this.analyticsConfiguration = builder.analyticsConfiguration;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(GetBucketAnalyticsConfigurationOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetBucketAnalyticsConfigurationOutput);
    }

    public AnalyticsConfiguration analyticsConfiguration() {
        return analyticsConfiguration;
    }

    public void setAnalyticsConfiguration(final AnalyticsConfiguration analyticsConfiguration) {
        this.analyticsConfiguration = analyticsConfiguration;
    }

    static final class Builder {
        private AnalyticsConfiguration analyticsConfiguration;

        private Builder() {
        }

        private Builder(GetBucketAnalyticsConfigurationOutput model) {
            analyticsConfiguration(model.analyticsConfiguration);
        }

        public GetBucketAnalyticsConfigurationOutput build() {
            return new com.amazonaws.s3.model.GetBucketAnalyticsConfigurationOutput(this);
        }

        /**
         * <p>The configuration and any analyses for the analytics filter.</p>
         */
        public final Builder analyticsConfiguration(AnalyticsConfiguration analyticsConfiguration) {
            this.analyticsConfiguration = analyticsConfiguration;
            return this;
        }
    }
}
