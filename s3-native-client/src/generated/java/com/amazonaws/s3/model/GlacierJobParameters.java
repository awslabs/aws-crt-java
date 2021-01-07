// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GlacierJobParameters {
    /**
     * <p>Retrieval tier at which the restore will be processed.</p>
     */
    Tier tier;

    GlacierJobParameters() {
        this.tier = null;
    }

    protected GlacierJobParameters(BuilderImpl builder) {
        this.tier = builder.tier;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(GlacierJobParameters.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GlacierJobParameters);
    }

    public Tier tier() {
        return tier;
    }

    public void setTier(final Tier tier) {
        this.tier = tier;
    }

    public interface Builder {
        Builder tier(Tier tier);

        GlacierJobParameters build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Retrieval tier at which the restore will be processed.</p>
         */
        Tier tier;

        protected BuilderImpl() {
        }

        private BuilderImpl(GlacierJobParameters model) {
            tier(model.tier);
        }

        public GlacierJobParameters build() {
            return new GlacierJobParameters(this);
        }

        public final Builder tier(Tier tier) {
            this.tier = tier;
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

        public Tier tier() {
            return tier;
        }

        public void setTier(final Tier tier) {
            this.tier = tier;
        }
    }
}
