// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ListBucketAnalyticsConfigurationsRequest {
    /**
     * <p>The name of the bucket from which analytics configurations are retrieved.</p>
     */
    String bucket;

    /**
     * <p>The ContinuationToken that represents a placeholder from where this request should
     *          begin.</p>
     */
    String continuationToken;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    ListBucketAnalyticsConfigurationsRequest() {
        this.bucket = "";
        this.continuationToken = "";
        this.expectedBucketOwner = "";
    }

    protected ListBucketAnalyticsConfigurationsRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.continuationToken = builder.continuationToken;
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
        return Objects.hash(ListBucketAnalyticsConfigurationsRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ListBucketAnalyticsConfigurationsRequest);
    }

    public String bucket() {
        return bucket;
    }

    public String continuationToken() {
        return continuationToken;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public void setContinuationToken(final String continuationToken) {
        this.continuationToken = continuationToken;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    public interface Builder {
        Builder bucket(String bucket);

        Builder continuationToken(String continuationToken);

        Builder expectedBucketOwner(String expectedBucketOwner);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The name of the bucket from which analytics configurations are retrieved.</p>
         */
        String bucket;

        /**
         * <p>The ContinuationToken that represents a placeholder from where this request should
         *          begin.</p>
         */
        String continuationToken;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        protected BuilderImpl() {
        }

        private BuilderImpl(ListBucketAnalyticsConfigurationsRequest model) {
            bucket(model.bucket);
            continuationToken(model.continuationToken);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public ListBucketAnalyticsConfigurationsRequest build() {
            return new ListBucketAnalyticsConfigurationsRequest(this);
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder continuationToken(String continuationToken) {
            this.continuationToken = continuationToken;
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

        public String continuationToken() {
            return continuationToken;
        }

        public String expectedBucketOwner() {
            return expectedBucketOwner;
        }

        public void setBucket(final String bucket) {
            this.bucket = bucket;
        }

        public void setContinuationToken(final String continuationToken) {
            this.continuationToken = continuationToken;
        }

        public void setExpectedBucketOwner(final String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
        }
    }
}
