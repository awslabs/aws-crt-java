// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.String;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.EnumGenerator")
enum ObjectStorageClass {
    STANDARD("software.amazon.smithy.crt.codegen.Field@5a74a88c"),

    REDUCED_REDUNDANCY("software.amazon.smithy.crt.codegen.Field@5a74a88c"),

    GLACIER("software.amazon.smithy.crt.codegen.Field@5a74a88c"),

    STANDARD_IA("software.amazon.smithy.crt.codegen.Field@5a74a88c"),

    ONEZONE_IA("software.amazon.smithy.crt.codegen.Field@5a74a88c"),

    INTELLIGENT_TIERING("software.amazon.smithy.crt.codegen.Field@5a74a88c"),

    DEEP_ARCHIVE("software.amazon.smithy.crt.codegen.Field@5a74a88c"),

    OUTPOSTS("software.amazon.smithy.crt.codegen.Field@5a74a88c"),

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
