// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class AbortMultipartUploadRequest {
    private String bucket;

    private String key;

    private String uploadId;

    private RequestPayer requestPayer;

    private String expectedBucketOwner;

    private AbortMultipartUploadRequest() {
        this.bucket = null;
        this.key = null;
        this.uploadId = null;
        this.requestPayer = null;
        this.expectedBucketOwner = null;
    }

    private AbortMultipartUploadRequest(Builder builder) {
        this.bucket = builder.bucket;
        this.key = builder.key;
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
        return Objects.hash(AbortMultipartUploadRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof AbortMultipartUploadRequest);
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

        private String uploadId;

        private RequestPayer requestPayer;

        private String expectedBucketOwner;

        private Builder() {
        }

        private Builder(AbortMultipartUploadRequest model) {
            bucket(model.bucket);
            key(model.key);
            uploadId(model.uploadId);
            requestPayer(model.requestPayer);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public AbortMultipartUploadRequest build() {
            return new com.amazonaws.s3.model.AbortMultipartUploadRequest(this);
        }

        /**
         * <p>The bucket name to which the upload was taking place. </p>
         *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>Key of the object for which the multipart upload was initiated.</p>
         */
        public final Builder key(String key) {
            this.key = key;
            return this;
        }

        /**
         * <p>Upload ID that identifies the multipart upload.</p>
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
