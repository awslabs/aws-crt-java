package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class GetBucketMetricsConfigurationOutput {
  private MetricsConfiguration metricsConfiguration;

  public GetBucketMetricsConfigurationOutput() {
    this.metricsConfiguration = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(GetBucketMetricsConfigurationOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof GetBucketMetricsConfigurationOutput);
  }

  /**
   * <p>Specifies a metrics configuration for the CloudWatch request metrics (specified by the
   *          metrics configuration ID) from an Amazon S3 bucket. If you're updating an existing metrics
   *          configuration, note that this is a full replacement of the existing metrics configuration.
   *          If you don't include the elements you want to keep, they are erased. For more information,
   *          see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/RESTBucketPUTMetricConfiguration.html"> PUT Bucket
   *             metrics</a> in the <i>Amazon Simple Storage Service API Reference</i>.</p>
   */
  public MetricsConfiguration getMetricsConfiguration() {
    return metricsConfiguration;
  }

  /**
   * <p>Specifies a metrics configuration for the CloudWatch request metrics (specified by the
   *          metrics configuration ID) from an Amazon S3 bucket. If you're updating an existing metrics
   *          configuration, note that this is a full replacement of the existing metrics configuration.
   *          If you don't include the elements you want to keep, they are erased. For more information,
   *          see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/RESTBucketPUTMetricConfiguration.html"> PUT Bucket
   *             metrics</a> in the <i>Amazon Simple Storage Service API Reference</i>.</p>
   */
  public void setMetricsConfiguration(final MetricsConfiguration metricsConfiguration) {
    this.metricsConfiguration = metricsConfiguration;
  }
}
