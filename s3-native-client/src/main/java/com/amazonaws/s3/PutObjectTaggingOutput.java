package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class PutObjectTaggingOutput {
  private String versionId;

  public String getVersionId() {
    return versionId;
  }

  public void setVersionId(final String versionId) {
    this.versionId = versionId;
  }
}
