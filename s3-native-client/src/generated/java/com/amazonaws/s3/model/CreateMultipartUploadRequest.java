// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;
import software.amazon.awssdk.crt.http.HttpHeader;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class CreateMultipartUploadRequest {
    /**
     * <p>The canned ACL to apply to the object.</p>
     *          <p>This action is not supported by Amazon S3 on Outposts.</p>
     */
    ObjectCannedACL aCL;

    /**
     * <p>The name of the bucket to which to initiate the upload</p>
     *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     */
    String bucket;

    /**
     * <p>Specifies caching behavior along the request/reply chain.</p>
     */
    String cacheControl;

    /**
     * <p>Specifies presentational information for the object.</p>
     */
    String contentDisposition;

    /**
     * <p>Specifies what content encodings have been applied to the object and thus what decoding
     *          mechanisms must be applied to obtain the media-type referenced by the Content-Type header
     *          field.</p>
     */
    String contentEncoding;

    /**
     * <p>The language the content is in.</p>
     */
    String contentLanguage;

    /**
     * <p>A standard MIME type describing the format of the object data.</p>
     */
    String contentType;

    /**
     * <p>The date and time at which the object is no longer cacheable.</p>
     */
    Instant expires;

    /**
     * <p>Gives the grantee READ, READ_ACP, and WRITE_ACP permissions on the
     *       object.</p>
     *          <p>This action is not supported by Amazon S3 on Outposts.</p>
     */
    String grantFullControl;

    /**
     * <p>Allows grantee to read the object data and its
     *       metadata.</p>
     *          <p>This action is not supported by Amazon S3 on Outposts.</p>
     */
    String grantRead;

    /**
     * <p>Allows grantee to read the object ACL.</p>
     *          <p>This action is not supported by Amazon S3 on Outposts.</p>
     */
    String grantReadACP;

    /**
     * <p>Allows grantee to write the ACL for the applicable
     *       object.</p>
     *          <p>This action is not supported by Amazon S3 on Outposts.</p>
     */
    String grantWriteACP;

    /**
     * <p>Object key for which the multipart upload is to be initiated.</p>
     */
    String key;

    /**
     * <p>A map of metadata to store with the object in S3.</p>
     */
    Map<String, String> metadata;

    /**
     * <p>The server-side encryption algorithm used when storing this object in Amazon S3 (for example,
     *          AES256, aws:kms).</p>
     */
    ServerSideEncryption serverSideEncryption;

    /**
     * <p>By default, Amazon S3 uses the STANDARD Storage Class to store newly created objects. The
     *          STANDARD storage class provides high durability and high availability. Depending on
     *          performance needs, you can specify a different Storage Class. Amazon S3 on Outposts only uses
     *          the OUTPOSTS Storage Class. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/storage-class-intro.html">Storage Classes</a> in the <i>Amazon S3
     *             Service Developer Guide</i>.</p>
     */
    StorageClass storageClass;

    /**
     * <p>If the bucket is configured as a website, redirects requests for this object to another
     *          object in the same bucket or to an external URL. Amazon S3 stores the value of this header in
     *          the object metadata.</p>
     */
    String websiteRedirectLocation;

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

    /**
     * <p>Specifies the ID of the symmetric customer managed AWS KMS CMK to use for object
     *          encryption. All GET and PUT requests for an object protected by AWS KMS will fail if not
     *          made via SSL or using SigV4. For information about configuring using any of the officially
     *          supported AWS SDKs and AWS CLI, see <a href="https://docs.aws.amazon.com/http:/docs.aws.amazon.com/AmazonS3/latest/dev/UsingAWSSDK.html#specify-signature-version">Specifying the Signature Version in Request Authentication</a>
     *          in the <i>Amazon S3 Developer Guide</i>.</p>
     */
    String sSEKMSKeyId;

    /**
     * <p>Specifies the AWS KMS Encryption Context to use for object encryption. The value of this
     *          header is a base64-encoded UTF-8 string holding JSON with the encryption context key-value
     *          pairs.</p>
     */
    String sSEKMSEncryptionContext;

    /**
     * <p>Specifies whether Amazon S3 should use an S3 Bucket Key for object encryption with server-side encryption using AWS KMS (SSE-KMS). Setting this header to <code>true</code> causes Amazon S3 to use an S3 Bucket Key for object encryption with SSE-KMS.</p>
     *          <p>Specifying this header with an object operation doesn’t affect bucket-level settings for S3 Bucket Key.</p>
     */
    Boolean bucketKeyEnabled;

    RequestPayer requestPayer;

    /**
     * <p>The tag-set for the object. The tag-set must be encoded as URL Query parameters.</p>
     */
    String tagging;

    /**
     * <p>Specifies the Object Lock mode that you want to apply to the uploaded object.</p>
     */
    ObjectLockMode objectLockMode;

    /**
     * <p>Specifies the date and time when you want the Object Lock to expire.</p>
     */
    Instant objectLockRetainUntilDate;

    /**
     * <p>Specifies whether you want to apply a Legal Hold to the uploaded object.</p>
     */
    ObjectLockLegalHoldStatus objectLockLegalHoldStatus;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    HttpHeader[] customHeaders;

    String customQueryParameters;

    CreateMultipartUploadRequest() {
        this.aCL = null;
        this.bucket = "";
        this.cacheControl = "";
        this.contentDisposition = "";
        this.contentEncoding = "";
        this.contentLanguage = "";
        this.contentType = "";
        this.expires = null;
        this.grantFullControl = "";
        this.grantRead = "";
        this.grantReadACP = "";
        this.grantWriteACP = "";
        this.key = "";
        this.metadata = null;
        this.serverSideEncryption = null;
        this.storageClass = null;
        this.websiteRedirectLocation = "";
        this.sSECustomerAlgorithm = "";
        this.sSECustomerKey = "";
        this.sSECustomerKeyMD5 = "";
        this.sSEKMSKeyId = "";
        this.sSEKMSEncryptionContext = "";
        this.bucketKeyEnabled = null;
        this.requestPayer = null;
        this.tagging = "";
        this.objectLockMode = null;
        this.objectLockRetainUntilDate = null;
        this.objectLockLegalHoldStatus = null;
        this.expectedBucketOwner = "";
        this.customHeaders = null;
        this.customQueryParameters = "";
    }

    protected CreateMultipartUploadRequest(BuilderImpl builder) {
        this.aCL = builder.aCL;
        this.bucket = builder.bucket;
        this.cacheControl = builder.cacheControl;
        this.contentDisposition = builder.contentDisposition;
        this.contentEncoding = builder.contentEncoding;
        this.contentLanguage = builder.contentLanguage;
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
        this.customHeaders = builder.customHeaders;
        this.customQueryParameters = builder.customQueryParameters;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(CreateMultipartUploadRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof CreateMultipartUploadRequest);
    }

    public ObjectCannedACL aCL() {
        return aCL;
    }

    public String bucket() {
        return bucket;
    }

    public String cacheControl() {
        return cacheControl;
    }

    public String contentDisposition() {
        return contentDisposition;
    }

    public String contentEncoding() {
        return contentEncoding;
    }

    public String contentLanguage() {
        return contentLanguage;
    }

    public String contentType() {
        return contentType;
    }

    public Instant expires() {
        return expires;
    }

    public String grantFullControl() {
        return grantFullControl;
    }

    public String grantRead() {
        return grantRead;
    }

    public String grantReadACP() {
        return grantReadACP;
    }

    public String grantWriteACP() {
        return grantWriteACP;
    }

    public String key() {
        return key;
    }

    public Map<String, String> metadata() {
        return metadata;
    }

    public ServerSideEncryption serverSideEncryption() {
        return serverSideEncryption;
    }

    public StorageClass storageClass() {
        return storageClass;
    }

    public String websiteRedirectLocation() {
        return websiteRedirectLocation;
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

    public String sSEKMSKeyId() {
        return sSEKMSKeyId;
    }

    public String sSEKMSEncryptionContext() {
        return sSEKMSEncryptionContext;
    }

    public Boolean bucketKeyEnabled() {
        return bucketKeyEnabled;
    }

    public RequestPayer requestPayer() {
        return requestPayer;
    }

    public String tagging() {
        return tagging;
    }

    public ObjectLockMode objectLockMode() {
        return objectLockMode;
    }

    public Instant objectLockRetainUntilDate() {
        return objectLockRetainUntilDate;
    }

    public ObjectLockLegalHoldStatus objectLockLegalHoldStatus() {
        return objectLockLegalHoldStatus;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public HttpHeader[] customHeaders() {
        return customHeaders;
    }

    public String customQueryParameters() {
        return customQueryParameters;
    }

    public interface Builder {
        Builder aCL(ObjectCannedACL aCL);

        Builder bucket(String bucket);

        Builder cacheControl(String cacheControl);

        Builder contentDisposition(String contentDisposition);

        Builder contentEncoding(String contentEncoding);

        Builder contentLanguage(String contentLanguage);

        Builder contentType(String contentType);

        Builder expires(Instant expires);

        Builder grantFullControl(String grantFullControl);

        Builder grantRead(String grantRead);

        Builder grantReadACP(String grantReadACP);

        Builder grantWriteACP(String grantWriteACP);

        Builder key(String key);

        Builder metadata(Map<String, String> metadata);

        Builder serverSideEncryption(ServerSideEncryption serverSideEncryption);

        Builder storageClass(StorageClass storageClass);

        Builder websiteRedirectLocation(String websiteRedirectLocation);

        Builder sSECustomerAlgorithm(String sSECustomerAlgorithm);

        Builder sSECustomerKey(String sSECustomerKey);

        Builder sSECustomerKeyMD5(String sSECustomerKeyMD5);

        Builder sSEKMSKeyId(String sSEKMSKeyId);

        Builder sSEKMSEncryptionContext(String sSEKMSEncryptionContext);

        Builder bucketKeyEnabled(Boolean bucketKeyEnabled);

        Builder requestPayer(RequestPayer requestPayer);

        Builder tagging(String tagging);

        Builder objectLockMode(ObjectLockMode objectLockMode);

        Builder objectLockRetainUntilDate(Instant objectLockRetainUntilDate);

        Builder objectLockLegalHoldStatus(ObjectLockLegalHoldStatus objectLockLegalHoldStatus);

        Builder expectedBucketOwner(String expectedBucketOwner);

        Builder customHeaders(HttpHeader[] customHeaders);

        Builder customQueryParameters(String customQueryParameters);

        CreateMultipartUploadRequest build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The canned ACL to apply to the object.</p>
         *          <p>This action is not supported by Amazon S3 on Outposts.</p>
         */
        ObjectCannedACL aCL;

        /**
         * <p>The name of the bucket to which to initiate the upload</p>
         *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        String bucket;

        /**
         * <p>Specifies caching behavior along the request/reply chain.</p>
         */
        String cacheControl;

        /**
         * <p>Specifies presentational information for the object.</p>
         */
        String contentDisposition;

        /**
         * <p>Specifies what content encodings have been applied to the object and thus what decoding
         *          mechanisms must be applied to obtain the media-type referenced by the Content-Type header
         *          field.</p>
         */
        String contentEncoding;

        /**
         * <p>The language the content is in.</p>
         */
        String contentLanguage;

        /**
         * <p>A standard MIME type describing the format of the object data.</p>
         */
        String contentType;

        /**
         * <p>The date and time at which the object is no longer cacheable.</p>
         */
        Instant expires;

        /**
         * <p>Gives the grantee READ, READ_ACP, and WRITE_ACP permissions on the
         *       object.</p>
         *          <p>This action is not supported by Amazon S3 on Outposts.</p>
         */
        String grantFullControl;

        /**
         * <p>Allows grantee to read the object data and its
         *       metadata.</p>
         *          <p>This action is not supported by Amazon S3 on Outposts.</p>
         */
        String grantRead;

        /**
         * <p>Allows grantee to read the object ACL.</p>
         *          <p>This action is not supported by Amazon S3 on Outposts.</p>
         */
        String grantReadACP;

        /**
         * <p>Allows grantee to write the ACL for the applicable
         *       object.</p>
         *          <p>This action is not supported by Amazon S3 on Outposts.</p>
         */
        String grantWriteACP;

        /**
         * <p>Object key for which the multipart upload is to be initiated.</p>
         */
        String key;

        /**
         * <p>A map of metadata to store with the object in S3.</p>
         */
        Map<String, String> metadata;

        /**
         * <p>The server-side encryption algorithm used when storing this object in Amazon S3 (for example,
         *          AES256, aws:kms).</p>
         */
        ServerSideEncryption serverSideEncryption;

        /**
         * <p>By default, Amazon S3 uses the STANDARD Storage Class to store newly created objects. The
         *          STANDARD storage class provides high durability and high availability. Depending on
         *          performance needs, you can specify a different Storage Class. Amazon S3 on Outposts only uses
         *          the OUTPOSTS Storage Class. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/storage-class-intro.html">Storage Classes</a> in the <i>Amazon S3
         *             Service Developer Guide</i>.</p>
         */
        StorageClass storageClass;

        /**
         * <p>If the bucket is configured as a website, redirects requests for this object to another
         *          object in the same bucket or to an external URL. Amazon S3 stores the value of this header in
         *          the object metadata.</p>
         */
        String websiteRedirectLocation;

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

        /**
         * <p>Specifies the ID of the symmetric customer managed AWS KMS CMK to use for object
         *          encryption. All GET and PUT requests for an object protected by AWS KMS will fail if not
         *          made via SSL or using SigV4. For information about configuring using any of the officially
         *          supported AWS SDKs and AWS CLI, see <a href="https://docs.aws.amazon.com/http:/docs.aws.amazon.com/AmazonS3/latest/dev/UsingAWSSDK.html#specify-signature-version">Specifying the Signature Version in Request Authentication</a>
         *          in the <i>Amazon S3 Developer Guide</i>.</p>
         */
        String sSEKMSKeyId;

        /**
         * <p>Specifies the AWS KMS Encryption Context to use for object encryption. The value of this
         *          header is a base64-encoded UTF-8 string holding JSON with the encryption context key-value
         *          pairs.</p>
         */
        String sSEKMSEncryptionContext;

        /**
         * <p>Specifies whether Amazon S3 should use an S3 Bucket Key for object encryption with server-side encryption using AWS KMS (SSE-KMS). Setting this header to <code>true</code> causes Amazon S3 to use an S3 Bucket Key for object encryption with SSE-KMS.</p>
         *          <p>Specifying this header with an object operation doesn’t affect bucket-level settings for S3 Bucket Key.</p>
         */
        Boolean bucketKeyEnabled;

        RequestPayer requestPayer;

        /**
         * <p>The tag-set for the object. The tag-set must be encoded as URL Query parameters.</p>
         */
        String tagging;

        /**
         * <p>Specifies the Object Lock mode that you want to apply to the uploaded object.</p>
         */
        ObjectLockMode objectLockMode;

        /**
         * <p>Specifies the date and time when you want the Object Lock to expire.</p>
         */
        Instant objectLockRetainUntilDate;

        /**
         * <p>Specifies whether you want to apply a Legal Hold to the uploaded object.</p>
         */
        ObjectLockLegalHoldStatus objectLockLegalHoldStatus;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        HttpHeader[] customHeaders;

        String customQueryParameters;

        protected BuilderImpl() {
        }

        private BuilderImpl(CreateMultipartUploadRequest model) {
            aCL(model.aCL);
            bucket(model.bucket);
            cacheControl(model.cacheControl);
            contentDisposition(model.contentDisposition);
            contentEncoding(model.contentEncoding);
            contentLanguage(model.contentLanguage);
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
            customHeaders(model.customHeaders);
            customQueryParameters(model.customQueryParameters);
        }

        public CreateMultipartUploadRequest build() {
            return new CreateMultipartUploadRequest(this);
        }

        public final Builder aCL(ObjectCannedACL aCL) {
            this.aCL = aCL;
            return this;
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder cacheControl(String cacheControl) {
            this.cacheControl = cacheControl;
            return this;
        }

        public final Builder contentDisposition(String contentDisposition) {
            this.contentDisposition = contentDisposition;
            return this;
        }

        public final Builder contentEncoding(String contentEncoding) {
            this.contentEncoding = contentEncoding;
            return this;
        }

        public final Builder contentLanguage(String contentLanguage) {
            this.contentLanguage = contentLanguage;
            return this;
        }

        public final Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public final Builder expires(Instant expires) {
            this.expires = expires;
            return this;
        }

        public final Builder grantFullControl(String grantFullControl) {
            this.grantFullControl = grantFullControl;
            return this;
        }

        public final Builder grantRead(String grantRead) {
            this.grantRead = grantRead;
            return this;
        }

        public final Builder grantReadACP(String grantReadACP) {
            this.grantReadACP = grantReadACP;
            return this;
        }

        public final Builder grantWriteACP(String grantWriteACP) {
            this.grantWriteACP = grantWriteACP;
            return this;
        }

        public final Builder key(String key) {
            this.key = key;
            return this;
        }

        public final Builder metadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public final Builder serverSideEncryption(ServerSideEncryption serverSideEncryption) {
            this.serverSideEncryption = serverSideEncryption;
            return this;
        }

        public final Builder storageClass(StorageClass storageClass) {
            this.storageClass = storageClass;
            return this;
        }

        public final Builder websiteRedirectLocation(String websiteRedirectLocation) {
            this.websiteRedirectLocation = websiteRedirectLocation;
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

        public final Builder sSEKMSKeyId(String sSEKMSKeyId) {
            this.sSEKMSKeyId = sSEKMSKeyId;
            return this;
        }

        public final Builder sSEKMSEncryptionContext(String sSEKMSEncryptionContext) {
            this.sSEKMSEncryptionContext = sSEKMSEncryptionContext;
            return this;
        }

        public final Builder bucketKeyEnabled(Boolean bucketKeyEnabled) {
            this.bucketKeyEnabled = bucketKeyEnabled;
            return this;
        }

        public final Builder requestPayer(RequestPayer requestPayer) {
            this.requestPayer = requestPayer;
            return this;
        }

        public final Builder tagging(String tagging) {
            this.tagging = tagging;
            return this;
        }

        public final Builder objectLockMode(ObjectLockMode objectLockMode) {
            this.objectLockMode = objectLockMode;
            return this;
        }

        public final Builder objectLockRetainUntilDate(Instant objectLockRetainUntilDate) {
            this.objectLockRetainUntilDate = objectLockRetainUntilDate;
            return this;
        }

        public final Builder objectLockLegalHoldStatus(
                ObjectLockLegalHoldStatus objectLockLegalHoldStatus) {
            this.objectLockLegalHoldStatus = objectLockLegalHoldStatus;
            return this;
        }

        public final Builder expectedBucketOwner(String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
            return this;
        }

        public final Builder customHeaders(HttpHeader[] customHeaders) {
            this.customHeaders = customHeaders;
            return this;
        }

        public final Builder customQueryParameters(String customQueryParameters) {
            this.customQueryParameters = customQueryParameters;
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

        public ObjectCannedACL aCL() {
            return aCL;
        }

        public String bucket() {
            return bucket;
        }

        public String cacheControl() {
            return cacheControl;
        }

        public String contentDisposition() {
            return contentDisposition;
        }

        public String contentEncoding() {
            return contentEncoding;
        }

        public String contentLanguage() {
            return contentLanguage;
        }

        public String contentType() {
            return contentType;
        }

        public Instant expires() {
            return expires;
        }

        public String grantFullControl() {
            return grantFullControl;
        }

        public String grantRead() {
            return grantRead;
        }

        public String grantReadACP() {
            return grantReadACP;
        }

        public String grantWriteACP() {
            return grantWriteACP;
        }

        public String key() {
            return key;
        }

        public Map<String, String> metadata() {
            return metadata;
        }

        public ServerSideEncryption serverSideEncryption() {
            return serverSideEncryption;
        }

        public StorageClass storageClass() {
            return storageClass;
        }

        public String websiteRedirectLocation() {
            return websiteRedirectLocation;
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

        public String sSEKMSKeyId() {
            return sSEKMSKeyId;
        }

        public String sSEKMSEncryptionContext() {
            return sSEKMSEncryptionContext;
        }

        public Boolean bucketKeyEnabled() {
            return bucketKeyEnabled;
        }

        public RequestPayer requestPayer() {
            return requestPayer;
        }

        public String tagging() {
            return tagging;
        }

        public ObjectLockMode objectLockMode() {
            return objectLockMode;
        }

        public Instant objectLockRetainUntilDate() {
            return objectLockRetainUntilDate;
        }

        public ObjectLockLegalHoldStatus objectLockLegalHoldStatus() {
            return objectLockLegalHoldStatus;
        }

        public String expectedBucketOwner() {
            return expectedBucketOwner;
        }

        public HttpHeader[] customHeaders() {
            return customHeaders;
        }

        public String customQueryParameters() {
            return customQueryParameters;
        }
    }
}
