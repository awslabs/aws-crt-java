package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.EmptyStructureGenerator")
public class DeleteBucketMetricsConfigurationResponse {
  public DeleteBucketMetricsConfigurationResponse() {
  }

  @Override
  public int hashCode() {
    return Objects.hash(DeleteBucketMetricsConfigurationResponse.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof DeleteBucketMetricsConfigurationResponse);
  }
}
