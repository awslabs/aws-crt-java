// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutObjectLockConfigurationOutput {
    private RequestCharged requestCharged;

    private PutObjectLockConfigurationOutput() {
        this.requestCharged = null;
    }

    private PutObjectLockConfigurationOutput(Builder builder) {
        this.requestCharged = builder.requestCharged;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(PutObjectLockConfigurationOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof PutObjectLockConfigurationOutput);
    }

    public RequestCharged requestCharged() {
        return requestCharged;
    }

    public void setRequestCharged(final RequestCharged requestCharged) {
        this.requestCharged = requestCharged;
    }

    static final class Builder {
        private RequestCharged requestCharged;

        private Builder() {
        }

        private Builder(PutObjectLockConfigurationOutput model) {
            requestCharged(model.requestCharged);
        }

        public PutObjectLockConfigurationOutput build() {
            return new com.amazonaws.s3.model.PutObjectLockConfigurationOutput(this);
        }

        public final Builder requestCharged(RequestCharged requestCharged) {
            this.requestCharged = requestCharged;
            return this;
        }
    }
}
