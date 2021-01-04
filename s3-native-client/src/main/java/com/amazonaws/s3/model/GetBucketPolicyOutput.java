// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketPolicyOutput {
    /**
     * <p>The bucket policy as a JSON document.</p>
     */
    String policy;

    GetBucketPolicyOutput() {
        this.policy = "";
    }

    protected GetBucketPolicyOutput(BuilderImpl builder) {
        this.policy = builder.policy;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(GetBucketPolicyOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetBucketPolicyOutput);
    }

    public String policy() {
        return policy;
    }

    public void setPolicy(final String policy) {
        this.policy = policy;
    }

    public interface Builder {
        Builder policy(String policy);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The bucket policy as a JSON document.</p>
         */
        String policy;

        protected BuilderImpl() {
        }

        private BuilderImpl(GetBucketPolicyOutput model) {
            policy(model.policy);
        }

        public GetBucketPolicyOutput build() {
            return new GetBucketPolicyOutput(this);
        }

        public final Builder policy(String policy) {
            this.policy = policy;
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

        public String policy() {
            return policy;
        }

        public void setPolicy(final String policy) {
            this.policy = policy;
        }
    }
}
