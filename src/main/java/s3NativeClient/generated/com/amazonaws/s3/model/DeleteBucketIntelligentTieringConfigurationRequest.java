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
public class DeleteBucketIntelligentTieringConfigurationRequest {
    /**
     * <p>The name of the Amazon S3 bucket whose configuration you want to modify or retrieve.</p>
     */
    String bucket;

    /**
     * <p>The ID used to identify the S3 Intelligent-Tiering configuration.</p>
     */
    String id;

    HttpHeader[] customHeaders;

    String customQueryParameters;

    DeleteBucketIntelligentTieringConfigurationRequest() {
        this.bucket = "";
        this.id = "";
        this.customHeaders = null;
        this.customQueryParameters = "";
    }

    protected DeleteBucketIntelligentTieringConfigurationRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.id = builder.id;
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
        return Objects.hash(DeleteBucketIntelligentTieringConfigurationRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof DeleteBucketIntelligentTieringConfigurationRequest);
    }

    public String bucket() {
        return bucket;
    }

    public String id() {
        return id;
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

        Builder customHeaders(HttpHeader[] customHeaders);

        Builder customQueryParameters(String customQueryParameters);

        DeleteBucketIntelligentTieringConfigurationRequest build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The name of the Amazon S3 bucket whose configuration you want to modify or retrieve.</p>
         */
        String bucket;

        /**
         * <p>The ID used to identify the S3 Intelligent-Tiering configuration.</p>
         */
        String id;

        HttpHeader[] customHeaders;

        String customQueryParameters;

        protected BuilderImpl() {
        }

        private BuilderImpl(DeleteBucketIntelligentTieringConfigurationRequest model) {
            bucket(model.bucket);
            id(model.id);
            customHeaders(model.customHeaders);
            customQueryParameters(model.customQueryParameters);
        }

        public DeleteBucketIntelligentTieringConfigurationRequest build() {
            return new DeleteBucketIntelligentTieringConfigurationRequest(this);
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder id(String id) {
            this.id = id;
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

        public HttpHeader[] customHeaders() {
            return customHeaders;
        }

        public String customQueryParameters() {
            return customQueryParameters;
        }
    }
}
