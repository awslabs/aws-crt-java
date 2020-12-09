package com.amazonaws.s3;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class UploadPartOutput {
  private ServerSideEncryption serverSideEncryption;

  private String eTag;

  private String sSECustomerAlgorithm;

  private String sSECustomerKeyMD5;

  private String sSEKMSKeyId;

  private Boolean bucketKeyEnabled;

  private RequestCharged requestCharged;

  public UploadPartOutput() {
    this.serverSideEncryption = null;
    this.eTag = null;
    this.sSECustomerAlgorithm = null;
    this.sSECustomerKeyMD5 = null;
    this.sSEKMSKeyId = null;
    this.bucketKeyEnabled = null;
    this.requestCharged = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(UploadPartOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof UploadPartOutput);
  }

  public String getServerSideEncryption() {
    return serverSideEncryption;
  }

  public void setServerSideEncryption(final String serverSideEncryption) {
    this.serverSideEncryption = serverSideEncryption;
  }

  public String getETag() {
    return eTag;
  }

  public void setETag(final String eTag) {
    this.eTag = eTag;
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
