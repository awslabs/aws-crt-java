package com.amazonaws.s3;

import java.lang.Boolean;
import java.lang.String;
import java.time.Instant;
import java.util.Map;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class CreateMultipartUploadRequest {
  private ObjectCannedACL aCL;

  private String bucket;

  private String cacheControl;

  private String contentDisposition;

  private String contentEncoding;

  private String contentLanguage;

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

  public ObjectCannedACL getACL() {
    return aCL;
  }

  public void setACL(final ObjectCannedACL aCL) {
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

  public ServerSideEncryption getServerSideEncryption() {
    return serverSideEncryption;
  }

  public void setServerSideEncryption(final ServerSideEncryption serverSideEncryption) {
    this.serverSideEncryption = serverSideEncryption;
  }

  public StorageClass getStorageClass() {
    return storageClass;
  }

  public void setStorageClass(final StorageClass storageClass) {
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

  /**
   * <p>Confirms that the requester knows that they will be charged for the request. Bucket
   *          owners need not specify this parameter in their requests. For information about downloading
   *          objects from requester pays buckets, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/ObjectsinRequesterPaysBuckets.html">Downloading Objects in
   *             Requestor Pays Buckets</a> in the <i>Amazon S3 Developer Guide</i>.</p>
   */
  public RequestPayer getRequestPayer() {
    return requestPayer;
  }

  /**
   * <p>Confirms that the requester knows that they will be charged for the request. Bucket
   *          owners need not specify this parameter in their requests. For information about downloading
   *          objects from requester pays buckets, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/ObjectsinRequesterPaysBuckets.html">Downloading Objects in
   *             Requestor Pays Buckets</a> in the <i>Amazon S3 Developer Guide</i>.</p>
   */
  public void setRequestPayer(final RequestPayer requestPayer) {
    this.requestPayer = requestPayer;
  }

  public String getTagging() {
    return tagging;
  }

  public void setTagging(final String tagging) {
    this.tagging = tagging;
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

  public String getExpectedBucketOwner() {
    return expectedBucketOwner;
  }

  public void setExpectedBucketOwner(final String expectedBucketOwner) {
    this.expectedBucketOwner = expectedBucketOwner;
  }
}
