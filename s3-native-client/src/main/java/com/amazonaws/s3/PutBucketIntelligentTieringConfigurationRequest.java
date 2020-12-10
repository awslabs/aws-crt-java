package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class PutBucketIntelligentTieringConfigurationRequest {
  private String bucket;

  private String id;

  private IntelligentTieringConfiguration intelligentTieringConfiguration;

  public String getBucket() {
    return bucket;
  }

  public void setBucket(final String bucket) {
    this.bucket = bucket;
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  /**
   * <p>Specifies the S3 Intelligent-Tiering configuration for an Amazon S3 bucket.</p>
   *          <p>For information about the S3 Intelligent-Tiering storage class, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/storage-class-intro.html#sc-dynamic-data-access">Storage class for
   *             automatically optimizing frequently and infrequently accessed objects</a>.</p>
   */
  public IntelligentTieringConfiguration getIntelligentTieringConfiguration() {
    return intelligentTieringConfiguration;
  }

  /**
   * <p>Specifies the S3 Intelligent-Tiering configuration for an Amazon S3 bucket.</p>
   *          <p>For information about the S3 Intelligent-Tiering storage class, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/storage-class-intro.html#sc-dynamic-data-access">Storage class for
   *             automatically optimizing frequently and infrequently accessed objects</a>.</p>
   */
  public void setIntelligentTieringConfiguration(
      final IntelligentTieringConfiguration intelligentTieringConfiguration) {
    this.intelligentTieringConfiguration = intelligentTieringConfiguration;
  }
}
