package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class GetBucketPolicyStatusOutput {
  private PolicyStatus policyStatus;

  public GetBucketPolicyStatusOutput() {
    this.policyStatus = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(GetBucketPolicyStatusOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof GetBucketPolicyStatusOutput);
  }

  /**
   * <p>The container element for a bucket's policy status.</p>
   */
  public PolicyStatus getPolicyStatus() {
    return policyStatus;
  }

  /**
   * <p>The container element for a bucket's policy status.</p>
   */
  public void setPolicyStatus(final PolicyStatus policyStatus) {
    this.policyStatus = policyStatus;
  }
}
