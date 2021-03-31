// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;
import software.amazon.awssdk.crt.http.HttpHeader;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ListBucketMetricsConfigurationsRequest {
    /**
     * <p>The name of the bucket containing the metrics configurations to retrieve.</p>
     */
    String bucket;

    /**
     * <p>The marker that is used to continue a metrics configuration listing that has been
     *          truncated. Use the NextContinuationToken from a previously truncated list response to
     *          continue the listing. The continuation token is an opaque value that Amazon S3
     *          understands.</p>
     */
    String continuationToken;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    HttpHeader[] customHeaders;

    String customQueryParameters;

    ListBucketMetricsConfigurationsRequest() {
        this.bucket = "";
        this.continuationToken = "";
        this.expectedBucketOwner = "";
        this.customHeaders = null;
        this.customQueryParameters = "";
    }

    protected ListBucketMetricsConfigurationsRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.continuationToken = builder.continuationToken;
        this.expectedBucketOwner = builder.expectedBucketOwner;
        this.customHeaders = builder.customHeaders;
        this.customQueryParameters = builder.customQueryParameters;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(ListBucketMetricsConfigurationsRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ListBucketMetricsConfigurationsRequest);
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

    public HttpHeader[] customHeaders() {
        return customHeaders;
    }

    public String customQueryParameters() {
        return customQueryParameters;
    }

    public interface Builder {
        Builder bucket(String bucket);

        Builder continuationToken(String continuationToken);

        Builder expectedBucketOwner(String expectedBucketOwner);

        Builder customHeaders(HttpHeader[] customHeaders);

        Builder customQueryParameters(String customQueryParameters);

        ListBucketMetricsConfigurationsRequest build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The name of the bucket containing the metrics configurations to retrieve.</p>
         */
        String bucket;

        /**
         * <p>The marker that is used to continue a metrics configuration listing that has been
         *          truncated. Use the NextContinuationToken from a previously truncated list response to
         *          continue the listing. The continuation token is an opaque value that Amazon S3
         *          understands.</p>
         */
        String continuationToken;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        HttpHeader[] customHeaders;

        String customQueryParameters;

        protected BuilderImpl() {
        }

        private BuilderImpl(ListBucketMetricsConfigurationsRequest model) {
            bucket(model.bucket);
            continuationToken(model.continuationToken);
            expectedBucketOwner(model.expectedBucketOwner);
            customHeaders(model.customHeaders);
            customQueryParameters(model.customQueryParameters);
        }

        public ListBucketMetricsConfigurationsRequest build() {
            return new ListBucketMetricsConfigurationsRequest(this);
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

        public final Builder customHeaders(HttpHeader[] customHeaders) {
            this.customHeaders = customHeaders;
            return this;
        }

        public final Builder customQueryParameters(String customQueryParameters) {
            this.customQueryParameters = customQueryParameters;
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

        public HttpHeader[] customHeaders() {
            return customHeaders;
        }

        public String customQueryParameters() {
            return customQueryParameters;
        }
    }
}
