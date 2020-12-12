// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Tiering {
    private Integer days;

    private IntelligentTieringAccessTier accessTier;

    private Tiering() {
        this.days = null;
        this.accessTier = null;
    }

    private Tiering(Builder builder) {
        this.days = builder.days;
        this.accessTier = builder.accessTier;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Tiering.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof Tiering);
    }

    public Integer days() {
        return days;
    }

    public void setDays(final Integer days) {
        this.days = days;
    }

    public IntelligentTieringAccessTier accessTier() {
        return accessTier;
    }

    public void setAccessTier(final IntelligentTieringAccessTier accessTier) {
        this.accessTier = accessTier;
    }

    static final class Builder {
        private Integer days;

        private IntelligentTieringAccessTier accessTier;

        private Builder() {
        }

        private Builder(Tiering model) {
            days(model.days);
            accessTier(model.accessTier);
        }

        public Tiering build() {
            return new com.amazonaws.s3.model.Tiering(this);
        }

        /**
         * <p>The number of consecutive days of no access after which an object will be eligible to be
         *          transitioned to the corresponding tier. The minimum number of days specified for
         *          Archive Access tier must be at least 90 days and Deep Archive Access tier must be at least
         *          180 days. The maximum can be up to 2 years (730 days).</p>
         */
        public final Builder days(Integer days) {
            this.days = days;
            return this;
        }

        /**
         * <p>S3 Intelligent-Tiering access tier. See <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/storage-class-intro.html#sc-dynamic-data-access">Storage class for
         *             automatically optimizing frequently and infrequently accessed objects</a> for a list
         *          of access tiers in the S3 Intelligent-Tiering storage class.</p>
         */
        public final Builder accessTier(IntelligentTieringAccessTier accessTier) {
            this.accessTier = accessTier;
            return this;
        }
    }
}
