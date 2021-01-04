// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutBucketAnalyticsConfigurationRequest {
    /**
     * <p>The name of the bucket to which an analytics configuration is stored.</p>
     */
    String bucket;

    /**
     * <p>The ID that identifies the analytics configuration.</p>
     */
    String id;

    /**
     * <p>The configuration and any analyses for the analytics filter.</p>
     */
    AnalyticsConfiguration analyticsConfiguration;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    PutBucketAnalyticsConfigurationRequest() {
        this.bucket = "";
        this.id = "";
        this.analyticsConfiguration = null;
        this.expectedBucketOwner = "";
    }

    protected PutBucketAnalyticsConfigurationRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.id = builder.id;
        this.analyticsConfiguration = builder.analyticsConfiguration;
        this.expectedBucketOwner = builder.expectedBucketOwner;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public String id() {
        return id;
    }

    public AnalyticsConfiguration analyticsConfiguration() {
        return analyticsConfiguration;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setAnalyticsConfiguration(final AnalyticsConfiguration analyticsConfiguration) {
        this.analyticsConfiguration = analyticsConfiguration;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    public interface Builder {
        Builder bucket(String bucket);

        Builder id(String id);

        Builder analyticsConfiguration(AnalyticsConfiguration analyticsConfiguration);

        Builder expectedBucketOwner(String expectedBucketOwner);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The name of the bucket to which an analytics configuration is stored.</p>
         */
        String bucket;

        /**
         * <p>The ID that identifies the analytics configuration.</p>
         */
        String id;

        /**
         * <p>The configuration and any analyses for the analytics filter.</p>
         */
        AnalyticsConfiguration analyticsConfiguration;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        protected BuilderImpl() {
        }

        private BuilderImpl(PutBucketAnalyticsConfigurationRequest model) {
            bucket(model.bucket);
            id(model.id);
            analyticsConfiguration(model.analyticsConfiguration);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public PutBucketAnalyticsConfigurationRequest build() {
            return new PutBucketAnalyticsConfigurationRequest(this);
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder id(String id) {
            this.id = id;
            return this;
        }

        public final Builder analyticsConfiguration(AnalyticsConfiguration analyticsConfiguration) {
            this.analyticsConfiguration = analyticsConfiguration;
            return this;
        }

        public final Builder expectedBucketOwner(String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
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

        public String bucket() {
            return bucket;
        }

        public String id() {
            return id;
        }

        public AnalyticsConfiguration analyticsConfiguration() {
            return analyticsConfiguration;
        }

        public String expectedBucketOwner() {
            return expectedBucketOwner;
        }

        public void setBucket(final String bucket) {
            this.bucket = bucket;
        }

        public void setId(final String id) {
            this.id = id;
        }

        public void setAnalyticsConfiguration(final AnalyticsConfiguration analyticsConfiguration) {
            this.analyticsConfiguration = analyticsConfiguration;
        }

        public void setExpectedBucketOwner(final String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
        }
    }
}
