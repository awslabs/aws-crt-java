// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketMetricsConfigurationOutput {
    private MetricsConfiguration metricsConfiguration;

    private GetBucketMetricsConfigurationOutput() {
        this.metricsConfiguration = null;
    }

    private GetBucketMetricsConfigurationOutput(Builder builder) {
        this.metricsConfiguration = builder.metricsConfiguration;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    static final class Builder {
        private MetricsConfiguration metricsConfiguration;

        private Builder() {
        }

        private Builder(GetBucketMetricsConfigurationOutput model) {
            metricsConfiguration(model.metricsConfiguration);
        }

        public GetBucketMetricsConfigurationOutput build() {
            return new com.amazonaws.s3.model.GetBucketMetricsConfigurationOutput(this);
        }

        /**
         * <p>Specifies the metrics configuration.</p>
         */
        public final Builder metricsConfiguration(MetricsConfiguration metricsConfiguration) {
            this.metricsConfiguration = metricsConfiguration;
            return this;
        }
    }
}
