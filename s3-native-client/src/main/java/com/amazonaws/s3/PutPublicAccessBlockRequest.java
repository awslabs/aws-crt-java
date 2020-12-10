package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class PutPublicAccessBlockRequest {
  private String bucket;

  private String contentMD5;

  private PublicAccessBlockConfiguration publicAccessBlockConfiguration;

  private String expectedBucketOwner;

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
   * <p>The PublicAccessBlock configuration that you want to apply to this Amazon S3 bucket. You can
   *          enable the configuration options in any combination. For more information about when Amazon S3
   *          considers a bucket or object public, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/access-control-block-public-access.html#access-control-block-public-access-policy-status">The Meaning of "Public"</a> in the <i>Amazon Simple Storage Service Developer Guide</i>. </p>
   */
  public PublicAccessBlockConfiguration getPublicAccessBlockConfiguration() {
    return publicAccessBlockConfiguration;
  }

  /**
   * <p>The PublicAccessBlock configuration that you want to apply to this Amazon S3 bucket. You can
   *          enable the configuration options in any combination. For more information about when Amazon S3
   *          considers a bucket or object public, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/access-control-block-public-access.html#access-control-block-public-access-policy-status">The Meaning of "Public"</a> in the <i>Amazon Simple Storage Service Developer Guide</i>. </p>
   */
  public void setPublicAccessBlockConfiguration(
      final PublicAccessBlockConfiguration publicAccessBlockConfiguration) {
    this.publicAccessBlockConfiguration = publicAccessBlockConfiguration;
  }

  public String getExpectedBucketOwner() {
    return expectedBucketOwner;
  }

  public void setExpectedBucketOwner(final String expectedBucketOwner) {
    this.expectedBucketOwner = expectedBucketOwner;
  }
}
