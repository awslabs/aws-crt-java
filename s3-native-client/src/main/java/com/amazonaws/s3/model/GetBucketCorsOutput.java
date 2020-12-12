// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketCorsOutput {
    private List<CORSRule> cORSRules;

    private GetBucketCorsOutput() {
        this.cORSRules = null;
    }

    private GetBucketCorsOutput(Builder builder) {
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
        return Objects.hash(GetBucketCorsOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetBucketCorsOutput);
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

        private Builder(GetBucketCorsOutput model) {
            cORSRules(model.cORSRules);
        }

        public GetBucketCorsOutput build() {
            return new com.amazonaws.s3.model.GetBucketCorsOutput(this);
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
