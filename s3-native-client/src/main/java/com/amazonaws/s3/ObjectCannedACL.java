package com.amazonaws.s3;

import static java.util.stream.Collectors.toSet;

import java.lang.String;
import java.util.Set;
import java.util.stream.Stream;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.EnumGenerator")
public enum ObjectCannedACL {
  PRIVATE("private"),

  PUBLIC_READ("public-read"),

  PUBLIC_READ_WRITE("public-read-write"),

  AUTHENTICATED_READ("authenticated-read"),

  AWS_EXEC_READ("aws-exec-read"),

  BUCKET_OWNER_READ("bucket-owner-read"),

  BUCKET_OWNER_FULL_CONTROL("bucket-owner-full-control"),

  UNKNOWN_TO_SDK_VERSION("UNKNOWN_TO_SDK_VERSION");

  private final String value;

  private ObjectCannedACL(String value) {
    this.value = value;
  }

  public static ObjectCannedACL fromValue(String value) {
    if (value == null) {
      return null;
    }
    return Stream.of(com.amazonaws.s3.ObjectCannedACL.values()).filter(e -> e.toString().equals(value)).findFirst().orElse(UNKNOWN_TO_SDK_VERSION);
  }

  public static Set<ObjectCannedACL> knownValues() {
    return Stream.of(values()).filter(v -> v != UNKNOWN_TO_SDK_VERSION).collect(toSet());
  }
}
