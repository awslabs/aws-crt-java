// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetObjectOutput {
    /**
     * <p>Object data.</p>
     */
    byte[] body;

    /**
     * <p>Specifies whether the object retrieved was (true) or was not (false) a Delete Marker. If
     *          false, this response header does not appear in the response.</p>
     */
    Boolean deleteMarker;

    /**
     * <p>Indicates that a range of bytes was specified.</p>
     */
    String acceptRanges;

    /**
     * <p>If the object expiration is configured (see PUT Bucket lifecycle), the response includes
     *          this header. It includes the expiry-date and rule-id key-value pairs providing object
     *          expiration information. The value of the rule-id is URL encoded.</p>
     */
    String expiration;

    /**
     * <p>Provides information about object restoration operation and expiration time of the
     *          restored object copy.</p>
     */
    String restore;

    /**
     * <p>Last modified date of the object</p>
     */
    Instant lastModified;

    /**
     * <p>Size of the body in bytes.</p>
     */
    Long contentLength;

    /**
     * <p>An ETag is an opaque identifier assigned by a web server to a specific version of a
     *          resource found at a URL.</p>
     */
    String eTag;

    /**
     * <p>This is set to the number of metadata entries not returned in <code>x-amz-meta</code>
     *          headers. This can happen if you create metadata using an API like SOAP that supports more
     *          flexible metadata than the REST API. For example, using SOAP, you can create metadata whose
     *          values are not legal HTTP headers.</p>
     */
    Integer missingMeta;

    /**
     * <p>Version of the object.</p>
     */
    String versionId;

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
     * <p>The portion of the object returned in the response.</p>
     */
    String contentRange;

    /**
     * <p>A standard MIME type describing the format of the object data.</p>
     */
    String contentType;

    /**
     * <p>The date and time at which the object is no longer cacheable.</p>
     */
    Instant expires;

    /**
     * <p>If the bucket is configured as a website, redirects requests for this object to another
     *          object in the same bucket or to an external URL. Amazon S3 stores the value of this header in
     *          the object metadata.</p>
     */
    String websiteRedirectLocation;

    /**
     * <p>The server-side encryption algorithm used when storing this object in Amazon S3 (for example,
     *          AES256, aws:kms).</p>
     */
    ServerSideEncryption serverSideEncryption;

    /**
     * <p>A map of metadata to store with the object in S3.</p>
     */
    Map<String, String> metadata;

    /**
     * <p>If server-side encryption with a customer-provided encryption key was requested, the
     *          response will include this header confirming the encryption algorithm used.</p>
     */
    String sSECustomerAlgorithm;

    /**
     * <p>If server-side encryption with a customer-provided encryption key was requested, the
     *          response will include this header to provide round-trip message integrity verification of
     *          the customer-provided encryption key.</p>
     */
    String sSECustomerKeyMD5;

    /**
     * <p>If present, specifies the ID of the AWS Key Management Service (AWS KMS) symmetric
     *          customer managed customer master key (CMK) that was used for the object.</p>
     */
    String sSEKMSKeyId;

    /**
     * <p>Indicates whether the object uses an S3 Bucket Key for server-side encryption with AWS KMS (SSE-KMS).</p>
     */
    Boolean bucketKeyEnabled;

    /**
     * <p>Provides storage class information of the object. Amazon S3 returns this header for all
     *          objects except for S3 Standard storage class objects.</p>
     */
    StorageClass storageClass;

    RequestCharged requestCharged;

    /**
     * <p>Amazon S3 can return this if your request involves a bucket that is either a source or
     *          destination in a replication rule.</p>
     */
    ReplicationStatus replicationStatus;

    /**
     * <p>The count of parts this object has.</p>
     */
    Integer partsCount;

    /**
     * <p>The number of tags, if any, on the object.</p>
     */
    Integer tagCount;

    /**
     * <p>The Object Lock mode currently in place for this object.</p>
     */
    ObjectLockMode objectLockMode;

    /**
     * <p>The date and time when this object's Object Lock will expire.</p>
     */
    Instant objectLockRetainUntilDate;

    /**
     * <p>Indicates whether this object has an active legal hold. This field is only returned if
     *          you have permission to view an object's legal hold status. </p>
     */
    ObjectLockLegalHoldStatus objectLockLegalHoldStatus;

    GetObjectOutput() {
        this.body = null;
        this.deleteMarker = null;
        this.acceptRanges = "";
        this.expiration = "";
        this.restore = "";
        this.lastModified = null;
        this.contentLength = null;
        this.eTag = "";
        this.missingMeta = null;
        this.versionId = "";
        this.cacheControl = "";
        this.contentDisposition = "";
        this.contentEncoding = "";
        this.contentLanguage = "";
        this.contentRange = "";
        this.contentType = "";
        this.expires = null;
        this.websiteRedirectLocation = "";
        this.serverSideEncryption = null;
        this.metadata = null;
        this.sSECustomerAlgorithm = "";
        this.sSECustomerKeyMD5 = "";
        this.sSEKMSKeyId = "";
        this.bucketKeyEnabled = null;
        this.storageClass = null;
        this.requestCharged = null;
        this.replicationStatus = null;
        this.partsCount = null;
        this.tagCount = null;
        this.objectLockMode = null;
        this.objectLockRetainUntilDate = null;
        this.objectLockLegalHoldStatus = null;
    }

    protected GetObjectOutput(BuilderImpl builder) {
        this.body = builder.body;
        this.deleteMarker = builder.deleteMarker;
        this.acceptRanges = builder.acceptRanges;
        this.expiration = builder.expiration;
        this.restore = builder.restore;
        this.lastModified = builder.lastModified;
        this.contentLength = builder.contentLength;
        this.eTag = builder.eTag;
        this.missingMeta = builder.missingMeta;
        this.versionId = builder.versionId;
        this.cacheControl = builder.cacheControl;
        this.contentDisposition = builder.contentDisposition;
        this.contentEncoding = builder.contentEncoding;
        this.contentLanguage = builder.contentLanguage;
        this.contentRange = builder.contentRange;
        this.contentType = builder.contentType;
        this.expires = builder.expires;
        this.websiteRedirectLocation = builder.websiteRedirectLocation;
        this.serverSideEncryption = builder.serverSideEncryption;
        this.metadata = builder.metadata;
        this.sSECustomerAlgorithm = builder.sSECustomerAlgorithm;
        this.sSECustomerKeyMD5 = builder.sSECustomerKeyMD5;
        this.sSEKMSKeyId = builder.sSEKMSKeyId;
        this.bucketKeyEnabled = builder.bucketKeyEnabled;
        this.storageClass = builder.storageClass;
        this.requestCharged = builder.requestCharged;
        this.replicationStatus = builder.replicationStatus;
        this.partsCount = builder.partsCount;
        this.tagCount = builder.tagCount;
        this.objectLockMode = builder.objectLockMode;
        this.objectLockRetainUntilDate = builder.objectLockRetainUntilDate;
        this.objectLockLegalHoldStatus = builder.objectLockLegalHoldStatus;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(GetObjectOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetObjectOutput);
    }

    public byte[] body() {
        return body;
    }

    public Boolean deleteMarker() {
        return deleteMarker;
    }

    public String acceptRanges() {
        return acceptRanges;
    }

    public String expiration() {
        return expiration;
    }

    public String restore() {
        return restore;
    }

    public Instant lastModified() {
        return lastModified;
    }

    public Long contentLength() {
        return contentLength;
    }

    public String eTag() {
        return eTag;
    }

    public Integer missingMeta() {
        return missingMeta;
    }

    public String versionId() {
        return versionId;
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

    public String contentRange() {
        return contentRange;
    }

    public String contentType() {
        return contentType;
    }

    public Instant expires() {
        return expires;
    }

    public String websiteRedirectLocation() {
        return websiteRedirectLocation;
    }

    public ServerSideEncryption serverSideEncryption() {
        return serverSideEncryption;
    }

    public Map<String, String> metadata() {
        return metadata;
    }

    public String sSECustomerAlgorithm() {
        return sSECustomerAlgorithm;
    }

    public String sSECustomerKeyMD5() {
        return sSECustomerKeyMD5;
    }

    public String sSEKMSKeyId() {
        return sSEKMSKeyId;
    }

    public Boolean bucketKeyEnabled() {
        return bucketKeyEnabled;
    }

    public StorageClass storageClass() {
        return storageClass;
    }

    public RequestCharged requestCharged() {
        return requestCharged;
    }

    public ReplicationStatus replicationStatus() {
        return replicationStatus;
    }

    public Integer partsCount() {
        return partsCount;
    }

    public Integer tagCount() {
        return tagCount;
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

    public interface Builder {
        Builder body(byte[] body);

        Builder deleteMarker(Boolean deleteMarker);

        Builder acceptRanges(String acceptRanges);

        Builder expiration(String expiration);

        Builder restore(String restore);

        Builder lastModified(Instant lastModified);

        Builder contentLength(Long contentLength);

        Builder eTag(String eTag);

        Builder missingMeta(Integer missingMeta);

        Builder versionId(String versionId);

        Builder cacheControl(String cacheControl);

        Builder contentDisposition(String contentDisposition);

        Builder contentEncoding(String contentEncoding);

        Builder contentLanguage(String contentLanguage);

        Builder contentRange(String contentRange);

        Builder contentType(String contentType);

        Builder expires(Instant expires);

        Builder websiteRedirectLocation(String websiteRedirectLocation);

        Builder serverSideEncryption(ServerSideEncryption serverSideEncryption);

        Builder metadata(Map<String, String> metadata);

        Builder sSECustomerAlgorithm(String sSECustomerAlgorithm);

        Builder sSECustomerKeyMD5(String sSECustomerKeyMD5);

        Builder sSEKMSKeyId(String sSEKMSKeyId);

        Builder bucketKeyEnabled(Boolean bucketKeyEnabled);

        Builder storageClass(StorageClass storageClass);

        Builder requestCharged(RequestCharged requestCharged);

        Builder replicationStatus(ReplicationStatus replicationStatus);

        Builder partsCount(Integer partsCount);

        Builder tagCount(Integer tagCount);

        Builder objectLockMode(ObjectLockMode objectLockMode);

        Builder objectLockRetainUntilDate(Instant objectLockRetainUntilDate);

        Builder objectLockLegalHoldStatus(ObjectLockLegalHoldStatus objectLockLegalHoldStatus);

        GetObjectOutput build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Object data.</p>
         */
        byte[] body;

        /**
         * <p>Specifies whether the object retrieved was (true) or was not (false) a Delete Marker. If
         *          false, this response header does not appear in the response.</p>
         */
        Boolean deleteMarker;

        /**
         * <p>Indicates that a range of bytes was specified.</p>
         */
        String acceptRanges;

        /**
         * <p>If the object expiration is configured (see PUT Bucket lifecycle), the response includes
         *          this header. It includes the expiry-date and rule-id key-value pairs providing object
         *          expiration information. The value of the rule-id is URL encoded.</p>
         */
        String expiration;

        /**
         * <p>Provides information about object restoration operation and expiration time of the
         *          restored object copy.</p>
         */
        String restore;

        /**
         * <p>Last modified date of the object</p>
         */
        Instant lastModified;

        /**
         * <p>Size of the body in bytes.</p>
         */
        Long contentLength;

        /**
         * <p>An ETag is an opaque identifier assigned by a web server to a specific version of a
         *          resource found at a URL.</p>
         */
        String eTag;

        /**
         * <p>This is set to the number of metadata entries not returned in <code>x-amz-meta</code>
         *          headers. This can happen if you create metadata using an API like SOAP that supports more
         *          flexible metadata than the REST API. For example, using SOAP, you can create metadata whose
         *          values are not legal HTTP headers.</p>
         */
        Integer missingMeta;

        /**
         * <p>Version of the object.</p>
         */
        String versionId;

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
         * <p>The portion of the object returned in the response.</p>
         */
        String contentRange;

        /**
         * <p>A standard MIME type describing the format of the object data.</p>
         */
        String contentType;

        /**
         * <p>The date and time at which the object is no longer cacheable.</p>
         */
        Instant expires;

        /**
         * <p>If the bucket is configured as a website, redirects requests for this object to another
         *          object in the same bucket or to an external URL. Amazon S3 stores the value of this header in
         *          the object metadata.</p>
         */
        String websiteRedirectLocation;

        /**
         * <p>The server-side encryption algorithm used when storing this object in Amazon S3 (for example,
         *          AES256, aws:kms).</p>
         */
        ServerSideEncryption serverSideEncryption;

        /**
         * <p>A map of metadata to store with the object in S3.</p>
         */
        Map<String, String> metadata;

        /**
         * <p>If server-side encryption with a customer-provided encryption key was requested, the
         *          response will include this header confirming the encryption algorithm used.</p>
         */
        String sSECustomerAlgorithm;

        /**
         * <p>If server-side encryption with a customer-provided encryption key was requested, the
         *          response will include this header to provide round-trip message integrity verification of
         *          the customer-provided encryption key.</p>
         */
        String sSECustomerKeyMD5;

        /**
         * <p>If present, specifies the ID of the AWS Key Management Service (AWS KMS) symmetric
         *          customer managed customer master key (CMK) that was used for the object.</p>
         */
        String sSEKMSKeyId;

        /**
         * <p>Indicates whether the object uses an S3 Bucket Key for server-side encryption with AWS KMS (SSE-KMS).</p>
         */
        Boolean bucketKeyEnabled;

        /**
         * <p>Provides storage class information of the object. Amazon S3 returns this header for all
         *          objects except for S3 Standard storage class objects.</p>
         */
        StorageClass storageClass;

        RequestCharged requestCharged;

        /**
         * <p>Amazon S3 can return this if your request involves a bucket that is either a source or
         *          destination in a replication rule.</p>
         */
        ReplicationStatus replicationStatus;

        /**
         * <p>The count of parts this object has.</p>
         */
        Integer partsCount;

        /**
         * <p>The number of tags, if any, on the object.</p>
         */
        Integer tagCount;

        /**
         * <p>The Object Lock mode currently in place for this object.</p>
         */
        ObjectLockMode objectLockMode;

        /**
         * <p>The date and time when this object's Object Lock will expire.</p>
         */
        Instant objectLockRetainUntilDate;

        /**
         * <p>Indicates whether this object has an active legal hold. This field is only returned if
         *          you have permission to view an object's legal hold status. </p>
         */
        ObjectLockLegalHoldStatus objectLockLegalHoldStatus;

        protected BuilderImpl() {
        }

        private BuilderImpl(GetObjectOutput model) {
            body(model.body);
            deleteMarker(model.deleteMarker);
            acceptRanges(model.acceptRanges);
            expiration(model.expiration);
            restore(model.restore);
            lastModified(model.lastModified);
            contentLength(model.contentLength);
            eTag(model.eTag);
            missingMeta(model.missingMeta);
            versionId(model.versionId);
            cacheControl(model.cacheControl);
            contentDisposition(model.contentDisposition);
            contentEncoding(model.contentEncoding);
            contentLanguage(model.contentLanguage);
            contentRange(model.contentRange);
            contentType(model.contentType);
            expires(model.expires);
            websiteRedirectLocation(model.websiteRedirectLocation);
            serverSideEncryption(model.serverSideEncryption);
            metadata(model.metadata);
            sSECustomerAlgorithm(model.sSECustomerAlgorithm);
            sSECustomerKeyMD5(model.sSECustomerKeyMD5);
            sSEKMSKeyId(model.sSEKMSKeyId);
            bucketKeyEnabled(model.bucketKeyEnabled);
            storageClass(model.storageClass);
            requestCharged(model.requestCharged);
            replicationStatus(model.replicationStatus);
            partsCount(model.partsCount);
            tagCount(model.tagCount);
            objectLockMode(model.objectLockMode);
            objectLockRetainUntilDate(model.objectLockRetainUntilDate);
            objectLockLegalHoldStatus(model.objectLockLegalHoldStatus);
        }

        public GetObjectOutput build() {
            return new GetObjectOutput(this);
        }

        public final Builder body(byte[] body) {
            this.body = body;
            return this;
        }

        public final Builder deleteMarker(Boolean deleteMarker) {
            this.deleteMarker = deleteMarker;
            return this;
        }

        public final Builder acceptRanges(String acceptRanges) {
            this.acceptRanges = acceptRanges;
            return this;
        }

        public final Builder expiration(String expiration) {
            this.expiration = expiration;
            return this;
        }

        public final Builder restore(String restore) {
            this.restore = restore;
            return this;
        }

        public final Builder lastModified(Instant lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public final Builder contentLength(Long contentLength) {
            this.contentLength = contentLength;
            return this;
        }

        public final Builder eTag(String eTag) {
            this.eTag = eTag;
            return this;
        }

        public final Builder missingMeta(Integer missingMeta) {
            this.missingMeta = missingMeta;
            return this;
        }

        public final Builder versionId(String versionId) {
            this.versionId = versionId;
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

        public final Builder contentRange(String contentRange) {
            this.contentRange = contentRange;
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

        public final Builder websiteRedirectLocation(String websiteRedirectLocation) {
            this.websiteRedirectLocation = websiteRedirectLocation;
            return this;
        }

        public final Builder serverSideEncryption(ServerSideEncryption serverSideEncryption) {
            this.serverSideEncryption = serverSideEncryption;
            return this;
        }

        public final Builder metadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public final Builder sSECustomerAlgorithm(String sSECustomerAlgorithm) {
            this.sSECustomerAlgorithm = sSECustomerAlgorithm;
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

        public final Builder bucketKeyEnabled(Boolean bucketKeyEnabled) {
            this.bucketKeyEnabled = bucketKeyEnabled;
            return this;
        }

        public final Builder storageClass(StorageClass storageClass) {
            this.storageClass = storageClass;
            return this;
        }

        public final Builder requestCharged(RequestCharged requestCharged) {
            this.requestCharged = requestCharged;
            return this;
        }

        public final Builder replicationStatus(ReplicationStatus replicationStatus) {
            this.replicationStatus = replicationStatus;
            return this;
        }

        public final Builder partsCount(Integer partsCount) {
            this.partsCount = partsCount;
            return this;
        }

        public final Builder tagCount(Integer tagCount) {
            this.tagCount = tagCount;
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

        public Boolean deleteMarker() {
            return deleteMarker;
        }

        public String acceptRanges() {
            return acceptRanges;
        }

        public String expiration() {
            return expiration;
        }

        public String restore() {
            return restore;
        }

        public Instant lastModified() {
            return lastModified;
        }

        public Long contentLength() {
            return contentLength;
        }

        public String eTag() {
            return eTag;
        }

        public Integer missingMeta() {
            return missingMeta;
        }

        public String versionId() {
            return versionId;
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

        public String contentRange() {
            return contentRange;
        }

        public String contentType() {
            return contentType;
        }

        public Instant expires() {
            return expires;
        }

        public String websiteRedirectLocation() {
            return websiteRedirectLocation;
        }

        public ServerSideEncryption serverSideEncryption() {
            return serverSideEncryption;
        }

        public Map<String, String> metadata() {
            return metadata;
        }

        public String sSECustomerAlgorithm() {
            return sSECustomerAlgorithm;
        }

        public String sSECustomerKeyMD5() {
            return sSECustomerKeyMD5;
        }

        public String sSEKMSKeyId() {
            return sSEKMSKeyId;
        }

        public Boolean bucketKeyEnabled() {
            return bucketKeyEnabled;
        }

        public StorageClass storageClass() {
            return storageClass;
        }

        public RequestCharged requestCharged() {
            return requestCharged;
        }

        public ReplicationStatus replicationStatus() {
            return replicationStatus;
        }

        public Integer partsCount() {
            return partsCount;
        }

        public Integer tagCount() {
            return tagCount;
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
    }
}
