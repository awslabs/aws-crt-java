package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class ListBucketIntelligentTieringConfigurationsRequest {
  private String bucket;

  private String continuationToken;

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
