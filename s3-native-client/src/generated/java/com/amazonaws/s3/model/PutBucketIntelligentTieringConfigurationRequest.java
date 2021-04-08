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
public class PutBucketIntelligentTieringConfigurationRequest {
    /**
     * <p>The name of the Amazon S3 bucket whose configuration you want to modify or retrieve.</p>
     */
    String bucket;

    /**
     * <p>The ID used to identify the S3 Intelligent-Tiering configuration.</p>
     */
    String id;

    /**
     * <p>Container for S3 Intelligent-Tiering configuration.</p>
     */
    IntelligentTieringConfiguration intelligentTieringConfiguration;

    HttpHeader[] customHeaders;

    String customQueryParameters;

    PutBucketIntelligentTieringConfigurationRequest() {
        this.bucket = "";
        this.id = "";
        this.intelligentTieringConfiguration = null;
        this.customHeaders = null;
        this.customQueryParameters = "";
    }

    protected PutBucketIntelligentTieringConfigurationRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.id = builder.id;
        this.intelligentTieringConfiguration = builder.intelligentTieringConfiguration;
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
        return Objects.hash(PutBucketIntelligentTieringConfigurationRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof PutBucketIntelligentTieringConfigurationRequest);
    }

    public String bucket() {
        return bucket;
    }

    public String id() {
        return id;
    }

    public IntelligentTieringConfiguration intelligentTieringConfiguration() {
        return intelligentTieringConfiguration;
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

        Builder intelligentTieringConfiguration(
                IntelligentTieringConfiguration intelligentTieringConfiguration);

        Builder customHeaders(HttpHeader[] customHeaders);

        Builder customQueryParameters(String customQueryParameters);

        PutBucketIntelligentTieringConfigurationRequest build();
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

        /**
         * <p>Container for S3 Intelligent-Tiering configuration.</p>
         */
        IntelligentTieringConfiguration intelligentTieringConfiguration;

        HttpHeader[] customHeaders;

        String customQueryParameters;

        protected BuilderImpl() {
        }

        private BuilderImpl(PutBucketIntelligentTieringConfigurationRequest model) {
            bucket(model.bucket);
            id(model.id);
            intelligentTieringConfiguration(model.intelligentTieringConfiguration);
            customHeaders(model.customHeaders);
            customQueryParameters(model.customQueryParameters);
        }

        public PutBucketIntelligentTieringConfigurationRequest build() {
            return new PutBucketIntelligentTieringConfigurationRequest(this);
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder id(String id) {
            this.id = id;
            return this;
        }

        public final Builder intelligentTieringConfiguration(
                IntelligentTieringConfiguration intelligentTieringConfiguration) {
            this.intelligentTieringConfiguration = intelligentTieringConfiguration;
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

        public IntelligentTieringConfiguration intelligentTieringConfiguration() {
            return intelligentTieringConfiguration;
        }

        public HttpHeader[] customHeaders() {
            return customHeaders;
        }

        public String customQueryParameters() {
            return customQueryParameters;
        }
    }
}
