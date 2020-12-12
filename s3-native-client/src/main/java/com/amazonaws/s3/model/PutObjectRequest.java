// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutObjectRequest {
    private ObjectCannedACL aCL;

    private byte[] body;

    private String bucket;

    private String cacheControl;

    private String contentDisposition;

    private String contentEncoding;

    private String contentLanguage;

    private Long contentLength;

    private String contentMD5;

    private String contentType;

    private Instant expires;

    private String grantFullControl;

    private String grantRead;

    private String grantReadACP;

    private String grantWriteACP;

    private String key;

    private Map<String, String> metadata;

    private ServerSideEncryption serverSideEncryption;

    private StorageClass storageClass;

    private String websiteRedirectLocation;

    private String sSECustomerAlgorithm;

    private String sSECustomerKey;

    private String sSECustomerKeyMD5;

    private String sSEKMSKeyId;

    private String sSEKMSEncryptionContext;

    private Boolean bucketKeyEnabled;

    private RequestPayer requestPayer;

    private String tagging;

    private ObjectLockMode objectLockMode;

    private Instant objectLockRetainUntilDate;

    private ObjectLockLegalHoldStatus objectLockLegalHoldStatus;

    private String expectedBucketOwner;

    private PutObjectRequest() {
        this.aCL = null;
        this.body = null;
        this.bucket = null;
        this.cacheControl = null;
        this.contentDisposition = null;
        this.contentEncoding = null;
        this.contentLanguage = null;
        this.contentLength = null;
        this.contentMD5 = null;
        this.contentType = null;
        this.expires = null;
        this.grantFullControl = null;
        this.grantRead = null;
        this.grantReadACP = null;
        this.grantWriteACP = null;
        this.key = null;
        this.metadata = null;
        this.serverSideEncryption = null;
        this.storageClass = null;
        this.websiteRedirectLocation = null;
        this.sSECustomerAlgorithm = null;
        this.sSECustomerKey = null;
        this.sSECustomerKeyMD5 = null;
        this.sSEKMSKeyId = null;
        this.sSEKMSEncryptionContext = null;
        this.bucketKeyEnabled = null;
        this.requestPayer = null;
        this.tagging = null;
        this.objectLockMode = null;
        this.objectLockRetainUntilDate = null;
        this.objectLockLegalHoldStatus = null;
        this.expectedBucketOwner = null;
    }

    private PutObjectRequest(Builder builder) {
        this.aCL = builder.aCL;
        this.body = builder.body;
        this.bucket = builder.bucket;
        this.cacheControl = builder.cacheControl;
        this.contentDisposition = builder.contentDisposition;
        this.contentEncoding = builder.contentEncoding;
        this.contentLanguage = builder.contentLanguage;
        this.contentLength = builder.contentLength;
        this.contentMD5 = builder.contentMD5;
        this.contentType = builder.contentType;
        this.expires = builder.expires;
        this.grantFullControl = builder.grantFullControl;
        this.grantRead = builder.grantRead;
        this.grantReadACP = builder.grantReadACP;
        this.grantWriteACP = builder.grantWriteACP;
        this.key = builder.key;
        this.metadata = builder.metadata;
        this.serverSideEncryption = builder.serverSideEncryption;
        this.storageClass = builder.storageClass;
        this.websiteRedirectLocation = builder.websiteRedirectLocation;
        this.sSECustomerAlgorithm = builder.sSECustomerAlgorithm;
        this.sSECustomerKey = builder.sSECustomerKey;
        this.sSECustomerKeyMD5 = builder.sSECustomerKeyMD5;
        this.sSEKMSKeyId = builder.sSEKMSKeyId;
        this.sSEKMSEncryptionContext = builder.sSEKMSEncryptionContext;
        this.bucketKeyEnabled = builder.bucketKeyEnabled;
        this.requestPayer = builder.requestPayer;
        this.tagging = builder.tagging;
        this.objectLockMode = builder.objectLockMode;
        this.objectLockRetainUntilDate = builder.objectLockRetainUntilDate;
        this.objectLockLegalHoldStatus = builder.objectLockLegalHoldStatus;
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
        return Objects.hash(PutObjectRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof PutObjectRequest);
    }

    public ObjectCannedACL aCL() {
        return aCL;
    }

    public void setACL(final ObjectCannedACL aCL) {
        this.aCL = aCL;
    }

    public byte[] body() {
        return body;
    }

    public void setBody(final byte[] body) {
        this.body = body;
    }

    public String bucket() {
        return bucket;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public String cacheControl() {
        return cacheControl;
    }

    public void setCacheControl(final String cacheControl) {
        this.cacheControl = cacheControl;
    }

    public String contentDisposition() {
        return contentDisposition;
    }

    public void setContentDisposition(final String contentDisposition) {
        this.contentDisposition = contentDisposition;
    }

    public String contentEncoding() {
        return contentEncoding;
    }

    public void setContentEncoding(final String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    public String contentLanguage() {
        return contentLanguage;
    }

    public void setContentLanguage(final String contentLanguage) {
        this.contentLanguage = contentLanguage;
    }

    public Long contentLength() {
        return contentLength;
    }

    public void setContentLength(final Long contentLength) {
        this.contentLength = contentLength;
    }

    public String contentMD5() {
        return contentMD5;
    }

    public void setContentMD5(final String contentMD5) {
        this.contentMD5 = contentMD5;
    }

    public String contentType() {
        return contentType;
    }

    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    public Instant expires() {
        return expires;
    }

    public void setExpires(final Instant expires) {
        this.expires = expires;
    }

    public String grantFullControl() {
        return grantFullControl;
    }

    public void setGrantFullControl(final String grantFullControl) {
        this.grantFullControl = grantFullControl;
    }

    public String grantRead() {
        return grantRead;
    }

    public void setGrantRead(final String grantRead) {
        this.grantRead = grantRead;
    }

    public String grantReadACP() {
        return grantReadACP;
    }

    public void setGrantReadACP(final String grantReadACP) {
        this.grantReadACP = grantReadACP;
    }

    public String grantWriteACP() {
        return grantWriteACP;
    }

    public void setGrantWriteACP(final String grantWriteACP) {
        this.grantWriteACP = grantWriteACP;
    }

    public String key() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public Map<String, String> metadata() {
        return metadata;
    }

    public void setMetadata(final Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public ServerSideEncryption serverSideEncryption() {
        return serverSideEncryption;
    }

    public void setServerSideEncryption(final ServerSideEncryption serverSideEncryption) {
        this.serverSideEncryption = serverSideEncryption;
    }

    public StorageClass storageClass() {
        return storageClass;
    }

    public void setStorageClass(final StorageClass storageClass) {
        this.storageClass = storageClass;
    }

    public String websiteRedirectLocation() {
        return websiteRedirectLocation;
    }

    public void setWebsiteRedirectLocation(final String websiteRedirectLocation) {
        this.websiteRedirectLocation = websiteRedirectLocation;
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

    public String sSEKMSKeyId() {
        return sSEKMSKeyId;
    }

    public void setSSEKMSKeyId(final String sSEKMSKeyId) {
        this.sSEKMSKeyId = sSEKMSKeyId;
    }

    public String sSEKMSEncryptionContext() {
        return sSEKMSEncryptionContext;
    }

    public void setSSEKMSEncryptionContext(final String sSEKMSEncryptionContext) {
        this.sSEKMSEncryptionContext = sSEKMSEncryptionContext;
    }

    public Boolean bucketKeyEnabled() {
        return bucketKeyEnabled;
    }

    public void setBucketKeyEnabled(final Boolean bucketKeyEnabled) {
        this.bucketKeyEnabled = bucketKeyEnabled;
    }

    public RequestPayer requestPayer() {
        return requestPayer;
    }

    public void setRequestPayer(final RequestPayer requestPayer) {
        this.requestPayer = requestPayer;
    }

    public String tagging() {
        return tagging;
    }

    public void setTagging(final String tagging) {
        this.tagging = tagging;
    }

    public ObjectLockMode objectLockMode() {
        return objectLockMode;
    }

    public void setObjectLockMode(final ObjectLockMode objectLockMode) {
        this.objectLockMode = objectLockMode;
    }

    public Instant objectLockRetainUntilDate() {
        return objectLockRetainUntilDate;
    }

    public void setObjectLockRetainUntilDate(final Instant objectLockRetainUntilDate) {
        this.objectLockRetainUntilDate = objectLockRetainUntilDate;
    }

    public ObjectLockLegalHoldStatus objectLockLegalHoldStatus() {
        return objectLockLegalHoldStatus;
    }

    public void setObjectLockLegalHoldStatus(
            final ObjectLockLegalHoldStatus objectLockLegalHoldStatus) {
        this.objectLockLegalHoldStatus = objectLockLegalHoldStatus;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    static final class Builder {
        private ObjectCannedACL aCL;

        private byte[] body;

        private String bucket;

        private String cacheControl;

        private String contentDisposition;

        private String contentEncoding;

        private String contentLanguage;

        private Long contentLength;

        private String contentMD5;

        private String contentType;

        private Instant expires;

        private String grantFullControl;

        private String grantRead;

        private String grantReadACP;

        private String grantWriteACP;

        private String key;

        private Map<String, String> metadata;

        private ServerSideEncryption serverSideEncryption;

        private StorageClass storageClass;

        private String websiteRedirectLocation;

        private String sSECustomerAlgorithm;

        private String sSECustomerKey;

        private String sSECustomerKeyMD5;

        private String sSEKMSKeyId;

        private String sSEKMSEncryptionContext;

        private Boolean bucketKeyEnabled;

        private RequestPayer requestPayer;

        private String tagging;

        private ObjectLockMode objectLockMode;

        private Instant objectLockRetainUntilDate;

        private ObjectLockLegalHoldStatus objectLockLegalHoldStatus;

        private String expectedBucketOwner;

        private Builder() {
        }

        private Builder(PutObjectRequest model) {
            aCL(model.aCL);
            body(model.body);
            bucket(model.bucket);
            cacheControl(model.cacheControl);
            contentDisposition(model.contentDisposition);
            contentEncoding(model.contentEncoding);
            contentLanguage(model.contentLanguage);
            contentLength(model.contentLength);
            contentMD5(model.contentMD5);
            contentType(model.contentType);
            expires(model.expires);
            grantFullControl(model.grantFullControl);
            grantRead(model.grantRead);
            grantReadACP(model.grantReadACP);
            grantWriteACP(model.grantWriteACP);
            key(model.key);
            metadata(model.metadata);
            serverSideEncryption(model.serverSideEncryption);
            storageClass(model.storageClass);
            websiteRedirectLocation(model.websiteRedirectLocation);
            sSECustomerAlgorithm(model.sSECustomerAlgorithm);
            sSECustomerKey(model.sSECustomerKey);
            sSECustomerKeyMD5(model.sSECustomerKeyMD5);
            sSEKMSKeyId(model.sSEKMSKeyId);
            sSEKMSEncryptionContext(model.sSEKMSEncryptionContext);
            bucketKeyEnabled(model.bucketKeyEnabled);
            requestPayer(model.requestPayer);
            tagging(model.tagging);
            objectLockMode(model.objectLockMode);
            objectLockRetainUntilDate(model.objectLockRetainUntilDate);
            objectLockLegalHoldStatus(model.objectLockLegalHoldStatus);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public PutObjectRequest build() {
            return new com.amazonaws.s3.model.PutObjectRequest(this);
        }

        /**
         * <p>The canned ACL to apply to the object. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/acl-overview.html#CannedACL">Canned
         *       ACL</a>.</p>
         *          <p>This action is not supported by Amazon S3 on Outposts.</p>
         */
        public final Builder aCL(ObjectCannedACL aCL) {
            this.aCL = aCL;
            return this;
        }

        /**
         * <p>Object data.</p>
         */
        public final Builder body(byte[] body) {
            this.body = body;
            return this;
        }

        /**
         * <p>The bucket name to which the PUT operation was initiated. </p>
         *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p> Can be used to specify caching behavior along the request/reply chain. For more
         *          information, see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9">http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9</a>.</p>
         */
        public final Builder cacheControl(String cacheControl) {
            this.cacheControl = cacheControl;
            return this;
        }

        /**
         * <p>Specifies presentational information for the object. For more information, see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec19.html#sec19.5.1">http://www.w3.org/Protocols/rfc2616/rfc2616-sec19.html#sec19.5.1</a>.</p>
         */
        public final Builder contentDisposition(String contentDisposition) {
            this.contentDisposition = contentDisposition;
            return this;
        }

        /**
         * <p>Specifies what content encodings have been applied to the object and thus what decoding
         *          mechanisms must be applied to obtain the media-type referenced by the Content-Type header
         *          field. For more information, see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.11">http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.11</a>.</p>
         */
        public final Builder contentEncoding(String contentEncoding) {
            this.contentEncoding = contentEncoding;
            return this;
        }

        /**
         * <p>The language the content is in.</p>
         */
        public final Builder contentLanguage(String contentLanguage) {
            this.contentLanguage = contentLanguage;
            return this;
        }

        /**
         * <p>Size of the body in bytes. This parameter is useful when the size of the body cannot be
         *          determined automatically. For more information, see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.13">http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.13</a>.</p>
         */
        public final Builder contentLength(Long contentLength) {
            this.contentLength = contentLength;
            return this;
        }

        /**
         * <p>The base64-encoded 128-bit MD5 digest of the message (without the headers) according to
         *          RFC 1864. This header can be used as a message integrity check to verify that the data is
         *          the same data that was originally sent. Although it is optional, we recommend using the
         *          Content-MD5 mechanism as an end-to-end integrity check. For more information about REST
         *          request authentication, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/RESTAuthentication.html">REST
         *             Authentication</a>.</p>
         */
        public final Builder contentMD5(String contentMD5) {
            this.contentMD5 = contentMD5;
            return this;
        }

        /**
         * <p>A standard MIME type describing the format of the contents. For more information, see
         *             <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.17">http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.17</a>.</p>
         */
        public final Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        /**
         * <p>The date and time at which the object is no longer cacheable. For more information, see
         *             <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.21">http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.21</a>.</p>
         */
        public final Builder expires(Instant expires) {
            this.expires = expires;
            return this;
        }

        /**
         * <p>Gives the grantee READ, READ_ACP, and WRITE_ACP permissions on the
         *       object.</p>
         *          <p>This action is not supported by Amazon S3 on Outposts.</p>
         */
        public final Builder grantFullControl(String grantFullControl) {
            this.grantFullControl = grantFullControl;
            return this;
        }

        /**
         * <p>Allows grantee to read the object data and its
         *       metadata.</p>
         *          <p>This action is not supported by Amazon S3 on Outposts.</p>
         */
        public final Builder grantRead(String grantRead) {
            this.grantRead = grantRead;
            return this;
        }

        /**
         * <p>Allows grantee to read the object ACL.</p>
         *          <p>This action is not supported by Amazon S3 on Outposts.</p>
         */
        public final Builder grantReadACP(String grantReadACP) {
            this.grantReadACP = grantReadACP;
            return this;
        }

        /**
         * <p>Allows grantee to write the ACL for the applicable
         *       object.</p>
         *          <p>This action is not supported by Amazon S3 on Outposts.</p>
         */
        public final Builder grantWriteACP(String grantWriteACP) {
            this.grantWriteACP = grantWriteACP;
            return this;
        }

        /**
         * <p>Object key for which the PUT operation was initiated.</p>
         */
        public final Builder key(String key) {
            this.key = key;
            return this;
        }

        /**
         * <p>A map of metadata to store with the object in S3.</p>
         */
        public final Builder metadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        /**
         * <p>The server-side encryption algorithm used when storing this object in Amazon S3 (for example,
         *          AES256, aws:kms).</p>
         */
        public final Builder serverSideEncryption(ServerSideEncryption serverSideEncryption) {
            this.serverSideEncryption = serverSideEncryption;
            return this;
        }

        /**
         * <p>By default, Amazon S3 uses the STANDARD Storage Class to store newly created objects. The
         *          STANDARD storage class provides high durability and high availability. Depending on
         *          performance needs, you can specify a different Storage Class. Amazon S3 on Outposts only uses
         *          the OUTPOSTS Storage Class. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/storage-class-intro.html">Storage Classes</a> in the <i>Amazon S3
         *             Service Developer Guide</i>.</p>
         */
        public final Builder storageClass(StorageClass storageClass) {
            this.storageClass = storageClass;
            return this;
        }

        /**
         * <p>If the bucket is configured as a website, redirects requests for this object to another
         *          object in the same bucket or to an external URL. Amazon S3 stores the value of this header in
         *          the object metadata. For information about object metadata, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/UsingMetadata.html">Object Key and Metadata</a>.</p>
         *
         *          <p>In the following example, the request header sets the redirect to an object
         *          (anotherPage.html) in the same bucket:</p>
         *
         *          <p>
         *             <code>x-amz-website-redirect-location: /anotherPage.html</code>
         *          </p>
         *
         *          <p>In the following example, the request header sets the object redirect to another
         *          website:</p>
         *
         *          <p>
         *             <code>x-amz-website-redirect-location: http://www.example.com/</code>
         *          </p>
         *
         *          <p>For more information about website hosting in Amazon S3, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/WebsiteHosting.html">Hosting Websites on Amazon S3</a> and <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/how-to-page-redirect.html">How to Configure Website Page
         *             Redirects</a>. </p>
         */
        public final Builder websiteRedirectLocation(String websiteRedirectLocation) {
            this.websiteRedirectLocation = websiteRedirectLocation;
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

        /**
         * <p>If <code>x-amz-server-side-encryption</code> is present and has the value of
         *             <code>aws:kms</code>, this header specifies the ID of the AWS Key Management Service
         *          (AWS KMS) symmetrical customer managed customer master key (CMK) that was used for the
         *          object.</p>
         *          <p> If the value of <code>x-amz-server-side-encryption</code> is <code>aws:kms</code>, this
         *          header specifies the ID of the symmetric customer managed AWS KMS CMK that will be used for
         *          the object. If you specify <code>x-amz-server-side-encryption:aws:kms</code>, but do not
         *             provide<code> x-amz-server-side-encryption-aws-kms-key-id</code>, Amazon S3 uses the AWS
         *          managed CMK in AWS to protect the data.</p>
         */
        public final Builder sSEKMSKeyId(String sSEKMSKeyId) {
            this.sSEKMSKeyId = sSEKMSKeyId;
            return this;
        }

        /**
         * <p>Specifies the AWS KMS Encryption Context to use for object encryption. The value of this
         *          header is a base64-encoded UTF-8 string holding JSON with the encryption context key-value
         *          pairs.</p>
         */
        public final Builder sSEKMSEncryptionContext(String sSEKMSEncryptionContext) {
            this.sSEKMSEncryptionContext = sSEKMSEncryptionContext;
            return this;
        }

        /**
         * <p>Specifies whether Amazon S3 should use an S3 Bucket Key for object encryption with server-side encryption using AWS KMS (SSE-KMS). Setting this header to <code>true</code> causes Amazon S3 to use an S3 Bucket Key for object encryption with SSE-KMS.</p>
         *          <p>Specifying this header with a PUT operation doesn’t affect bucket-level settings for S3 Bucket Key.</p>
         */
        public final Builder bucketKeyEnabled(Boolean bucketKeyEnabled) {
            this.bucketKeyEnabled = bucketKeyEnabled;
            return this;
        }

        public final Builder requestPayer(RequestPayer requestPayer) {
            this.requestPayer = requestPayer;
            return this;
        }

        /**
         * <p>The tag-set for the object. The tag-set must be encoded as URL Query parameters. (For
         *          example, "Key1=Value1")</p>
         */
        public final Builder tagging(String tagging) {
            this.tagging = tagging;
            return this;
        }

        /**
         * <p>The Object Lock mode that you want to apply to this object.</p>
         */
        public final Builder objectLockMode(ObjectLockMode objectLockMode) {
            this.objectLockMode = objectLockMode;
            return this;
        }

        /**
         * <p>The date and time when you want this object's Object Lock to expire.</p>
         */
        public final Builder objectLockRetainUntilDate(Instant objectLockRetainUntilDate) {
            this.objectLockRetainUntilDate = objectLockRetainUntilDate;
            return this;
        }

        /**
         * <p>Specifies whether a legal hold will be applied to this object. For more information
         *          about S3 Object Lock, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/object-lock.html">Object
         *          Lock</a>.</p>
         */
        public final Builder objectLockLegalHoldStatus(
                ObjectLockLegalHoldStatus objectLockLegalHoldStatus) {
            this.objectLockLegalHoldStatus = objectLockLegalHoldStatus;
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
