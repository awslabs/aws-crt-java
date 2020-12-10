package com.amazonaws.s3;

import java.lang.Boolean;
import java.lang.String;
import java.util.List;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class ListBucketInventoryConfigurationsOutput {
  private String continuationToken;

  private List<InventoryConfiguration> inventoryConfigurationList;

  private Boolean isTruncated;

  private String nextContinuationToken;

  public String getContinuationToken() {
    return continuationToken;
  }

  public void setContinuationToken(final String continuationToken) {
    this.continuationToken = continuationToken;
  }

  public List<InventoryConfiguration> getInventoryConfigurationList() {
    return inventoryConfigurationList;
  }

  public void setInventoryConfigurationList(
      final List<InventoryConfiguration> inventoryConfigurationList) {
    this.inventoryConfigurationList = inventoryConfigurationList;
  }

  public Boolean isIsTruncated() {
    return isTruncated;
  }

  public void setIsTruncated(final Boolean isTruncated) {
    this.isTruncated = isTruncated;
  }

  public String getNextContinuationToken() {
    return nextContinuationToken;
  }

  public void setNextContinuationToken(final String nextContinuationToken) {
    this.nextContinuationToken = nextContinuationToken;
  }
}
