package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class OutputSerialization {
  private CSVOutput cSV;

  private JSONOutput jSON;

  /**
   * <p>Describes how uncompressed comma-separated values (CSV)-formatted results are
   *          formatted.</p>
   */
  public CSVOutput getCSV() {
    return cSV;
  }

  /**
   * <p>Describes how uncompressed comma-separated values (CSV)-formatted results are
   *          formatted.</p>
   */
  public void setCSV(final CSVOutput cSV) {
    this.cSV = cSV;
  }

  /**
   * <p>Specifies JSON as request's output serialization format.</p>
   */
  public JSONOutput getJSON() {
    return jSON;
  }

  /**
   * <p>Specifies JSON as request's output serialization format.</p>
   */
  public void setJSON(final JSONOutput jSON) {
    this.jSON = jSON;
  }
}
