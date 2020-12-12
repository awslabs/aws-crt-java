// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutBucketRequestPaymentRequest {
    private String bucket;

    private String contentMD5;

    private RequestPaymentConfiguration requestPaymentConfiguration;

    private String expectedBucketOwner;

    private PutBucketRequestPaymentRequest() {
        this.bucket = null;
        this.contentMD5 = null;
        this.requestPaymentConfiguration = null;
        this.expectedBucketOwner = null;
    }

    private PutBucketRequestPaymentRequest(Builder builder) {
        this.bucket = builder.bucket;
        this.contentMD5 = builder.contentMD5;
        this.requestPaymentConfiguration = builder.requestPaymentConfiguration;
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
        return Objects.hash(PutBucketRequestPaymentRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof PutBucketRequestPaymentRequest);
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

    public RequestPaymentConfiguration requestPaymentConfiguration() {
        return requestPaymentConfiguration;
    }

    public void setRequestPaymentConfiguration(
            final RequestPaymentConfiguration requestPaymentConfiguration) {
        this.requestPaymentConfiguration = requestPaymentConfiguration;
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

        private RequestPaymentConfiguration requestPaymentConfiguration;

        private String expectedBucketOwner;

        private Builder() {
        }

        private Builder(PutBucketRequestPaymentRequest model) {
            bucket(model.bucket);
            contentMD5(model.contentMD5);
            requestPaymentConfiguration(model.requestPaymentConfiguration);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public PutBucketRequestPaymentRequest build() {
            return new com.amazonaws.s3.model.PutBucketRequestPaymentRequest(this);
        }

        /**
         * <p>The bucket name.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>>The base64-encoded 128-bit MD5 digest of the data. You must use this header as a
         *          message integrity check to verify that the request body was not corrupted in transit. For
         *          more information, see <a href="http://www.ietf.org/rfc/rfc1864.txt">RFC
         *          1864</a>.</p>
         *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
         */
        public final Builder contentMD5(String contentMD5) {
            this.contentMD5 = contentMD5;
            return this;
        }

        /**
         * <p>Container for Payer.</p>
         */
        public final Builder requestPaymentConfiguration(
                RequestPaymentConfiguration requestPaymentConfiguration) {
            this.requestPaymentConfiguration = requestPaymentConfiguration;
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
