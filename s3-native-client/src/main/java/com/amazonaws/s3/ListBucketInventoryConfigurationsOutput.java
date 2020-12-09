package com.amazonaws.s3;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class ListBucketInventoryConfigurationsOutput {
  private String continuationToken;

  private List<InventoryConfiguration> inventoryConfigurationList;

  private Boolean isTruncated;

  private String nextContinuationToken;

  public ListBucketInventoryConfigurationsOutput() {
    this.continuationToken = null;
    this.inventoryConfigurationList = null;
    this.isTruncated = null;
    this.nextContinuationToken = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(ListBucketInventoryConfigurationsOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof ListBucketInventoryConfigurationsOutput);
  }

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
