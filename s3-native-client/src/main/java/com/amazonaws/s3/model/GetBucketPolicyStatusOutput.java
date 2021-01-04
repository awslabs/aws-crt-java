// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketPolicyStatusOutput {
    /**
     * <p>The policy status for the specified bucket.</p>
     */
    PolicyStatus policyStatus;

    GetBucketPolicyStatusOutput() {
        this.policyStatus = null;
    }

    protected GetBucketPolicyStatusOutput(BuilderImpl builder) {
        this.policyStatus = builder.policyStatus;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public interface Builder {
        Builder policyStatus(PolicyStatus policyStatus);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The policy status for the specified bucket.</p>
         */
        PolicyStatus policyStatus;

        protected BuilderImpl() {
        }

        private BuilderImpl(GetBucketPolicyStatusOutput model) {
            policyStatus(model.policyStatus);
        }

        public GetBucketPolicyStatusOutput build() {
            return new GetBucketPolicyStatusOutput(this);
        }

        public final Builder policyStatus(PolicyStatus policyStatus) {
            this.policyStatus = policyStatus;
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

        public PolicyStatus policyStatus() {
            return policyStatus;
        }

        public void setPolicyStatus(final PolicyStatus policyStatus) {
            this.policyStatus = policyStatus;
        }
    }
}
