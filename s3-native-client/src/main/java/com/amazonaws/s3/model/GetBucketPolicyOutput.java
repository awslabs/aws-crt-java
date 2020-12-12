// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketPolicyOutput {
    private String policy;

    private GetBucketPolicyOutput() {
        this.policy = null;
    }

    private GetBucketPolicyOutput(Builder builder) {
        this.policy = builder.policy;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    static final class Builder {
        private String policy;

        private Builder() {
        }

        private Builder(GetBucketPolicyOutput model) {
            policy(model.policy);
        }

        public GetBucketPolicyOutput build() {
            return new com.amazonaws.s3.model.GetBucketPolicyOutput(this);
        }

        /**
         * <p>The bucket policy as a JSON document.</p>
         */
        public final Builder policy(String policy) {
            this.policy = policy;
            return this;
        }
    }
}
