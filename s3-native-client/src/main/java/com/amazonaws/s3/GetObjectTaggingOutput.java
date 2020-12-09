package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class GetObjectTaggingOutput {
  private String versionId;

  private List<Tag> tagSet;

  public GetObjectTaggingOutput() {
    this.versionId = null;
    this.tagSet = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(GetObjectTaggingOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof GetObjectTaggingOutput);
  }

  public String getVersionId() {
    return versionId;
  }

  public void setVersionId(final String versionId) {
    this.versionId = versionId;
  }

  public List<Tag> getTagSet() {
    return tagSet;
  }

  public void setTagSet(final List<Tag> tagSet) {
    this.tagSet = tagSet;
  }
}
