package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class SelectObjectContentOutput {
  private SelectObjectContentEventStream payload;

  public SelectObjectContentOutput() {
    this.payload = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(SelectObjectContentOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof SelectObjectContentOutput);
  }

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
