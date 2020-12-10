package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class EncryptionConfiguration {
  private String replicaKmsKeyID;

  public String getReplicaKmsKeyID() {
    return replicaKmsKeyID;
  }

  public void setReplicaKmsKeyID(final String replicaKmsKeyID) {
    this.replicaKmsKeyID = replicaKmsKeyID;
  }
}
