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
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetObjectOutput {
    private byte[] body;

    private Boolean deleteMarker;

    private String acceptRanges;

    private String expiration;

    private String restore;

    private Instant lastModified;

    private Long contentLength;

    private String eTag;

    private Integer missingMeta;

    private String versionId;

    private String cacheControl;

    private String contentDisposition;

    private String contentEncoding;

    private String contentLanguage;

    private String contentRange;

    private String contentType;

    private Instant expires;

    private String websiteRedirectLocation;

    private ServerSideEncryption serverSideEncryption;

    private Map<String, String> metadata;

    private String sSECustomerAlgorithm;

    private String sSECustomerKeyMD5;

    private String sSEKMSKeyId;

    private Boolean bucketKeyEnabled;

    private StorageClass storageClass;

    private RequestCharged requestCharged;

    private ReplicationStatus replicationStatus;

    private Integer partsCount;

    private Integer tagCount;

    private ObjectLockMode objectLockMode;

    private Instant objectLockRetainUntilDate;

    private ObjectLockLegalHoldStatus objectLockLegalHoldStatus;

    private GetObjectOutput() {
        this.body = null;
        this.deleteMarker = null;
        this.acceptRanges = null;
        this.expiration = null;
        this.restore = null;
        this.lastModified = null;
        this.contentLength = null;
        this.eTag = null;
        this.missingMeta = null;
        this.versionId = null;
        this.cacheControl = null;
        this.contentDisposition = null;
        this.contentEncoding = null;
        this.contentLanguage = null;
        this.contentRange = null;
        this.contentType = null;
        this.expires = null;
        this.websiteRedirectLocation = null;
        this.serverSideEncryption = null;
        this.metadata = null;
        this.sSECustomerAlgorithm = null;
        this.sSECustomerKeyMD5 = null;
        this.sSEKMSKeyId = null;
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

    private GetObjectOutput(Builder builder) {
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

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setBody(final byte[] body) {
        this.body = body;
    }

    public Boolean deleteMarker() {
        return deleteMarker;
    }

    public void setDeleteMarker(final Boolean deleteMarker) {
        this.deleteMarker = deleteMarker;
    }

    public String acceptRanges() {
        return acceptRanges;
    }

    public void setAcceptRanges(final String acceptRanges) {
        this.acceptRanges = acceptRanges;
    }

    public String expiration() {
        return expiration;
    }

    public void setExpiration(final String expiration) {
        this.expiration = expiration;
    }

    public String restore() {
        return restore;
    }

    public void setRestore(final String restore) {
        this.restore = restore;
    }

    public Instant lastModified() {
        return lastModified;
    }

    public void setLastModified(final Instant lastModified) {
        this.lastModified = lastModified;
    }

    public Long contentLength() {
        return contentLength;
    }

    public void setContentLength(final Long contentLength) {
        this.contentLength = contentLength;
    }

    public String eTag() {
        return eTag;
    }

    public void setETag(final String eTag) {
        this.eTag = eTag;
    }

    public Integer missingMeta() {
        return missingMeta;
    }

    public void setMissingMeta(final Integer missingMeta) {
        this.missingMeta = missingMeta;
    }

    public String versionId() {
        return versionId;
    }

    public void setVersionId(final String versionId) {
        this.versionId = versionId;
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

    public String contentRange() {
        return contentRange;
    }

    public void setContentRange(final String contentRange) {
        this.contentRange = contentRange;
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

    public String websiteRedirectLocation() {
        return websiteRedirectLocation;
    }

    public void setWebsiteRedirectLocation(final String websiteRedirectLocation) {
        this.websiteRedirectLocation = websiteRedirectLocation;
    }

    public ServerSideEncryption serverSideEncryption() {
        return serverSideEncryption;
    }

    public void setServerSideEncryption(final ServerSideEncryption serverSideEncryption) {
        this.serverSideEncryption = serverSideEncryption;
    }

    public Map<String, String> metadata() {
        return metadata;
    }

    public void setMetadata(final Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public String sSECustomerAlgorithm() {
        return sSECustomerAlgorithm;
    }

    public void setSSECustomerAlgorithm(final String sSECustomerAlgorithm) {
        this.sSECustomerAlgorithm = sSECustomerAlgorithm;
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

    public Boolean bucketKeyEnabled() {
        return bucketKeyEnabled;
    }

    public void setBucketKeyEnabled(final Boolean bucketKeyEnabled) {
        this.bucketKeyEnabled = bucketKeyEnabled;
    }

    public StorageClass storageClass() {
        return storageClass;
    }

    public void setStorageClass(final StorageClass storageClass) {
        this.storageClass = storageClass;
    }

    public RequestCharged requestCharged() {
        return requestCharged;
    }

    public void setRequestCharged(final RequestCharged requestCharged) {
        this.requestCharged = requestCharged;
    }

    public ReplicationStatus replicationStatus() {
        return replicationStatus;
    }

    public void setReplicationStatus(final ReplicationStatus replicationStatus) {
        this.replicationStatus = replicationStatus;
    }

    public Integer partsCount() {
        return partsCount;
    }

    public void setPartsCount(final Integer partsCount) {
        this.partsCount = partsCount;
    }

    public Integer tagCount() {
        return tagCount;
    }

    public void setTagCount(final Integer tagCount) {
        this.tagCount = tagCount;
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

    static final class Builder {
        private byte[] body;

        private Boolean deleteMarker;

        private String acceptRanges;

        private String expiration;

        private String restore;

        private Instant lastModified;

        private Long contentLength;

        private String eTag;

        private Integer missingMeta;

        private String versionId;

        private String cacheControl;

        private String contentDisposition;

        private String contentEncoding;

        private String contentLanguage;

        private String contentRange;

        private String contentType;

        private Instant expires;

        private String websiteRedirectLocation;

        private ServerSideEncryption serverSideEncryption;

        private Map<String, String> metadata;

        private String sSECustomerAlgorithm;

        private String sSECustomerKeyMD5;

        private String sSEKMSKeyId;

        private Boolean bucketKeyEnabled;

        private StorageClass storageClass;

        private RequestCharged requestCharged;

        private ReplicationStatus replicationStatus;

        private Integer partsCount;

        private Integer tagCount;

        private ObjectLockMode objectLockMode;

        private Instant objectLockRetainUntilDate;

        private ObjectLockLegalHoldStatus objectLockLegalHoldStatus;

        private Builder() {
        }

        private Builder(GetObjectOutput model) {
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
            return new com.amazonaws.s3.model.GetObjectOutput(this);
        }

        /**
         * <p>Object data.</p>
         */
        public final Builder body(byte[] body) {
            this.body = body;
            return this;
        }

        /**
         * <p>Specifies whether the object retrieved was (true) or was not (false) a Delete Marker. If
         *          false, this response header does not appear in the response.</p>
         */
        public final Builder deleteMarker(Boolean deleteMarker) {
            this.deleteMarker = deleteMarker;
            return this;
        }

        /**
         * <p>Indicates that a range of bytes was specified.</p>
         */
        public final Builder acceptRanges(String acceptRanges) {
            this.acceptRanges = acceptRanges;
            return this;
        }

        /**
         * <p>If the object expiration is configured (see PUT Bucket lifecycle), the response includes
         *          this header. It includes the expiry-date and rule-id key-value pairs providing object
         *          expiration information. The value of the rule-id is URL encoded.</p>
         */
        public final Builder expiration(String expiration) {
            this.expiration = expiration;
            return this;
        }

        /**
         * <p>Provides information about object restoration operation and expiration time of the
         *          restored object copy.</p>
         */
        public final Builder restore(String restore) {
            this.restore = restore;
            return this;
        }

        /**
         * <p>Last modified date of the object</p>
         */
        public final Builder lastModified(Instant lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        /**
         * <p>Size of the body in bytes.</p>
         */
        public final Builder contentLength(Long contentLength) {
            this.contentLength = contentLength;
            return this;
        }

        /**
         * <p>An ETag is an opaque identifier assigned by a web server to a specific version of a
         *          resource found at a URL.</p>
         */
        public final Builder eTag(String eTag) {
            this.eTag = eTag;
            return this;
        }

        /**
         * <p>This is set to the number of metadata entries not returned in <code>x-amz-meta</code>
         *          headers. This can happen if you create metadata using an API like SOAP that supports more
         *          flexible metadata than the REST API. For example, using SOAP, you can create metadata whose
         *          values are not legal HTTP headers.</p>
         */
        public final Builder missingMeta(Integer missingMeta) {
            this.missingMeta = missingMeta;
            return this;
        }

        /**
         * <p>Version of the object.</p>
         */
        public final Builder versionId(String versionId) {
            this.versionId = versionId;
            return this;
        }

        /**
         * <p>Specifies caching behavior along the request/reply chain.</p>
         */
        public final Builder cacheControl(String cacheControl) {
            this.cacheControl = cacheControl;
            return this;
        }

        /**
         * <p>Specifies presentational information for the object.</p>
         */
        public final Builder contentDisposition(String contentDisposition) {
            this.contentDisposition = contentDisposition;
            return this;
        }

        /**
         * <p>Specifies what content encodings have been applied to the object and thus what decoding
         *          mechanisms must be applied to obtain the media-type referenced by the Content-Type header
         *          field.</p>
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
         * <p>The portion of the object returned in the response.</p>
         */
        public final Builder contentRange(String contentRange) {
            this.contentRange = contentRange;
            return this;
        }

        /**
         * <p>A standard MIME type describing the format of the object data.</p>
         */
        public final Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        /**
         * <p>The date and time at which the object is no longer cacheable.</p>
         */
        public final Builder expires(Instant expires) {
            this.expires = expires;
            return this;
        }

        /**
         * <p>If the bucket is configured as a website, redirects requests for this object to another
         *          object in the same bucket or to an external URL. Amazon S3 stores the value of this header in
         *          the object metadata.</p>
         */
        public final Builder websiteRedirectLocation(String websiteRedirectLocation) {
            this.websiteRedirectLocation = websiteRedirectLocation;
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
         * <p>A map of metadata to store with the object in S3.</p>
         */
        public final Builder metadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        /**
         * <p>If server-side encryption with a customer-provided encryption key was requested, the
         *          response will include this header confirming the encryption algorithm used.</p>
         */
        public final Builder sSECustomerAlgorithm(String sSECustomerAlgorithm) {
            this.sSECustomerAlgorithm = sSECustomerAlgorithm;
            return this;
        }

        /**
         * <p>If server-side encryption with a customer-provided encryption key was requested, the
         *          response will include this header to provide round-trip message integrity verification of
         *          the customer-provided encryption key.</p>
         */
        public final Builder sSECustomerKeyMD5(String sSECustomerKeyMD5) {
            this.sSECustomerKeyMD5 = sSECustomerKeyMD5;
            return this;
        }

        /**
         * <p>If present, specifies the ID of the AWS Key Management Service (AWS KMS) symmetric
         *          customer managed customer master key (CMK) that was used for the object.</p>
         */
        public final Builder sSEKMSKeyId(String sSEKMSKeyId) {
            this.sSEKMSKeyId = sSEKMSKeyId;
            return this;
        }

        /**
         * <p>Indicates whether the object uses an S3 Bucket Key for server-side encryption with AWS KMS (SSE-KMS).</p>
         */
        public final Builder bucketKeyEnabled(Boolean bucketKeyEnabled) {
            this.bucketKeyEnabled = bucketKeyEnabled;
            return this;
        }

        /**
         * <p>Provides storage class information of the object. Amazon S3 returns this header for all
         *          objects except for S3 Standard storage class objects.</p>
         */
        public final Builder storageClass(StorageClass storageClass) {
            this.storageClass = storageClass;
            return this;
        }

        public final Builder requestCharged(RequestCharged requestCharged) {
            this.requestCharged = requestCharged;
            return this;
        }

        /**
         * <p>Amazon S3 can return this if your request involves a bucket that is either a source or
         *          destination in a replication rule.</p>
         */
        public final Builder replicationStatus(ReplicationStatus replicationStatus) {
            this.replicationStatus = replicationStatus;
            return this;
        }

        /**
         * <p>The count of parts this object has.</p>
         */
        public final Builder partsCount(Integer partsCount) {
            this.partsCount = partsCount;
            return this;
        }

        /**
         * <p>The number of tags, if any, on the object.</p>
         */
        public final Builder tagCount(Integer tagCount) {
            this.tagCount = tagCount;
            return this;
        }

        /**
         * <p>The Object Lock mode currently in place for this object.</p>
         */
        public final Builder objectLockMode(ObjectLockMode objectLockMode) {
            this.objectLockMode = objectLockMode;
            return this;
        }

        /**
         * <p>The date and time when this object's Object Lock will expire.</p>
         */
        public final Builder objectLockRetainUntilDate(Instant objectLockRetainUntilDate) {
            this.objectLockRetainUntilDate = objectLockRetainUntilDate;
            return this;
        }

        /**
         * <p>Indicates whether this object has an active legal hold. This field is only returned if
         *          you have permission to view an object's legal hold status. </p>
         */
        public final Builder objectLockLegalHoldStatus(
                ObjectLockLegalHoldStatus objectLockLegalHoldStatus) {
            this.objectLockLegalHoldStatus = objectLockLegalHoldStatus;
            return this;
        }
    }
}
