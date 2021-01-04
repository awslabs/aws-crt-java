// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetObjectLegalHoldOutput {
    /**
     * <p>The current Legal Hold status for the specified object.</p>
     */
    ObjectLockLegalHold legalHold;

    GetObjectLegalHoldOutput() {
        this.legalHold = null;
    }

    protected GetObjectLegalHoldOutput(BuilderImpl builder) {
        this.legalHold = builder.legalHold;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public interface Builder {
        Builder legalHold(ObjectLockLegalHold legalHold);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The current Legal Hold status for the specified object.</p>
         */
        ObjectLockLegalHold legalHold;

        protected BuilderImpl() {
        }

        private BuilderImpl(GetObjectLegalHoldOutput model) {
            legalHold(model.legalHold);
        }

        public GetObjectLegalHoldOutput build() {
            return new GetObjectLegalHoldOutput(this);
        }

        public final Builder legalHold(ObjectLockLegalHold legalHold) {
            this.legalHold = legalHold;
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

        public ObjectLockLegalHold legalHold() {
            return legalHold;
        }

        public void setLegalHold(final ObjectLockLegalHold legalHold) {
            this.legalHold = legalHold;
        }
    }
}
