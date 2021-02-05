// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.String;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.EnumGenerator")
public enum ObjectStorageClass {
    STANDARD("software.amazon.smithy.crt.codegen.Field@75319d17"),

    REDUCED_REDUNDANCY("software.amazon.smithy.crt.codegen.Field@75319d17"),

    GLACIER("software.amazon.smithy.crt.codegen.Field@75319d17"),

    STANDARD_IA("software.amazon.smithy.crt.codegen.Field@75319d17"),

    ONEZONE_IA("software.amazon.smithy.crt.codegen.Field@75319d17"),

    INTELLIGENT_TIERING("software.amazon.smithy.crt.codegen.Field@75319d17"),

    DEEP_ARCHIVE("software.amazon.smithy.crt.codegen.Field@75319d17"),

    OUTPOSTS("software.amazon.smithy.crt.codegen.Field@75319d17"),

    UNKNOWN_TO_SDK_VERSION(null);

    String value;

    private ObjectStorageClass(String value) {
        this.value = value;
    }

    public static ObjectStorageClass fromValue(String value) {
        if (value == null) {
            return null;
        }
        return Stream.of(com.amazonaws.s3.model.ObjectStorageClass.values()).filter(e -> e.toString().equals(value)).findFirst().orElse(UNKNOWN_TO_SDK_VERSION);
    }

    public static Set<ObjectStorageClass> knownValues() {
        return Stream.of(values()).filter(v -> v != UNKNOWN_TO_SDK_VERSION).collect(Collectors.toSet());
    }

    public String value() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }
}
