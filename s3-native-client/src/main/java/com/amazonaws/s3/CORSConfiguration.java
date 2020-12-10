package com.amazonaws.s3;

import java.util.List;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class CORSConfiguration {
  private List<CORSRule> cORSRules;

  public List<CORSRule> getCORSRules() {
    return cORSRules;
  }

  public void setCORSRules(final List<CORSRule> cORSRules) {
    this.cORSRules = cORSRules;
  }
}
