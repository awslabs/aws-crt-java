// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutObjectRetentionOutput {
    RequestCharged requestCharged;

    PutObjectRetentionOutput() {
        this.requestCharged = null;
    }

    protected PutObjectRetentionOutput(BuilderImpl builder) {
        this.requestCharged = builder.requestCharged;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(PutObjectRetentionOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof PutObjectRetentionOutput);
    }

    public RequestCharged requestCharged() {
        return requestCharged;
    }

    public interface Builder {
        Builder requestCharged(RequestCharged requestCharged);

        PutObjectRetentionOutput build();
    }

    protected static class BuilderImpl implements Builder {
        RequestCharged requestCharged;

        protected BuilderImpl() {
        }

        private BuilderImpl(PutObjectRetentionOutput model) {
            requestCharged(model.requestCharged);
        }

        public PutObjectRetentionOutput build() {
            return new PutObjectRetentionOutput(this);
        }

        public final Builder requestCharged(RequestCharged requestCharged) {
            this.requestCharged = requestCharged;
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

        public RequestCharged requestCharged() {
            return requestCharged;
        }
    }
}
