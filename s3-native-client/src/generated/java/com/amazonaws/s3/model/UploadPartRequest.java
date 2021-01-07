// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class UploadPartRequest {
    /**
     * <p>Object data.</p>
     */
    byte[] body;

    /**
     * <p>The name of the bucket to which the multipart upload was initiated.</p>
     *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     */
    String bucket;

    /**
     * <p>Size of the body in bytes. This parameter is useful when the size of the body cannot be
     *          determined automatically.</p>
     */
    Long contentLength;

    /**
     * <p>The base64-encoded 128-bit MD5 digest of the part data. This parameter is auto-populated
     *          when using the command from the CLI. This parameter is required if object lock parameters
     *          are specified.</p>
     */
    String contentMD5;

    /**
     * <p>Object key for which the multipart upload was initiated.</p>
     */
    String key;

    /**
     * <p>Part number of part being uploaded. This is a positive integer between 1 and
     *          10,000.</p>
     */
    Integer partNumber;

    /**
     * <p>Upload ID identifying the multipart upload whose part is being uploaded.</p>
     */
    String uploadId;

    /**
     * <p>Specifies the algorithm to use to when encrypting the object (for example,
     *          AES256).</p>
     */
    String sSECustomerAlgorithm;

    /**
     * <p>Specifies the customer-provided encryption key for Amazon S3 to use in encrypting data. This
     *          value is used to store the object and then it is discarded; Amazon S3 does not store the
     *          encryption key. The key must be appropriate for use with the algorithm specified in the
     *             <code>x-amz-server-side-encryption-customer-algorithm header</code>. This must be the
     *          same encryption key specified in the initiate multipart upload request.</p>
     */
    String sSECustomerKey;

    /**
     * <p>Specifies the 128-bit MD5 digest of the encryption key according to RFC 1321. Amazon S3 uses
     *          this header for a message integrity check to ensure that the encryption key was transmitted
     *          without error.</p>
     */
    String sSECustomerKeyMD5;

    RequestPayer requestPayer;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    UploadPartRequest() {
        this.body = null;
        this.bucket = "";
        this.contentLength = null;
        this.contentMD5 = "";
        this.key = "";
        this.partNumber = null;
        this.uploadId = "";
        this.sSECustomerAlgorithm = "";
        this.sSECustomerKey = "";
        this.sSECustomerKeyMD5 = "";
        this.requestPayer = null;
        this.expectedBucketOwner = "";
    }

    protected UploadPartRequest(BuilderImpl builder) {
        this.body = builder.body;
        this.bucket = builder.bucket;
        this.contentLength = builder.contentLength;
        this.contentMD5 = builder.contentMD5;
        this.key = builder.key;
        this.partNumber = builder.partNumber;
        this.uploadId = builder.uploadId;
        this.sSECustomerAlgorithm = builder.sSECustomerAlgorithm;
        this.sSECustomerKey = builder.sSECustomerKey;
        this.sSECustomerKeyMD5 = builder.sSECustomerKeyMD5;
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
        return Objects.hash(UploadPartRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof UploadPartRequest);
    }

    public byte[] body() {
        return body;
    }

    public String bucket() {
        return bucket;
    }

    public Long contentLength() {
        return contentLength;
    }

    public String contentMD5() {
        return contentMD5;
    }

    public String key() {
        return key;
    }

    public Integer partNumber() {
        return partNumber;
    }

    public String uploadId() {
        return uploadId;
    }

    public String sSECustomerAlgorithm() {
        return sSECustomerAlgorithm;
    }

    public String sSECustomerKey() {
        return sSECustomerKey;
    }

    public String sSECustomerKeyMD5() {
        return sSECustomerKeyMD5;
    }

    public RequestPayer requestPayer() {
        return requestPayer;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public void setBody(final byte[] body) {
        this.body = body;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public void setContentLength(final Long contentLength) {
        this.contentLength = contentLength;
    }

    public void setContentMD5(final String contentMD5) {
        this.contentMD5 = contentMD5;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public void setPartNumber(final Integer partNumber) {
        this.partNumber = partNumber;
    }

    public void setUploadId(final String uploadId) {
        this.uploadId = uploadId;
    }

    public void setSSECustomerAlgorithm(final String sSECustomerAlgorithm) {
        this.sSECustomerAlgorithm = sSECustomerAlgorithm;
    }

    public void setSSECustomerKey(final String sSECustomerKey) {
        this.sSECustomerKey = sSECustomerKey;
    }

    public void setSSECustomerKeyMD5(final String sSECustomerKeyMD5) {
        this.sSECustomerKeyMD5 = sSECustomerKeyMD5;
    }

    public void setRequestPayer(final RequestPayer requestPayer) {
        this.requestPayer = requestPayer;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    public interface Builder {
        Builder body(byte[] body);

        Builder bucket(String bucket);

        Builder contentLength(Long contentLength);

        Builder contentMD5(String contentMD5);

        Builder key(String key);

        Builder partNumber(Integer partNumber);

        Builder uploadId(String uploadId);

        Builder sSECustomerAlgorithm(String sSECustomerAlgorithm);

        Builder sSECustomerKey(String sSECustomerKey);

        Builder sSECustomerKeyMD5(String sSECustomerKeyMD5);

        Builder requestPayer(RequestPayer requestPayer);

        Builder expectedBucketOwner(String expectedBucketOwner);

        UploadPartRequest build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Object data.</p>
         */
        byte[] body;

        /**
         * <p>The name of the bucket to which the multipart upload was initiated.</p>
         *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        String bucket;

        /**
         * <p>Size of the body in bytes. This parameter is useful when the size of the body cannot be
         *          determined automatically.</p>
         */
        Long contentLength;

        /**
         * <p>The base64-encoded 128-bit MD5 digest of the part data. This parameter is auto-populated
         *          when using the command from the CLI. This parameter is required if object lock parameters
         *          are specified.</p>
         */
        String contentMD5;

        /**
         * <p>Object key for which the multipart upload was initiated.</p>
         */
        String key;

        /**
         * <p>Part number of part being uploaded. This is a positive integer between 1 and
         *          10,000.</p>
         */
        Integer partNumber;

        /**
         * <p>Upload ID identifying the multipart upload whose part is being uploaded.</p>
         */
        String uploadId;

        /**
         * <p>Specifies the algorithm to use to when encrypting the object (for example,
         *          AES256).</p>
         */
        String sSECustomerAlgorithm;

        /**
         * <p>Specifies the customer-provided encryption key for Amazon S3 to use in encrypting data. This
         *          value is used to store the object and then it is discarded; Amazon S3 does not store the
         *          encryption key. The key must be appropriate for use with the algorithm specified in the
         *             <code>x-amz-server-side-encryption-customer-algorithm header</code>. This must be the
         *          same encryption key specified in the initiate multipart upload request.</p>
         */
        String sSECustomerKey;

        /**
         * <p>Specifies the 128-bit MD5 digest of the encryption key according to RFC 1321. Amazon S3 uses
         *          this header for a message integrity check to ensure that the encryption key was transmitted
         *          without error.</p>
         */
        String sSECustomerKeyMD5;

        RequestPayer requestPayer;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        protected BuilderImpl() {
        }

        private BuilderImpl(UploadPartRequest model) {
            body(model.body);
            bucket(model.bucket);
            contentLength(model.contentLength);
            contentMD5(model.contentMD5);
            key(model.key);
            partNumber(model.partNumber);
            uploadId(model.uploadId);
            sSECustomerAlgorithm(model.sSECustomerAlgorithm);
            sSECustomerKey(model.sSECustomerKey);
            sSECustomerKeyMD5(model.sSECustomerKeyMD5);
            requestPayer(model.requestPayer);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public UploadPartRequest build() {
            return new UploadPartRequest(this);
        }

        public final Builder body(byte[] body) {
            this.body = body;
            return this;
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder contentLength(Long contentLength) {
            this.contentLength = contentLength;
            return this;
        }

        public final Builder contentMD5(String contentMD5) {
            this.contentMD5 = contentMD5;
            return this;
        }

        public final Builder key(String key) {
            this.key = key;
            return this;
        }

        public final Builder partNumber(Integer partNumber) {
            this.partNumber = partNumber;
            return this;
        }

        public final Builder uploadId(String uploadId) {
            this.uploadId = uploadId;
            return this;
        }

        public final Builder sSECustomerAlgorithm(String sSECustomerAlgorithm) {
            this.sSECustomerAlgorithm = sSECustomerAlgorithm;
            return this;
        }

        public final Builder sSECustomerKey(String sSECustomerKey) {
            this.sSECustomerKey = sSECustomerKey;
            return this;
        }

        public final Builder sSECustomerKeyMD5(String sSECustomerKeyMD5) {
            this.sSECustomerKeyMD5 = sSECustomerKeyMD5;
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

        public byte[] body() {
            return body;
        }

        public String bucket() {
            return bucket;
        }

        public Long contentLength() {
            return contentLength;
        }

        public String contentMD5() {
            return contentMD5;
        }

        public String key() {
            return key;
        }

        public Integer partNumber() {
            return partNumber;
        }

        public String uploadId() {
            return uploadId;
        }

        public String sSECustomerAlgorithm() {
            return sSECustomerAlgorithm;
        }

        public String sSECustomerKey() {
            return sSECustomerKey;
        }

        public String sSECustomerKeyMD5() {
            return sSECustomerKeyMD5;
        }

        public RequestPayer requestPayer() {
            return requestPayer;
        }

        public String expectedBucketOwner() {
            return expectedBucketOwner;
        }

        public void setBody(final byte[] body) {
            this.body = body;
        }

        public void setBucket(final String bucket) {
            this.bucket = bucket;
        }

        public void setContentLength(final Long contentLength) {
            this.contentLength = contentLength;
        }

        public void setContentMD5(final String contentMD5) {
            this.contentMD5 = contentMD5;
        }

        public void setKey(final String key) {
            this.key = key;
        }

        public void setPartNumber(final Integer partNumber) {
            this.partNumber = partNumber;
        }

        public void setUploadId(final String uploadId) {
            this.uploadId = uploadId;
        }

        public void setSSECustomerAlgorithm(final String sSECustomerAlgorithm) {
            this.sSECustomerAlgorithm = sSECustomerAlgorithm;
        }

        public void setSSECustomerKey(final String sSECustomerKey) {
            this.sSECustomerKey = sSECustomerKey;
        }

        public void setSSECustomerKeyMD5(final String sSECustomerKeyMD5) {
            this.sSECustomerKeyMD5 = sSECustomerKeyMD5;
        }

        public void setRequestPayer(final RequestPayer requestPayer) {
            this.requestPayer = requestPayer;
        }

        public void setExpectedBucketOwner(final String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
        }
    }
}
