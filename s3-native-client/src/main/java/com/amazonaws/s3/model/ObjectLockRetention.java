// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.time.Instant;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ObjectLockRetention {
    private ObjectLockRetentionMode mode;

    private Instant retainUntilDate;

    private ObjectLockRetention() {
        this.mode = null;
        this.retainUntilDate = null;
    }

    private ObjectLockRetention(Builder builder) {
        this.mode = builder.mode;
        this.retainUntilDate = builder.retainUntilDate;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ObjectLockRetention.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ObjectLockRetention);
    }

    public ObjectLockRetentionMode mode() {
        return mode;
    }

    public void setMode(final ObjectLockRetentionMode mode) {
        this.mode = mode;
    }

    public Instant retainUntilDate() {
        return retainUntilDate;
    }

    public void setRetainUntilDate(final Instant retainUntilDate) {
        this.retainUntilDate = retainUntilDate;
    }

    static final class Builder {
        private ObjectLockRetentionMode mode;

        private Instant retainUntilDate;

        private Builder() {
        }

        private Builder(ObjectLockRetention model) {
            mode(model.mode);
            retainUntilDate(model.retainUntilDate);
        }

        public ObjectLockRetention build() {
            return new com.amazonaws.s3.model.ObjectLockRetention(this);
        }

        /**
         * <p>Indicates the Retention mode for the specified object.</p>
         */
        public final Builder mode(ObjectLockRetentionMode mode) {
            this.mode = mode;
            return this;
        }

        /**
         * <p>The date on which this Object Lock Retention will expire.</p>
         */
        public final Builder retainUntilDate(Instant retainUntilDate) {
            this.retainUntilDate = retainUntilDate;
            return this;
        }
    }
}
