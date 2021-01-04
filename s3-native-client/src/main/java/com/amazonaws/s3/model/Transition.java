// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.time.Instant;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Transition {
    /**
     * <p>Indicates when objects are transitioned to the specified storage class. The date value
     *          must be in ISO 8601 format. The time is always midnight UTC.</p>
     */
    Instant date;

    /**
     * <p>Indicates the number of days after creation when objects are transitioned to the
     *          specified storage class. The value must be a positive integer.</p>
     */
    Integer days;

    /**
     * <p>The storage class to which you want the object to transition.</p>
     */
    TransitionStorageClass storageClass;

    Transition() {
        this.date = null;
        this.days = null;
        this.storageClass = null;
    }

    protected Transition(BuilderImpl builder) {
        this.date = builder.date;
        this.days = builder.days;
        this.storageClass = builder.storageClass;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(Transition.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof Transition);
    }

    public Instant date() {
        return date;
    }

    public Integer days() {
        return days;
    }

    public TransitionStorageClass storageClass() {
        return storageClass;
    }

    public void setDate(final Instant date) {
        this.date = date;
    }

    public void setDays(final Integer days) {
        this.days = days;
    }

    public void setStorageClass(final TransitionStorageClass storageClass) {
        this.storageClass = storageClass;
    }

    public interface Builder {
        Builder date(Instant date);

        Builder days(Integer days);

        Builder storageClass(TransitionStorageClass storageClass);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Indicates when objects are transitioned to the specified storage class. The date value
         *          must be in ISO 8601 format. The time is always midnight UTC.</p>
         */
        Instant date;

        /**
         * <p>Indicates the number of days after creation when objects are transitioned to the
         *          specified storage class. The value must be a positive integer.</p>
         */
        Integer days;

        /**
         * <p>The storage class to which you want the object to transition.</p>
         */
        TransitionStorageClass storageClass;

        protected BuilderImpl() {
        }

        private BuilderImpl(Transition model) {
            date(model.date);
            days(model.days);
            storageClass(model.storageClass);
        }

        public Transition build() {
            return new Transition(this);
        }

        public final Builder date(Instant date) {
            this.date = date;
            return this;
        }

        public final Builder days(Integer days) {
            this.days = days;
            return this;
        }

        public final Builder storageClass(TransitionStorageClass storageClass) {
            this.storageClass = storageClass;
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

        public Instant date() {
            return date;
        }

        public Integer days() {
            return days;
        }

        public TransitionStorageClass storageClass() {
            return storageClass;
        }

        public void setDate(final Instant date) {
            this.date = date;
        }

        public void setDays(final Integer days) {
            this.days = days;
        }

        public void setStorageClass(final TransitionStorageClass storageClass) {
            this.storageClass = storageClass;
        }
    }
}
