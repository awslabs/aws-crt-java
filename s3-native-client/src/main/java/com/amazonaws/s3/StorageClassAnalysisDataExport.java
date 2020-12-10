package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class StorageClassAnalysisDataExport {
  private StorageClassAnalysisSchemaVersion outputSchemaVersion;

  private AnalyticsExportDestination destination;

  public StorageClassAnalysisSchemaVersion getOutputSchemaVersion() {
    return outputSchemaVersion;
  }

  public void setOutputSchemaVersion(final StorageClassAnalysisSchemaVersion outputSchemaVersion) {
    this.outputSchemaVersion = outputSchemaVersion;
  }

  /**
   * <p>Where to publish the analytics results.</p>
   */
  public AnalyticsExportDestination getDestination() {
    return destination;
  }

  /**
   * <p>Where to publish the analytics results.</p>
   */
  public void setDestination(final AnalyticsExportDestination destination) {
    this.destination = destination;
  }
}
