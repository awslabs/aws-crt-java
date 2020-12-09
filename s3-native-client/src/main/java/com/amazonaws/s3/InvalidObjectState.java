package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class InvalidObjectState {
  private StorageClass storageClass;

  private IntelligentTieringAccessTier accessTier;

  public InvalidObjectState() {
    this.storageClass = null;
    this.accessTier = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(InvalidObjectState.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof InvalidObjectState);
  }

  public String getStorageClass() {
    return storageClass;
  }

  public void setStorageClass(final String storageClass) {
    this.storageClass = storageClass;
  }

  public String getAccessTier() {
    return accessTier;
  }

  public void setAccessTier(final String accessTier) {
    this.accessTier = accessTier;
  }
}
