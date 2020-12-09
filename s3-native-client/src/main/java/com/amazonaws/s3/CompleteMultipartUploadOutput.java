package com.amazonaws.s3;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class CompleteMultipartUploadOutput {
  private String location;

  private String bucket;

  private String key;

  private String expiration;

  private String eTag;

  private ServerSideEncryption serverSideEncryption;

  private String versionId;

  private String sSEKMSKeyId;

  private Boolean bucketKeyEnabled;

  private RequestCharged requestCharged;

  public CompleteMultipartUploadOutput() {
    this.location = null;
    this.bucket = null;
    this.key = null;
    this.expiration = null;
    this.eTag = null;
    this.serverSideEncryption = null;
    this.versionId = null;
    this.sSEKMSKeyId = null;
    this.bucketKeyEnabled = null;
    this.requestCharged = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(CompleteMultipartUploadOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof CompleteMultipartUploadOutput);
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(final String location) {
    this.location = location;
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

  public String getExpiration() {
    return expiration;
  }

  public void setExpiration(final String expiration) {
    this.expiration = expiration;
  }

  public String getETag() {
    return eTag;
  }

  public void setETag(final String eTag) {
    this.eTag = eTag;
  }

  public String getServerSideEncryption() {
    return serverSideEncryption;
  }

  public void setServerSideEncryption(final String serverSideEncryption) {
    this.serverSideEncryption = serverSideEncryption;
  }

  public String getVersionId() {
    return versionId;
  }

  public void setVersionId(final String versionId) {
    this.versionId = versionId;
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

  /**
   * <p>If present, indicates that the requester was successfully charged for the
   *          request.</p>
   */
  public String getRequestCharged() {
    return requestCharged;
  }

  /**
   * <p>If present, indicates that the requester was successfully charged for the
   *          request.</p>
   */
  public void setRequestCharged(final String requestCharged) {
    this.requestCharged = requestCharged;
  }
}
