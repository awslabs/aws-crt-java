package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class GetBucketPolicyStatusOutput {
  private PolicyStatus policyStatus;

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
