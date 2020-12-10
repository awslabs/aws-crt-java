package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class StatsEvent {
  private Stats details;

  /**
   * <p>Container for the stats details.</p>
   */
  public Stats getDetails() {
    return details;
  }

  /**
   * <p>Container for the stats details.</p>
   */
  public void setDetails(final Stats details) {
    this.details = details;
  }
}
