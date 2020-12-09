package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class PutBucketAclResponse {
  public PutBucketAclResponse() {
  }

  @Override
  public int hashCode() {
    return Objects.hash(PutBucketAclResponse.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof PutBucketAclResponse);
  }
}
