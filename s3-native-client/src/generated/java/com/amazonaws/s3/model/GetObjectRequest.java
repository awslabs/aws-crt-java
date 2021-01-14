// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.time.Instant;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetObjectRequest {
    /**
     * <p>The bucket name containing the object. </p>
     *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     */
    String bucket;

    /**
     * <p>Return the object only if its entity tag (ETag) is the same as the one specified,
     *          otherwise return a 412 (precondition failed).</p>
     */
    String ifMatch;

    /**
     * <p>Return the object only if it has been modified since the specified time, otherwise
     *          return a 304 (not modified).</p>
     */
    Instant ifModifiedSince;

    /**
     * <p>Return the object only if its entity tag (ETag) is different from the one specified,
     *          otherwise return a 304 (not modified).</p>
     */
    String ifNoneMatch;

    /**
     * <p>Return the object only if it has not been modified since the specified time, otherwise
     *          return a 412 (precondition failed).</p>
     */
    Instant ifUnmodifiedSince;

    /**
     * <p>Key of the object to get.</p>
     */
    String key;

    /**
     * <p>Downloads the specified range bytes of an object. For more information about the HTTP
     *          Range header, see <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.35">https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.35</a>.</p>
     *          <note>
     *             <p>Amazon S3 doesn't support retrieving multiple ranges of data per <code>GET</code>
     *             request.</p>
     *          </note>
     */
    String range;

    /**
     * <p>Sets the <code>Cache-Control</code> header of the response.</p>
     */
    String responseCacheControl;

    /**
     * <p>Sets the <code>Content-Disposition</code> header of the response</p>
     */
    String responseContentDisposition;

    /**
     * <p>Sets the <code>Content-Encoding</code> header of the response.</p>
     */
    String responseContentEncoding;

    /**
     * <p>Sets the <code>Content-Language</code> header of the response.</p>
     */
    String responseContentLanguage;

    /**
     * <p>Sets the <code>Content-Type</code> header of the response.</p>
     */
    String responseContentType;

    /**
     * <p>Sets the <code>Expires</code> header of the response.</p>
     */
    Instant responseExpires;

    /**
     * <p>VersionId used to reference a specific version of the object.</p>
     */
    String versionId;

    /**
     * <p>Specifies the algorithm to use to when encrypting the object (for example,
     *          AES256).</p>
     */
    String sSECustomerAlgorithm;

    /**
     * <p>Specifies the customer-provided encryption key for Amazon S3 to use in encrypting data. This
     *          value is used to store the object and then it is discarded; Amazon S3 does not store the
     *          encryption key. The key must be appropriate for use with the algorithm specified in the
     *             <code>x-amz-server-side-encryption-customer-algorithm</code> header.</p>
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
     * <p>Part number of the object being read. This is a positive integer between 1 and 10,000.
     *          Effectively performs a 'ranged' GET request for the part specified. Useful for downloading
     *          just a part of an object.</p>
     */
    Integer partNumber;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    GetObjectRequest() {
        this.bucket = "";
        this.ifMatch = "";
        this.ifModifiedSince = null;
        this.ifNoneMatch = "";
        this.ifUnmodifiedSince = null;
        this.key = "";
        this.range = "";
        this.responseCacheControl = "";
        this.responseContentDisposition = "";
        this.responseContentEncoding = "";
        this.responseContentLanguage = "";
        this.responseContentType = "";
        this.responseExpires = null;
        this.versionId = "";
        this.sSECustomerAlgorithm = "";
        this.sSECustomerKey = "";
        this.sSECustomerKeyMD5 = "";
        this.requestPayer = null;
        this.partNumber = null;
        this.expectedBucketOwner = "";
    }

    protected GetObjectRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.ifMatch = builder.ifMatch;
        this.ifModifiedSince = builder.ifModifiedSince;
        this.ifNoneMatch = builder.ifNoneMatch;
        this.ifUnmodifiedSince = builder.ifUnmodifiedSince;
        this.key = builder.key;
        this.range = builder.range;
        this.responseCacheControl = builder.responseCacheControl;
        this.responseContentDisposition = builder.responseContentDisposition;
        this.responseContentEncoding = builder.responseContentEncoding;
        this.responseContentLanguage = builder.responseContentLanguage;
        this.responseContentType = builder.responseContentType;
        this.responseExpires = builder.responseExpires;
        this.versionId = builder.versionId;
        this.sSECustomerAlgorithm = builder.sSECustomerAlgorithm;
        this.sSECustomerKey = builder.sSECustomerKey;
        this.sSECustomerKeyMD5 = builder.sSECustomerKeyMD5;
        this.requestPayer = builder.requestPayer;
        this.partNumber = builder.partNumber;
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
        return Objects.hash(GetObjectRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetObjectRequest);
    }

    public String bucket() {
        return bucket;
    }

    public String ifMatch() {
        return ifMatch;
    }

    public Instant ifModifiedSince() {
        return ifModifiedSince;
    }

    public String ifNoneMatch() {
        return ifNoneMatch;
    }

    public Instant ifUnmodifiedSince() {
        return ifUnmodifiedSince;
    }

    public String key() {
        return key;
    }

    public String range() {
        return range;
    }

    public String responseCacheControl() {
        return responseCacheControl;
    }

    public String responseContentDisposition() {
        return responseContentDisposition;
    }

    public String responseContentEncoding() {
        return responseContentEncoding;
    }

    public String responseContentLanguage() {
        return responseContentLanguage;
    }

    public String responseContentType() {
        return responseContentType;
    }

    public Instant responseExpires() {
        return responseExpires;
    }

    public String versionId() {
        return versionId;
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

    public Integer partNumber() {
        return partNumber;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public interface Builder {
        Builder bucket(String bucket);

        Builder ifMatch(String ifMatch);

        Builder ifModifiedSince(Instant ifModifiedSince);

        Builder ifNoneMatch(String ifNoneMatch);

        Builder ifUnmodifiedSince(Instant ifUnmodifiedSince);

        Builder key(String key);

        Builder range(String range);

        Builder responseCacheControl(String responseCacheControl);

        Builder responseContentDisposition(String responseContentDisposition);

        Builder responseContentEncoding(String responseContentEncoding);

        Builder responseContentLanguage(String responseContentLanguage);

        Builder responseContentType(String responseContentType);

        Builder responseExpires(Instant responseExpires);

        Builder versionId(String versionId);

        Builder sSECustomerAlgorithm(String sSECustomerAlgorithm);

        Builder sSECustomerKey(String sSECustomerKey);

        Builder sSECustomerKeyMD5(String sSECustomerKeyMD5);

        Builder requestPayer(RequestPayer requestPayer);

        Builder partNumber(Integer partNumber);

        Builder expectedBucketOwner(String expectedBucketOwner);

        GetObjectRequest build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The bucket name containing the object. </p>
         *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        String bucket;

        /**
         * <p>Return the object only if its entity tag (ETag) is the same as the one specified,
         *          otherwise return a 412 (precondition failed).</p>
         */
        String ifMatch;

        /**
         * <p>Return the object only if it has been modified since the specified time, otherwise
         *          return a 304 (not modified).</p>
         */
        Instant ifModifiedSince;

        /**
         * <p>Return the object only if its entity tag (ETag) is different from the one specified,
         *          otherwise return a 304 (not modified).</p>
         */
        String ifNoneMatch;

        /**
         * <p>Return the object only if it has not been modified since the specified time, otherwise
         *          return a 412 (precondition failed).</p>
         */
        Instant ifUnmodifiedSince;

        /**
         * <p>Key of the object to get.</p>
         */
        String key;

        /**
         * <p>Downloads the specified range bytes of an object. For more information about the HTTP
         *          Range header, see <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.35">https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.35</a>.</p>
         *          <note>
         *             <p>Amazon S3 doesn't support retrieving multiple ranges of data per <code>GET</code>
         *             request.</p>
         *          </note>
         */
        String range;

        /**
         * <p>Sets the <code>Cache-Control</code> header of the response.</p>
         */
        String responseCacheControl;

        /**
         * <p>Sets the <code>Content-Disposition</code> header of the response</p>
         */
        String responseContentDisposition;

        /**
         * <p>Sets the <code>Content-Encoding</code> header of the response.</p>
         */
        String responseContentEncoding;

        /**
         * <p>Sets the <code>Content-Language</code> header of the response.</p>
         */
        String responseContentLanguage;

        /**
         * <p>Sets the <code>Content-Type</code> header of the response.</p>
         */
        String responseContentType;

        /**
         * <p>Sets the <code>Expires</code> header of the response.</p>
         */
        Instant responseExpires;

        /**
         * <p>VersionId used to reference a specific version of the object.</p>
         */
        String versionId;

        /**
         * <p>Specifies the algorithm to use to when encrypting the object (for example,
         *          AES256).</p>
         */
        String sSECustomerAlgorithm;

        /**
         * <p>Specifies the customer-provided encryption key for Amazon S3 to use in encrypting data. This
         *          value is used to store the object and then it is discarded; Amazon S3 does not store the
         *          encryption key. The key must be appropriate for use with the algorithm specified in the
         *             <code>x-amz-server-side-encryption-customer-algorithm</code> header.</p>
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
         * <p>Part number of the object being read. This is a positive integer between 1 and 10,000.
         *          Effectively performs a 'ranged' GET request for the part specified. Useful for downloading
         *          just a part of an object.</p>
         */
        Integer partNumber;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        protected BuilderImpl() {
        }

        private BuilderImpl(GetObjectRequest model) {
            bucket(model.bucket);
            ifMatch(model.ifMatch);
            ifModifiedSince(model.ifModifiedSince);
            ifNoneMatch(model.ifNoneMatch);
            ifUnmodifiedSince(model.ifUnmodifiedSince);
            key(model.key);
            range(model.range);
            responseCacheControl(model.responseCacheControl);
            responseContentDisposition(model.responseContentDisposition);
            responseContentEncoding(model.responseContentEncoding);
            responseContentLanguage(model.responseContentLanguage);
            responseContentType(model.responseContentType);
            responseExpires(model.responseExpires);
            versionId(model.versionId);
            sSECustomerAlgorithm(model.sSECustomerAlgorithm);
            sSECustomerKey(model.sSECustomerKey);
            sSECustomerKeyMD5(model.sSECustomerKeyMD5);
            requestPayer(model.requestPayer);
            partNumber(model.partNumber);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public GetObjectRequest build() {
            return new GetObjectRequest(this);
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder ifMatch(String ifMatch) {
            this.ifMatch = ifMatch;
            return this;
        }

        public final Builder ifModifiedSince(Instant ifModifiedSince) {
            this.ifModifiedSince = ifModifiedSince;
            return this;
        }

        public final Builder ifNoneMatch(String ifNoneMatch) {
            this.ifNoneMatch = ifNoneMatch;
            return this;
        }

        public final Builder ifUnmodifiedSince(Instant ifUnmodifiedSince) {
            this.ifUnmodifiedSince = ifUnmodifiedSince;
            return this;
        }

        public final Builder key(String key) {
            this.key = key;
            return this;
        }

        public final Builder range(String range) {
            this.range = range;
            return this;
        }

        public final Builder responseCacheControl(String responseCacheControl) {
            this.responseCacheControl = responseCacheControl;
            return this;
        }

        public final Builder responseContentDisposition(String responseContentDisposition) {
            this.responseContentDisposition = responseContentDisposition;
            return this;
        }

        public final Builder responseContentEncoding(String responseContentEncoding) {
            this.responseContentEncoding = responseContentEncoding;
            return this;
        }

        public final Builder responseContentLanguage(String responseContentLanguage) {
            this.responseContentLanguage = responseContentLanguage;
            return this;
        }

        public final Builder responseContentType(String responseContentType) {
            this.responseContentType = responseContentType;
            return this;
        }

        public final Builder responseExpires(Instant responseExpires) {
            this.responseExpires = responseExpires;
            return this;
        }

        public final Builder versionId(String versionId) {
            this.versionId = versionId;
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

        public final Builder partNumber(Integer partNumber) {
            this.partNumber = partNumber;
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

        public String ifMatch() {
            return ifMatch;
        }

        public Instant ifModifiedSince() {
            return ifModifiedSince;
        }

        public String ifNoneMatch() {
            return ifNoneMatch;
        }

        public Instant ifUnmodifiedSince() {
            return ifUnmodifiedSince;
        }

        public String key() {
            return key;
        }

        public String range() {
            return range;
        }

        public String responseCacheControl() {
            return responseCacheControl;
        }

        public String responseContentDisposition() {
            return responseContentDisposition;
        }

        public String responseContentEncoding() {
            return responseContentEncoding;
        }

        public String responseContentLanguage() {
            return responseContentLanguage;
        }

        public String responseContentType() {
            return responseContentType;
        }

        public Instant responseExpires() {
            return responseExpires;
        }

        public String versionId() {
            return versionId;
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

        public Integer partNumber() {
            return partNumber;
        }

        public String expectedBucketOwner() {
            return expectedBucketOwner;
        }
    }
}
