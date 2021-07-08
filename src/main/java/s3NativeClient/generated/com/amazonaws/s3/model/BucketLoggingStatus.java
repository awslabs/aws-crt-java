// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class BucketLoggingStatus {
    LoggingEnabled loggingEnabled;

    BucketLoggingStatus() {
        this.loggingEnabled = null;
    }

    protected BucketLoggingStatus(BuilderImpl builder) {
        this.loggingEnabled = builder.loggingEnabled;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(BucketLoggingStatus.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof BucketLoggingStatus);
    }

    public LoggingEnabled loggingEnabled() {
        return loggingEnabled;
    }

    public interface Builder {
        Builder loggingEnabled(LoggingEnabled loggingEnabled);

        BucketLoggingStatus build();
    }

    protected static class BuilderImpl implements Builder {
        LoggingEnabled loggingEnabled;

        protected BuilderImpl() {
        }

        private BuilderImpl(BucketLoggingStatus model) {
            loggingEnabled(model.loggingEnabled);
        }

        public BucketLoggingStatus build() {
            return new BucketLoggingStatus(this);
        }

        public final Builder loggingEnabled(LoggingEnabled loggingEnabled) {
            this.loggingEnabled = loggingEnabled;
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

        public LoggingEnabled loggingEnabled() {
            return loggingEnabled;
        }
    }
}
