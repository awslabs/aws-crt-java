// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class CORSConfiguration {
    private List<CORSRule> cORSRules;

    private CORSConfiguration() {
        this.cORSRules = null;
    }

    private CORSConfiguration(Builder builder) {
        this.cORSRules = builder.cORSRules;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(CORSConfiguration.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof CORSConfiguration);
    }

    public List<CORSRule> cORSRules() {
        return cORSRules;
    }

    public void setCORSRules(final List<CORSRule> cORSRules) {
        this.cORSRules = cORSRules;
    }

    static final class Builder {
        private List<CORSRule> cORSRules;

        private Builder() {
        }

        private Builder(CORSConfiguration model) {
            cORSRules(model.cORSRules);
        }

        public CORSConfiguration build() {
            return new com.amazonaws.s3.model.CORSConfiguration(this);
        }

        /**
         * <p>A set of origins and methods (cross-origin access that you want to allow). You can add
         *          up to 100 rules to the configuration.</p>
         */
        public final Builder cORSRules(List<CORSRule> cORSRules) {
            this.cORSRules = cORSRules;
            return this;
        }
    }
}
