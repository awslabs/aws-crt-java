package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class GetBucketTaggingOutput {
  private List<Tag> tagSet;

  public GetBucketTaggingOutput() {
    this.tagSet = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(GetBucketTaggingOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof GetBucketTaggingOutput);
  }

  public List<Tag> getTagSet() {
    return tagSet;
  }

  public void setTagSet(final List<Tag> tagSet) {
    this.tagSet = tagSet;
  }
}
