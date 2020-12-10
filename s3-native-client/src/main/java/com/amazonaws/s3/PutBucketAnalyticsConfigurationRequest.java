package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class PutBucketAnalyticsConfigurationRequest {
  private String bucket;

  private String id;

  private AnalyticsConfiguration analyticsConfiguration;

  private String expectedBucketOwner;

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
   * <p> Specifies the configuration and any analyses for the analytics filter of an Amazon S3
   *          bucket.</p>
   */
  public AnalyticsConfiguration getAnalyticsConfiguration() {
    return analyticsConfiguration;
  }

  /**
   * <p> Specifies the configuration and any analyses for the analytics filter of an Amazon S3
   *          bucket.</p>
   */
  public void setAnalyticsConfiguration(final AnalyticsConfiguration analyticsConfiguration) {
    this.analyticsConfiguration = analyticsConfiguration;
  }

  public String getExpectedBucketOwner() {
    return expectedBucketOwner;
  }

  public void setExpectedBucketOwner(final String expectedBucketOwner) {
    this.expectedBucketOwner = expectedBucketOwner;
  }
}
