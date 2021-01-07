// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.String;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.EnumGenerator")
enum InventoryOptionalField {
    SIZE("software.amazon.smithy.crt.codegen.Field@60c62c7f"),

    LAST_MODIFIED_DATE("software.amazon.smithy.crt.codegen.Field@60c62c7f"),

    STORAGE_CLASS("software.amazon.smithy.crt.codegen.Field@60c62c7f"),

    E_TAG("software.amazon.smithy.crt.codegen.Field@60c62c7f"),

    IS_MULTIPART_UPLOADED("software.amazon.smithy.crt.codegen.Field@60c62c7f"),

    REPLICATION_STATUS("software.amazon.smithy.crt.codegen.Field@60c62c7f"),

    ENCRYPTION_STATUS("software.amazon.smithy.crt.codegen.Field@60c62c7f"),

    OBJECT_LOCK_RETAIN_UNTIL_DATE("software.amazon.smithy.crt.codegen.Field@60c62c7f"),

    OBJECT_LOCK_MODE("software.amazon.smithy.crt.codegen.Field@60c62c7f"),

    OBJECT_LOCK_LEGAL_HOLD_STATUS("software.amazon.smithy.crt.codegen.Field@60c62c7f"),

    INTELLIGENT_TIERING_ACCESS_TIER("software.amazon.smithy.crt.codegen.Field@60c62c7f"),

    UNKNOWN_TO_SDK_VERSION(null);

    String value;

    private InventoryOptionalField(String value) {
        this.value = value;
    }

    public static InventoryOptionalField fromValue(String value) {
        if (value == null) {
            return null;
        }
        return Stream.of(com.amazonaws.s3.model.InventoryOptionalField.values()).filter(e -> e.toString().equals(value)).findFirst().orElse(UNKNOWN_TO_SDK_VERSION);
    }

    public static Set<InventoryOptionalField> knownValues() {
        return Stream.of(values()).filter(v -> v != UNKNOWN_TO_SDK_VERSION).collect(Collectors.toSet());
    }

    public String value() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }
}
