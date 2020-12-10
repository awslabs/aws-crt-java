package com.amazonaws.s3;

import java.lang.String;
import java.util.List;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class ReplicationConfiguration {
  private String role;

  private List<ReplicationRule> rules;

  public String getRole() {
    return role;
  }

  public void setRole(final String role) {
    this.role = role;
  }

  public List<ReplicationRule> getRules() {
    return rules;
  }

  public void setRules(final List<ReplicationRule> rules) {
    this.rules = rules;
  }
}
