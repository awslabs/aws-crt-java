package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class SelectObjectContentOutput {
  private SelectObjectContentEventStream payload;

  /**
   * <p>The container for selecting objects from a content event stream.</p>
   */
  public SelectObjectContentEventStream getPayload() {
    return payload;
  }

  /**
   * <p>The container for selecting objects from a content event stream.</p>
   */
  public void setPayload(final SelectObjectContentEventStream payload) {
    this.payload = payload;
  }
}
