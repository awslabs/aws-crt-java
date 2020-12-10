package com.amazonaws.s3;

import static java.util.stream.Collectors.toSet;

import java.lang.String;
import java.util.Set;
import java.util.stream.Stream;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.EnumGenerator")
public enum BucketCannedACL {
  PRIVATE("private"),

  PUBLIC_READ("public-read"),

  PUBLIC_READ_WRITE("public-read-write"),

  AUTHENTICATED_READ("authenticated-read"),

  UNKNOWN_TO_SDK_VERSION("UNKNOWN_TO_SDK_VERSION");

  private final String value;

  private BucketCannedACL(String value) {
    this.value = value;
  }

  public static BucketCannedACL fromValue(String value) {
    if (value == null) {
      return null;
    }
    return Stream.of(com.amazonaws.s3.BucketCannedACL.values()).filter(e -> e.toString().equals(value)).findFirst().orElse(UNKNOWN_TO_SDK_VERSION);
  }

  public static Set<BucketCannedACL> knownValues() {
    return Stream.of(values()).filter(v -> v != UNKNOWN_TO_SDK_VERSION).collect(toSet());
  }
}
