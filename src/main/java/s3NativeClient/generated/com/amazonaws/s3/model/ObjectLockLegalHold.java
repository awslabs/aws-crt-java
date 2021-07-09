// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ObjectLockLegalHold {
    /**
     * <p>Indicates whether the specified object has a Legal Hold in place.</p>
     */
    ObjectLockLegalHoldStatus status;

    ObjectLockLegalHold() {
        this.status = null;
    }

    protected ObjectLockLegalHold(BuilderImpl builder) {
        this.status = builder.status;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(ObjectLockLegalHold.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ObjectLockLegalHold);
    }

    public ObjectLockLegalHoldStatus status() {
        return status;
    }

    public interface Builder {
        Builder status(ObjectLockLegalHoldStatus status);

        ObjectLockLegalHold build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Indicates whether the specified object has a Legal Hold in place.</p>
         */
        ObjectLockLegalHoldStatus status;

        protected BuilderImpl() {
        }

        private BuilderImpl(ObjectLockLegalHold model) {
            status(model.status);
        }

        public ObjectLockLegalHold build() {
            return new ObjectLockLegalHold(this);
        }

        public final Builder status(ObjectLockLegalHoldStatus status) {
            this.status = status;
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

        public ObjectLockLegalHoldStatus status() {
            return status;
        }
    }
}
