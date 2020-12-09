package com.amazonaws.s3;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class ListBucketMetricsConfigurationsOutput {
  private Boolean isTruncated;

  private String continuationToken;

  private String nextContinuationToken;

  private List<MetricsConfiguration> metricsConfigurationList;

  public ListBucketMetricsConfigurationsOutput() {
    this.isTruncated = null;
    this.continuationToken = null;
    this.nextContinuationToken = null;
    this.metricsConfigurationList = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(ListBucketMetricsConfigurationsOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof ListBucketMetricsConfigurationsOutput);
  }

  public Boolean isIsTruncated() {
    return isTruncated;
  }

  public void setIsTruncated(final Boolean isTruncated) {
    this.isTruncated = isTruncated;
  }

  public String getContinuationToken() {
    return continuationToken;
  }

  public void setContinuationToken(final String continuationToken) {
    this.continuationToken = continuationToken;
  }

  public String getNextContinuationToken() {
    return nextContinuationToken;
  }

  public void setNextContinuationToken(final String nextContinuationToken) {
    this.nextContinuationToken = nextContinuationToken;
  }

  public List<MetricsConfiguration> getMetricsConfigurationList() {
    return metricsConfigurationList;
  }

  public void setMetricsConfigurationList(
      final List<MetricsConfiguration> metricsConfigurationList) {
    this.metricsConfigurationList = metricsConfigurationList;
  }
}
