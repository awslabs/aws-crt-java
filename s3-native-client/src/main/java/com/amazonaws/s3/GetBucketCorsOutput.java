package com.amazonaws.s3;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import javax.annotation.processing.Generated;

@Generated("aws.crt.java.generator")
public class GetBucketCorsOutput {
  private List<CORSRule> cORSRules;

  public GetBucketCorsOutput() {
    this.cORSRules = null;
  }

  @Override
  public int hashCode() {
    return Objects.hash(GetBucketCorsOutput.class);
  }

  @Override
  public boolean equals(Object rhs) {
    if (rhs == null) return false;
    return (rhs instanceof GetBucketCorsOutput);
  }

  public List<CORSRule> getCORSRules() {
    return cORSRules;
  }

  public void setCORSRules(final List<CORSRule> cORSRules) {
    this.cORSRules = cORSRules;
  }
}
