// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutPublicAccessBlockRequest {
    private String bucket;

    private String contentMD5;

    private PublicAccessBlockConfiguration publicAccessBlockConfiguration;

    private String expectedBucketOwner;

    private PutPublicAccessBlockRequest() {
        this.bucket = null;
        this.contentMD5 = null;
        this.publicAccessBlockConfiguration = null;
        this.expectedBucketOwner = null;
    }

    private PutPublicAccessBlockRequest(Builder builder) {
        this.bucket = builder.bucket;
        this.contentMD5 = builder.contentMD5;
        this.publicAccessBlockConfiguration = builder.publicAccessBlockConfiguration;
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

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public String contentMD5() {
        return contentMD5;
    }

    public void setContentMD5(final String contentMD5) {
        this.contentMD5 = contentMD5;
    }

    public PublicAccessBlockConfiguration publicAccessBlockConfiguration() {
        return publicAccessBlockConfiguration;
    }

    public void setPublicAccessBlockConfiguration(
            final PublicAccessBlockConfiguration publicAccessBlockConfiguration) {
        this.publicAccessBlockConfiguration = publicAccessBlockConfiguration;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    static final class Builder {
        private String bucket;

        private String contentMD5;

        private PublicAccessBlockConfiguration publicAccessBlockConfiguration;

        private String expectedBucketOwner;

        private Builder() {
        }

        private Builder(PutPublicAccessBlockRequest model) {
            bucket(model.bucket);
            contentMD5(model.contentMD5);
            publicAccessBlockConfiguration(model.publicAccessBlockConfiguration);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public PutPublicAccessBlockRequest build() {
            return new com.amazonaws.s3.model.PutPublicAccessBlockRequest(this);
        }

        /**
         * <p>The name of the Amazon S3 bucket whose <code>PublicAccessBlock</code> configuration you want
         *          to set.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>The MD5 hash of the <code>PutPublicAccessBlock</code> request body. </p>
         *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
         */
        public final Builder contentMD5(String contentMD5) {
            this.contentMD5 = contentMD5;
            return this;
        }

        /**
         * <p>The <code>PublicAccessBlock</code> configuration that you want to apply to this Amazon S3
         *          bucket. You can enable the configuration options in any combination. For more information
         *          about when Amazon S3 considers a bucket or object public, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/access-control-block-public-access.html#access-control-block-public-access-policy-status">The Meaning of "Public"</a> in the <i>Amazon Simple Storage Service Developer
         *          Guide</i>.</p>
         */
        public final Builder publicAccessBlockConfiguration(
                PublicAccessBlockConfiguration publicAccessBlockConfiguration) {
            this.publicAccessBlockConfiguration = publicAccessBlockConfiguration;
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
