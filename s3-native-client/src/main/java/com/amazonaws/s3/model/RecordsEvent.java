// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class RecordsEvent {
    private byte[] payload;

    private RecordsEvent() {
        this.payload = null;
    }

    private RecordsEvent(Builder builder) {
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
        return Objects.hash(RecordsEvent.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof RecordsEvent);
    }

    public byte[] payload() {
        return payload;
    }

    public void setPayload(final byte[] payload) {
        this.payload = payload;
    }

    static final class Builder {
        private byte[] payload;

        private Builder() {
        }

        private Builder(RecordsEvent model) {
            payload(model.payload);
        }

        public RecordsEvent build() {
            return new com.amazonaws.s3.model.RecordsEvent(this);
        }

        /**
         * <p>The byte array of partial, one or more result records.</p>
         */
        public final Builder payload(byte[] payload) {
            this.payload = payload;
            return this;
        }
    }
}
