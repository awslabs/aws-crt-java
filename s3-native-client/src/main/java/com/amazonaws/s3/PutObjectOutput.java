package com.amazonaws.s3;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class PutObjectOutput {
  private String expiration;

  private String eTag;

  private ServerSideEncryption serverSideEncryption;

  private String versionId;

  private String sSECustomerAlgorithm;

  private String sSECustomerKeyMD5;

  private String sSEKMSKeyId;

  private String sSEKMSEncryptionContext;

  private Boolean bucketKeyEnabled;

  private RequestCharged requestCharged;

  public PutObjectOutput() {
    this.expiration = null;
    this.eTag = null;
    this.serverSideEncryption = null;
    this.versionId = null;
    this.sSECustomerAlgorithm = null;
    this.sSECustomerKeyMD5 = null;
    this.sSEKMSKeyId = null;
    this.sSEKMSEncryptionContext = null;
    this.bucketKeyEnabled = null;
    this.requestCharged = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(PutObjectOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof PutObjectOutput);
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
