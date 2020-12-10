package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class InvalidObjectState {
  private StorageClass storageClass;

  private IntelligentTieringAccessTier accessTier;

  public StorageClass getStorageClass() {
    return storageClass;
  }

  public void setStorageClass(final StorageClass storageClass) {
    this.storageClass = storageClass;
  }

  public IntelligentTieringAccessTier getAccessTier() {
    return accessTier;
  }

  public void setAccessTier(final IntelligentTieringAccessTier accessTier) {
    this.accessTier = accessTier;
  }
}
