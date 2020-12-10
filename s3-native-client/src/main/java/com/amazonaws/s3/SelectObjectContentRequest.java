package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class SelectObjectContentRequest {
  private String bucket;

  private String key;

  private String sSECustomerAlgorithm;

  private String sSECustomerKey;

  private String sSECustomerKeyMD5;

  private String expression;

  private ExpressionType expressionType;

  private RequestProgress requestProgress;

  private InputSerialization inputSerialization;

  private OutputSerialization outputSerialization;

  private ScanRange scanRange;

  private String expectedBucketOwner;

  public String getBucket() {
    return bucket;
  }

  public void setBucket(final String bucket) {
    this.bucket = bucket;
  }

  public String getKey() {
    return key;
  }

  public void setKey(final String key) {
    this.key = key;
  }

  public String getSSECustomerAlgorithm() {
    return sSECustomerAlgorithm;
  }

  public void setSSECustomerAlgorithm(final String sSECustomerAlgorithm) {
    this.sSECustomerAlgorithm = sSECustomerAlgorithm;
  }

  public String getSSECustomerKey() {
    return sSECustomerKey;
  }

  public void setSSECustomerKey(final String sSECustomerKey) {
    this.sSECustomerKey = sSECustomerKey;
  }

  public String getSSECustomerKeyMD5() {
    return sSECustomerKeyMD5;
  }

  public void setSSECustomerKeyMD5(final String sSECustomerKeyMD5) {
    this.sSECustomerKeyMD5 = sSECustomerKeyMD5;
  }

  public String getExpression() {
    return expression;
  }

  public void setExpression(final String expression) {
    this.expression = expression;
  }

  public ExpressionType getExpressionType() {
    return expressionType;
  }

  public void setExpressionType(final ExpressionType expressionType) {
    this.expressionType = expressionType;
  }

  /**
   * <p>Container for specifying if periodic <code>QueryProgress</code> messages should be
   *          sent.</p>
   */
  public RequestProgress getRequestProgress() {
    return requestProgress;
  }

  /**
   * <p>Container for specifying if periodic <code>QueryProgress</code> messages should be
   *          sent.</p>
   */
  public void setRequestProgress(final RequestProgress requestProgress) {
    this.requestProgress = requestProgress;
  }

  /**
   * <p>Describes the serialization format of the object.</p>
   */
  public InputSerialization getInputSerialization() {
    return inputSerialization;
  }

  /**
   * <p>Describes the serialization format of the object.</p>
   */
  public void setInputSerialization(final InputSerialization inputSerialization) {
    this.inputSerialization = inputSerialization;
  }

  /**
   * <p>Describes how results of the Select job are serialized.</p>
   */
  public OutputSerialization getOutputSerialization() {
    return outputSerialization;
  }

  /**
   * <p>Describes how results of the Select job are serialized.</p>
   */
  public void setOutputSerialization(final OutputSerialization outputSerialization) {
    this.outputSerialization = outputSerialization;
  }

  /**
   * <p>Specifies the byte range of the object to get the records from. A record is processed
   *          when its first byte is contained by the range. This parameter is optional, but when
   *          specified, it must not be empty. See RFC 2616, Section 14.35.1 about how to specify the
   *          start and end of the range.</p>
   */
  public ScanRange getScanRange() {
    return scanRange;
  }

  /**
   * <p>Specifies the byte range of the object to get the records from. A record is processed
   *          when its first byte is contained by the range. This parameter is optional, but when
   *          specified, it must not be empty. See RFC 2616, Section 14.35.1 about how to specify the
   *          start and end of the range.</p>
   */
  public void setScanRange(final ScanRange scanRange) {
    this.scanRange = scanRange;
  }

  public String getExpectedBucketOwner() {
    return expectedBucketOwner;
  }

  public void setExpectedBucketOwner(final String expectedBucketOwner) {
    this.expectedBucketOwner = expectedBucketOwner;
  }
}
