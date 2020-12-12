// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetObjectLegalHoldOutput {
    private ObjectLockLegalHold legalHold;

    private GetObjectLegalHoldOutput() {
        this.legalHold = null;
    }

    private GetObjectLegalHoldOutput(Builder builder) {
        this.legalHold = builder.legalHold;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(GetObjectLegalHoldOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetObjectLegalHoldOutput);
    }

    public ObjectLockLegalHold legalHold() {
        return legalHold;
    }

    public void setLegalHold(final ObjectLockLegalHold legalHold) {
        this.legalHold = legalHold;
    }

    static final class Builder {
        private ObjectLockLegalHold legalHold;

        private Builder() {
        }

        private Builder(GetObjectLegalHoldOutput model) {
            legalHold(model.legalHold);
        }

        public GetObjectLegalHoldOutput build() {
            return new com.amazonaws.s3.model.GetObjectLegalHoldOutput(this);
        }

        /**
         * <p>The current Legal Hold status for the specified object.</p>
         */
        public final Builder legalHold(ObjectLockLegalHold legalHold) {
            this.legalHold = legalHold;
            return this;
        }
    }
}
