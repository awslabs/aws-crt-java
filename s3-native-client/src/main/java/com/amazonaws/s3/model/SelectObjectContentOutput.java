// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class SelectObjectContentOutput {
    /**
     * <p>The array of results.</p>
     */
    SelectObjectContentEventStream payload;

    SelectObjectContentOutput() {
        this.payload = null;
    }

    protected SelectObjectContentOutput(BuilderImpl builder) {
        this.payload = builder.payload;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(SelectObjectContentOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof SelectObjectContentOutput);
    }

    public SelectObjectContentEventStream payload() {
        return payload;
    }

    public void setPayload(final SelectObjectContentEventStream payload) {
        this.payload = payload;
    }

    public interface Builder {
        Builder payload(SelectObjectContentEventStream payload);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The array of results.</p>
         */
        SelectObjectContentEventStream payload;

        protected BuilderImpl() {
        }

        private BuilderImpl(SelectObjectContentOutput model) {
            payload(model.payload);
        }

        public SelectObjectContentOutput build() {
            return new SelectObjectContentOutput(this);
        }

        public final Builder payload(SelectObjectContentEventStream payload) {
            this.payload = payload;
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

        public SelectObjectContentEventStream payload() {
            return payload;
        }

        public void setPayload(final SelectObjectContentEventStream payload) {
            this.payload = payload;
        }
    }
}
