package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class PutBucketAccelerateConfigurationRequest {
  private String bucket;

  private AccelerateConfiguration accelerateConfiguration;

  private String expectedBucketOwner;

  public PutBucketAccelerateConfigurationRequest() {
    this.bucket = null;
    this.accelerateConfiguration = null;
    this.expectedBucketOwner = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(PutBucketAccelerateConfigurationRequest.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof PutBucketAccelerateConfigurationRequest);
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(final String bucket) {
    this.bucket = bucket;
  }

  /**
   * <p>Configures the transfer acceleration state for an Amazon S3 bucket. For more information, see
   *             <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/transfer-acceleration.html">Amazon S3
   *             Transfer Acceleration</a> in the <i>Amazon Simple Storage Service Developer
   *          Guide</i>.</p>
   */
  public AccelerateConfiguration getAccelerateConfiguration() {
    return accelerateConfiguration;
  }

  /**
   * <p>Configures the transfer acceleration state for an Amazon S3 bucket. For more information, see
   *             <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/transfer-acceleration.html">Amazon S3
   *             Transfer Acceleration</a> in the <i>Amazon Simple Storage Service Developer
   *          Guide</i>.</p>
   */
  public void setAccelerateConfiguration(final AccelerateConfiguration accelerateConfiguration) {
    this.accelerateConfiguration = accelerateConfiguration;
  }

  public String getExpectedBucketOwner() {
    return expectedBucketOwner;
  }

  public void setExpectedBucketOwner(final String expectedBucketOwner) {
    this.expectedBucketOwner = expectedBucketOwner;
  }
}
