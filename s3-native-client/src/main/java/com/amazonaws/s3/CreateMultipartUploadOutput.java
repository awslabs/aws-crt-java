package com.amazonaws.s3;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.time.Instant;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class CreateMultipartUploadOutput {
  private Instant abortDate;

  private String abortRuleId;

  private String bucket;

  private String key;

  private String uploadId;

  private ServerSideEncryption serverSideEncryption;

  private String sSECustomerAlgorithm;

  private String sSECustomerKeyMD5;

  private String sSEKMSKeyId;

  private String sSEKMSEncryptionContext;

  private Boolean bucketKeyEnabled;

  private RequestCharged requestCharged;

  public CreateMultipartUploadOutput() {
    this.abortDate = null;
    this.abortRuleId = null;
    this.bucket = null;
    this.key = null;
    this.uploadId = null;
    this.serverSideEncryption = null;
    this.sSECustomerAlgorithm = null;
    this.sSECustomerKeyMD5 = null;
    this.sSEKMSKeyId = null;
    this.sSEKMSEncryptionContext = null;
    this.bucketKeyEnabled = null;
    this.requestCharged = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(CreateMultipartUploadOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof CreateMultipartUploadOutput);
  }

  public Instant getAbortDate() {
    return abortDate;
  }

  public void setAbortDate(final Instant abortDate) {
    this.abortDate = abortDate;
  }

  public String getAbortRuleId() {
    return abortRuleId;
  }

  public void setAbortRuleId(final String abortRuleId) {
    this.abortRuleId = abortRuleId;
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

  public String getUploadId() {
    return uploadId;
  }

  public void setUploadId(final String uploadId) {
    this.uploadId = uploadId;
  }

  public String getServerSideEncryption() {
    return serverSideEncryption;
  }

  public void setServerSideEncryption(final String serverSideEncryption) {
    this.serverSideEncryption = serverSideEncryption;
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
