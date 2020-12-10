package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class GetBucketPolicyOutput {
  private String policy;

  public String getPolicy() {
    return policy;
  }

  public void setPolicy(final String policy) {
    this.policy = policy;
  }
}
