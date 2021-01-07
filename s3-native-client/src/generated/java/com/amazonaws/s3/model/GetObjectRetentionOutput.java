// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetObjectRetentionOutput {
    /**
     * <p>The container element for an object's retention settings.</p>
     */
    ObjectLockRetention retention;

    GetObjectRetentionOutput() {
        this.retention = null;
    }

    protected GetObjectRetentionOutput(BuilderImpl builder) {
        this.retention = builder.retention;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(GetObjectRetentionOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetObjectRetentionOutput);
    }

    public ObjectLockRetention retention() {
        return retention;
    }

    public void setRetention(final ObjectLockRetention retention) {
        this.retention = retention;
    }

    public interface Builder {
        Builder retention(ObjectLockRetention retention);

        GetObjectRetentionOutput build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The container element for an object's retention settings.</p>
         */
        ObjectLockRetention retention;

        protected BuilderImpl() {
        }

        private BuilderImpl(GetObjectRetentionOutput model) {
            retention(model.retention);
        }

        public GetObjectRetentionOutput build() {
            return new GetObjectRetentionOutput(this);
        }

        public final Builder retention(ObjectLockRetention retention) {
            this.retention = retention;
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

        public ObjectLockRetention retention() {
            return retention;
        }

        public void setRetention(final ObjectLockRetention retention) {
            this.retention = retention;
        }
    }
}
