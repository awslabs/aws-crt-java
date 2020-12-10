package com.amazonaws.s3;

import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class InputSerialization {
  private CSVInput cSV;

  private CompressionType compressionType;

  private JSONInput jSON;

  private ParquetInput parquet;

  /**
   * <p>Describes how an uncompressed comma-separated values (CSV)-formatted input object is
   *          formatted.</p>
   */
  public CSVInput getCSV() {
    return cSV;
  }

  /**
   * <p>Describes how an uncompressed comma-separated values (CSV)-formatted input object is
   *          formatted.</p>
   */
  public void setCSV(final CSVInput cSV) {
    this.cSV = cSV;
  }

  public CompressionType getCompressionType() {
    return compressionType;
  }

  public void setCompressionType(final CompressionType compressionType) {
    this.compressionType = compressionType;
  }

  /**
   * <p>Specifies JSON as object's input serialization format.</p>
   */
  public JSONInput getJSON() {
    return jSON;
  }

  /**
   * <p>Specifies JSON as object's input serialization format.</p>
   */
  public void setJSON(final JSONInput jSON) {
    this.jSON = jSON;
  }

  /**
   * <p>Container for Parquet.</p>
   */
  public ParquetInput getParquet() {
    return parquet;
  }

  /**
   * <p>Container for Parquet.</p>
   */
  public void setParquet(final ParquetInput parquet) {
    this.parquet = parquet;
  }
}
