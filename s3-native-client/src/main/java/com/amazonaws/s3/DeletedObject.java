package com.amazonaws.s3;

import java.lang.Boolean;
import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class DeletedObject {
  private String key;

  private String versionId;

  private Boolean deleteMarker;

  private String deleteMarkerVersionId;

  public String getKey() {
    return key;
  }

  public void setKey(final String key) {
    this.key = key;
  }

  public String getVersionId() {
    return versionId;
  }

  public void setVersionId(final String versionId) {
    this.versionId = versionId;
  }

  public Boolean isDeleteMarker() {
    return deleteMarker;
  }

  public void setDeleteMarker(final Boolean deleteMarker) {
    this.deleteMarker = deleteMarker;
  }

  public String getDeleteMarkerVersionId() {
    return deleteMarkerVersionId;
  }

  public void setDeleteMarkerVersionId(final String deleteMarkerVersionId) {
    this.deleteMarkerVersionId = deleteMarkerVersionId;
  }
}
