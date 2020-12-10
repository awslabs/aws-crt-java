package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class ObjectLockRule {
  private DefaultRetention defaultRetention;

  /**
   * <p>The container element for specifying the default Object Lock retention settings for new
   *          objects placed in the specified bucket.</p>
   */
  public DefaultRetention getDefaultRetention() {
    return defaultRetention;
  }

  /**
   * <p>The container element for specifying the default Object Lock retention settings for new
   *          objects placed in the specified bucket.</p>
   */
  public void setDefaultRetention(final DefaultRetention defaultRetention) {
    this.defaultRetention = defaultRetention;
  }
}
