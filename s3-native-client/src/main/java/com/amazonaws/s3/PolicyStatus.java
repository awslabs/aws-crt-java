package com.amazonaws.s3;

import java.lang.Boolean;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class PolicyStatus {
  private Boolean isPublic;

  public Boolean isIsPublic() {
    return isPublic;
  }

  public void setIsPublic(final Boolean isPublic) {
    this.isPublic = isPublic;
  }
}
