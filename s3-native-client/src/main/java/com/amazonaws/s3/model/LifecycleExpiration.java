// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.time.Instant;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class LifecycleExpiration {
    private Instant date;

    private Integer days;

    private Boolean expiredObjectDeleteMarker;

    private LifecycleExpiration() {
        this.date = null;
        this.days = null;
        this.expiredObjectDeleteMarker = null;
    }

    private LifecycleExpiration(Builder builder) {
        this.date = builder.date;
        this.days = builder.days;
        this.expiredObjectDeleteMarker = builder.expiredObjectDeleteMarker;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setDate(final Instant date) {
        this.date = date;
    }

    public Integer days() {
        return days;
    }

    public void setDays(final Integer days) {
        this.days = days;
    }

    public Boolean expiredObjectDeleteMarker() {
        return expiredObjectDeleteMarker;
    }

    public void setExpiredObjectDeleteMarker(final Boolean expiredObjectDeleteMarker) {
        this.expiredObjectDeleteMarker = expiredObjectDeleteMarker;
    }

    static final class Builder {
        private Instant date;

        private Integer days;

        private Boolean expiredObjectDeleteMarker;

        private Builder() {
        }

        private Builder(LifecycleExpiration model) {
            date(model.date);
            days(model.days);
            expiredObjectDeleteMarker(model.expiredObjectDeleteMarker);
        }

        public LifecycleExpiration build() {
            return new com.amazonaws.s3.model.LifecycleExpiration(this);
        }

        /**
         * <p>Indicates at what date the object is to be moved or deleted. Should be in GMT ISO 8601
         *          Format.</p>
         */
        public final Builder date(Instant date) {
            this.date = date;
            return this;
        }

        /**
         * <p>Indicates the lifetime, in days, of the objects that are subject to the rule. The value
         *          must be a non-zero positive integer.</p>
         */
        public final Builder days(Integer days) {
            this.days = days;
            return this;
        }

        /**
         * <p>Indicates whether Amazon S3 will remove a delete marker with no noncurrent versions. If set
         *          to true, the delete marker will be expired; if set to false the policy takes no action.
         *          This cannot be specified with Days or Date in a Lifecycle Expiration Policy.</p>
         */
        public final Builder expiredObjectDeleteMarker(Boolean expiredObjectDeleteMarker) {
            this.expiredObjectDeleteMarker = expiredObjectDeleteMarker;
            return this;
        }
    }
}
