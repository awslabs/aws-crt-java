package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class MetricsConfiguration {
  private String id;

  private MetricsFilter filter;

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  /**
   * <p>Specifies a metrics configuration filter. The metrics configuration only includes
   *          objects that meet the filter's criteria. A filter must be a prefix, a tag, or a conjunction
   *          (MetricsAndOperator).</p>
   */
  public MetricsFilter getFilter() {
    return filter;
  }

  /**
   * <p>Specifies a metrics configuration filter. The metrics configuration only includes
   *          objects that meet the filter's criteria. A filter must be a prefix, a tag, or a conjunction
   *          (MetricsAndOperator).</p>
   */
  public void setFilter(final MetricsFilter filter) {
    this.filter = filter;
  }
}
