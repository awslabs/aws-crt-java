package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class FilterRule {
  private FilterRuleName name;

  private String value;

  public FilterRuleName getName() {
    return name;
  }

  public void setName(final FilterRuleName name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(final String value) {
    this.value = value;
  }
}
