package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class AnalyticsS3BucketDestination {
  private AnalyticsS3ExportFileFormat format;

  private String bucketAccountId;

  private String bucket;

  private String prefix;

  public AnalyticsS3ExportFileFormat getFormat() {
    return format;
  }

  public void setFormat(final AnalyticsS3ExportFileFormat format) {
    this.format = format;
  }

  public String getBucketAccountId() {
    return bucketAccountId;
  }

  public void setBucketAccountId(final String bucketAccountId) {
    this.bucketAccountId = bucketAccountId;
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(final String bucket) {
    this.bucket = bucket;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(final String prefix) {
    this.prefix = prefix;
  }
}
