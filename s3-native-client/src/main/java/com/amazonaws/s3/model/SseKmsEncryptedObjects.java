// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class SseKmsEncryptedObjects {
    /**
     * <p>Specifies whether Amazon S3 replicates objects created with server-side encryption using a
     *          customer master key (CMK) stored in AWS Key Management Service.</p>
     */
    SseKmsEncryptedObjectsStatus status;

    SseKmsEncryptedObjects() {
        this.status = null;
    }

    protected SseKmsEncryptedObjects(BuilderImpl builder) {
        this.status = builder.status;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public interface Builder {
        Builder status(SseKmsEncryptedObjectsStatus status);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Specifies whether Amazon S3 replicates objects created with server-side encryption using a
         *          customer master key (CMK) stored in AWS Key Management Service.</p>
         */
        SseKmsEncryptedObjectsStatus status;

        protected BuilderImpl() {
        }

        private BuilderImpl(SseKmsEncryptedObjects model) {
            status(model.status);
        }

        public SseKmsEncryptedObjects build() {
            return new SseKmsEncryptedObjects(this);
        }

        public final Builder status(SseKmsEncryptedObjectsStatus status) {
            this.status = status;
            return this;
        }

        @Override
        public int hashCode() {
            return Objects.hash(BuilderImpl.class);
        }

        @Override
        public boolean equals(Object rhs) {
            if (rhs == null) return false;
            return (rhs instanceof BuilderImpl);
        }

        public SseKmsEncryptedObjectsStatus status() {
            return status;
        }

        public void setStatus(final SseKmsEncryptedObjectsStatus status) {
            this.status = status;
        }
    }
}
