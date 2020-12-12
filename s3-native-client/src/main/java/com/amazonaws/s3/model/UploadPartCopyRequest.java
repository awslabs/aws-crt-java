// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.time.Instant;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class UploadPartCopyRequest {
    private String bucket;

    private String copySource;

    private String copySourceIfMatch;

    private Instant copySourceIfModifiedSince;

    private String copySourceIfNoneMatch;

    private Instant copySourceIfUnmodifiedSince;

    private String copySourceRange;

    private String key;

    private Integer partNumber;

    private String uploadId;

    private String sSECustomerAlgorithm;

    private String sSECustomerKey;

    private String sSECustomerKeyMD5;

    private String copySourceSSECustomerAlgorithm;

    private String copySourceSSECustomerKey;

    private String copySourceSSECustomerKeyMD5;

    private RequestPayer requestPayer;

    private String expectedBucketOwner;

    private String expectedSourceBucketOwner;

    private UploadPartCopyRequest() {
        this.bucket = null;
        this.copySource = null;
        this.copySourceIfMatch = null;
        this.copySourceIfModifiedSince = null;
        this.copySourceIfNoneMatch = null;
        this.copySourceIfUnmodifiedSince = null;
        this.copySourceRange = null;
        this.key = null;
        this.partNumber = null;
        this.uploadId = null;
        this.sSECustomerAlgorithm = null;
        this.sSECustomerKey = null;
        this.sSECustomerKeyMD5 = null;
        this.copySourceSSECustomerAlgorithm = null;
        this.copySourceSSECustomerKey = null;
        this.copySourceSSECustomerKeyMD5 = null;
        this.requestPayer = null;
        this.expectedBucketOwner = null;
        this.expectedSourceBucketOwner = null;
    }

    private UploadPartCopyRequest(Builder builder) {
        this.bucket = builder.bucket;
        this.copySource = builder.copySource;
        this.copySourceIfMatch = builder.copySourceIfMatch;
        this.copySourceIfModifiedSince = builder.copySourceIfModifiedSince;
        this.copySourceIfNoneMatch = builder.copySourceIfNoneMatch;
        this.copySourceIfUnmodifiedSince = builder.copySourceIfUnmodifiedSince;
        this.copySourceRange = builder.copySourceRange;
        this.key = builder.key;
        this.partNumber = builder.partNumber;
        this.uploadId = builder.uploadId;
        this.sSECustomerAlgorithm = builder.sSECustomerAlgorithm;
        this.sSECustomerKey = builder.sSECustomerKey;
        this.sSECustomerKeyMD5 = builder.sSECustomerKeyMD5;
        this.copySourceSSECustomerAlgorithm = builder.copySourceSSECustomerAlgorithm;
        this.copySourceSSECustomerKey = builder.copySourceSSECustomerKey;
        this.copySourceSSECustomerKeyMD5 = builder.copySourceSSECustomerKeyMD5;
        this.requestPayer = builder.requestPayer;
        this.expectedBucketOwner = builder.expectedBucketOwner;
        this.expectedSourceBucketOwner = builder.expectedSourceBucketOwner;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(UploadPartCopyRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof UploadPartCopyRequest);
    }

    public String bucket() {
        return bucket;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public String copySource() {
        return copySource;
    }

    public void setCopySource(final String copySource) {
        this.copySource = copySource;
    }

    public String copySourceIfMatch() {
        return copySourceIfMatch;
    }

    public void setCopySourceIfMatch(final String copySourceIfMatch) {
        this.copySourceIfMatch = copySourceIfMatch;
    }

    public Instant copySourceIfModifiedSince() {
        return copySourceIfModifiedSince;
    }

    public void setCopySourceIfModifiedSince(final Instant copySourceIfModifiedSince) {
        this.copySourceIfModifiedSince = copySourceIfModifiedSince;
    }

    public String copySourceIfNoneMatch() {
        return copySourceIfNoneMatch;
    }

    public void setCopySourceIfNoneMatch(final String copySourceIfNoneMatch) {
        this.copySourceIfNoneMatch = copySourceIfNoneMatch;
    }

    public Instant copySourceIfUnmodifiedSince() {
        return copySourceIfUnmodifiedSince;
    }

    public void setCopySourceIfUnmodifiedSince(final Instant copySourceIfUnmodifiedSince) {
        this.copySourceIfUnmodifiedSince = copySourceIfUnmodifiedSince;
    }

    public String copySourceRange() {
        return copySourceRange;
    }

    public void setCopySourceRange(final String copySourceRange) {
        this.copySourceRange = copySourceRange;
    }

    public String key() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public Integer partNumber() {
        return partNumber;
    }

    public void setPartNumber(final Integer partNumber) {
        this.partNumber = partNumber;
    }

    public String uploadId() {
        return uploadId;
    }

    public void setUploadId(final String uploadId) {
        this.uploadId = uploadId;
    }

    public String sSECustomerAlgorithm() {
        return sSECustomerAlgorithm;
    }

    public void setSSECustomerAlgorithm(final String sSECustomerAlgorithm) {
        this.sSECustomerAlgorithm = sSECustomerAlgorithm;
    }

    public String sSECustomerKey() {
        return sSECustomerKey;
    }

    public void setSSECustomerKey(final String sSECustomerKey) {
        this.sSECustomerKey = sSECustomerKey;
    }

    public String sSECustomerKeyMD5() {
        return sSECustomerKeyMD5;
    }

    public void setSSECustomerKeyMD5(final String sSECustomerKeyMD5) {
        this.sSECustomerKeyMD5 = sSECustomerKeyMD5;
    }

    public String copySourceSSECustomerAlgorithm() {
        return copySourceSSECustomerAlgorithm;
    }

    public void setCopySourceSSECustomerAlgorithm(final String copySourceSSECustomerAlgorithm) {
        this.copySourceSSECustomerAlgorithm = copySourceSSECustomerAlgorithm;
    }

    public String copySourceSSECustomerKey() {
        return copySourceSSECustomerKey;
    }

    public void setCopySourceSSECustomerKey(final String copySourceSSECustomerKey) {
        this.copySourceSSECustomerKey = copySourceSSECustomerKey;
    }

    public String copySourceSSECustomerKeyMD5() {
        return copySourceSSECustomerKeyMD5;
    }

    public void setCopySourceSSECustomerKeyMD5(final String copySourceSSECustomerKeyMD5) {
        this.copySourceSSECustomerKeyMD5 = copySourceSSECustomerKeyMD5;
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

    public String expectedSourceBucketOwner() {
        return expectedSourceBucketOwner;
    }

    public void setExpectedSourceBucketOwner(final String expectedSourceBucketOwner) {
        this.expectedSourceBucketOwner = expectedSourceBucketOwner;
    }

    static final class Builder {
        private String bucket;

        private String copySource;

        private String copySourceIfMatch;

        private Instant copySourceIfModifiedSince;

        private String copySourceIfNoneMatch;

        private Instant copySourceIfUnmodifiedSince;

        private String copySourceRange;

        private String key;

        private Integer partNumber;

        private String uploadId;

        private String sSECustomerAlgorithm;

        private String sSECustomerKey;

        private String sSECustomerKeyMD5;

        private String copySourceSSECustomerAlgorithm;

        private String copySourceSSECustomerKey;

        private String copySourceSSECustomerKeyMD5;

        private RequestPayer requestPayer;

        private String expectedBucketOwner;

        private String expectedSourceBucketOwner;

        private Builder() {
        }

        private Builder(UploadPartCopyRequest model) {
            bucket(model.bucket);
            copySource(model.copySource);
            copySourceIfMatch(model.copySourceIfMatch);
            copySourceIfModifiedSince(model.copySourceIfModifiedSince);
            copySourceIfNoneMatch(model.copySourceIfNoneMatch);
            copySourceIfUnmodifiedSince(model.copySourceIfUnmodifiedSince);
            copySourceRange(model.copySourceRange);
            key(model.key);
            partNumber(model.partNumber);
            uploadId(model.uploadId);
            sSECustomerAlgorithm(model.sSECustomerAlgorithm);
            sSECustomerKey(model.sSECustomerKey);
            sSECustomerKeyMD5(model.sSECustomerKeyMD5);
            copySourceSSECustomerAlgorithm(model.copySourceSSECustomerAlgorithm);
            copySourceSSECustomerKey(model.copySourceSSECustomerKey);
            copySourceSSECustomerKeyMD5(model.copySourceSSECustomerKeyMD5);
            requestPayer(model.requestPayer);
            expectedBucketOwner(model.expectedBucketOwner);
            expectedSourceBucketOwner(model.expectedSourceBucketOwner);
        }

        public UploadPartCopyRequest build() {
            return new com.amazonaws.s3.model.UploadPartCopyRequest(this);
        }

        /**
         * <p>The bucket name.</p>
         *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>Specifies the source object for the copy operation. You specify the value in one of two
         *          formats, depending on whether you want to access the source object through an <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/access-points.html">access
         *          point</a>:</p>
         *          <ul>
         *             <li>
         *                <p>For objects not accessed through an access point, specify the name of the source
         *                bucket and key of the source object, separated by a slash (/). For example, to copy
         *                the object <code>reports/january.pdf</code> from the bucket
         *                   <code>awsexamplebucket</code>, use
         *                   <code>awsexamplebucket/reports/january.pdf</code>. The value must be URL
         *                encoded.</p>
         *             </li>
         *             <li> 
         *                <p>For objects accessed through access points, specify the Amazon Resource Name (ARN) of the object as accessed through the access point, in the format <code>arn:aws:s3:<Region>:<account-id>:accesspoint/<access-point-name>/object/<key></code>. For example, to copy the object <code>reports/january.pdf</code> through access point <code>my-access-point</code> owned by account <code>123456789012</code> in Region <code>us-west-2</code>, use the URL encoding of <code>arn:aws:s3:us-west-2:123456789012:accesspoint/my-access-point/object/reports/january.pdf</code>. The value must be URL encoded.</p> 
         *                <note>
         *                   <p>Amazon S3 supports copy operations using access points only when the source and destination buckets are in the same AWS Region.</p>
         *                </note> 
         *                <p>Alternatively, for objects accessed through Amazon S3 on Outposts, specify the ARN of the object as accessed in the format <code>arn:aws:s3-outposts:<Region>:<account-id>:outpost/<outpost-id>/object/<key></code>. For example, to copy the object <code>reports/january.pdf</code> through outpost <code>my-outpost</code> owned by account <code>123456789012</code> in Region <code>us-west-2</code>, use the URL encoding of <code>arn:aws:s3-outposts:us-west-2:123456789012:outpost/my-outpost/object/reports/january.pdf</code>. The value must be URL encoded.  </p> 
         *             </li>
         *          </ul>
         *          <p>To copy a specific version of an object, append <code>?versionId=<version-id></code>
         *          to the value (for example,
         *             <code>awsexamplebucket/reports/january.pdf?versionId=QUpfdndhfd8438MNFDN93jdnJFkdmqnh893</code>).
         *          If you don't specify a version ID, Amazon S3 copies the latest version of the source
         *          object.</p>
         */
        public final Builder copySource(String copySource) {
            this.copySource = copySource;
            return this;
        }

        /**
         * <p>Copies the object if its entity tag (ETag) matches the specified tag.</p>
         */
        public final Builder copySourceIfMatch(String copySourceIfMatch) {
            this.copySourceIfMatch = copySourceIfMatch;
            return this;
        }

        /**
         * <p>Copies the object if it has been modified since the specified time.</p>
         */
        public final Builder copySourceIfModifiedSince(Instant copySourceIfModifiedSince) {
            this.copySourceIfModifiedSince = copySourceIfModifiedSince;
            return this;
        }

        /**
         * <p>Copies the object if its entity tag (ETag) is different than the specified ETag.</p>
         */
        public final Builder copySourceIfNoneMatch(String copySourceIfNoneMatch) {
            this.copySourceIfNoneMatch = copySourceIfNoneMatch;
            return this;
        }

        /**
         * <p>Copies the object if it hasn't been modified since the specified time.</p>
         */
        public final Builder copySourceIfUnmodifiedSince(Instant copySourceIfUnmodifiedSince) {
            this.copySourceIfUnmodifiedSince = copySourceIfUnmodifiedSince;
            return this;
        }

        /**
         * <p>The range of bytes to copy from the source object. The range value must use the form
         *          bytes=first-last, where the first and last are the zero-based byte offsets to copy. For
         *          example, bytes=0-9 indicates that you want to copy the first 10 bytes of the source. You
         *          can copy a range only if the source object is greater than 5 MB.</p>
         */
        public final Builder copySourceRange(String copySourceRange) {
            this.copySourceRange = copySourceRange;
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
         * <p>Part number of part being copied. This is a positive integer between 1 and
         *          10,000.</p>
         */
        public final Builder partNumber(Integer partNumber) {
            this.partNumber = partNumber;
            return this;
        }

        /**
         * <p>Upload ID identifying the multipart upload whose part is being copied.</p>
         */
        public final Builder uploadId(String uploadId) {
            this.uploadId = uploadId;
            return this;
        }

        /**
         * <p>Specifies the algorithm to use to when encrypting the object (for example,
         *          AES256).</p>
         */
        public final Builder sSECustomerAlgorithm(String sSECustomerAlgorithm) {
            this.sSECustomerAlgorithm = sSECustomerAlgorithm;
            return this;
        }

        /**
         * <p>Specifies the customer-provided encryption key for Amazon S3 to use in encrypting data. This
         *          value is used to store the object and then it is discarded; Amazon S3 does not store the
         *          encryption key. The key must be appropriate for use with the algorithm specified in the
         *             <code>x-amz-server-side-encryption-customer-algorithm</code> header. This must be the
         *          same encryption key specified in the initiate multipart upload request.</p>
         */
        public final Builder sSECustomerKey(String sSECustomerKey) {
            this.sSECustomerKey = sSECustomerKey;
            return this;
        }

        /**
         * <p>Specifies the 128-bit MD5 digest of the encryption key according to RFC 1321. Amazon S3 uses
         *          this header for a message integrity check to ensure that the encryption key was transmitted
         *          without error.</p>
         */
        public final Builder sSECustomerKeyMD5(String sSECustomerKeyMD5) {
            this.sSECustomerKeyMD5 = sSECustomerKeyMD5;
            return this;
        }

        /**
         * <p>Specifies the algorithm to use when decrypting the source object (for example,
         *          AES256).</p>
         */
        public final Builder copySourceSSECustomerAlgorithm(String copySourceSSECustomerAlgorithm) {
            this.copySourceSSECustomerAlgorithm = copySourceSSECustomerAlgorithm;
            return this;
        }

        /**
         * <p>Specifies the customer-provided encryption key for Amazon S3 to use to decrypt the source
         *          object. The encryption key provided in this header must be one that was used when the
         *          source object was created.</p>
         */
        public final Builder copySourceSSECustomerKey(String copySourceSSECustomerKey) {
            this.copySourceSSECustomerKey = copySourceSSECustomerKey;
            return this;
        }

        /**
         * <p>Specifies the 128-bit MD5 digest of the encryption key according to RFC 1321. Amazon S3 uses
         *          this header for a message integrity check to ensure that the encryption key was transmitted
         *          without error.</p>
         */
        public final Builder copySourceSSECustomerKeyMD5(String copySourceSSECustomerKeyMD5) {
            this.copySourceSSECustomerKeyMD5 = copySourceSSECustomerKeyMD5;
            return this;
        }

        public final Builder requestPayer(RequestPayer requestPayer) {
            this.requestPayer = requestPayer;
            return this;
        }

        /**
         * <p>The account id of the expected destination bucket owner. If the destination bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        public final Builder expectedBucketOwner(String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
            return this;
        }

        /**
         * <p>The account id of the expected source bucket owner. If the source bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        public final Builder expectedSourceBucketOwner(String expectedSourceBucketOwner) {
            this.expectedSourceBucketOwner = expectedSourceBucketOwner;
            return this;
        }
    }
}
