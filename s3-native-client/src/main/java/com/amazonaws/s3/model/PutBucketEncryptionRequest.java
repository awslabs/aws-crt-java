// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutBucketEncryptionRequest {
    private String bucket;

    private String contentMD5;

    private ServerSideEncryptionConfiguration serverSideEncryptionConfiguration;

    private String expectedBucketOwner;

    private PutBucketEncryptionRequest() {
        this.bucket = null;
        this.contentMD5 = null;
        this.serverSideEncryptionConfiguration = null;
        this.expectedBucketOwner = null;
    }

    private PutBucketEncryptionRequest(Builder builder) {
        this.bucket = builder.bucket;
        this.contentMD5 = builder.contentMD5;
        this.serverSideEncryptionConfiguration = builder.serverSideEncryptionConfiguration;
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
        return Objects.hash(PutBucketEncryptionRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof PutBucketEncryptionRequest);
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

    public ServerSideEncryptionConfiguration serverSideEncryptionConfiguration() {
        return serverSideEncryptionConfiguration;
    }

    public void setServerSideEncryptionConfiguration(
            final ServerSideEncryptionConfiguration serverSideEncryptionConfiguration) {
        this.serverSideEncryptionConfiguration = serverSideEncryptionConfiguration;
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

        private ServerSideEncryptionConfiguration serverSideEncryptionConfiguration;

        private String expectedBucketOwner;

        private Builder() {
        }

        private Builder(PutBucketEncryptionRequest model) {
            bucket(model.bucket);
            contentMD5(model.contentMD5);
            serverSideEncryptionConfiguration(model.serverSideEncryptionConfiguration);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public PutBucketEncryptionRequest build() {
            return new com.amazonaws.s3.model.PutBucketEncryptionRequest(this);
        }

        /**
         * <p>Specifies default encryption for a bucket using server-side encryption with Amazon S3-managed
         *          keys (SSE-S3) or customer master keys stored in AWS KMS (SSE-KMS). For information about
         *          the Amazon S3 default encryption feature, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/bucket-encryption.html">Amazon S3 Default Bucket Encryption</a>
         *          in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>The base64-encoded 128-bit MD5 digest of the server-side encryption configuration.</p>
         *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
         */
        public final Builder contentMD5(String contentMD5) {
            this.contentMD5 = contentMD5;
            return this;
        }

        public final Builder serverSideEncryptionConfiguration(
                ServerSideEncryptionConfiguration serverSideEncryptionConfiguration) {
            this.serverSideEncryptionConfiguration = serverSideEncryptionConfiguration;
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
