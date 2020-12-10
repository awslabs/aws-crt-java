package com.amazonaws.s3;

import static java.util.stream.Collectors.toSet;

import java.lang.String;
import java.util.Set;
import java.util.stream.Stream;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.EnumGenerator")
public enum StorageClassAnalysisSchemaVersion {
  V_1("V_1"),

  UNKNOWN_TO_SDK_VERSION("UNKNOWN_TO_SDK_VERSION");

  private final String value;

  private StorageClassAnalysisSchemaVersion(String value) {
    this.value = value;
  }

  public static StorageClassAnalysisSchemaVersion fromValue(String value) {
    if (value == null) {
      return null;
    }
    return Stream.of(com.amazonaws.s3.StorageClassAnalysisSchemaVersion.values()).filter(e -> e.toString().equals(value)).findFirst().orElse(UNKNOWN_TO_SDK_VERSION);
  }

  public static Set<StorageClassAnalysisSchemaVersion> knownValues() {
    return Stream.of(values()).filter(v -> v != UNKNOWN_TO_SDK_VERSION).collect(toSet());
  }
}
