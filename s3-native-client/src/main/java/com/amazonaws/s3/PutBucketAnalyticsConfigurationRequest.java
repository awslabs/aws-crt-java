package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class PutBucketAnalyticsConfigurationRequest {
  private String bucket;

  private String id;

  private AnalyticsConfiguration analyticsConfiguration;

  private String expectedBucketOwner;

  public PutBucketAnalyticsConfigurationRequest() {
    this.bucket = null;
    this.id = null;
    this.analyticsConfiguration = null;
    this.expectedBucketOwner = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(PutBucketAnalyticsConfigurationRequest.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof PutBucketAnalyticsConfigurationRequest);
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
