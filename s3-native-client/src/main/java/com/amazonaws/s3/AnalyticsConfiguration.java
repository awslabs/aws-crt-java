package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class AnalyticsConfiguration {
  private String id;

  private AnalyticsFilter filter;

  private StorageClassAnalysis storageClassAnalysis;

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  /**
   * <p>The filter used to describe a set of objects for analyses. A filter must have exactly
   *          one prefix, one tag, or one conjunction (AnalyticsAndOperator). If no filter is provided,
   *          all objects will be considered in any analysis.</p>
   */
  public AnalyticsFilter getFilter() {
    return filter;
  }

  /**
   * <p>The filter used to describe a set of objects for analyses. A filter must have exactly
   *          one prefix, one tag, or one conjunction (AnalyticsAndOperator). If no filter is provided,
   *          all objects will be considered in any analysis.</p>
   */
  public void setFilter(final AnalyticsFilter filter) {
    this.filter = filter;
  }

  /**
   * <p>Specifies data related to access patterns to be collected and made available to analyze
   *          the tradeoffs between different storage classes for an Amazon S3 bucket.</p>
   */
  public StorageClassAnalysis getStorageClassAnalysis() {
    return storageClassAnalysis;
  }

  /**
   * <p>Specifies data related to access patterns to be collected and made available to analyze
   *          the tradeoffs between different storage classes for an Amazon S3 bucket.</p>
   */
  public void setStorageClassAnalysis(final StorageClassAnalysis storageClassAnalysis) {
    this.storageClassAnalysis = storageClassAnalysis;
  }
}
