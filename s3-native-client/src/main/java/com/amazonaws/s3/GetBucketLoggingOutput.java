package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class GetBucketLoggingOutput {
  private LoggingEnabled loggingEnabled;

  public GetBucketLoggingOutput() {
    this.loggingEnabled = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(GetBucketLoggingOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof GetBucketLoggingOutput);
  }

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
