package com.amazonaws.s3;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class CopyObjectRequest {
  private ObjectCannedACL aCL;

  private String bucket;

  private String cacheControl;

  private String contentDisposition;

  private String contentEncoding;

  private String contentLanguage;

  private String contentType;

  private String copySource;

  private String copySourceIfMatch;

  private Instant copySourceIfModifiedSince;

  private String copySourceIfNoneMatch;

  private Instant copySourceIfUnmodifiedSince;

  private Instant expires;

  private String grantFullControl;

  private String grantRead;

  private String grantReadACP;

  private String grantWriteACP;

  private String key;

  private Map<String, String> metadata;

  private MetadataDirective metadataDirective;

  private TaggingDirective taggingDirective;

  private ServerSideEncryption serverSideEncryption;

  private StorageClass storageClass;

  private String websiteRedirectLocation;

  private String sSECustomerAlgorithm;

  private String sSECustomerKey;

  private String sSECustomerKeyMD5;

  private String sSEKMSKeyId;

  private String sSEKMSEncryptionContext;

  private Boolean bucketKeyEnabled;

  private String copySourceSSECustomerAlgorithm;

  private String copySourceSSECustomerKey;

  private String copySourceSSECustomerKeyMD5;

  private RequestPayer requestPayer;

  private String tagging;

  private ObjectLockMode objectLockMode;

  private Instant objectLockRetainUntilDate;

  private ObjectLockLegalHoldStatus objectLockLegalHoldStatus;

  private String expectedBucketOwner;

  private String expectedSourceBucketOwner;

  public CopyObjectRequest() {
    this.aCL = null;
    this.bucket = null;
    this.cacheControl = null;
    this.contentDisposition = null;
    this.contentEncoding = null;
    this.contentLanguage = null;
    this.contentType = null;
    this.copySource = null;
    this.copySourceIfMatch = null;
    this.copySourceIfModifiedSince = null;
    this.copySourceIfNoneMatch = null;
    this.copySourceIfUnmodifiedSince = null;
    this.expires = null;
    this.grantFullControl = null;
    this.grantRead = null;
    this.grantReadACP = null;
    this.grantWriteACP = null;
    this.key = null;
    this.metadata = null;
    this.metadataDirective = null;
    this.taggingDirective = null;
    this.serverSideEncryption = null;
    this.storageClass = null;
    this.websiteRedirectLocation = null;
    this.sSECustomerAlgorithm = null;
    this.sSECustomerKey = null;
    this.sSECustomerKeyMD5 = null;
    this.sSEKMSKeyId = null;
    this.sSEKMSEncryptionContext = null;
    this.bucketKeyEnabled = null;
    this.copySourceSSECustomerAlgorithm = null;
    this.copySourceSSECustomerKey = null;
    this.copySourceSSECustomerKeyMD5 = null;
    this.requestPayer = null;
    this.tagging = null;
    this.objectLockMode = null;
    this.objectLockRetainUntilDate = null;
    this.objectLockLegalHoldStatus = null;
    this.expectedBucketOwner = null;
    this.expectedSourceBucketOwner = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(CopyObjectRequest.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof CopyObjectRequest);
  }

  public String getACL() {
    return aCL;
  }

  public void setACL(final String aCL) {
    this.aCL = aCL;
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(final String bucket) {
    this.bucket = bucket;
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

  public String getCopySource() {
    return copySource;
  }

  public void setCopySource(final String copySource) {
    this.copySource = copySource;
  }

  public String getCopySourceIfMatch() {
    return copySourceIfMatch;
  }

  public void setCopySourceIfMatch(final String copySourceIfMatch) {
    this.copySourceIfMatch = copySourceIfMatch;
  }

  public Instant getCopySourceIfModifiedSince() {
    return copySourceIfModifiedSince;
  }

  public void setCopySourceIfModifiedSince(final Instant copySourceIfModifiedSince) {
    this.copySourceIfModifiedSince = copySourceIfModifiedSince;
  }

  public String getCopySourceIfNoneMatch() {
    return copySourceIfNoneMatch;
  }

  public void setCopySourceIfNoneMatch(final String copySourceIfNoneMatch) {
    this.copySourceIfNoneMatch = copySourceIfNoneMatch;
  }

  public Instant getCopySourceIfUnmodifiedSince() {
    return copySourceIfUnmodifiedSince;
  }

  public void setCopySourceIfUnmodifiedSince(final Instant copySourceIfUnmodifiedSince) {
    this.copySourceIfUnmodifiedSince = copySourceIfUnmodifiedSince;
  }

  public Instant getExpires() {
    return expires;
  }

  public void setExpires(final Instant expires) {
    this.expires = expires;
  }

  public String getGrantFullControl() {
    return grantFullControl;
  }

  public void setGrantFullControl(final String grantFullControl) {
    this.grantFullControl = grantFullControl;
  }

  public String getGrantRead() {
    return grantRead;
  }

  public void setGrantRead(final String grantRead) {
    this.grantRead = grantRead;
  }

  public String getGrantReadACP() {
    return grantReadACP;
  }

  public void setGrantReadACP(final String grantReadACP) {
    this.grantReadACP = grantReadACP;
  }

  public String getGrantWriteACP() {
    return grantWriteACP;
  }

  public void setGrantWriteACP(final String grantWriteACP) {
    this.grantWriteACP = grantWriteACP;
  }

  public String getKey() {
    return key;
  }

  public void setKey(final String key) {
    this.key = key;
  }

  public Map<String, String> getMetadata() {
    return metadata;
  }

  public void setMetadata(final Map<String, String> metadata) {
    this.metadata = metadata;
  }

  public String getMetadataDirective() {
    return metadataDirective;
  }

  public void setMetadataDirective(final String metadataDirective) {
    this.metadataDirective = metadataDirective;
  }

  public String getTaggingDirective() {
    return taggingDirective;
  }

  public void setTaggingDirective(final String taggingDirective) {
    this.taggingDirective = taggingDirective;
  }

  public String getServerSideEncryption() {
    return serverSideEncryption;
  }

  public void setServerSideEncryption(final String serverSideEncryption) {
    this.serverSideEncryption = serverSideEncryption;
  }

  public String getStorageClass() {
    return storageClass;
  }

  public void setStorageClass(final String storageClass) {
    this.storageClass = storageClass;
  }

  public String getWebsiteRedirectLocation() {
    return websiteRedirectLocation;
  }

  public void setWebsiteRedirectLocation(final String websiteRedirectLocation) {
    this.websiteRedirectLocation = websiteRedirectLocation;
  }

  public String getSSECustomerAlgorithm() {
    return sSECustomerAlgorithm;
  }

  public void setSSECustomerAlgorithm(final String sSECustomerAlgorithm) {
    this.sSECustomerAlgorithm = sSECustomerAlgorithm;
  }

  public String getSSECustomerKey() {
    return sSECustomerKey;
  }

  public void setSSECustomerKey(final String sSECustomerKey) {
    this.sSECustomerKey = sSECustomerKey;
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

  public String getSSEKMSEncryptionContext() {
    return sSEKMSEncryptionContext;
  }

  public void setSSEKMSEncryptionContext(final String sSEKMSEncryptionContext) {
    this.sSEKMSEncryptionContext = sSEKMSEncryptionContext;
  }

  public Boolean isBucketKeyEnabled() {
    return bucketKeyEnabled;
  }

  public void setBucketKeyEnabled(final Boolean bucketKeyEnabled) {
    this.bucketKeyEnabled = bucketKeyEnabled;
  }

  public String getCopySourceSSECustomerAlgorithm() {
    return copySourceSSECustomerAlgorithm;
  }

  public void setCopySourceSSECustomerAlgorithm(final String copySourceSSECustomerAlgorithm) {
    this.copySourceSSECustomerAlgorithm = copySourceSSECustomerAlgorithm;
  }

  public String getCopySourceSSECustomerKey() {
    return copySourceSSECustomerKey;
  }

  public void setCopySourceSSECustomerKey(final String copySourceSSECustomerKey) {
    this.copySourceSSECustomerKey = copySourceSSECustomerKey;
  }

  public String getCopySourceSSECustomerKeyMD5() {
    return copySourceSSECustomerKeyMD5;
  }

  public void setCopySourceSSECustomerKeyMD5(final String copySourceSSECustomerKeyMD5) {
    this.copySourceSSECustomerKeyMD5 = copySourceSSECustomerKeyMD5;
  }

  /**
   * <p>Confirms that the requester knows that they will be charged for the request. Bucket
   *          owners need not specify this parameter in their requests. For information about downloading
   *          objects from requester pays buckets, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/ObjectsinRequesterPaysBuckets.html">Downloading Objects in
   *             Requestor Pays Buckets</a> in the <i>Amazon S3 Developer Guide</i>.</p>
   */
  public String getRequestPayer() {
    return requestPayer;
  }

  /**
   * <p>Confirms that the requester knows that they will be charged for the request. Bucket
   *          owners need not specify this parameter in their requests. For information about downloading
   *          objects from requester pays buckets, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/ObjectsinRequesterPaysBuckets.html">Downloading Objects in
   *             Requestor Pays Buckets</a> in the <i>Amazon S3 Developer Guide</i>.</p>
   */
  public void setRequestPayer(final String requestPayer) {
    this.requestPayer = requestPayer;
  }

  public String getTagging() {
    return tagging;
  }

  public void setTagging(final String tagging) {
    this.tagging = tagging;
  }

  public String getObjectLockMode() {
    return objectLockMode;
  }

  public void setObjectLockMode(final String objectLockMode) {
    this.objectLockMode = objectLockMode;
  }

  public Instant getObjectLockRetainUntilDate() {
    return objectLockRetainUntilDate;
  }

  public void setObjectLockRetainUntilDate(final Instant objectLockRetainUntilDate) {
    this.objectLockRetainUntilDate = objectLockRetainUntilDate;
  }

  public String getObjectLockLegalHoldStatus() {
    return objectLockLegalHoldStatus;
  }

  public void setObjectLockLegalHoldStatus(final String objectLockLegalHoldStatus) {
    this.objectLockLegalHoldStatus = objectLockLegalHoldStatus;
  }

  public String getExpectedBucketOwner() {
    return expectedBucketOwner;
  }

  public void setExpectedBucketOwner(final String expectedBucketOwner) {
    this.expectedBucketOwner = expectedBucketOwner;
  }

  public String getExpectedSourceBucketOwner() {
    return expectedSourceBucketOwner;
  }

  public void setExpectedSourceBucketOwner(final String expectedSourceBucketOwner) {
    this.expectedSourceBucketOwner = expectedSourceBucketOwner;
  }
}
