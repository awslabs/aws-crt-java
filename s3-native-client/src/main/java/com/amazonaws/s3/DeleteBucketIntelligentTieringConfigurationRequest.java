package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class DeleteBucketIntelligentTieringConfigurationRequest {
  private String bucket;

  private String id;

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
