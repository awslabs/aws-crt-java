// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Tiering {
    /**
     * <p>The number of consecutive days of no access after which an object will be eligible to be
     *          transitioned to the corresponding tier. The minimum number of days specified for
     *          Archive Access tier must be at least 90 days and Deep Archive Access tier must be at least
     *          180 days. The maximum can be up to 2 years (730 days).</p>
     */
    Integer days;

    /**
     * <p>S3 Intelligent-Tiering access tier. See <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/storage-class-intro.html#sc-dynamic-data-access">Storage class for
     *             automatically optimizing frequently and infrequently accessed objects</a> for a list
     *          of access tiers in the S3 Intelligent-Tiering storage class.</p>
     */
    IntelligentTieringAccessTier accessTier;

    Tiering() {
        this.days = null;
        this.accessTier = null;
    }

    protected Tiering(BuilderImpl builder) {
        this.days = builder.days;
        this.accessTier = builder.accessTier;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public IntelligentTieringAccessTier accessTier() {
        return accessTier;
    }

    public interface Builder {
        Builder days(Integer days);

        Builder accessTier(IntelligentTieringAccessTier accessTier);

        Tiering build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The number of consecutive days of no access after which an object will be eligible to be
         *          transitioned to the corresponding tier. The minimum number of days specified for
         *          Archive Access tier must be at least 90 days and Deep Archive Access tier must be at least
         *          180 days. The maximum can be up to 2 years (730 days).</p>
         */
        Integer days;

        /**
         * <p>S3 Intelligent-Tiering access tier. See <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/storage-class-intro.html#sc-dynamic-data-access">Storage class for
         *             automatically optimizing frequently and infrequently accessed objects</a> for a list
         *          of access tiers in the S3 Intelligent-Tiering storage class.</p>
         */
        IntelligentTieringAccessTier accessTier;

        protected BuilderImpl() {
        }

        private BuilderImpl(Tiering model) {
            days(model.days);
            accessTier(model.accessTier);
        }

        public Tiering build() {
            return new Tiering(this);
        }

        public final Builder days(Integer days) {
            this.days = days;
            return this;
        }

        public final Builder accessTier(IntelligentTieringAccessTier accessTier) {
            this.accessTier = accessTier;
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

        public Integer days() {
            return days;
        }

        public IntelligentTieringAccessTier accessTier() {
            return accessTier;
        }
    }
}
