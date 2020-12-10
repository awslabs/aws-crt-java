package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.UnionGenerator")
public class AnalyticsFilter {
  private String prefix;

  private Tag tag;

  private AnalyticsAndOperator and;

  private AnalyticsFilter(Builder builder) {
    this.prefix = builder.prefix;
    this.tag = builder.tag;
    this.and = builder.and;
  }

  @Override
  public int hashCode() {
    return Objects.hash(AnalyticsFilter.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof AnalyticsFilter);
  }

  public String prefix() {
    return prefix;
  }

  public Tag tag() {
    return tag;
  }

  public AnalyticsAndOperator and() {
    return and;
  }

  public Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  static final class Builder {
    private String prefix;

    private Tag tag;

    private AnalyticsAndOperator and;

    private Builder() {
    }

    private Builder(AnalyticsFilter model) {
      prefix(model.prefix);
      tag(model.tag);
      and(model.and);
    }

    public AnalyticsFilter build() {
      return new com.amazonaws.s3.AnalyticsFilter(this);
    }

    public String getPrefix() {
      return prefix;
    }

    public void setPrefix(final String prefix) {
      this.prefix = prefix;
    }

    public final Builder prefix(String prefix) {
      this.prefix = prefix;
      return this;
    }

    public Tag getTag() {
      return tag;
    }

    public void setTag(final Tag tag) {
      this.tag = tag;
    }

    public final Builder tag(Tag tag) {
      this.tag = tag;
      return this;
    }

    public AnalyticsAndOperator getAnd() {
      return and;
    }

    public void setAnd(final AnalyticsAndOperator and) {
      this.and = and;
    }

    public final Builder and(AnalyticsAndOperator and) {
      this.and = and;
      return this;
    }
  }
}
