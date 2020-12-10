package com.amazonaws.s3;

import static java.util.stream.Collectors.toSet;

import java.lang.String;
import java.util.Set;
import java.util.stream.Stream;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.EnumGenerator")
public enum RestoreRequestType {
  SELECT("SELECT"),

  UNKNOWN_TO_SDK_VERSION("UNKNOWN_TO_SDK_VERSION");

  private final String value;

  private RestoreRequestType(String value) {
    this.value = value;
  }

  public static RestoreRequestType fromValue(String value) {
    if (value == null) {
      return null;
    }
    return Stream.of(com.amazonaws.s3.RestoreRequestType.values()).filter(e -> e.toString().equals(value)).findFirst().orElse(UNKNOWN_TO_SDK_VERSION);
  }

  public static Set<RestoreRequestType> knownValues() {
    return Stream.of(values()).filter(v -> v != UNKNOWN_TO_SDK_VERSION).collect(toSet());
  }
}
