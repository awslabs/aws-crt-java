// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.String;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.EnumGenerator")
public enum ObjectLockRetentionMode {
    GOVERNANCE("GOVERNANCE"),

    COMPLIANCE("COMPLIANCE"),

    UNKNOWN_TO_SDK_VERSION("UNKNOWN_TO_SDK_VERSION");

    private final String value;

    private ObjectLockRetentionMode(String value) {
        this.value = value;
    }

    public static ObjectLockRetentionMode fromValue(String value) {
        if (value == null) {
            return null;
        }
        return Stream.of(com.amazonaws.s3.model.ObjectLockRetentionMode.values()).filter(e -> e.toString().equals(value)).findFirst().orElse(UNKNOWN_TO_SDK_VERSION);
    }

    public static Set<ObjectLockRetentionMode> knownValues() {
        return Stream.of(values()).filter(v -> v != UNKNOWN_TO_SDK_VERSION).collect(Collectors.toSet());
    }
}
