package com.amazonaws.s3;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class ListBucketIntelligentTieringConfigurationsOutput {
  private Boolean isTruncated;

  private String continuationToken;

  private String nextContinuationToken;

  private List<IntelligentTieringConfiguration> intelligentTieringConfigurationList;

  public ListBucketIntelligentTieringConfigurationsOutput() {
    this.isTruncated = null;
    this.continuationToken = null;
    this.nextContinuationToken = null;
    this.intelligentTieringConfigurationList = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(ListBucketIntelligentTieringConfigurationsOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof ListBucketIntelligentTieringConfigurationsOutput);
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

  public List<IntelligentTieringConfiguration> getIntelligentTieringConfigurationList() {
    return intelligentTieringConfigurationList;
  }

  public void setIntelligentTieringConfigurationList(
      final List<IntelligentTieringConfiguration> intelligentTieringConfigurationList) {
    this.intelligentTieringConfigurationList = intelligentTieringConfigurationList;
  }
}
