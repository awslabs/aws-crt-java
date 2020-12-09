package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class GetBucketAnalyticsConfigurationOutput {
  private AnalyticsConfiguration analyticsConfiguration;

  public GetBucketAnalyticsConfigurationOutput() {
    this.analyticsConfiguration = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(GetBucketAnalyticsConfigurationOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof GetBucketAnalyticsConfigurationOutput);
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
}
