// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.String;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.EnumGenerator")
public enum Event {
    S3_REDUCED_REDUNDANCY_LOST_OBJECT("software.amazon.smithy.crt.codegen.Field@3e3ec994"),

    S3_OBJECT_CREATED("software.amazon.smithy.crt.codegen.Field@3e3ec994"),

    S3_OBJECT_CREATED_PUT("software.amazon.smithy.crt.codegen.Field@3e3ec994"),

    S3_OBJECT_CREATED_POST("software.amazon.smithy.crt.codegen.Field@3e3ec994"),

    S3_OBJECT_CREATED_COPY("software.amazon.smithy.crt.codegen.Field@3e3ec994"),

    S3_OBJECT_CREATED_COMPLETE_MULTIPART_UPLOAD("software.amazon.smithy.crt.codegen.Field@3e3ec994"),

    S3_OBJECT_REMOVED("software.amazon.smithy.crt.codegen.Field@3e3ec994"),

    S3_OBJECT_REMOVED_DELETE("software.amazon.smithy.crt.codegen.Field@3e3ec994"),

    S3_OBJECT_REMOVED_DELETE_MARKER_CREATED("software.amazon.smithy.crt.codegen.Field@3e3ec994"),

    S3_OBJECT_RESTORE("software.amazon.smithy.crt.codegen.Field@3e3ec994"),

    S3_OBJECT_RESTORE_POST("software.amazon.smithy.crt.codegen.Field@3e3ec994"),

    S3_OBJECT_RESTORE_COMPLETED("software.amazon.smithy.crt.codegen.Field@3e3ec994"),

    S3_REPLICATION("software.amazon.smithy.crt.codegen.Field@3e3ec994"),

    S3_REPLICATION_OPERATION_FAILED_REPLICATION("software.amazon.smithy.crt.codegen.Field@3e3ec994"),

    S3_REPLICATION_OPERATION_NOT_TRACKED("software.amazon.smithy.crt.codegen.Field@3e3ec994"),

    S3_REPLICATION_OPERATION_MISSED_THRESHOLD("software.amazon.smithy.crt.codegen.Field@3e3ec994"),

    S3_REPLICATION_OPERATION_REPLICATED_AFTER_THRESHOLD("software.amazon.smithy.crt.codegen.Field@3e3ec994"),

    UNKNOWN_TO_SDK_VERSION(null);

    String value;

    private Event(String value) {
        this.value = value;
    }

    public static Event fromValue(String value) {
        if (value == null) {
            return null;
        }
        return Stream.of(com.amazonaws.s3.model.Event.values()).filter(e -> e.toString().equals(value)).findFirst().orElse(UNKNOWN_TO_SDK_VERSION);
    }

    public static Set<Event> knownValues() {
        return Stream.of(values()).filter(v -> v != UNKNOWN_TO_SDK_VERSION).collect(Collectors.toSet());
    }

    public String value() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }
}
