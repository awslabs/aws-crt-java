// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.String;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.EnumGenerator")
enum BucketLocationConstraint {
    AF_SOUTH_1("software.amazon.smithy.crt.codegen.Field@101ec0be"),

    AP_EAST_1("software.amazon.smithy.crt.codegen.Field@101ec0be"),

    AP_NORTHEAST_1("software.amazon.smithy.crt.codegen.Field@101ec0be"),

    AP_NORTHEAST_2("software.amazon.smithy.crt.codegen.Field@101ec0be"),

    AP_NORTHEAST_3("software.amazon.smithy.crt.codegen.Field@101ec0be"),

    AP_SOUTH_1("software.amazon.smithy.crt.codegen.Field@101ec0be"),

    AP_SOUTHEAST_1("software.amazon.smithy.crt.codegen.Field@101ec0be"),

    AP_SOUTHEAST_2("software.amazon.smithy.crt.codegen.Field@101ec0be"),

    CA_CENTRAL_1("software.amazon.smithy.crt.codegen.Field@101ec0be"),

    CN_NORTH_1("software.amazon.smithy.crt.codegen.Field@101ec0be"),

    CN_NORTHWEST_1("software.amazon.smithy.crt.codegen.Field@101ec0be"),

    EU("software.amazon.smithy.crt.codegen.Field@101ec0be"),

    EU_CENTRAL_1("software.amazon.smithy.crt.codegen.Field@101ec0be"),

    EU_NORTH_1("software.amazon.smithy.crt.codegen.Field@101ec0be"),

    EU_SOUTH_1("software.amazon.smithy.crt.codegen.Field@101ec0be"),

    EU_WEST_1("software.amazon.smithy.crt.codegen.Field@101ec0be"),

    EU_WEST_2("software.amazon.smithy.crt.codegen.Field@101ec0be"),

    EU_WEST_3("software.amazon.smithy.crt.codegen.Field@101ec0be"),

    ME_SOUTH_1("software.amazon.smithy.crt.codegen.Field@101ec0be"),

    SA_EAST_1("software.amazon.smithy.crt.codegen.Field@101ec0be"),

    US_EAST_2("software.amazon.smithy.crt.codegen.Field@101ec0be"),

    US_GOV_EAST_1("software.amazon.smithy.crt.codegen.Field@101ec0be"),

    US_GOV_WEST_1("software.amazon.smithy.crt.codegen.Field@101ec0be"),

    US_WEST_1("software.amazon.smithy.crt.codegen.Field@101ec0be"),

    US_WEST_2("software.amazon.smithy.crt.codegen.Field@101ec0be"),

    UNKNOWN_TO_SDK_VERSION(null);

    String value;

    private BucketLocationConstraint(String value) {
        this.value = value;
    }

    public static BucketLocationConstraint fromValue(String value) {
        if (value == null) {
            return null;
        }
        return Stream.of(com.amazonaws.s3.model.BucketLocationConstraint.values()).filter(e -> e.toString().equals(value)).findFirst().orElse(UNKNOWN_TO_SDK_VERSION);
    }

    public static Set<BucketLocationConstraint> knownValues() {
        return Stream.of(values()).filter(v -> v != UNKNOWN_TO_SDK_VERSION).collect(Collectors.toSet());
    }

    public String value() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }
}
