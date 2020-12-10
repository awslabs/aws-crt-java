package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class IntelligentTieringFilter {
  private String prefix;

  private Tag tag;

  private IntelligentTieringAndOperator and;

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(final String prefix) {
    this.prefix = prefix;
  }

  /**
   * <p>A container of a key value name pair.</p>
   */
  public Tag getTag() {
    return tag;
  }

  /**
   * <p>A container of a key value name pair.</p>
   */
  public void setTag(final Tag tag) {
    this.tag = tag;
  }

  /**
   * <p>A container for specifying S3 Intelligent-Tiering filters. The filters determine the
   *          subset of objects to which the rule applies.</p>
   */
  public IntelligentTieringAndOperator getAnd() {
    return and;
  }

  /**
   * <p>A container for specifying S3 Intelligent-Tiering filters. The filters determine the
   *          subset of objects to which the rule applies.</p>
   */
  public void setAnd(final IntelligentTieringAndOperator and) {
    this.and = and;
  }
}
