package com.amazonaws.s3;

import java.lang.String;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.StructureGenerator")
public class Condition {
  private String httpErrorCodeReturnedEquals;

  private String keyPrefixEquals;

  public String getHttpErrorCodeReturnedEquals() {
    return httpErrorCodeReturnedEquals;
  }

  public void setHttpErrorCodeReturnedEquals(final String httpErrorCodeReturnedEquals) {
    this.httpErrorCodeReturnedEquals = httpErrorCodeReturnedEquals;
  }

  public String getKeyPrefixEquals() {
    return keyPrefixEquals;
  }

  public void setKeyPrefixEquals(final String keyPrefixEquals) {
    this.keyPrefixEquals = keyPrefixEquals;
  }
}
