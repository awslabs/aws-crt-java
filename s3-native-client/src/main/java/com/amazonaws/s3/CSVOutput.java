package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class CSVOutput {
  private QuoteFields quoteFields;

  private String quoteEscapeCharacter;

  private String recordDelimiter;

  private String fieldDelimiter;

  private String quoteCharacter;

  public QuoteFields getQuoteFields() {
    return quoteFields;
  }

  public void setQuoteFields(final QuoteFields quoteFields) {
    this.quoteFields = quoteFields;
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
}
