// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class CORSConfiguration {
    /**
     * <p>A set of origins and methods (cross-origin access that you want to allow). You can add
     *          up to 100 rules to the configuration.</p>
     */
    List<CORSRule> cORSRules;

    CORSConfiguration() {
        this.cORSRules = null;
    }

    protected CORSConfiguration(BuilderImpl builder) {
        this.cORSRules = builder.cORSRules;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public interface Builder {
        Builder cORSRules(List<CORSRule> cORSRules);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>A set of origins and methods (cross-origin access that you want to allow). You can add
         *          up to 100 rules to the configuration.</p>
         */
        List<CORSRule> cORSRules;

        protected BuilderImpl() {
        }

        private BuilderImpl(CORSConfiguration model) {
            cORSRules(model.cORSRules);
        }

        public CORSConfiguration build() {
            return new CORSConfiguration(this);
        }

        public final Builder cORSRules(List<CORSRule> cORSRules) {
            this.cORSRules = cORSRules;
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

        public List<CORSRule> cORSRules() {
            return cORSRules;
        }

        public void setCORSRules(final List<CORSRule> cORSRules) {
            this.cORSRules = cORSRules;
        }
    }
}
