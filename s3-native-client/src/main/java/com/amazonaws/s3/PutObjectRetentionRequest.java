package com.amazonaws.s3;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class PutObjectRetentionRequest {
  private String bucket;

  private String key;

  private ObjectLockRetention retention;

  private RequestPayer requestPayer;

  private String versionId;

  private Boolean bypassGovernanceRetention;

  private String contentMD5;

  private String expectedBucketOwner;

  public PutObjectRetentionRequest() {
    this.bucket = null;
    this.key = null;
    this.retention = null;
    this.requestPayer = null;
    this.versionId = null;
    this.bypassGovernanceRetention = null;
    this.contentMD5 = null;
    this.expectedBucketOwner = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(PutObjectRetentionRequest.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof PutObjectRetentionRequest);
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(final String bucket) {
    this.bucket = bucket;
  }

  public String getKey() {
    return key;
  }

  public void setKey(final String key) {
    this.key = key;
  }

  /**
   * <p>A Retention configuration for an object.</p>
   */
  public ObjectLockRetention getRetention() {
    return retention;
  }

  /**
   * <p>A Retention configuration for an object.</p>
   */
  public void setRetention(final ObjectLockRetention retention) {
    this.retention = retention;
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

  public String getVersionId() {
    return versionId;
  }

  public void setVersionId(final String versionId) {
    this.versionId = versionId;
  }

  public Boolean isBypassGovernanceRetention() {
    return bypassGovernanceRetention;
  }

  public void setBypassGovernanceRetention(final Boolean bypassGovernanceRetention) {
    this.bypassGovernanceRetention = bypassGovernanceRetention;
  }

  public String getContentMD5() {
    return contentMD5;
  }

  public void setContentMD5(final String contentMD5) {
    this.contentMD5 = contentMD5;
  }

  public String getExpectedBucketOwner() {
    return expectedBucketOwner;
  }

  public void setExpectedBucketOwner(final String expectedBucketOwner) {
    this.expectedBucketOwner = expectedBucketOwner;
  }
}
