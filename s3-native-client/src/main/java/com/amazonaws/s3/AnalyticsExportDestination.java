package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class AnalyticsExportDestination {
  private AnalyticsS3BucketDestination s3BucketDestination;

  /**
   * <p>Contains information about where to publish the analytics results.</p>
   */
  public AnalyticsS3BucketDestination getS3BucketDestination() {
    return s3BucketDestination;
  }

  /**
   * <p>Contains information about where to publish the analytics results.</p>
   */
  public void setS3BucketDestination(final AnalyticsS3BucketDestination s3BucketDestination) {
    this.s3BucketDestination = s3BucketDestination;
  }
}
