package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class PutBucketAccelerateConfigurationRequest {
  private String bucket;

  private AccelerateConfiguration accelerateConfiguration;

  private String expectedBucketOwner;

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
