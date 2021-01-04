// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class RecordsEvent {
    /**
     * <p>The byte array of partial, one or more result records.</p>
     */
    byte[] payload;

    RecordsEvent() {
        this.payload = null;
    }

    protected RecordsEvent(BuilderImpl builder) {
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

    public interface Builder {
        Builder payload(byte[] payload);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The byte array of partial, one or more result records.</p>
         */
        byte[] payload;

        protected BuilderImpl() {
        }

        private BuilderImpl(RecordsEvent model) {
            payload(model.payload);
        }

        public RecordsEvent build() {
            return new RecordsEvent(this);
        }

        public final Builder payload(byte[] payload) {
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

        public byte[] payload() {
            return payload;
        }

        public void setPayload(final byte[] payload) {
            this.payload = payload;
        }
    }
}
