package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class PutBucketLoggingRequest {
  private String bucket;

  private BucketLoggingStatus bucketLoggingStatus;

  private String contentMD5;

  private String expectedBucketOwner;

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
