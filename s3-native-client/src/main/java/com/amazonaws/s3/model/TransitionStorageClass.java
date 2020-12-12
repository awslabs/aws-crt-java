// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.String;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.EnumGenerator")
public enum TransitionStorageClass {
    GLACIER("GLACIER"),

    STANDARD_IA("STANDARD_IA"),

    ONEZONE_IA("ONEZONE_IA"),

    INTELLIGENT_TIERING("INTELLIGENT_TIERING"),

    DEEP_ARCHIVE("DEEP_ARCHIVE"),

    UNKNOWN_TO_SDK_VERSION("UNKNOWN_TO_SDK_VERSION");

    private final String value;

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
}
