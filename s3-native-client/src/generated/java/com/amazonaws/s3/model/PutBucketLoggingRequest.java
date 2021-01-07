// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutBucketLoggingRequest {
    /**
     * <p>The name of the bucket for which to set the logging parameters.</p>
     */
    String bucket;

    /**
     * <p>Container for logging status information.</p>
     */
    BucketLoggingStatus bucketLoggingStatus;

    /**
     * <p>The MD5 hash of the <code>PutBucketLogging</code> request body.</p>
     *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
     */
    String contentMD5;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    PutBucketLoggingRequest() {
        this.bucket = "";
        this.bucketLoggingStatus = null;
        this.contentMD5 = "";
        this.expectedBucketOwner = "";
    }

    protected PutBucketLoggingRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.bucketLoggingStatus = builder.bucketLoggingStatus;
        this.contentMD5 = builder.contentMD5;
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

    public BucketLoggingStatus bucketLoggingStatus() {
        return bucketLoggingStatus;
    }

    public String contentMD5() {
        return contentMD5;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public void setBucketLoggingStatus(final BucketLoggingStatus bucketLoggingStatus) {
        this.bucketLoggingStatus = bucketLoggingStatus;
    }

    public void setContentMD5(final String contentMD5) {
        this.contentMD5 = contentMD5;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    public interface Builder {
        Builder bucket(String bucket);

        Builder bucketLoggingStatus(BucketLoggingStatus bucketLoggingStatus);

        Builder contentMD5(String contentMD5);

        Builder expectedBucketOwner(String expectedBucketOwner);

        PutBucketLoggingRequest build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The name of the bucket for which to set the logging parameters.</p>
         */
        String bucket;

        /**
         * <p>Container for logging status information.</p>
         */
        BucketLoggingStatus bucketLoggingStatus;

        /**
         * <p>The MD5 hash of the <code>PutBucketLogging</code> request body.</p>
         *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
         */
        String contentMD5;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        protected BuilderImpl() {
        }

        private BuilderImpl(PutBucketLoggingRequest model) {
            bucket(model.bucket);
            bucketLoggingStatus(model.bucketLoggingStatus);
            contentMD5(model.contentMD5);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public PutBucketLoggingRequest build() {
            return new PutBucketLoggingRequest(this);
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder bucketLoggingStatus(BucketLoggingStatus bucketLoggingStatus) {
            this.bucketLoggingStatus = bucketLoggingStatus;
            return this;
        }

        public final Builder contentMD5(String contentMD5) {
            this.contentMD5 = contentMD5;
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

        public BucketLoggingStatus bucketLoggingStatus() {
            return bucketLoggingStatus;
        }

        public String contentMD5() {
            return contentMD5;
        }

        public String expectedBucketOwner() {
            return expectedBucketOwner;
        }

        public void setBucket(final String bucket) {
            this.bucket = bucket;
        }

        public void setBucketLoggingStatus(final BucketLoggingStatus bucketLoggingStatus) {
            this.bucketLoggingStatus = bucketLoggingStatus;
        }

        public void setContentMD5(final String contentMD5) {
            this.contentMD5 = contentMD5;
        }

        public void setExpectedBucketOwner(final String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
        }
    }
}
