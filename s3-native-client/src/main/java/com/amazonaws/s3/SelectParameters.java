package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class SelectParameters {
  private InputSerialization inputSerialization;

  private ExpressionType expressionType;

  private String expression;

  private OutputSerialization outputSerialization;

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

  public ExpressionType getExpressionType() {
    return expressionType;
  }

  public void setExpressionType(final ExpressionType expressionType) {
    this.expressionType = expressionType;
  }

  public String getExpression() {
    return expression;
  }

  public void setExpression(final String expression) {
    this.expression = expression;
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
}
