// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutBucketMetricsConfigurationRequest {
    private String bucket;

    private String id;

    private MetricsConfiguration metricsConfiguration;

    private String expectedBucketOwner;

    private PutBucketMetricsConfigurationRequest() {
        this.bucket = null;
        this.id = null;
        this.metricsConfiguration = null;
        this.expectedBucketOwner = null;
    }

    private PutBucketMetricsConfigurationRequest(Builder builder) {
        this.bucket = builder.bucket;
        this.id = builder.id;
        this.metricsConfiguration = builder.metricsConfiguration;
        this.expectedBucketOwner = builder.expectedBucketOwner;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(PutBucketMetricsConfigurationRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof PutBucketMetricsConfigurationRequest);
    }

    public String bucket() {
        return bucket;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public String id() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public MetricsConfiguration metricsConfiguration() {
        return metricsConfiguration;
    }

    public void setMetricsConfiguration(final MetricsConfiguration metricsConfiguration) {
        this.metricsConfiguration = metricsConfiguration;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    static final class Builder {
        private String bucket;

        private String id;

        private MetricsConfiguration metricsConfiguration;

        private String expectedBucketOwner;

        private Builder() {
        }

        private Builder(PutBucketMetricsConfigurationRequest model) {
            bucket(model.bucket);
            id(model.id);
            metricsConfiguration(model.metricsConfiguration);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public PutBucketMetricsConfigurationRequest build() {
            return new com.amazonaws.s3.model.PutBucketMetricsConfigurationRequest(this);
        }

        /**
         * <p>The name of the bucket for which the metrics configuration is set.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>The ID used to identify the metrics configuration.</p>
         */
        public final Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * <p>Specifies the metrics configuration.</p>
         */
        public final Builder metricsConfiguration(MetricsConfiguration metricsConfiguration) {
            this.metricsConfiguration = metricsConfiguration;
            return this;
        }

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        public final Builder expectedBucketOwner(String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
            return this;
        }
    }
}
