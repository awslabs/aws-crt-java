package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class GetBucketAnalyticsConfigurationOutput {
  private AnalyticsConfiguration analyticsConfiguration;

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
