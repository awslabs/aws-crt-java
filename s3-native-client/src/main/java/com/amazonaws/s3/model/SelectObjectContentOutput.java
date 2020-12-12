// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class SelectObjectContentOutput {
    private SelectObjectContentEventStream payload;

    private SelectObjectContentOutput() {
        this.payload = null;
    }

    private SelectObjectContentOutput(Builder builder) {
        this.payload = builder.payload;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    static final class Builder {
        private SelectObjectContentEventStream payload;

        private Builder() {
        }

        private Builder(SelectObjectContentOutput model) {
            payload(model.payload);
        }

        public SelectObjectContentOutput build() {
            return new com.amazonaws.s3.model.SelectObjectContentOutput(this);
        }

        /**
         * <p>The array of results.</p>
         */
        public final Builder payload(SelectObjectContentEventStream payload) {
            this.payload = payload;
            return this;
        }
    }
}
