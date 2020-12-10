package com.amazonaws.s3;

import static java.util.stream.Collectors.toSet;

import java.lang.String;
import java.util.Set;
import java.util.stream.Stream;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.EnumGenerator")
public enum IntelligentTieringAccessTier {
  ARCHIVE_ACCESS("ARCHIVE_ACCESS"),

  DEEP_ARCHIVE_ACCESS("DEEP_ARCHIVE_ACCESS"),

  UNKNOWN_TO_SDK_VERSION("UNKNOWN_TO_SDK_VERSION");

  private final String value;

  private IntelligentTieringAccessTier(String value) {
    this.value = value;
  }

  public static IntelligentTieringAccessTier fromValue(String value) {
    if (value == null) {
      return null;
    }
    return Stream.of(com.amazonaws.s3.IntelligentTieringAccessTier.values()).filter(e -> e.toString().equals(value)).findFirst().orElse(UNKNOWN_TO_SDK_VERSION);
  }

  public static Set<IntelligentTieringAccessTier> knownValues() {
    return Stream.of(values()).filter(v -> v != UNKNOWN_TO_SDK_VERSION).collect(toSet());
  }
}
