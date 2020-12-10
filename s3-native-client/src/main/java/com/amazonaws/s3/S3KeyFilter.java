package com.amazonaws.s3;

import java.util.List;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class S3KeyFilter {
  private List<FilterRule> filterRules;

  /**
   * <p>A list of containers for the key-value pair that defines the criteria for the filter
   *          rule.</p>
   */
  public List<FilterRule> getFilterRules() {
    return filterRules;
  }

  /**
   * <p>A list of containers for the key-value pair that defines the criteria for the filter
   *          rule.</p>
   */
  public void setFilterRules(final List<FilterRule> filterRules) {
    this.filterRules = filterRules;
  }
}
