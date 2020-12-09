package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class PutBucketLoggingRequest {
  private String bucket;

  private BucketLoggingStatus bucketLoggingStatus;

  private String contentMD5;

  private String expectedBucketOwner;

  public PutBucketLoggingRequest() {
    this.bucket = null;
    this.bucketLoggingStatus = null;
    this.contentMD5 = null;
    this.expectedBucketOwner = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(PutBucketLoggingRequest.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof PutBucketLoggingRequest);
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(final String bucket) {
    this.bucket = bucket;
  }

  /**
   * <p>Container for logging status information.</p>
   */
  public BucketLoggingStatus getBucketLoggingStatus() {
    return bucketLoggingStatus;
  }

  /**
   * <p>Container for logging status information.</p>
   */
  public void setBucketLoggingStatus(final BucketLoggingStatus bucketLoggingStatus) {
    this.bucketLoggingStatus = bucketLoggingStatus;
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
