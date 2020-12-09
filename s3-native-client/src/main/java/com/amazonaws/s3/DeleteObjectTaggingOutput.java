package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class DeleteObjectTaggingOutput {
  private String versionId;

  public DeleteObjectTaggingOutput() {
    this.versionId = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(DeleteObjectTaggingOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof DeleteObjectTaggingOutput);
  }

  public String getVersionId() {
    return versionId;
  }

  public void setVersionId(final String versionId) {
    this.versionId = versionId;
  }
}
