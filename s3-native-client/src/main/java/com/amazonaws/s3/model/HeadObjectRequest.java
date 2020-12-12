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
public class HeadObjectRequest {
    private String bucket;

    private String ifMatch;

    private Instant ifModifiedSince;

    private String ifNoneMatch;

    private Instant ifUnmodifiedSince;

    private String key;

    private String range;

    private String versionId;

    private String sSECustomerAlgorithm;

    private String sSECustomerKey;

    private String sSECustomerKeyMD5;

    private RequestPayer requestPayer;

    private Integer partNumber;

    private String expectedBucketOwner;

    private HeadObjectRequest() {
        this.bucket = null;
        this.ifMatch = null;
        this.ifModifiedSince = null;
        this.ifNoneMatch = null;
        this.ifUnmodifiedSince = null;
        this.key = null;
        this.range = null;
        this.versionId = null;
        this.sSECustomerAlgorithm = null;
        this.sSECustomerKey = null;
        this.sSECustomerKeyMD5 = null;
        this.requestPayer = null;
        this.partNumber = null;
        this.expectedBucketOwner = null;
    }

    private HeadObjectRequest(Builder builder) {
        this.bucket = builder.bucket;
        this.ifMatch = builder.ifMatch;
        this.ifModifiedSince = builder.ifModifiedSince;
        this.ifNoneMatch = builder.ifNoneMatch;
        this.ifUnmodifiedSince = builder.ifUnmodifiedSince;
        this.key = builder.key;
        this.range = builder.range;
        this.versionId = builder.versionId;
        this.sSECustomerAlgorithm = builder.sSECustomerAlgorithm;
        this.sSECustomerKey = builder.sSECustomerKey;
        this.sSECustomerKeyMD5 = builder.sSECustomerKeyMD5;
        this.requestPayer = builder.requestPayer;
        this.partNumber = builder.partNumber;
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
        return Objects.hash(HeadObjectRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof HeadObjectRequest);
    }

    public String bucket() {
        return bucket;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public String ifMatch() {
        return ifMatch;
    }

    public void setIfMatch(final String ifMatch) {
        this.ifMatch = ifMatch;
    }

    public Instant ifModifiedSince() {
        return ifModifiedSince;
    }

    public void setIfModifiedSince(final Instant ifModifiedSince) {
        this.ifModifiedSince = ifModifiedSince;
    }

    public String ifNoneMatch() {
        return ifNoneMatch;
    }

    public void setIfNoneMatch(final String ifNoneMatch) {
        this.ifNoneMatch = ifNoneMatch;
    }

    public Instant ifUnmodifiedSince() {
        return ifUnmodifiedSince;
    }

    public void setIfUnmodifiedSince(final Instant ifUnmodifiedSince) {
        this.ifUnmodifiedSince = ifUnmodifiedSince;
    }

    public String key() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public String range() {
        return range;
    }

    public void setRange(final String range) {
        this.range = range;
    }

    public String versionId() {
        return versionId;
    }

    public void setVersionId(final String versionId) {
        this.versionId = versionId;
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

    public RequestPayer requestPayer() {
        return requestPayer;
    }

    public void setRequestPayer(final RequestPayer requestPayer) {
        this.requestPayer = requestPayer;
    }

    public Integer partNumber() {
        return partNumber;
    }

    public void setPartNumber(final Integer partNumber) {
        this.partNumber = partNumber;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    static final class Builder {
        private String bucket;

        private String ifMatch;

        private Instant ifModifiedSince;

        private String ifNoneMatch;

        private Instant ifUnmodifiedSince;

        private String key;

        private String range;

        private String versionId;

        private String sSECustomerAlgorithm;

        private String sSECustomerKey;

        private String sSECustomerKeyMD5;

        private RequestPayer requestPayer;

        private Integer partNumber;

        private String expectedBucketOwner;

        private Builder() {
        }

        private Builder(HeadObjectRequest model) {
            bucket(model.bucket);
            ifMatch(model.ifMatch);
            ifModifiedSince(model.ifModifiedSince);
            ifNoneMatch(model.ifNoneMatch);
            ifUnmodifiedSince(model.ifUnmodifiedSince);
            key(model.key);
            range(model.range);
            versionId(model.versionId);
            sSECustomerAlgorithm(model.sSECustomerAlgorithm);
            sSECustomerKey(model.sSECustomerKey);
            sSECustomerKeyMD5(model.sSECustomerKeyMD5);
            requestPayer(model.requestPayer);
            partNumber(model.partNumber);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public HeadObjectRequest build() {
            return new com.amazonaws.s3.model.HeadObjectRequest(this);
        }

        /**
         * <p>The name of the bucket containing the object.</p>
         *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>Return the object only if its entity tag (ETag) is the same as the one specified,
         *          otherwise return a 412 (precondition failed).</p>
         */
        public final Builder ifMatch(String ifMatch) {
            this.ifMatch = ifMatch;
            return this;
        }

        /**
         * <p>Return the object only if it has been modified since the specified time, otherwise
         *          return a 304 (not modified).</p>
         */
        public final Builder ifModifiedSince(Instant ifModifiedSince) {
            this.ifModifiedSince = ifModifiedSince;
            return this;
        }

        /**
         * <p>Return the object only if its entity tag (ETag) is different from the one specified,
         *          otherwise return a 304 (not modified).</p>
         */
        public final Builder ifNoneMatch(String ifNoneMatch) {
            this.ifNoneMatch = ifNoneMatch;
            return this;
        }

        /**
         * <p>Return the object only if it has not been modified since the specified time, otherwise
         *          return a 412 (precondition failed).</p>
         */
        public final Builder ifUnmodifiedSince(Instant ifUnmodifiedSince) {
            this.ifUnmodifiedSince = ifUnmodifiedSince;
            return this;
        }

        /**
         * <p>The object key.</p>
         */
        public final Builder key(String key) {
            this.key = key;
            return this;
        }

        /**
         * <p>Downloads the specified range bytes of an object. For more information about the HTTP
         *          Range header, see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.35">http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.35</a>.</p>
         *          <note>
         *             <p>Amazon S3 doesn't support retrieving multiple ranges of data per <code>GET</code>
         *             request.</p>
         *          </note>
         */
        public final Builder range(String range) {
            this.range = range;
            return this;
        }

        /**
         * <p>VersionId used to reference a specific version of the object.</p>
         */
        public final Builder versionId(String versionId) {
            this.versionId = versionId;
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
         *             <code>x-amz-server-side-encryption-customer-algorithm</code> header.</p>
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

        public final Builder requestPayer(RequestPayer requestPayer) {
            this.requestPayer = requestPayer;
            return this;
        }

        /**
         * <p>Part number of the object being read. This is a positive integer between 1 and 10,000.
         *          Effectively performs a 'ranged' HEAD request for the part specified. Useful querying about
         *          the size of the part and the number of parts in this object.</p>
         */
        public final Builder partNumber(Integer partNumber) {
            this.partNumber = partNumber;
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
