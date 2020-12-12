// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ObjectLockLegalHold {
    private ObjectLockLegalHoldStatus status;

    private ObjectLockLegalHold() {
        this.status = null;
    }

    private ObjectLockLegalHold(Builder builder) {
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
        return Objects.hash(ObjectLockLegalHold.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ObjectLockLegalHold);
    }

    public ObjectLockLegalHoldStatus status() {
        return status;
    }

    public void setStatus(final ObjectLockLegalHoldStatus status) {
        this.status = status;
    }

    static final class Builder {
        private ObjectLockLegalHoldStatus status;

        private Builder() {
        }

        private Builder(ObjectLockLegalHold model) {
            status(model.status);
        }

        public ObjectLockLegalHold build() {
            return new com.amazonaws.s3.model.ObjectLockLegalHold(this);
        }

        /**
         * <p>Indicates whether the specified object has a Legal Hold in place.</p>
         */
        public final Builder status(ObjectLockLegalHoldStatus status) {
            this.status = status;
            return this;
        }
    }
}
