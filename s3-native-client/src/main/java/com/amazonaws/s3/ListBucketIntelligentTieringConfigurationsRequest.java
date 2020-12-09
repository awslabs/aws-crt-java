package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class ListBucketIntelligentTieringConfigurationsRequest {
  private String bucket;

  private String continuationToken;

  public ListBucketIntelligentTieringConfigurationsRequest() {
    this.bucket = null;
    this.continuationToken = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(ListBucketIntelligentTieringConfigurationsRequest.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof ListBucketIntelligentTieringConfigurationsRequest);
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(final String bucket) {
    this.bucket = bucket;
  }

  public String getContinuationToken() {
    return continuationToken;
  }

  public void setContinuationToken(final String continuationToken) {
    this.continuationToken = continuationToken;
  }
}
