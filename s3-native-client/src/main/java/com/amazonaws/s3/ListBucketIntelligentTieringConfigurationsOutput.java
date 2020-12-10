package com.amazonaws.s3;

import java.lang.Boolean;
import java.lang.String;
import java.util.List;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class ListBucketIntelligentTieringConfigurationsOutput {
  private Boolean isTruncated;

  private String continuationToken;

  private String nextContinuationToken;

  private List<IntelligentTieringConfiguration> intelligentTieringConfigurationList;

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
