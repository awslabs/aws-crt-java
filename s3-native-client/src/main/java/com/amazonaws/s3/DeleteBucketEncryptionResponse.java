package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class DeleteBucketEncryptionResponse {
  public DeleteBucketEncryptionResponse() {
  }

  @Override
  public int hashCode() {
    return Objects.hash(DeleteBucketEncryptionResponse.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof DeleteBucketEncryptionResponse);
  }
}
