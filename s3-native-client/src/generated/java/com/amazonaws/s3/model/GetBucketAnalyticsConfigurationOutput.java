// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketAnalyticsConfigurationOutput {
    /**
     * <p>The configuration and any analyses for the analytics filter.</p>
     */
    AnalyticsConfiguration analyticsConfiguration;

    GetBucketAnalyticsConfigurationOutput() {
        this.analyticsConfiguration = null;
    }

    protected GetBucketAnalyticsConfigurationOutput(BuilderImpl builder) {
        this.analyticsConfiguration = builder.analyticsConfiguration;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public interface Builder {
        Builder analyticsConfiguration(AnalyticsConfiguration analyticsConfiguration);

        GetBucketAnalyticsConfigurationOutput build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The configuration and any analyses for the analytics filter.</p>
         */
        AnalyticsConfiguration analyticsConfiguration;

        protected BuilderImpl() {
        }

        private BuilderImpl(GetBucketAnalyticsConfigurationOutput model) {
            analyticsConfiguration(model.analyticsConfiguration);
        }

        public GetBucketAnalyticsConfigurationOutput build() {
            return new GetBucketAnalyticsConfigurationOutput(this);
        }

        public final Builder analyticsConfiguration(AnalyticsConfiguration analyticsConfiguration) {
            this.analyticsConfiguration = analyticsConfiguration;
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

        public AnalyticsConfiguration analyticsConfiguration() {
            return analyticsConfiguration;
        }

        public void setAnalyticsConfiguration(final AnalyticsConfiguration analyticsConfiguration) {
            this.analyticsConfiguration = analyticsConfiguration;
        }
    }
}
