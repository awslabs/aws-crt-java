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
public class ListBucketIntelligentTieringConfigurationsRequest {
    /**
     * <p>The name of the Amazon S3 bucket whose configuration you want to modify or retrieve.</p>
     */
    String bucket;

    /**
     * <p>The ContinuationToken that represents a placeholder from where this request should
     *          begin.</p>
     */
    String continuationToken;

    HttpHeader[] customHeaders;

    String customQueryParameters;

    ListBucketIntelligentTieringConfigurationsRequest() {
        this.bucket = "";
        this.continuationToken = "";
        this.customHeaders = null;
        this.customQueryParameters = "";
    }

    protected ListBucketIntelligentTieringConfigurationsRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.continuationToken = builder.continuationToken;
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
        return Objects.hash(ListBucketIntelligentTieringConfigurationsRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ListBucketIntelligentTieringConfigurationsRequest);
    }

    public String bucket() {
        return bucket;
    }

    public String continuationToken() {
        return continuationToken;
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

        Builder customHeaders(HttpHeader[] customHeaders);

        Builder customQueryParameters(String customQueryParameters);

        ListBucketIntelligentTieringConfigurationsRequest build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The name of the Amazon S3 bucket whose configuration you want to modify or retrieve.</p>
         */
        String bucket;

        /**
         * <p>The ContinuationToken that represents a placeholder from where this request should
         *          begin.</p>
         */
        String continuationToken;

        HttpHeader[] customHeaders;

        String customQueryParameters;

        protected BuilderImpl() {
        }

        private BuilderImpl(ListBucketIntelligentTieringConfigurationsRequest model) {
            bucket(model.bucket);
            continuationToken(model.continuationToken);
            customHeaders(model.customHeaders);
            customQueryParameters(model.customQueryParameters);
        }

        public ListBucketIntelligentTieringConfigurationsRequest build() {
            return new ListBucketIntelligentTieringConfigurationsRequest(this);
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder continuationToken(String continuationToken) {
            this.continuationToken = continuationToken;
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

        public HttpHeader[] customHeaders() {
            return customHeaders;
        }

        public String customQueryParameters() {
            return customQueryParameters;
        }
    }
}
