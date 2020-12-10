package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class PutBucketMetricsConfigurationRequest {
  private String bucket;

  private String id;

  private MetricsConfiguration metricsConfiguration;

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

  public String getExpectedBucketOwner() {
    return expectedBucketOwner;
  }

  public void setExpectedBucketOwner(final String expectedBucketOwner) {
    this.expectedBucketOwner = expectedBucketOwner;
  }
}
