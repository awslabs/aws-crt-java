package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class Owner {
  private String displayName;

  private String iD;

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(final String displayName) {
    this.displayName = displayName;
  }

  public String getID() {
    return iD;
  }

  public void setID(final String iD) {
    this.iD = iD;
  }
}
