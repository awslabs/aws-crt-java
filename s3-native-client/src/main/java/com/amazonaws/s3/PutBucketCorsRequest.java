package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class PutBucketCorsRequest {
  private String bucket;

  private CORSConfiguration cORSConfiguration;

  private String contentMD5;

  private String expectedBucketOwner;

  public PutBucketCorsRequest() {
    this.bucket = null;
    this.cORSConfiguration = null;
    this.contentMD5 = null;
    this.expectedBucketOwner = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(PutBucketCorsRequest.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof PutBucketCorsRequest);
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(final String bucket) {
    this.bucket = bucket;
  }

  /**
   * <p>Describes the cross-origin access configuration for objects in an Amazon S3 bucket. For more
   *          information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/cors.html">Enabling
   *             Cross-Origin Resource Sharing</a> in the <i>Amazon Simple Storage Service Developer
   *             Guide</i>.</p>
   */
  public CORSConfiguration getCORSConfiguration() {
    return cORSConfiguration;
  }

  /**
   * <p>Describes the cross-origin access configuration for objects in an Amazon S3 bucket. For more
   *          information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/cors.html">Enabling
   *             Cross-Origin Resource Sharing</a> in the <i>Amazon Simple Storage Service Developer
   *             Guide</i>.</p>
   */
  public void setCORSConfiguration(final CORSConfiguration cORSConfiguration) {
    this.cORSConfiguration = cORSConfiguration;
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
