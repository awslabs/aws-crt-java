package com.amazonaws.s3;

import java.lang.Integer;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class NoncurrentVersionTransition {
  private Integer noncurrentDays;

  private TransitionStorageClass storageClass;

  public Integer getNoncurrentDays() {
    return noncurrentDays;
  }

  public void setNoncurrentDays(final Integer noncurrentDays) {
    this.noncurrentDays = noncurrentDays;
  }

  public TransitionStorageClass getStorageClass() {
    return storageClass;
  }

  public void setStorageClass(final TransitionStorageClass storageClass) {
    this.storageClass = storageClass;
  }
}
