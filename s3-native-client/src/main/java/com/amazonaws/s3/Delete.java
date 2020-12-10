package com.amazonaws.s3;

import java.lang.Boolean;
import java.util.List;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class Delete {
  private List<ObjectIdentifier> objects;

  private Boolean quiet;

  public List<ObjectIdentifier> getObjects() {
    return objects;
  }

  public void setObjects(final List<ObjectIdentifier> objects) {
    this.objects = objects;
  }

  public Boolean isQuiet() {
    return quiet;
  }

  public void setQuiet(final Boolean quiet) {
    this.quiet = quiet;
  }
}
