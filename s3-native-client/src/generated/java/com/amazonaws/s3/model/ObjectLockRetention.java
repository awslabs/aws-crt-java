// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.time.Instant;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ObjectLockRetention {
    /**
     * <p>Indicates the Retention mode for the specified object.</p>
     */
    ObjectLockRetentionMode mode;

    /**
     * <p>The date on which this Object Lock Retention will expire.</p>
     */
    Instant retainUntilDate;

    ObjectLockRetention() {
        this.mode = null;
        this.retainUntilDate = null;
    }

    protected ObjectLockRetention(BuilderImpl builder) {
        this.mode = builder.mode;
        this.retainUntilDate = builder.retainUntilDate;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public Instant retainUntilDate() {
        return retainUntilDate;
    }

    public void setMode(final ObjectLockRetentionMode mode) {
        this.mode = mode;
    }

    public void setRetainUntilDate(final Instant retainUntilDate) {
        this.retainUntilDate = retainUntilDate;
    }

    public interface Builder {
        Builder mode(ObjectLockRetentionMode mode);

        Builder retainUntilDate(Instant retainUntilDate);

        ObjectLockRetention build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Indicates the Retention mode for the specified object.</p>
         */
        ObjectLockRetentionMode mode;

        /**
         * <p>The date on which this Object Lock Retention will expire.</p>
         */
        Instant retainUntilDate;

        protected BuilderImpl() {
        }

        private BuilderImpl(ObjectLockRetention model) {
            mode(model.mode);
            retainUntilDate(model.retainUntilDate);
        }

        public ObjectLockRetention build() {
            return new ObjectLockRetention(this);
        }

        public final Builder mode(ObjectLockRetentionMode mode) {
            this.mode = mode;
            return this;
        }

        public final Builder retainUntilDate(Instant retainUntilDate) {
            this.retainUntilDate = retainUntilDate;
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

        public ObjectLockRetentionMode mode() {
            return mode;
        }

        public Instant retainUntilDate() {
            return retainUntilDate;
        }

        public void setMode(final ObjectLockRetentionMode mode) {
            this.mode = mode;
        }

        public void setRetainUntilDate(final Instant retainUntilDate) {
            this.retainUntilDate = retainUntilDate;
        }
    }
}
