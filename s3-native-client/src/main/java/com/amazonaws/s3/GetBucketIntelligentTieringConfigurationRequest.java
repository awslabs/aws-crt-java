package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class GetBucketIntelligentTieringConfigurationRequest {
  private String bucket;

  private String id;

  public GetBucketIntelligentTieringConfigurationRequest() {
    this.bucket = null;
    this.id = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(GetBucketIntelligentTieringConfigurationRequest.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof GetBucketIntelligentTieringConfigurationRequest);
  }

  public String getBucket() {
    return bucket;
  }

  public void setBucket(final String bucket) {
    this.bucket = bucket;
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }
}
