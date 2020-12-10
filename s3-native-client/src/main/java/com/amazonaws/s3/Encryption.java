package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class Encryption {
  private ServerSideEncryption encryptionType;

  private String kMSKeyId;

  private String kMSContext;

  public ServerSideEncryption getEncryptionType() {
    return encryptionType;
  }

  public void setEncryptionType(final ServerSideEncryption encryptionType) {
    this.encryptionType = encryptionType;
  }

  public String getKMSKeyId() {
    return kMSKeyId;
  }

  public void setKMSKeyId(final String kMSKeyId) {
    this.kMSKeyId = kMSKeyId;
  }

  public String getKMSContext() {
    return kMSContext;
  }

  public void setKMSContext(final String kMSContext) {
    this.kMSContext = kMSContext;
  }
}
