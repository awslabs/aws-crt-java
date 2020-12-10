package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class InventorySchedule {
  private InventoryFrequency frequency;

  public InventoryFrequency getFrequency() {
    return frequency;
  }

  public void setFrequency(final InventoryFrequency frequency) {
    this.frequency = frequency;
  }
}
