// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketMetricsConfigurationOutput {
    /**
     * <p>Specifies the metrics configuration.</p>
     */
    MetricsConfiguration metricsConfiguration;

    GetBucketMetricsConfigurationOutput() {
        this.metricsConfiguration = null;
    }

    protected GetBucketMetricsConfigurationOutput(BuilderImpl builder) {
        this.metricsConfiguration = builder.metricsConfiguration;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(GetBucketMetricsConfigurationOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetBucketMetricsConfigurationOutput);
    }

    public MetricsConfiguration metricsConfiguration() {
        return metricsConfiguration;
    }

    public void setMetricsConfiguration(final MetricsConfiguration metricsConfiguration) {
        this.metricsConfiguration = metricsConfiguration;
    }

    public interface Builder {
        Builder metricsConfiguration(MetricsConfiguration metricsConfiguration);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Specifies the metrics configuration.</p>
         */
        MetricsConfiguration metricsConfiguration;

        protected BuilderImpl() {
        }

        private BuilderImpl(GetBucketMetricsConfigurationOutput model) {
            metricsConfiguration(model.metricsConfiguration);
        }

        public GetBucketMetricsConfigurationOutput build() {
            return new GetBucketMetricsConfigurationOutput(this);
        }

        public final Builder metricsConfiguration(MetricsConfiguration metricsConfiguration) {
            this.metricsConfiguration = metricsConfiguration;
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

        public MetricsConfiguration metricsConfiguration() {
            return metricsConfiguration;
        }

        public void setMetricsConfiguration(final MetricsConfiguration metricsConfiguration) {
            this.metricsConfiguration = metricsConfiguration;
        }
    }
}
