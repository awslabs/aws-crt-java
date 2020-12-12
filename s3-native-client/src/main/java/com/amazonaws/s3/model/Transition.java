// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.time.Instant;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Transition {
    private Instant date;

    private Integer days;

    private TransitionStorageClass storageClass;

    private Transition() {
        this.date = null;
        this.days = null;
        this.storageClass = null;
    }

    private Transition(Builder builder) {
        this.date = builder.date;
        this.days = builder.days;
        this.storageClass = builder.storageClass;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setDate(final Instant date) {
        this.date = date;
    }

    public Integer days() {
        return days;
    }

    public void setDays(final Integer days) {
        this.days = days;
    }

    public TransitionStorageClass storageClass() {
        return storageClass;
    }

    public void setStorageClass(final TransitionStorageClass storageClass) {
        this.storageClass = storageClass;
    }

    static final class Builder {
        private Instant date;

        private Integer days;

        private TransitionStorageClass storageClass;

        private Builder() {
        }

        private Builder(Transition model) {
            date(model.date);
            days(model.days);
            storageClass(model.storageClass);
        }

        public Transition build() {
            return new com.amazonaws.s3.model.Transition(this);
        }

        /**
         * <p>Indicates when objects are transitioned to the specified storage class. The date value
         *          must be in ISO 8601 format. The time is always midnight UTC.</p>
         */
        public final Builder date(Instant date) {
            this.date = date;
            return this;
        }

        /**
         * <p>Indicates the number of days after creation when objects are transitioned to the
         *          specified storage class. The value must be a positive integer.</p>
         */
        public final Builder days(Integer days) {
            this.days = days;
            return this;
        }

        /**
         * <p>The storage class to which you want the object to transition.</p>
         */
        public final Builder storageClass(TransitionStorageClass storageClass) {
            this.storageClass = storageClass;
            return this;
        }
    }
}
