package com.amazonaws.s3;

import static java.util.stream.Collectors.toSet;

import java.lang.String;
import java.util.Set;
import java.util.stream.Stream;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.EnumGenerator")
public enum StorageClass {
  STANDARD("STANDARD"),

  REDUCED_REDUNDANCY("REDUCED_REDUNDANCY"),

  STANDARD_IA("STANDARD_IA"),

  ONEZONE_IA("ONEZONE_IA"),

  INTELLIGENT_TIERING("INTELLIGENT_TIERING"),

  GLACIER("GLACIER"),

  DEEP_ARCHIVE("DEEP_ARCHIVE"),

  OUTPOSTS("OUTPOSTS"),

  UNKNOWN_TO_SDK_VERSION("UNKNOWN_TO_SDK_VERSION");

  private final String value;

  private StorageClass(String value) {
    this.value = value;
  }

  public static StorageClass fromValue(String value) {
    if (value == null) {
      return null;
    }
    return Stream.of(com.amazonaws.s3.StorageClass.values()).filter(e -> e.toString().equals(value)).findFirst().orElse(UNKNOWN_TO_SDK_VERSION);
  }

  public static Set<StorageClass> knownValues() {
    return Stream.of(values()).filter(v -> v != UNKNOWN_TO_SDK_VERSION).collect(toSet());
  }
}
