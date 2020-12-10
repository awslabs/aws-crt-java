package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class Grant {
  private Grantee grantee;

  private Permission permission;

  /**
   * <p>Container for the person being granted permissions.</p>
   */
  public Grantee getGrantee() {
    return grantee;
  }

  /**
   * <p>Container for the person being granted permissions.</p>
   */
  public void setGrantee(final Grantee grantee) {
    this.grantee = grantee;
  }

  public Permission getPermission() {
    return permission;
  }

  public void setPermission(final Permission permission) {
    this.permission = permission;
  }
}
