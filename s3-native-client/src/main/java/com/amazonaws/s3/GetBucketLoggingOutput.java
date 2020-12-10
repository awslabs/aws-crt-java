package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class GetBucketLoggingOutput {
  private LoggingEnabled loggingEnabled;

  /**
   * <p>Describes where logs are stored and the prefix that Amazon S3 assigns to all log object keys
   *          for a bucket. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/RESTBucketPUTlogging.html">PUT Bucket logging</a> in the
   *             <i>Amazon Simple Storage Service API Reference</i>.</p>
   */
  public LoggingEnabled getLoggingEnabled() {
    return loggingEnabled;
  }

  /**
   * <p>Describes where logs are stored and the prefix that Amazon S3 assigns to all log object keys
   *          for a bucket. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/RESTBucketPUTlogging.html">PUT Bucket logging</a> in the
   *             <i>Amazon Simple Storage Service API Reference</i>.</p>
   */
  public void setLoggingEnabled(final LoggingEnabled loggingEnabled) {
    this.loggingEnabled = loggingEnabled;
  }
}
