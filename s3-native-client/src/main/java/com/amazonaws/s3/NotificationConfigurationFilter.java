package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class NotificationConfigurationFilter {
  private S3KeyFilter key;

  /**
   * <p>A container for object key name prefix and suffix filtering rules.</p>
   */
  public S3KeyFilter getKey() {
    return key;
  }

  /**
   * <p>A container for object key name prefix and suffix filtering rules.</p>
   */
  public void setKey(final S3KeyFilter key) {
    this.key = key;
  }
}
