package com.amazonaws.s3;

import static java.util.stream.Collectors.toSet;

import java.lang.String;
import java.util.Set;
import java.util.stream.Stream;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.EnumGenerator")
public enum EncodingType {
  URL("url"),

  UNKNOWN_TO_SDK_VERSION("UNKNOWN_TO_SDK_VERSION");

  private final String value;

  private EncodingType(String value) {
    this.value = value;
  }

  public static EncodingType fromValue(String value) {
    if (value == null) {
      return null;
    }
    return Stream.of(com.amazonaws.s3.EncodingType.values()).filter(e -> e.toString().equals(value)).findFirst().orElse(UNKNOWN_TO_SDK_VERSION);
  }

  public static Set<EncodingType> knownValues() {
    return Stream.of(values()).filter(v -> v != UNKNOWN_TO_SDK_VERSION).collect(toSet());
  }
}
