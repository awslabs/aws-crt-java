package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class ObjectLockConfiguration {
  private ObjectLockEnabled objectLockEnabled;

  private ObjectLockRule rule;

  public ObjectLockEnabled getObjectLockEnabled() {
    return objectLockEnabled;
  }

  public void setObjectLockEnabled(final ObjectLockEnabled objectLockEnabled) {
    this.objectLockEnabled = objectLockEnabled;
  }

  /**
   * <p>The container element for an Object Lock rule.</p>
   */
  public ObjectLockRule getRule() {
    return rule;
  }

  /**
   * <p>The container element for an Object Lock rule.</p>
   */
  public void setRule(final ObjectLockRule rule) {
    this.rule = rule;
  }
}
