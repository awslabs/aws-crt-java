// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutBucketCorsRequest {
    /**
     * <p>Specifies the bucket impacted by the <code>cors</code>configuration.</p>
     */
    String bucket;

    /**
     * <p>Describes the cross-origin access configuration for objects in an Amazon S3 bucket. For more
     *          information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/cors.html">Enabling Cross-Origin Resource
     *             Sharing</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     */
    CORSConfiguration cORSConfiguration;

    /**
     * <p>The base64-encoded 128-bit MD5 digest of the data. This header must be used as a message
     *          integrity check to verify that the request body was not corrupted in transit. For more
     *          information, go to <a href="http://www.ietf.org/rfc/rfc1864.txt">RFC
     *          1864.</a>
     *          </p>
     *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
     */
    String contentMD5;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    PutBucketCorsRequest() {
        this.bucket = "";
        this.cORSConfiguration = null;
        this.contentMD5 = "";
        this.expectedBucketOwner = "";
    }

    protected PutBucketCorsRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.cORSConfiguration = builder.cORSConfiguration;
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
        return Objects.hash(PutBucketCorsRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof PutBucketCorsRequest);
    }

    public String bucket() {
        return bucket;
    }

    public CORSConfiguration cORSConfiguration() {
        return cORSConfiguration;
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

    public void setCORSConfiguration(final CORSConfiguration cORSConfiguration) {
        this.cORSConfiguration = cORSConfiguration;
    }

    public void setContentMD5(final String contentMD5) {
        this.contentMD5 = contentMD5;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    public interface Builder {
        Builder bucket(String bucket);

        Builder cORSConfiguration(CORSConfiguration cORSConfiguration);

        Builder contentMD5(String contentMD5);

        Builder expectedBucketOwner(String expectedBucketOwner);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Specifies the bucket impacted by the <code>cors</code>configuration.</p>
         */
        String bucket;

        /**
         * <p>Describes the cross-origin access configuration for objects in an Amazon S3 bucket. For more
         *          information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/cors.html">Enabling Cross-Origin Resource
         *             Sharing</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        CORSConfiguration cORSConfiguration;

        /**
         * <p>The base64-encoded 128-bit MD5 digest of the data. This header must be used as a message
         *          integrity check to verify that the request body was not corrupted in transit. For more
         *          information, go to <a href="http://www.ietf.org/rfc/rfc1864.txt">RFC
         *          1864.</a>
         *          </p>
         *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
         */
        String contentMD5;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        protected BuilderImpl() {
        }

        private BuilderImpl(PutBucketCorsRequest model) {
            bucket(model.bucket);
            cORSConfiguration(model.cORSConfiguration);
            contentMD5(model.contentMD5);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public PutBucketCorsRequest build() {
            return new PutBucketCorsRequest(this);
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder cORSConfiguration(CORSConfiguration cORSConfiguration) {
            this.cORSConfiguration = cORSConfiguration;
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

        public CORSConfiguration cORSConfiguration() {
            return cORSConfiguration;
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

        public void setCORSConfiguration(final CORSConfiguration cORSConfiguration) {
            this.cORSConfiguration = cORSConfiguration;
        }

        public void setContentMD5(final String contentMD5) {
            this.contentMD5 = contentMD5;
        }

        public void setExpectedBucketOwner(final String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
        }
    }
}
