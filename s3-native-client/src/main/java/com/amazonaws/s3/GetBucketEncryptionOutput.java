package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class GetBucketEncryptionOutput {
  private ServerSideEncryptionConfiguration serverSideEncryptionConfiguration;

  /**
   * <p>Specifies the default server-side-encryption configuration.</p>
   */
  public ServerSideEncryptionConfiguration getServerSideEncryptionConfiguration() {
    return serverSideEncryptionConfiguration;
  }

  /**
   * <p>Specifies the default server-side-encryption configuration.</p>
   */
  public void setServerSideEncryptionConfiguration(
      final ServerSideEncryptionConfiguration serverSideEncryptionConfiguration) {
    this.serverSideEncryptionConfiguration = serverSideEncryptionConfiguration;
  }
}
