package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class GetBucketLifecycleConfigurationOutput {
  private List<LifecycleRule> rules;

  public GetBucketLifecycleConfigurationOutput() {
    this.rules = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(GetBucketLifecycleConfigurationOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof GetBucketLifecycleConfigurationOutput);
  }

  public List<LifecycleRule> getRules() {
    return rules;
  }

  public void setRules(final List<LifecycleRule> rules) {
    this.rules = rules;
  }
}
