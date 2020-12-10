package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class PutBucketLifecycleConfigurationRequest {
  private String bucket;

  private BucketLifecycleConfiguration lifecycleConfiguration;

  private String expectedBucketOwner;

  public String getBucket() {
    return bucket;
  }

  public void setBucket(final String bucket) {
    this.bucket = bucket;
  }

  /**
   * <p>Specifies the lifecycle configuration for objects in an Amazon S3 bucket. For more
   *          information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/object-lifecycle-mgmt.html">Object Lifecycle Management</a>
   *          in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
   */
  public BucketLifecycleConfiguration getLifecycleConfiguration() {
    return lifecycleConfiguration;
  }

  /**
   * <p>Specifies the lifecycle configuration for objects in an Amazon S3 bucket. For more
   *          information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/object-lifecycle-mgmt.html">Object Lifecycle Management</a>
   *          in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
   */
  public void setLifecycleConfiguration(final BucketLifecycleConfiguration lifecycleConfiguration) {
    this.lifecycleConfiguration = lifecycleConfiguration;
  }

  public String getExpectedBucketOwner() {
    return expectedBucketOwner;
  }

  public void setExpectedBucketOwner(final String expectedBucketOwner) {
    this.expectedBucketOwner = expectedBucketOwner;
  }
}
