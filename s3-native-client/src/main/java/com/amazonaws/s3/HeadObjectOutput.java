package com.amazonaws.s3;

import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Long;
import java.lang.String;
import java.time.Instant;
import java.util.Map;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
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

  public Boolean isDeleteMarker() {
    return deleteMarker;
  }

  public void setDeleteMarker(final Boolean deleteMarker) {
    this.deleteMarker = deleteMarker;
  }

  public String getAcceptRanges() {
    return acceptRanges;
  }

  public void setAcceptRanges(final String acceptRanges) {
    this.acceptRanges = acceptRanges;
  }

  public String getExpiration() {
    return expiration;
  }

  public void setExpiration(final String expiration) {
    this.expiration = expiration;
  }

  public String getRestore() {
    return restore;
  }

  public void setRestore(final String restore) {
    this.restore = restore;
  }

  public ArchiveStatus getArchiveStatus() {
    return archiveStatus;
  }

  public void setArchiveStatus(final ArchiveStatus archiveStatus) {
    this.archiveStatus = archiveStatus;
  }

  public Instant getLastModified() {
    return lastModified;
  }

  public void setLastModified(final Instant lastModified) {
    this.lastModified = lastModified;
  }

  public Long getContentLength() {
    return contentLength;
  }

  public void setContentLength(final Long contentLength) {
    this.contentLength = contentLength;
  }

  public String getETag() {
    return eTag;
  }

  public void setETag(final String eTag) {
    this.eTag = eTag;
  }

  public Integer getMissingMeta() {
    return missingMeta;
  }

  public void setMissingMeta(final Integer missingMeta) {
    this.missingMeta = missingMeta;
  }

  public String getVersionId() {
    return versionId;
  }

  public void setVersionId(final String versionId) {
    this.versionId = versionId;
  }

  public String getCacheControl() {
    return cacheControl;
  }

  public void setCacheControl(final String cacheControl) {
    this.cacheControl = cacheControl;
  }

  public String getContentDisposition() {
    return contentDisposition;
  }

  public void setContentDisposition(final String contentDisposition) {
    this.contentDisposition = contentDisposition;
  }

  public String getContentEncoding() {
    return contentEncoding;
  }

  public void setContentEncoding(final String contentEncoding) {
    this.contentEncoding = contentEncoding;
  }

  public String getContentLanguage() {
    return contentLanguage;
  }

  public void setContentLanguage(final String contentLanguage) {
    this.contentLanguage = contentLanguage;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(final String contentType) {
    this.contentType = contentType;
  }

  public Instant getExpires() {
    return expires;
  }

  public void setExpires(final Instant expires) {
    this.expires = expires;
  }

  public String getWebsiteRedirectLocation() {
    return websiteRedirectLocation;
  }

  public void setWebsiteRedirectLocation(final String websiteRedirectLocation) {
    this.websiteRedirectLocation = websiteRedirectLocation;
  }

  public ServerSideEncryption getServerSideEncryption() {
    return serverSideEncryption;
  }

  public void setServerSideEncryption(final ServerSideEncryption serverSideEncryption) {
    this.serverSideEncryption = serverSideEncryption;
  }

  public Map<String, String> getMetadata() {
    return metadata;
  }

  public void setMetadata(final Map<String, String> metadata) {
    this.metadata = metadata;
  }

  public String getSSECustomerAlgorithm() {
    return sSECustomerAlgorithm;
  }

  public void setSSECustomerAlgorithm(final String sSECustomerAlgorithm) {
    this.sSECustomerAlgorithm = sSECustomerAlgorithm;
  }

  public String getSSECustomerKeyMD5() {
    return sSECustomerKeyMD5;
  }

  public void setSSECustomerKeyMD5(final String sSECustomerKeyMD5) {
    this.sSECustomerKeyMD5 = sSECustomerKeyMD5;
  }

  public String getSSEKMSKeyId() {
    return sSEKMSKeyId;
  }

  public void setSSEKMSKeyId(final String sSEKMSKeyId) {
    this.sSEKMSKeyId = sSEKMSKeyId;
  }

  public Boolean isBucketKeyEnabled() {
    return bucketKeyEnabled;
  }

  public void setBucketKeyEnabled(final Boolean bucketKeyEnabled) {
    this.bucketKeyEnabled = bucketKeyEnabled;
  }

  public StorageClass getStorageClass() {
    return storageClass;
  }

  public void setStorageClass(final StorageClass storageClass) {
    this.storageClass = storageClass;
  }

  /**
   * <p>If present, indicates that the requester was successfully charged for the
   *          request.</p>
   */
  public RequestCharged getRequestCharged() {
    return requestCharged;
  }

  /**
   * <p>If present, indicates that the requester was successfully charged for the
   *          request.</p>
   */
  public void setRequestCharged(final RequestCharged requestCharged) {
    this.requestCharged = requestCharged;
  }

  public ReplicationStatus getReplicationStatus() {
    return replicationStatus;
  }

  public void setReplicationStatus(final ReplicationStatus replicationStatus) {
    this.replicationStatus = replicationStatus;
  }

  public Integer getPartsCount() {
    return partsCount;
  }

  public void setPartsCount(final Integer partsCount) {
    this.partsCount = partsCount;
  }

  public ObjectLockMode getObjectLockMode() {
    return objectLockMode;
  }

  public void setObjectLockMode(final ObjectLockMode objectLockMode) {
    this.objectLockMode = objectLockMode;
  }

  public Instant getObjectLockRetainUntilDate() {
    return objectLockRetainUntilDate;
  }

  public void setObjectLockRetainUntilDate(final Instant objectLockRetainUntilDate) {
    this.objectLockRetainUntilDate = objectLockRetainUntilDate;
  }

  public ObjectLockLegalHoldStatus getObjectLockLegalHoldStatus() {
    return objectLockLegalHoldStatus;
  }

  public void setObjectLockLegalHoldStatus(
      final ObjectLockLegalHoldStatus objectLockLegalHoldStatus) {
    this.objectLockLegalHoldStatus = objectLockLegalHoldStatus;
  }
}
