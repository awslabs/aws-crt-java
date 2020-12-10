package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class ServerSideEncryptionByDefault {
  private ServerSideEncryption sSEAlgorithm;

  private String kMSMasterKeyID;

  public ServerSideEncryption getSSEAlgorithm() {
    return sSEAlgorithm;
  }

  public void setSSEAlgorithm(final ServerSideEncryption sSEAlgorithm) {
    this.sSEAlgorithm = sSEAlgorithm;
  }

  public String getKMSMasterKeyID() {
    return kMSMasterKeyID;
  }

  public void setKMSMasterKeyID(final String kMSMasterKeyID) {
    this.kMSMasterKeyID = kMSMasterKeyID;
  }
}
