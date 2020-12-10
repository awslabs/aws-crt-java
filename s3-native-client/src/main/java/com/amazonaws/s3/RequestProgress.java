package com.amazonaws.s3;

import java.lang.Boolean;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class RequestProgress {
  private Boolean enabled;

  public Boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(final Boolean enabled) {
    this.enabled = enabled;
  }
}
