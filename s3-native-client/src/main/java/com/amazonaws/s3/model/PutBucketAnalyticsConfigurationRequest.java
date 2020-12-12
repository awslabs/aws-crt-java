// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutBucketAnalyticsConfigurationRequest {
    private String bucket;

    private String id;

    private AnalyticsConfiguration analyticsConfiguration;

    private String expectedBucketOwner;

    private PutBucketAnalyticsConfigurationRequest() {
        this.bucket = null;
        this.id = null;
        this.analyticsConfiguration = null;
        this.expectedBucketOwner = null;
    }

    private PutBucketAnalyticsConfigurationRequest(Builder builder) {
        this.bucket = builder.bucket;
        this.id = builder.id;
        this.analyticsConfiguration = builder.analyticsConfiguration;
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
        return Objects.hash(PutBucketAnalyticsConfigurationRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof PutBucketAnalyticsConfigurationRequest);
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

    public AnalyticsConfiguration analyticsConfiguration() {
        return analyticsConfiguration;
    }

    public void setAnalyticsConfiguration(final AnalyticsConfiguration analyticsConfiguration) {
        this.analyticsConfiguration = analyticsConfiguration;
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

        private AnalyticsConfiguration analyticsConfiguration;

        private String expectedBucketOwner;

        private Builder() {
        }

        private Builder(PutBucketAnalyticsConfigurationRequest model) {
            bucket(model.bucket);
            id(model.id);
            analyticsConfiguration(model.analyticsConfiguration);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public PutBucketAnalyticsConfigurationRequest build() {
            return new com.amazonaws.s3.model.PutBucketAnalyticsConfigurationRequest(this);
        }

        /**
         * <p>The name of the bucket to which an analytics configuration is stored.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>The ID that identifies the analytics configuration.</p>
         */
        public final Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * <p>The configuration and any analyses for the analytics filter.</p>
         */
        public final Builder analyticsConfiguration(AnalyticsConfiguration analyticsConfiguration) {
            this.analyticsConfiguration = analyticsConfiguration;
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
