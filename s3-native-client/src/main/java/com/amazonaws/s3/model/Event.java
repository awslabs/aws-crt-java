// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.String;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.EnumGenerator")
public enum Event {
    S3_REDUCED_REDUNDANCY_LOST_OBJECT("s3:ReducedRedundancyLostObject"),

    S3_OBJECT_CREATED("s3:ObjectCreated:*"),

    S3_OBJECT_CREATED_PUT("s3:ObjectCreated:Put"),

    S3_OBJECT_CREATED_POST("s3:ObjectCreated:Post"),

    S3_OBJECT_CREATED_COPY("s3:ObjectCreated:Copy"),

    S3_OBJECT_CREATED_COMPLETE_MULTIPART_UPLOAD("s3:ObjectCreated:CompleteMultipartUpload"),

    S3_OBJECT_REMOVED("s3:ObjectRemoved:*"),

    S3_OBJECT_REMOVED_DELETE("s3:ObjectRemoved:Delete"),

    S3_OBJECT_REMOVED_DELETE_MARKER_CREATED("s3:ObjectRemoved:DeleteMarkerCreated"),

    S3_OBJECT_RESTORE("s3:ObjectRestore:*"),

    S3_OBJECT_RESTORE_POST("s3:ObjectRestore:Post"),

    S3_OBJECT_RESTORE_COMPLETED("s3:ObjectRestore:Completed"),

    S3_REPLICATION("s3:Replication:*"),

    S3_REPLICATION_OPERATION_FAILED_REPLICATION("s3:Replication:OperationFailedReplication"),

    S3_REPLICATION_OPERATION_NOT_TRACKED("s3:Replication:OperationNotTracked"),

    S3_REPLICATION_OPERATION_MISSED_THRESHOLD("s3:Replication:OperationMissedThreshold"),

    S3_REPLICATION_OPERATION_REPLICATED_AFTER_THRESHOLD("s3:Replication:OperationReplicatedAfterThreshold"),

    UNKNOWN_TO_SDK_VERSION("UNKNOWN_TO_SDK_VERSION");

    private final String value;

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
}
