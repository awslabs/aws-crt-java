// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutBucketTaggingRequest {
    private String bucket;

    private String contentMD5;

    private Tagging tagging;

    private String expectedBucketOwner;

    private PutBucketTaggingRequest() {
        this.bucket = null;
        this.contentMD5 = null;
        this.tagging = null;
        this.expectedBucketOwner = null;
    }

    private PutBucketTaggingRequest(Builder builder) {
        this.bucket = builder.bucket;
        this.contentMD5 = builder.contentMD5;
        this.tagging = builder.tagging;
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
        return Objects.hash(PutBucketTaggingRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof PutBucketTaggingRequest);
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

    public Tagging tagging() {
        return tagging;
    }

    public void setTagging(final Tagging tagging) {
        this.tagging = tagging;
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

        private Tagging tagging;

        private String expectedBucketOwner;

        private Builder() {
        }

        private Builder(PutBucketTaggingRequest model) {
            bucket(model.bucket);
            contentMD5(model.contentMD5);
            tagging(model.tagging);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public PutBucketTaggingRequest build() {
            return new com.amazonaws.s3.model.PutBucketTaggingRequest(this);
        }

        /**
         * <p>The bucket name.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>The base64-encoded 128-bit MD5 digest of the data. You must use this header as a message
         *          integrity check to verify that the request body was not corrupted in transit. For more
         *          information, see <a href="http://www.ietf.org/rfc/rfc1864.txt">RFC 1864</a>.</p>
         *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
         */
        public final Builder contentMD5(String contentMD5) {
            this.contentMD5 = contentMD5;
            return this;
        }

        /**
         * <p>Container for the <code>TagSet</code> and <code>Tag</code> elements.</p>
         */
        public final Builder tagging(Tagging tagging) {
            this.tagging = tagging;
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
