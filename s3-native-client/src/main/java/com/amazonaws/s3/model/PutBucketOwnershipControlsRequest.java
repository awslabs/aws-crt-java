// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutBucketOwnershipControlsRequest {
    /**
     * <p>The name of the Amazon S3 bucket whose <code>OwnershipControls</code> you want to set.</p>
     */
    String bucket;

    /**
     * <p>The MD5 hash of the <code>OwnershipControls</code> request body. </p>
     *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
     */
    String contentMD5;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    /**
     * <p>The <code>OwnershipControls</code> (BucketOwnerPreferred or ObjectWriter) that you want
     *          to apply to this Amazon S3 bucket.</p>
     */
    OwnershipControls ownershipControls;

    PutBucketOwnershipControlsRequest() {
        this.bucket = "";
        this.contentMD5 = "";
        this.expectedBucketOwner = "";
        this.ownershipControls = null;
    }

    protected PutBucketOwnershipControlsRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.contentMD5 = builder.contentMD5;
        this.expectedBucketOwner = builder.expectedBucketOwner;
        this.ownershipControls = builder.ownershipControls;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(PutBucketOwnershipControlsRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof PutBucketOwnershipControlsRequest);
    }

    public String bucket() {
        return bucket;
    }

    public String contentMD5() {
        return contentMD5;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public OwnershipControls ownershipControls() {
        return ownershipControls;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public void setContentMD5(final String contentMD5) {
        this.contentMD5 = contentMD5;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    public void setOwnershipControls(final OwnershipControls ownershipControls) {
        this.ownershipControls = ownershipControls;
    }

    public interface Builder {
        Builder bucket(String bucket);

        Builder contentMD5(String contentMD5);

        Builder expectedBucketOwner(String expectedBucketOwner);

        Builder ownershipControls(OwnershipControls ownershipControls);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The name of the Amazon S3 bucket whose <code>OwnershipControls</code> you want to set.</p>
         */
        String bucket;

        /**
         * <p>The MD5 hash of the <code>OwnershipControls</code> request body. </p>
         *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
         */
        String contentMD5;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        /**
         * <p>The <code>OwnershipControls</code> (BucketOwnerPreferred or ObjectWriter) that you want
         *          to apply to this Amazon S3 bucket.</p>
         */
        OwnershipControls ownershipControls;

        protected BuilderImpl() {
        }

        private BuilderImpl(PutBucketOwnershipControlsRequest model) {
            bucket(model.bucket);
            contentMD5(model.contentMD5);
            expectedBucketOwner(model.expectedBucketOwner);
            ownershipControls(model.ownershipControls);
        }

        public PutBucketOwnershipControlsRequest build() {
            return new PutBucketOwnershipControlsRequest(this);
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
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

        public final Builder ownershipControls(OwnershipControls ownershipControls) {
            this.ownershipControls = ownershipControls;
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

        public String expectedBucketOwner() {
            return expectedBucketOwner;
        }

        public OwnershipControls ownershipControls() {
            return ownershipControls;
        }

        public void setBucket(final String bucket) {
            this.bucket = bucket;
        }

        public void setContentMD5(final String contentMD5) {
            this.contentMD5 = contentMD5;
        }

        public void setExpectedBucketOwner(final String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
        }

        public void setOwnershipControls(final OwnershipControls ownershipControls) {
            this.ownershipControls = ownershipControls;
        }
    }
}
