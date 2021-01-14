// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.String;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.EnumGenerator")
public enum StorageClass {
    STANDARD("software.amazon.smithy.crt.codegen.Field@3df75bb6"),

    REDUCED_REDUNDANCY("software.amazon.smithy.crt.codegen.Field@3df75bb6"),

    STANDARD_IA("software.amazon.smithy.crt.codegen.Field@3df75bb6"),

    ONEZONE_IA("software.amazon.smithy.crt.codegen.Field@3df75bb6"),

    INTELLIGENT_TIERING("software.amazon.smithy.crt.codegen.Field@3df75bb6"),

    GLACIER("software.amazon.smithy.crt.codegen.Field@3df75bb6"),

    DEEP_ARCHIVE("software.amazon.smithy.crt.codegen.Field@3df75bb6"),

    OUTPOSTS("software.amazon.smithy.crt.codegen.Field@3df75bb6"),

    UNKNOWN_TO_SDK_VERSION(null);

    String value;

    private StorageClass(String value) {
        this.value = value;
    }

    public static StorageClass fromValue(String value) {
        if (value == null) {
            return null;
        }
        return Stream.of(com.amazonaws.s3.model.StorageClass.values()).filter(e -> e.toString().equals(value)).findFirst().orElse(UNKNOWN_TO_SDK_VERSION);
    }

    public static Set<StorageClass> knownValues() {
        return Stream.of(values()).filter(v -> v != UNKNOWN_TO_SDK_VERSION).collect(Collectors.toSet());
    }

    public String value() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }
}
