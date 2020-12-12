// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutBucketLoggingRequest {
    private String bucket;

    private BucketLoggingStatus bucketLoggingStatus;

    private String contentMD5;

    private String expectedBucketOwner;

    private PutBucketLoggingRequest() {
        this.bucket = null;
        this.bucketLoggingStatus = null;
        this.contentMD5 = null;
        this.expectedBucketOwner = null;
    }

    private PutBucketLoggingRequest(Builder builder) {
        this.bucket = builder.bucket;
        this.bucketLoggingStatus = builder.bucketLoggingStatus;
        this.contentMD5 = builder.contentMD5;
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
        return Objects.hash(PutBucketLoggingRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof PutBucketLoggingRequest);
    }

    public String bucket() {
        return bucket;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public BucketLoggingStatus bucketLoggingStatus() {
        return bucketLoggingStatus;
    }

    public void setBucketLoggingStatus(final BucketLoggingStatus bucketLoggingStatus) {
        this.bucketLoggingStatus = bucketLoggingStatus;
    }

    public String contentMD5() {
        return contentMD5;
    }

    public void setContentMD5(final String contentMD5) {
        this.contentMD5 = contentMD5;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    static final class Builder {
        private String bucket;

        private BucketLoggingStatus bucketLoggingStatus;

        private String contentMD5;

        private String expectedBucketOwner;

        private Builder() {
        }

        private Builder(PutBucketLoggingRequest model) {
            bucket(model.bucket);
            bucketLoggingStatus(model.bucketLoggingStatus);
            contentMD5(model.contentMD5);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public PutBucketLoggingRequest build() {
            return new com.amazonaws.s3.model.PutBucketLoggingRequest(this);
        }

        /**
         * <p>The name of the bucket for which to set the logging parameters.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>Container for logging status information.</p>
         */
        public final Builder bucketLoggingStatus(BucketLoggingStatus bucketLoggingStatus) {
            this.bucketLoggingStatus = bucketLoggingStatus;
            return this;
        }

        /**
         * <p>The MD5 hash of the <code>PutBucketLogging</code> request body.</p>
         *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
         */
        public final Builder contentMD5(String contentMD5) {
            this.contentMD5 = contentMD5;
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
