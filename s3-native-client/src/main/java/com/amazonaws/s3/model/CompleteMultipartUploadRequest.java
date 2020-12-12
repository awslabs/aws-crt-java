// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class CompleteMultipartUploadRequest {
    private String bucket;

    private String key;

    private CompletedMultipartUpload multipartUpload;

    private String uploadId;

    private RequestPayer requestPayer;

    private String expectedBucketOwner;

    private CompleteMultipartUploadRequest() {
        this.bucket = null;
        this.key = null;
        this.multipartUpload = null;
        this.uploadId = null;
        this.requestPayer = null;
        this.expectedBucketOwner = null;
    }

    private CompleteMultipartUploadRequest(Builder builder) {
        this.bucket = builder.bucket;
        this.key = builder.key;
        this.multipartUpload = builder.multipartUpload;
        this.uploadId = builder.uploadId;
        this.requestPayer = builder.requestPayer;
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

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public String key() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public CompletedMultipartUpload multipartUpload() {
        return multipartUpload;
    }

    public void setMultipartUpload(final CompletedMultipartUpload multipartUpload) {
        this.multipartUpload = multipartUpload;
    }

    public String uploadId() {
        return uploadId;
    }

    public void setUploadId(final String uploadId) {
        this.uploadId = uploadId;
    }

    public RequestPayer requestPayer() {
        return requestPayer;
    }

    public void setRequestPayer(final RequestPayer requestPayer) {
        this.requestPayer = requestPayer;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    static final class Builder {
        private String bucket;

        private String key;

        private CompletedMultipartUpload multipartUpload;

        private String uploadId;

        private RequestPayer requestPayer;

        private String expectedBucketOwner;

        private Builder() {
        }

        private Builder(CompleteMultipartUploadRequest model) {
            bucket(model.bucket);
            key(model.key);
            multipartUpload(model.multipartUpload);
            uploadId(model.uploadId);
            requestPayer(model.requestPayer);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public CompleteMultipartUploadRequest build() {
            return new com.amazonaws.s3.model.CompleteMultipartUploadRequest(this);
        }

        /**
         * <p>Name of the bucket to which the multipart upload was initiated.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>Object key for which the multipart upload was initiated.</p>
         */
        public final Builder key(String key) {
            this.key = key;
            return this;
        }

        /**
         * <p>The container for the multipart upload request information.</p>
         */
        public final Builder multipartUpload(CompletedMultipartUpload multipartUpload) {
            this.multipartUpload = multipartUpload;
            return this;
        }

        /**
         * <p>ID for the initiated multipart upload.</p>
         */
        public final Builder uploadId(String uploadId) {
            this.uploadId = uploadId;
            return this;
        }

        public final Builder requestPayer(RequestPayer requestPayer) {
            this.requestPayer = requestPayer;
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
