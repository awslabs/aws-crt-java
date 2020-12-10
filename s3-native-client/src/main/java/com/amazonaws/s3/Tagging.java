package com.amazonaws.s3;

import java.util.List;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class Tagging {
  private List<Tag> tagSet;

  public List<Tag> getTagSet() {
    return tagSet;
  }

  public void setTagSet(final List<Tag> tagSet) {
    this.tagSet = tagSet;
  }
}
