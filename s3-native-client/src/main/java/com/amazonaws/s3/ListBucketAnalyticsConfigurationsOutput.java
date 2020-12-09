package com.amazonaws.s3;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class ListBucketAnalyticsConfigurationsOutput {
  private Boolean isTruncated;

  private String continuationToken;

  private String nextContinuationToken;

  private List<AnalyticsConfiguration> analyticsConfigurationList;

  public ListBucketAnalyticsConfigurationsOutput() {
    this.isTruncated = null;
    this.continuationToken = null;
    this.nextContinuationToken = null;
    this.analyticsConfigurationList = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(ListBucketAnalyticsConfigurationsOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof ListBucketAnalyticsConfigurationsOutput);
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

  public List<AnalyticsConfiguration> getAnalyticsConfigurationList() {
    return analyticsConfigurationList;
  }

  public void setAnalyticsConfigurationList(
      final List<AnalyticsConfiguration> analyticsConfigurationList) {
    this.analyticsConfigurationList = analyticsConfigurationList;
  }
}
