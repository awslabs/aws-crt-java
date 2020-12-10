package com.amazonaws.s3;

import java.lang.Boolean;
import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class CSVInput {
  private FileHeaderInfo fileHeaderInfo;

  private String comments;

  private String quoteEscapeCharacter;

  private String recordDelimiter;

  private String fieldDelimiter;

  private String quoteCharacter;

  private Boolean allowQuotedRecordDelimiter;

  public FileHeaderInfo getFileHeaderInfo() {
    return fileHeaderInfo;
  }

  public void setFileHeaderInfo(final FileHeaderInfo fileHeaderInfo) {
    this.fileHeaderInfo = fileHeaderInfo;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(final String comments) {
    this.comments = comments;
  }

  public String getQuoteEscapeCharacter() {
    return quoteEscapeCharacter;
  }

  public void setQuoteEscapeCharacter(final String quoteEscapeCharacter) {
    this.quoteEscapeCharacter = quoteEscapeCharacter;
  }

  public String getRecordDelimiter() {
    return recordDelimiter;
  }

  public void setRecordDelimiter(final String recordDelimiter) {
    this.recordDelimiter = recordDelimiter;
  }

  public String getFieldDelimiter() {
    return fieldDelimiter;
  }

  public void setFieldDelimiter(final String fieldDelimiter) {
    this.fieldDelimiter = fieldDelimiter;
  }

  public String getQuoteCharacter() {
    return quoteCharacter;
  }

  public void setQuoteCharacter(final String quoteCharacter) {
    this.quoteCharacter = quoteCharacter;
  }

  public Boolean isAllowQuotedRecordDelimiter() {
    return allowQuotedRecordDelimiter;
  }

  public void setAllowQuotedRecordDelimiter(final Boolean allowQuotedRecordDelimiter) {
    this.allowQuotedRecordDelimiter = allowQuotedRecordDelimiter;
  }
}
