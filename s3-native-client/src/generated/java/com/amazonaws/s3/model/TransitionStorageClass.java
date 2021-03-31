// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.String;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.EnumGenerator")
public enum TransitionStorageClass {
    GLACIER("software.amazon.smithy.crt.codegen.Field@4504719b"),

    STANDARD_IA("software.amazon.smithy.crt.codegen.Field@4504719b"),

    ONEZONE_IA("software.amazon.smithy.crt.codegen.Field@4504719b"),

    INTELLIGENT_TIERING("software.amazon.smithy.crt.codegen.Field@4504719b"),

    DEEP_ARCHIVE("software.amazon.smithy.crt.codegen.Field@4504719b"),

    UNKNOWN_TO_SDK_VERSION(null);

    String value;

    private TransitionStorageClass(String value) {
        this.value = value;
    }

    public static TransitionStorageClass fromValue(String value) {
        if (value == null) {
            return null;
        }
        return Stream.of(com.amazonaws.s3.model.TransitionStorageClass.values()).filter(e -> e.toString().equals(value)).findFirst().orElse(UNKNOWN_TO_SDK_VERSION);
    }

    public static Set<TransitionStorageClass> knownValues() {
        return Stream.of(values()).filter(v -> v != UNKNOWN_TO_SDK_VERSION).collect(Collectors.toSet());
    }

    public String value() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }
}
