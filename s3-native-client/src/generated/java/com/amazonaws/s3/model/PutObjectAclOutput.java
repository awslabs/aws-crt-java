// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutObjectAclOutput {
    RequestCharged requestCharged;

    PutObjectAclOutput() {
        this.requestCharged = null;
    }

    protected PutObjectAclOutput(BuilderImpl builder) {
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
        return Objects.hash(PutObjectAclOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof PutObjectAclOutput);
    }

    public RequestCharged requestCharged() {
        return requestCharged;
    }

    public interface Builder {
        Builder requestCharged(RequestCharged requestCharged);

        PutObjectAclOutput build();
    }

    protected static class BuilderImpl implements Builder {
        RequestCharged requestCharged;

        protected BuilderImpl() {
        }

        private BuilderImpl(PutObjectAclOutput model) {
            requestCharged(model.requestCharged);
        }

        public PutObjectAclOutput build() {
            return new PutObjectAclOutput(this);
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
