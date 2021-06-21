// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.time.Instant;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class LifecycleExpiration {
    /**
     * <p>Indicates at what date the object is to be moved or deleted. Should be in GMT ISO 8601
     *          Format.</p>
     */
    Instant date;

    /**
     * <p>Indicates the lifetime, in days, of the objects that are subject to the rule. The value
     *          must be a non-zero positive integer.</p>
     */
    Integer days;

    /**
     * <p>Indicates whether Amazon S3 will remove a delete marker with no noncurrent versions. If set
     *          to true, the delete marker will be expired; if set to false the policy takes no action.
     *          This cannot be specified with Days or Date in a Lifecycle Expiration Policy.</p>
     */
    Boolean expiredObjectDeleteMarker;

    LifecycleExpiration() {
        this.date = null;
        this.days = null;
        this.expiredObjectDeleteMarker = null;
    }

    protected LifecycleExpiration(BuilderImpl builder) {
        this.date = builder.date;
        this.days = builder.days;
        this.expiredObjectDeleteMarker = builder.expiredObjectDeleteMarker;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(LifecycleExpiration.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof LifecycleExpiration);
    }

    public Instant date() {
        return date;
    }

    public Integer days() {
        return days;
    }

    public Boolean expiredObjectDeleteMarker() {
        return expiredObjectDeleteMarker;
    }

    public interface Builder {
        Builder date(Instant date);

        Builder days(Integer days);

        Builder expiredObjectDeleteMarker(Boolean expiredObjectDeleteMarker);

        LifecycleExpiration build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Indicates at what date the object is to be moved or deleted. Should be in GMT ISO 8601
         *          Format.</p>
         */
        Instant date;

        /**
         * <p>Indicates the lifetime, in days, of the objects that are subject to the rule. The value
         *          must be a non-zero positive integer.</p>
         */
        Integer days;

        /**
         * <p>Indicates whether Amazon S3 will remove a delete marker with no noncurrent versions. If set
         *          to true, the delete marker will be expired; if set to false the policy takes no action.
         *          This cannot be specified with Days or Date in a Lifecycle Expiration Policy.</p>
         */
        Boolean expiredObjectDeleteMarker;

        protected BuilderImpl() {
        }

        private BuilderImpl(LifecycleExpiration model) {
            date(model.date);
            days(model.days);
            expiredObjectDeleteMarker(model.expiredObjectDeleteMarker);
        }

        public LifecycleExpiration build() {
            return new LifecycleExpiration(this);
        }

        public final Builder date(Instant date) {
            this.date = date;
            return this;
        }

        public final Builder days(Integer days) {
            this.days = days;
            return this;
        }

        public final Builder expiredObjectDeleteMarker(Boolean expiredObjectDeleteMarker) {
            this.expiredObjectDeleteMarker = expiredObjectDeleteMarker;
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

        public Boolean expiredObjectDeleteMarker() {
            return expiredObjectDeleteMarker;
        }
    }
}
