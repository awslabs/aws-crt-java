package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class PutObjectTaggingOutput {
  private String versionId;

  public PutObjectTaggingOutput() {
    this.versionId = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(PutObjectTaggingOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof PutObjectTaggingOutput);
  }

  public String getVersionId() {
    return versionId;
  }

  public void setVersionId(final String versionId) {
    this.versionId = versionId;
  }
}
