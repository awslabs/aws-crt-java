// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketPolicyStatusOutput {
    private PolicyStatus policyStatus;

    private GetBucketPolicyStatusOutput() {
        this.policyStatus = null;
    }

    private GetBucketPolicyStatusOutput(Builder builder) {
        this.policyStatus = builder.policyStatus;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(GetBucketPolicyStatusOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetBucketPolicyStatusOutput);
    }

    public PolicyStatus policyStatus() {
        return policyStatus;
    }

    public void setPolicyStatus(final PolicyStatus policyStatus) {
        this.policyStatus = policyStatus;
    }

    static final class Builder {
        private PolicyStatus policyStatus;

        private Builder() {
        }

        private Builder(GetBucketPolicyStatusOutput model) {
            policyStatus(model.policyStatus);
        }

        public GetBucketPolicyStatusOutput build() {
            return new com.amazonaws.s3.model.GetBucketPolicyStatusOutput(this);
        }

        /**
         * <p>The policy status for the specified bucket.</p>
         */
        public final Builder policyStatus(PolicyStatus policyStatus) {
            this.policyStatus = policyStatus;
            return this;
        }
    }
}
