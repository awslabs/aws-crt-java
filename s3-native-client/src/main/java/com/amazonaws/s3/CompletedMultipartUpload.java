package com.amazonaws.s3;

import java.util.List;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class CompletedMultipartUpload {
  private List<CompletedPart> parts;

  public List<CompletedPart> getParts() {
    return parts;
  }

  public void setParts(final List<CompletedPart> parts) {
    this.parts = parts;
  }
}
