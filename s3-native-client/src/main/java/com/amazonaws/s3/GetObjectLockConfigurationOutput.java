package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class GetObjectLockConfigurationOutput {
  private ObjectLockConfiguration objectLockConfiguration;

  /**
   * <p>The container element for Object Lock configuration parameters.</p>
   */
  public ObjectLockConfiguration getObjectLockConfiguration() {
    return objectLockConfiguration;
  }

  /**
   * <p>The container element for Object Lock configuration parameters.</p>
   */
  public void setObjectLockConfiguration(final ObjectLockConfiguration objectLockConfiguration) {
    this.objectLockConfiguration = objectLockConfiguration;
  }
}
