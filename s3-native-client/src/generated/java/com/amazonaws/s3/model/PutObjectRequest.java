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
import software.amazon.aws.sdk.crt.annotations.Generated;
import software.amazon.awssdk.crt.http.HttpHeader;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutObjectRequest {
    /**
     * <p>The canned ACL to apply to the object. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/acl-overview.html#CannedACL">Canned
     *       ACL</a>.</p>
     *          <p>This action is not supported by Amazon S3 on Outposts.</p>
     */
    ObjectCannedACL aCL;

    /**
     * <p>Object data.</p>
     */
    byte[] body;

    /**
     * <p>The bucket name to which the PUT operation was initiated. </p>
     *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     */
    String bucket;

    /**
     * <p> Can be used to specify caching behavior along the request/reply chain. For more
     *          information, see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9">http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9</a>.</p>
     */
    String cacheControl;

    /**
     * <p>Specifies presentational information for the object. For more information, see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec19.html#sec19.5.1">http://www.w3.org/Protocols/rfc2616/rfc2616-sec19.html#sec19.5.1</a>.</p>
     */
    String contentDisposition;

    /**
     * <p>Specifies what content encodings have been applied to the object and thus what decoding
     *          mechanisms must be applied to obtain the media-type referenced by the Content-Type header
     *          field. For more information, see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.11">http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.11</a>.</p>
     */
    String contentEncoding;

    /**
     * <p>The language the content is in.</p>
     */
    String contentLanguage;

    /**
     * <p>Size of the body in bytes. This parameter is useful when the size of the body cannot be
     *          determined automatically. For more information, see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.13">http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.13</a>.</p>
     */
    Long contentLength;

    /**
     * <p>The base64-encoded 128-bit MD5 digest of the message (without the headers) according to
     *          RFC 1864. This header can be used as a message integrity check to verify that the data is
     *          the same data that was originally sent. Although it is optional, we recommend using the
     *          Content-MD5 mechanism as an end-to-end integrity check. For more information about REST
     *          request authentication, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/RESTAuthentication.html">REST
     *             Authentication</a>.</p>
     */
    String contentMD5;

    /**
     * <p>A standard MIME type describing the format of the contents. For more information, see
     *             <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.17">http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.17</a>.</p>
     */
    String contentType;

    /**
     * <p>The date and time at which the object is no longer cacheable. For more information, see
     *             <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.21">http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.21</a>.</p>
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
     * <p>Object key for which the PUT operation was initiated.</p>
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
    String sSEKMSKeyId;

    /**
     * <p>Specifies the AWS KMS Encryption Context to use for object encryption. The value of this
     *          header is a base64-encoded UTF-8 string holding JSON with the encryption context key-value
     *          pairs.</p>
     */
    String sSEKMSEncryptionContext;

    /**
     * <p>Specifies whether Amazon S3 should use an S3 Bucket Key for object encryption with server-side encryption using AWS KMS (SSE-KMS). Setting this header to <code>true</code> causes Amazon S3 to use an S3 Bucket Key for object encryption with SSE-KMS.</p>
     *          <p>Specifying this header with a PUT operation doesn’t affect bucket-level settings for S3 Bucket Key.</p>
     */
    Boolean bucketKeyEnabled;

    RequestPayer requestPayer;

    /**
     * <p>The tag-set for the object. The tag-set must be encoded as URL Query parameters. (For
     *          example, "Key1=Value1")</p>
     */
    String tagging;

    /**
     * <p>The Object Lock mode that you want to apply to this object.</p>
     */
    ObjectLockMode objectLockMode;

    /**
     * <p>The date and time when you want this object's Object Lock to expire.</p>
     */
    Instant objectLockRetainUntilDate;

    /**
     * <p>Specifies whether a legal hold will be applied to this object. For more information
     *          about S3 Object Lock, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/object-lock.html">Object
     *          Lock</a>.</p>
     */
    ObjectLockLegalHoldStatus objectLockLegalHoldStatus;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    HttpHeader[] customHeaders;

    String customQueryParameters;

    PutObjectRequest() {
        this.aCL = null;
        this.body = null;
        this.bucket = "";
        this.cacheControl = "";
        this.contentDisposition = "";
        this.contentEncoding = "";
        this.contentLanguage = "";
        this.contentLength = null;
        this.contentMD5 = "";
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

    protected PutObjectRequest(BuilderImpl builder) {
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

    public byte[] body() {
        return body;
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

    public Long contentLength() {
        return contentLength;
    }

    public String contentMD5() {
        return contentMD5;
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

        Builder body(byte[] body);

        Builder bucket(String bucket);

        Builder cacheControl(String cacheControl);

        Builder contentDisposition(String contentDisposition);

        Builder contentEncoding(String contentEncoding);

        Builder contentLanguage(String contentLanguage);

        Builder contentLength(Long contentLength);

        Builder contentMD5(String contentMD5);

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

        PutObjectRequest build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The canned ACL to apply to the object. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/acl-overview.html#CannedACL">Canned
         *       ACL</a>.</p>
         *          <p>This action is not supported by Amazon S3 on Outposts.</p>
         */
        ObjectCannedACL aCL;

        /**
         * <p>Object data.</p>
         */
        byte[] body;

        /**
         * <p>The bucket name to which the PUT operation was initiated. </p>
         *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        String bucket;

        /**
         * <p> Can be used to specify caching behavior along the request/reply chain. For more
         *          information, see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9">http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9</a>.</p>
         */
        String cacheControl;

        /**
         * <p>Specifies presentational information for the object. For more information, see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec19.html#sec19.5.1">http://www.w3.org/Protocols/rfc2616/rfc2616-sec19.html#sec19.5.1</a>.</p>
         */
        String contentDisposition;

        /**
         * <p>Specifies what content encodings have been applied to the object and thus what decoding
         *          mechanisms must be applied to obtain the media-type referenced by the Content-Type header
         *          field. For more information, see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.11">http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.11</a>.</p>
         */
        String contentEncoding;

        /**
         * <p>The language the content is in.</p>
         */
        String contentLanguage;

        /**
         * <p>Size of the body in bytes. This parameter is useful when the size of the body cannot be
         *          determined automatically. For more information, see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.13">http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.13</a>.</p>
         */
        Long contentLength;

        /**
         * <p>The base64-encoded 128-bit MD5 digest of the message (without the headers) according to
         *          RFC 1864. This header can be used as a message integrity check to verify that the data is
         *          the same data that was originally sent. Although it is optional, we recommend using the
         *          Content-MD5 mechanism as an end-to-end integrity check. For more information about REST
         *          request authentication, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/RESTAuthentication.html">REST
         *             Authentication</a>.</p>
         */
        String contentMD5;

        /**
         * <p>A standard MIME type describing the format of the contents. For more information, see
         *             <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.17">http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.17</a>.</p>
         */
        String contentType;

        /**
         * <p>The date and time at which the object is no longer cacheable. For more information, see
         *             <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.21">http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.21</a>.</p>
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
         * <p>Object key for which the PUT operation was initiated.</p>
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
        String sSEKMSKeyId;

        /**
         * <p>Specifies the AWS KMS Encryption Context to use for object encryption. The value of this
         *          header is a base64-encoded UTF-8 string holding JSON with the encryption context key-value
         *          pairs.</p>
         */
        String sSEKMSEncryptionContext;

        /**
         * <p>Specifies whether Amazon S3 should use an S3 Bucket Key for object encryption with server-side encryption using AWS KMS (SSE-KMS). Setting this header to <code>true</code> causes Amazon S3 to use an S3 Bucket Key for object encryption with SSE-KMS.</p>
         *          <p>Specifying this header with a PUT operation doesn’t affect bucket-level settings for S3 Bucket Key.</p>
         */
        Boolean bucketKeyEnabled;

        RequestPayer requestPayer;

        /**
         * <p>The tag-set for the object. The tag-set must be encoded as URL Query parameters. (For
         *          example, "Key1=Value1")</p>
         */
        String tagging;

        /**
         * <p>The Object Lock mode that you want to apply to this object.</p>
         */
        ObjectLockMode objectLockMode;

        /**
         * <p>The date and time when you want this object's Object Lock to expire.</p>
         */
        Instant objectLockRetainUntilDate;

        /**
         * <p>Specifies whether a legal hold will be applied to this object. For more information
         *          about S3 Object Lock, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/object-lock.html">Object
         *          Lock</a>.</p>
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

        private BuilderImpl(PutObjectRequest model) {
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
            customHeaders(model.customHeaders);
            customQueryParameters(model.customQueryParameters);
        }

        public PutObjectRequest build() {
            return new PutObjectRequest(this);
        }

        public final Builder aCL(ObjectCannedACL aCL) {
            this.aCL = aCL;
            return this;
        }

        public final Builder body(byte[] body) {
            this.body = body;
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

        public final Builder contentLength(Long contentLength) {
            this.contentLength = contentLength;
            return this;
        }

        public final Builder contentMD5(String contentMD5) {
            this.contentMD5 = contentMD5;
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

        public byte[] body() {
            return body;
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

        public Long contentLength() {
            return contentLength;
        }

        public String contentMD5() {
            return contentMD5;
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
