// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.String;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.EnumGenerator")
public enum ReplicaModificationsStatus {
    ENABLED("software.amazon.smithy.crt.codegen.Field@686c8aa"),

    DISABLED("software.amazon.smithy.crt.codegen.Field@686c8aa"),

    UNKNOWN_TO_SDK_VERSION(null);

    String value;

    private ReplicaModificationsStatus(String value) {
        this.value = value;
    }

    public static ReplicaModificationsStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        return Stream.of(com.amazonaws.s3.model.ReplicaModificationsStatus.values()).filter(e -> e.toString().equals(value)).findFirst().orElse(UNKNOWN_TO_SDK_VERSION);
    }

    public static Set<ReplicaModificationsStatus> knownValues() {
        return Stream.of(values()).filter(v -> v != UNKNOWN_TO_SDK_VERSION).collect(Collectors.toSet());
    }

    public String value() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }
}
