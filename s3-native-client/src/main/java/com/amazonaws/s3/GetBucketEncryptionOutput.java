package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class GetBucketEncryptionOutput {
  private ServerSideEncryptionConfiguration serverSideEncryptionConfiguration;

  public GetBucketEncryptionOutput() {
    this.serverSideEncryptionConfiguration = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(GetBucketEncryptionOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof GetBucketEncryptionOutput);
  }

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
