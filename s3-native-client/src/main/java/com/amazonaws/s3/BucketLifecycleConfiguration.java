package com.amazonaws.s3;

import java.util.List;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class BucketLifecycleConfiguration {
  private List<LifecycleRule> rules;

  public List<LifecycleRule> getRules() {
    return rules;
  }

  public void setRules(final List<LifecycleRule> rules) {
    this.rules = rules;
  }
}
