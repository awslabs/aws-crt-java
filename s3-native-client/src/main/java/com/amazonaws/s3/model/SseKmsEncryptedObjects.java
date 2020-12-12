// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class SseKmsEncryptedObjects {
    private SseKmsEncryptedObjectsStatus status;

    private SseKmsEncryptedObjects() {
        this.status = null;
    }

    private SseKmsEncryptedObjects(Builder builder) {
        this.status = builder.status;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(SseKmsEncryptedObjects.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof SseKmsEncryptedObjects);
    }

    public SseKmsEncryptedObjectsStatus status() {
        return status;
    }

    public void setStatus(final SseKmsEncryptedObjectsStatus status) {
        this.status = status;
    }

    static final class Builder {
        private SseKmsEncryptedObjectsStatus status;

        private Builder() {
        }

        private Builder(SseKmsEncryptedObjects model) {
            status(model.status);
        }

        public SseKmsEncryptedObjects build() {
            return new com.amazonaws.s3.model.SseKmsEncryptedObjects(this);
        }

        /**
         * <p>Specifies whether Amazon S3 replicates objects created with server-side encryption using a
         *          customer master key (CMK) stored in AWS Key Management Service.</p>
         */
        public final Builder status(SseKmsEncryptedObjectsStatus status) {
            this.status = status;
            return this;
        }
    }
}
