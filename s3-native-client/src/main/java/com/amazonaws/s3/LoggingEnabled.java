package com.amazonaws.s3;

import java.lang.String;
import java.util.List;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class LoggingEnabled {
  private String targetBucket;

  private List<TargetGrant> targetGrants;

  private String targetPrefix;

  public String getTargetBucket() {
    return targetBucket;
  }

  public void setTargetBucket(final String targetBucket) {
    this.targetBucket = targetBucket;
  }

  public List<TargetGrant> getTargetGrants() {
    return targetGrants;
  }

  public void setTargetGrants(final List<TargetGrant> targetGrants) {
    this.targetGrants = targetGrants;
  }

  public String getTargetPrefix() {
    return targetPrefix;
  }

  public void setTargetPrefix(final String targetPrefix) {
    this.targetPrefix = targetPrefix;
  }
}
