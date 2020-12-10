package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class StorageClassAnalysis {
  private StorageClassAnalysisDataExport dataExport;

  /**
   * <p>Container for data related to the storage class analysis for an Amazon S3 bucket for
   *          export.</p>
   */
  public StorageClassAnalysisDataExport getDataExport() {
    return dataExport;
  }

  /**
   * <p>Container for data related to the storage class analysis for an Amazon S3 bucket for
   *          export.</p>
   */
  public void setDataExport(final StorageClassAnalysisDataExport dataExport) {
    this.dataExport = dataExport;
  }
}
