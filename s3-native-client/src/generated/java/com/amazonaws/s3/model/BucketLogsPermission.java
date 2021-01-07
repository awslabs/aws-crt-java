// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.String;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.EnumGenerator")
enum BucketLogsPermission {
    FULL_CONTROL("software.amazon.smithy.crt.codegen.Field@6de8027d"),

    READ("software.amazon.smithy.crt.codegen.Field@6de8027d"),

    WRITE("software.amazon.smithy.crt.codegen.Field@6de8027d"),

    UNKNOWN_TO_SDK_VERSION(null);

    String value;

    private BucketLogsPermission(String value) {
        this.value = value;
    }

    public static BucketLogsPermission fromValue(String value) {
        if (value == null) {
            return null;
        }
        return Stream.of(com.amazonaws.s3.model.BucketLogsPermission.values()).filter(e -> e.toString().equals(value)).findFirst().orElse(UNKNOWN_TO_SDK_VERSION);
    }

    public static Set<BucketLogsPermission> knownValues() {
        return Stream.of(values()).filter(v -> v != UNKNOWN_TO_SDK_VERSION).collect(Collectors.toSet());
    }

    public String value() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }
}
