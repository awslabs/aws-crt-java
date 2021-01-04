// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutBucketWebsiteRequest {
    /**
     * <p>The bucket name.</p>
     */
    String bucket;

    /**
     * <p>The base64-encoded 128-bit MD5 digest of the data. You must use this header as a message
     *          integrity check to verify that the request body was not corrupted in transit. For more
     *          information, see <a href="http://www.ietf.org/rfc/rfc1864.txt">RFC 1864</a>.</p>
     *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
     */
    String contentMD5;

    /**
     * <p>Container for the request.</p>
     */
    WebsiteConfiguration websiteConfiguration;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    PutBucketWebsiteRequest() {
        this.bucket = "";
        this.contentMD5 = "";
        this.websiteConfiguration = null;
        this.expectedBucketOwner = "";
    }

    protected PutBucketWebsiteRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.contentMD5 = builder.contentMD5;
        this.websiteConfiguration = builder.websiteConfiguration;
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
        return Objects.hash(PutBucketWebsiteRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof PutBucketWebsiteRequest);
    }

    public String bucket() {
        return bucket;
    }

    public String contentMD5() {
        return contentMD5;
    }

    public WebsiteConfiguration websiteConfiguration() {
        return websiteConfiguration;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public void setContentMD5(final String contentMD5) {
        this.contentMD5 = contentMD5;
    }

    public void setWebsiteConfiguration(final WebsiteConfiguration websiteConfiguration) {
        this.websiteConfiguration = websiteConfiguration;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    public interface Builder {
        Builder bucket(String bucket);

        Builder contentMD5(String contentMD5);

        Builder websiteConfiguration(WebsiteConfiguration websiteConfiguration);

        Builder expectedBucketOwner(String expectedBucketOwner);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The bucket name.</p>
         */
        String bucket;

        /**
         * <p>The base64-encoded 128-bit MD5 digest of the data. You must use this header as a message
         *          integrity check to verify that the request body was not corrupted in transit. For more
         *          information, see <a href="http://www.ietf.org/rfc/rfc1864.txt">RFC 1864</a>.</p>
         *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
         */
        String contentMD5;

        /**
         * <p>Container for the request.</p>
         */
        WebsiteConfiguration websiteConfiguration;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        protected BuilderImpl() {
        }

        private BuilderImpl(PutBucketWebsiteRequest model) {
            bucket(model.bucket);
            contentMD5(model.contentMD5);
            websiteConfiguration(model.websiteConfiguration);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public PutBucketWebsiteRequest build() {
            return new PutBucketWebsiteRequest(this);
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder contentMD5(String contentMD5) {
            this.contentMD5 = contentMD5;
            return this;
        }

        public final Builder websiteConfiguration(WebsiteConfiguration websiteConfiguration) {
            this.websiteConfiguration = websiteConfiguration;
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

        public String contentMD5() {
            return contentMD5;
        }

        public WebsiteConfiguration websiteConfiguration() {
            return websiteConfiguration;
        }

        public String expectedBucketOwner() {
            return expectedBucketOwner;
        }

        public void setBucket(final String bucket) {
            this.bucket = bucket;
        }

        public void setContentMD5(final String contentMD5) {
            this.contentMD5 = contentMD5;
        }

        public void setWebsiteConfiguration(final WebsiteConfiguration websiteConfiguration) {
            this.websiteConfiguration = websiteConfiguration;
        }

        public void setExpectedBucketOwner(final String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
        }
    }
}
