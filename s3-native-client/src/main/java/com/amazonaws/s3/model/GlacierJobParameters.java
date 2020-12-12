// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GlacierJobParameters {
    private Tier tier;

    private GlacierJobParameters() {
        this.tier = null;
    }

    private GlacierJobParameters(Builder builder) {
        this.tier = builder.tier;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    static final class Builder {
        private Tier tier;

        private Builder() {
        }

        private Builder(GlacierJobParameters model) {
            tier(model.tier);
        }

        public GlacierJobParameters build() {
            return new com.amazonaws.s3.model.GlacierJobParameters(this);
        }

        /**
         * <p>Retrieval tier at which the restore will be processed.</p>
         */
        public final Builder tier(Tier tier) {
            this.tier = tier;
            return this;
        }
    }
}
