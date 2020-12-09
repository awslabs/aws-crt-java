package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class GetBucketPolicyOutput {
  private String policy;

  public GetBucketPolicyOutput() {
    this.policy = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(GetBucketPolicyOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof GetBucketPolicyOutput);
  }

  public String getPolicy() {
    return policy;
  }

  public void setPolicy(final String policy) {
    this.policy = policy;
  }
}
