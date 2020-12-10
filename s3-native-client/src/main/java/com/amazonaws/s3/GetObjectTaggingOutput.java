package com.amazonaws.s3;

import java.lang.String;
import java.util.List;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class GetObjectTaggingOutput {
  private String versionId;

  private List<Tag> tagSet;

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
