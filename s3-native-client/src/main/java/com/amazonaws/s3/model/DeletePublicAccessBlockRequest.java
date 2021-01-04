// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class DeletePublicAccessBlockRequest {
    /**
     * <p>The Amazon S3 bucket whose <code>PublicAccessBlock</code> configuration you want to delete.
     *       </p>
     */
    String bucket;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    DeletePublicAccessBlockRequest() {
        this.bucket = "";
        this.expectedBucketOwner = "";
    }

    protected DeletePublicAccessBlockRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
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
        return Objects.hash(DeletePublicAccessBlockRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof DeletePublicAccessBlockRequest);
    }

    public String bucket() {
        return bucket;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    public interface Builder {
        Builder bucket(String bucket);

        Builder expectedBucketOwner(String expectedBucketOwner);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The Amazon S3 bucket whose <code>PublicAccessBlock</code> configuration you want to delete.
         *       </p>
         */
        String bucket;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        protected BuilderImpl() {
        }

        private BuilderImpl(DeletePublicAccessBlockRequest model) {
            bucket(model.bucket);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public DeletePublicAccessBlockRequest build() {
            return new DeletePublicAccessBlockRequest(this);
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
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

        public String expectedBucketOwner() {
            return expectedBucketOwner;
        }

        public void setBucket(final String bucket) {
            this.bucket = bucket;
        }

        public void setExpectedBucketOwner(final String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
        }
    }
}
