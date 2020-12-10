package com.amazonaws.s3;

import java.util.List;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class ServerSideEncryptionConfiguration {
  private List<ServerSideEncryptionRule> rules;

  public List<ServerSideEncryptionRule> getRules() {
    return rules;
  }

  public void setRules(final List<ServerSideEncryptionRule> rules) {
    this.rules = rules;
  }
}
