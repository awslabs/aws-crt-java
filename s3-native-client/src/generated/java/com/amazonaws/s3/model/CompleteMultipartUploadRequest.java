// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class CompleteMultipartUploadRequest {
    /**
     * <p>Name of the bucket to which the multipart upload was initiated.</p>
     */
    String bucket;

    /**
     * <p>Object key for which the multipart upload was initiated.</p>
     */
    String key;

    /**
     * <p>The container for the multipart upload request information.</p>
     */
    CompletedMultipartUpload multipartUpload;

    /**
     * <p>ID for the initiated multipart upload.</p>
     */
    String uploadId;

    RequestPayer requestPayer;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    CompleteMultipartUploadRequest() {
        this.bucket = "";
        this.key = "";
        this.multipartUpload = null;
        this.uploadId = "";
        this.requestPayer = null;
        this.expectedBucketOwner = "";
    }

    protected CompleteMultipartUploadRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.key = builder.key;
        this.multipartUpload = builder.multipartUpload;
        this.uploadId = builder.uploadId;
        this.requestPayer = builder.requestPayer;
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
        return Objects.hash(CompleteMultipartUploadRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof CompleteMultipartUploadRequest);
    }

    public String bucket() {
        return bucket;
    }

    public String key() {
        return key;
    }

    public CompletedMultipartUpload multipartUpload() {
        return multipartUpload;
    }

    public String uploadId() {
        return uploadId;
    }

    public RequestPayer requestPayer() {
        return requestPayer;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public void setMultipartUpload(final CompletedMultipartUpload multipartUpload) {
        this.multipartUpload = multipartUpload;
    }

    public void setUploadId(final String uploadId) {
        this.uploadId = uploadId;
    }

    public void setRequestPayer(final RequestPayer requestPayer) {
        this.requestPayer = requestPayer;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    public interface Builder {
        Builder bucket(String bucket);

        Builder key(String key);

        Builder multipartUpload(CompletedMultipartUpload multipartUpload);

        Builder uploadId(String uploadId);

        Builder requestPayer(RequestPayer requestPayer);

        Builder expectedBucketOwner(String expectedBucketOwner);

        CompleteMultipartUploadRequest build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Name of the bucket to which the multipart upload was initiated.</p>
         */
        String bucket;

        /**
         * <p>Object key for which the multipart upload was initiated.</p>
         */
        String key;

        /**
         * <p>The container for the multipart upload request information.</p>
         */
        CompletedMultipartUpload multipartUpload;

        /**
         * <p>ID for the initiated multipart upload.</p>
         */
        String uploadId;

        RequestPayer requestPayer;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        protected BuilderImpl() {
        }

        private BuilderImpl(CompleteMultipartUploadRequest model) {
            bucket(model.bucket);
            key(model.key);
            multipartUpload(model.multipartUpload);
            uploadId(model.uploadId);
            requestPayer(model.requestPayer);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public CompleteMultipartUploadRequest build() {
            return new CompleteMultipartUploadRequest(this);
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder key(String key) {
            this.key = key;
            return this;
        }

        public final Builder multipartUpload(CompletedMultipartUpload multipartUpload) {
            this.multipartUpload = multipartUpload;
            return this;
        }

        public final Builder uploadId(String uploadId) {
            this.uploadId = uploadId;
            return this;
        }

        public final Builder requestPayer(RequestPayer requestPayer) {
            this.requestPayer = requestPayer;
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

        public String key() {
            return key;
        }

        public CompletedMultipartUpload multipartUpload() {
            return multipartUpload;
        }

        public String uploadId() {
            return uploadId;
        }

        public RequestPayer requestPayer() {
            return requestPayer;
        }

        public String expectedBucketOwner() {
            return expectedBucketOwner;
        }

        public void setBucket(final String bucket) {
            this.bucket = bucket;
        }

        public void setKey(final String key) {
            this.key = key;
        }

        public void setMultipartUpload(final CompletedMultipartUpload multipartUpload) {
            this.multipartUpload = multipartUpload;
        }

        public void setUploadId(final String uploadId) {
            this.uploadId = uploadId;
        }

        public void setRequestPayer(final RequestPayer requestPayer) {
            this.requestPayer = requestPayer;
        }

        public void setExpectedBucketOwner(final String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
        }
    }
}
