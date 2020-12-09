package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class PutBucketIntelligentTieringConfigurationRequest {
  private String bucket;

  private String id;

  private IntelligentTieringConfiguration intelligentTieringConfiguration;

  public PutBucketIntelligentTieringConfigurationRequest() {
    this.bucket = null;
    this.id = null;
    this.intelligentTieringConfiguration = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(PutBucketIntelligentTieringConfigurationRequest.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof PutBucketIntelligentTieringConfigurationRequest);
  }

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
