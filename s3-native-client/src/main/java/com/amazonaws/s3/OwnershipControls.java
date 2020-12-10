package com.amazonaws.s3;

import java.util.List;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class OwnershipControls {
  private List<OwnershipControlsRule> rules;

  public List<OwnershipControlsRule> getRules() {
    return rules;
  }

  public void setRules(final List<OwnershipControlsRule> rules) {
    this.rules = rules;
  }
}
