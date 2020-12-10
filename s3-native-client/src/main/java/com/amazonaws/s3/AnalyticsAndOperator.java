package com.amazonaws.s3;

import java.lang.String;
import java.util.List;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class AnalyticsAndOperator {
  private String prefix;

  private List<Tag> tags;

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(final String prefix) {
    this.prefix = prefix;
  }

  public List<Tag> getTags() {
    return tags;
  }

  public void setTags(final List<Tag> tags) {
    this.tags = tags;
  }
}
