// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Map;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;
import software.amazon.awssdk.crt.http.HttpHeader;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutPublicAccessBlockRequest {
    /**
     * <p>The name of the Amazon S3 bucket whose <code>PublicAccessBlock</code> configuration you want
     *          to set.</p>
     */
    String bucket;

    /**
     * <p>The MD5 hash of the <code>PutPublicAccessBlock</code> request body. </p>
     *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
     */
    String contentMD5;

    /**
     * <p>The <code>PublicAccessBlock</code> configuration that you want to apply to this Amazon S3
     *          bucket. You can enable the configuration options in any combination. For more information
     *          about when Amazon S3 considers a bucket or object public, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/access-control-block-public-access.html#access-control-block-public-access-policy-status">The Meaning of "Public"</a> in the <i>Amazon Simple Storage Service Developer
     *          Guide</i>.</p>
     */
    PublicAccessBlockConfiguration publicAccessBlockConfiguration;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    HttpHeader[] customHeaders;

    Map<String, String> customQueryParameters;

    PutPublicAccessBlockRequest() {
        this.bucket = "";
        this.contentMD5 = "";
        this.publicAccessBlockConfiguration = null;
        this.expectedBucketOwner = "";
        this.customHeaders = null;
        this.customQueryParameters = null;
    }

    protected PutPublicAccessBlockRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.contentMD5 = builder.contentMD5;
        this.publicAccessBlockConfiguration = builder.publicAccessBlockConfiguration;
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
        return Objects.hash(PutPublicAccessBlockRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof PutPublicAccessBlockRequest);
    }

    public String bucket() {
        return bucket;
    }

    public String contentMD5() {
        return contentMD5;
    }

    public PublicAccessBlockConfiguration publicAccessBlockConfiguration() {
        return publicAccessBlockConfiguration;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public HttpHeader[] customHeaders() {
        return customHeaders;
    }

    public Map<String, String> customQueryParameters() {
        return customQueryParameters;
    }

    public interface Builder {
        Builder bucket(String bucket);

        Builder contentMD5(String contentMD5);

        Builder publicAccessBlockConfiguration(
                PublicAccessBlockConfiguration publicAccessBlockConfiguration);

        Builder expectedBucketOwner(String expectedBucketOwner);

        Builder customHeaders(HttpHeader[] customHeaders);

        Builder customQueryParameters(Map<String, String> customQueryParameters);

        PutPublicAccessBlockRequest build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The name of the Amazon S3 bucket whose <code>PublicAccessBlock</code> configuration you want
         *          to set.</p>
         */
        String bucket;

        /**
         * <p>The MD5 hash of the <code>PutPublicAccessBlock</code> request body. </p>
         *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
         */
        String contentMD5;

        /**
         * <p>The <code>PublicAccessBlock</code> configuration that you want to apply to this Amazon S3
         *          bucket. You can enable the configuration options in any combination. For more information
         *          about when Amazon S3 considers a bucket or object public, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/access-control-block-public-access.html#access-control-block-public-access-policy-status">The Meaning of "Public"</a> in the <i>Amazon Simple Storage Service Developer
         *          Guide</i>.</p>
         */
        PublicAccessBlockConfiguration publicAccessBlockConfiguration;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        HttpHeader[] customHeaders;

        Map<String, String> customQueryParameters;

        protected BuilderImpl() {
        }

        private BuilderImpl(PutPublicAccessBlockRequest model) {
            bucket(model.bucket);
            contentMD5(model.contentMD5);
            publicAccessBlockConfiguration(model.publicAccessBlockConfiguration);
            expectedBucketOwner(model.expectedBucketOwner);
            customHeaders(model.customHeaders);
            customQueryParameters(model.customQueryParameters);
        }

        public PutPublicAccessBlockRequest build() {
            return new PutPublicAccessBlockRequest(this);
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder contentMD5(String contentMD5) {
            this.contentMD5 = contentMD5;
            return this;
        }

        public final Builder publicAccessBlockConfiguration(
                PublicAccessBlockConfiguration publicAccessBlockConfiguration) {
            this.publicAccessBlockConfiguration = publicAccessBlockConfiguration;
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

        public final Builder customQueryParameters(Map<String, String> customQueryParameters) {
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

        public String contentMD5() {
            return contentMD5;
        }

        public PublicAccessBlockConfiguration publicAccessBlockConfiguration() {
            return publicAccessBlockConfiguration;
        }

        public String expectedBucketOwner() {
            return expectedBucketOwner;
        }

        public HttpHeader[] customHeaders() {
            return customHeaders;
        }

        public Map<String, String> customQueryParameters() {
            return customQueryParameters;
        }
    }
}
