// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class AbortMultipartUploadRequest {
    /**
     * <p>The bucket name to which the upload was taking place. </p>
     *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     */
    String bucket;

    /**
     * <p>Key of the object for which the multipart upload was initiated.</p>
     */
    String key;

    /**
     * <p>Upload ID that identifies the multipart upload.</p>
     */
    String uploadId;

    RequestPayer requestPayer;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    AbortMultipartUploadRequest() {
        this.bucket = "";
        this.key = "";
        this.uploadId = "";
        this.requestPayer = null;
        this.expectedBucketOwner = "";
    }

    protected AbortMultipartUploadRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.key = builder.key;
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

    public String key() {
        return key;
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

        Builder uploadId(String uploadId);

        Builder requestPayer(RequestPayer requestPayer);

        Builder expectedBucketOwner(String expectedBucketOwner);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The bucket name to which the upload was taking place. </p>
         *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        String bucket;

        /**
         * <p>Key of the object for which the multipart upload was initiated.</p>
         */
        String key;

        /**
         * <p>Upload ID that identifies the multipart upload.</p>
         */
        String uploadId;

        RequestPayer requestPayer;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        protected BuilderImpl() {
        }

        private BuilderImpl(AbortMultipartUploadRequest model) {
            bucket(model.bucket);
            key(model.key);
            uploadId(model.uploadId);
            requestPayer(model.requestPayer);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public AbortMultipartUploadRequest build() {
            return new AbortMultipartUploadRequest(this);
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder key(String key) {
            this.key = key;
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
