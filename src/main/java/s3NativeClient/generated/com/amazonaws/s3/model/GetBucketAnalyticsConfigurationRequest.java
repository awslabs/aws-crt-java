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
public class GetBucketAnalyticsConfigurationRequest {
    /**
     * <p>The name of the bucket from which an analytics configuration is retrieved.</p>
     */
    String bucket;

    /**
     * <p>The ID that identifies the analytics configuration.</p>
     */
    String id;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    HttpHeader[] customHeaders;

    String customQueryParameters;

    GetBucketAnalyticsConfigurationRequest() {
        this.bucket = "";
        this.id = "";
        this.expectedBucketOwner = "";
        this.customHeaders = null;
        this.customQueryParameters = "";
    }

    protected GetBucketAnalyticsConfigurationRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.id = builder.id;
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
        return Objects.hash(GetBucketAnalyticsConfigurationRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetBucketAnalyticsConfigurationRequest);
    }

    public String bucket() {
        return bucket;
    }

    public String id() {
        return id;
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

        Builder id(String id);

        Builder expectedBucketOwner(String expectedBucketOwner);

        Builder customHeaders(HttpHeader[] customHeaders);

        Builder customQueryParameters(String customQueryParameters);

        GetBucketAnalyticsConfigurationRequest build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The name of the bucket from which an analytics configuration is retrieved.</p>
         */
        String bucket;

        /**
         * <p>The ID that identifies the analytics configuration.</p>
         */
        String id;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        HttpHeader[] customHeaders;

        String customQueryParameters;

        protected BuilderImpl() {
        }

        private BuilderImpl(GetBucketAnalyticsConfigurationRequest model) {
            bucket(model.bucket);
            id(model.id);
            expectedBucketOwner(model.expectedBucketOwner);
            customHeaders(model.customHeaders);
            customQueryParameters(model.customQueryParameters);
        }

        public GetBucketAnalyticsConfigurationRequest build() {
            return new GetBucketAnalyticsConfigurationRequest(this);
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder id(String id) {
            this.id = id;
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

        public String id() {
            return id;
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
