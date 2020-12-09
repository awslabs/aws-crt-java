package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class PutBucketRequestPaymentRequest {
  private String bucket;

  private String contentMD5;

  private RequestPaymentConfiguration requestPaymentConfiguration;

  private String expectedBucketOwner;

  public PutBucketRequestPaymentRequest() {
    this.bucket = null;
    this.contentMD5 = null;
    this.requestPaymentConfiguration = null;
    this.expectedBucketOwner = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(PutBucketRequestPaymentRequest.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof PutBucketRequestPaymentRequest);
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(final String bucket) {
    this.bucket = bucket;
  }

  public String getContentMD5() {
    return contentMD5;
  }

  public void setContentMD5(final String contentMD5) {
    this.contentMD5 = contentMD5;
  }

  /**
   * <p>Container for Payer.</p>
   */
  public RequestPaymentConfiguration getRequestPaymentConfiguration() {
    return requestPaymentConfiguration;
  }

  /**
   * <p>Container for Payer.</p>
   */
  public void setRequestPaymentConfiguration(
      final RequestPaymentConfiguration requestPaymentConfiguration) {
    this.requestPaymentConfiguration = requestPaymentConfiguration;
  }

  public String getExpectedBucketOwner() {
    return expectedBucketOwner;
  }

  public void setExpectedBucketOwner(final String expectedBucketOwner) {
    this.expectedBucketOwner = expectedBucketOwner;
  }
}
