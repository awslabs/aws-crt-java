package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class AccessControlTranslation {
  private OwnerOverride owner;

  public OwnerOverride getOwner() {
    return owner;
  }

  public void setOwner(final OwnerOverride owner) {
    this.owner = owner;
  }
}
