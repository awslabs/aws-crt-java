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
public class HeadObjectOutput {
    private Boolean deleteMarker;

    private String acceptRanges;

    private String expiration;

    private String restore;

    private ArchiveStatus archiveStatus;

    private Instant lastModified;

    private Long contentLength;

    private String eTag;

    private Integer missingMeta;

    private String versionId;

    private String cacheControl;

    private String contentDisposition;

    private String contentEncoding;

    private String contentLanguage;

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

    private ObjectLockMode objectLockMode;

    private Instant objectLockRetainUntilDate;

    private ObjectLockLegalHoldStatus objectLockLegalHoldStatus;

    private HeadObjectOutput() {
        this.deleteMarker = null;
        this.acceptRanges = null;
        this.expiration = null;
        this.restore = null;
        this.archiveStatus = null;
        this.lastModified = null;
        this.contentLength = null;
        this.eTag = null;
        this.missingMeta = null;
        this.versionId = null;
        this.cacheControl = null;
        this.contentDisposition = null;
        this.contentEncoding = null;
        this.contentLanguage = null;
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
        this.objectLockMode = null;
        this.objectLockRetainUntilDate = null;
        this.objectLockLegalHoldStatus = null;
    }

    private HeadObjectOutput(Builder builder) {
        this.deleteMarker = builder.deleteMarker;
        this.acceptRanges = builder.acceptRanges;
        this.expiration = builder.expiration;
        this.restore = builder.restore;
        this.archiveStatus = builder.archiveStatus;
        this.lastModified = builder.lastModified;
        this.contentLength = builder.contentLength;
        this.eTag = builder.eTag;
        this.missingMeta = builder.missingMeta;
        this.versionId = builder.versionId;
        this.cacheControl = builder.cacheControl;
        this.contentDisposition = builder.contentDisposition;
        this.contentEncoding = builder.contentEncoding;
        this.contentLanguage = builder.contentLanguage;
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
        return Objects.hash(HeadObjectOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof HeadObjectOutput);
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

    public ArchiveStatus archiveStatus() {
        return archiveStatus;
    }

    public void setArchiveStatus(final ArchiveStatus archiveStatus) {
        this.archiveStatus = archiveStatus;
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
        private Boolean deleteMarker;

        private String acceptRanges;

        private String expiration;

        private String restore;

        private ArchiveStatus archiveStatus;

        private Instant lastModified;

        private Long contentLength;

        private String eTag;

        private Integer missingMeta;

        private String versionId;

        private String cacheControl;

        private String contentDisposition;

        private String contentEncoding;

        private String contentLanguage;

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

        private ObjectLockMode objectLockMode;

        private Instant objectLockRetainUntilDate;

        private ObjectLockLegalHoldStatus objectLockLegalHoldStatus;

        private Builder() {
        }

        private Builder(HeadObjectOutput model) {
            deleteMarker(model.deleteMarker);
            acceptRanges(model.acceptRanges);
            expiration(model.expiration);
            restore(model.restore);
            archiveStatus(model.archiveStatus);
            lastModified(model.lastModified);
            contentLength(model.contentLength);
            eTag(model.eTag);
            missingMeta(model.missingMeta);
            versionId(model.versionId);
            cacheControl(model.cacheControl);
            contentDisposition(model.contentDisposition);
            contentEncoding(model.contentEncoding);
            contentLanguage(model.contentLanguage);
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
            objectLockMode(model.objectLockMode);
            objectLockRetainUntilDate(model.objectLockRetainUntilDate);
            objectLockLegalHoldStatus(model.objectLockLegalHoldStatus);
        }

        public HeadObjectOutput build() {
            return new com.amazonaws.s3.model.HeadObjectOutput(this);
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
         * <p>If the object is an archived object (an object whose storage class is GLACIER), the
         *          response includes this header if either the archive restoration is in progress (see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_RestoreObject.html">RestoreObject</a> or an archive copy is already restored.</p>
         *
         *          <p> If an archive copy is already restored, the header value indicates when Amazon S3 is
         *          scheduled to delete the object copy. For example:</p>
         *
         *          <p>
         *             <code>x-amz-restore: ongoing-request="false", expiry-date="Fri, 23 Dec 2012 00:00:00
         *             GMT"</code>
         *          </p>
         *
         *          <p>If the object restoration is in progress, the header returns the value
         *             <code>ongoing-request="true"</code>.</p>
         *
         *          <p>For more information about archiving objects, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/object-lifecycle-mgmt.html#lifecycle-transition-general-considerations">Transitioning Objects: General Considerations</a>.</p>
         */
        public final Builder restore(String restore) {
            this.restore = restore;
            return this;
        }

        /**
         * <p>The archive state of the head object.</p>
         */
        public final Builder archiveStatus(ArchiveStatus archiveStatus) {
            this.archiveStatus = archiveStatus;
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
         * <p>If the object is stored using server-side encryption either with an AWS KMS customer
         *          master key (CMK) or an Amazon S3-managed encryption key, the response includes this header with
         *          the value of the server-side encryption algorithm used when storing this object in Amazon
         *          S3 (for example, AES256, aws:kms).</p>
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
         *
         *          <p>For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/storage-class-intro.html">Storage
         *             Classes</a>.</p>
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
         * <p>Amazon S3 can return this header if your request involves a bucket that is either a source or
         *          a destination in a replication rule.</p>
         *
         *          <p>In replication, you have a source bucket on which you configure replication and
         *          destination bucket or buckets where Amazon S3 stores object replicas. When you request an object
         *             (<code>GetObject</code>) or object metadata (<code>HeadObject</code>) from these
         *          buckets, Amazon S3 will return the <code>x-amz-replication-status</code> header in the response
         *          as follows:</p>
         *          <ul>
         *             <li>
         *                <p>If requesting an object from the source bucket — Amazon S3 will return the
         *                   <code>x-amz-replication-status</code> header if the object in your request is
         *                eligible for replication.</p>
         *                <p> For example, suppose that in your replication configuration, you specify object
         *                prefix <code>TaxDocs</code> requesting Amazon S3 to replicate objects with key prefix
         *                   <code>TaxDocs</code>. Any objects you upload with this key name prefix, for
         *                example <code>TaxDocs/document1.pdf</code>, are eligible for replication. For any
         *                object request with this key name prefix, Amazon S3 will return the
         *                   <code>x-amz-replication-status</code> header with value PENDING, COMPLETED or
         *                FAILED indicating object replication status.</p>
         *             </li>
         *             <li>
         *                <p>If requesting an object from a destination bucket — Amazon S3 will return the
         *                   <code>x-amz-replication-status</code> header with value REPLICA if the object in
         *                your request is a replica that Amazon S3 created and there is no replica modification
         *                replication in progress.</p>
         *             </li>
         *             <li>
         *                <p>When replicating objects to multiple destination buckets the
         *                   <code>x-amz-replication-status</code> header acts differently. The header of the
         *                source object will only return a value of COMPLETED when replication is successful to
         *                all destinations. The header will remain at value PENDING until replication has
         *                completed for all destinations. If one or more destinations fails replication the
         *                header will return FAILED. </p>
         *             </li>
         *          </ul>
         *
         *          <p>For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/NotificationHowTo.html">Replication</a>.</p>
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
         * <p>The Object Lock mode, if any, that's in effect for this object. This header is only
         *          returned if the requester has the <code>s3:GetObjectRetention</code> permission. For more
         *          information about S3 Object Lock, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/object-lock.html">Object
         *             Lock</a>. </p>
         */
        public final Builder objectLockMode(ObjectLockMode objectLockMode) {
            this.objectLockMode = objectLockMode;
            return this;
        }

        /**
         * <p>The date and time when the Object Lock retention period expires. This header is only
         *          returned if the requester has the <code>s3:GetObjectRetention</code> permission.</p>
         */
        public final Builder objectLockRetainUntilDate(Instant objectLockRetainUntilDate) {
            this.objectLockRetainUntilDate = objectLockRetainUntilDate;
            return this;
        }

        /**
         * <p>Specifies whether a legal hold is in effect for this object. This header is only
         *          returned if the requester has the <code>s3:GetObjectLegalHold</code> permission. This
         *          header is not returned if the specified version of this object has never had a legal hold
         *          applied. For more information about S3 Object Lock, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/object-lock.html">Object Lock</a>.</p>
         */
        public final Builder objectLockLegalHoldStatus(
                ObjectLockLegalHoldStatus objectLockLegalHoldStatus) {
            this.objectLockLegalHoldStatus = objectLockLegalHoldStatus;
            return this;
        }
    }
}
