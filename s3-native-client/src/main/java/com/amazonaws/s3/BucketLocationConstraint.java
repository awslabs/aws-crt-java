package com.amazonaws.s3;

import static java.util.stream.Collectors.toSet;

import java.lang.String;
import java.util.Set;
import java.util.stream.Stream;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.EnumGenerator")
public enum BucketLocationConstraint {
  AF_SOUTH_1("af-south-1"),

  AP_EAST_1("ap-east-1"),

  AP_NORTHEAST_1("ap-northeast-1"),

  AP_NORTHEAST_2("ap-northeast-2"),

  AP_NORTHEAST_3("ap-northeast-3"),

  AP_SOUTH_1("ap-south-1"),

  AP_SOUTHEAST_1("ap-southeast-1"),

  AP_SOUTHEAST_2("ap-southeast-2"),

  CA_CENTRAL_1("ca-central-1"),

  CN_NORTH_1("cn-north-1"),

  CN_NORTHWEST_1("cn-northwest-1"),

  EU("EU"),

  EU_CENTRAL_1("eu-central-1"),

  EU_NORTH_1("eu-north-1"),

  EU_SOUTH_1("eu-south-1"),

  EU_WEST_1("eu-west-1"),

  EU_WEST_2("eu-west-2"),

  EU_WEST_3("eu-west-3"),

  ME_SOUTH_1("me-south-1"),

  SA_EAST_1("sa-east-1"),

  US_EAST_2("us-east-2"),

  US_GOV_EAST_1("us-gov-east-1"),

  US_GOV_WEST_1("us-gov-west-1"),

  US_WEST_1("us-west-1"),

  US_WEST_2("us-west-2"),

  UNKNOWN_TO_SDK_VERSION("UNKNOWN_TO_SDK_VERSION");

  private final String value;

  private BucketLocationConstraint(String value) {
    this.value = value;
  }

  public static BucketLocationConstraint fromValue(String value) {
    if (value == null) {
      return null;
    }
    return Stream.of(com.amazonaws.s3.BucketLocationConstraint.values()).filter(e -> e.toString().equals(value)).findFirst().orElse(UNKNOWN_TO_SDK_VERSION);
  }

  public static Set<BucketLocationConstraint> knownValues() {
    return Stream.of(values()).filter(v -> v != UNKNOWN_TO_SDK_VERSION).collect(toSet());
  }
}
