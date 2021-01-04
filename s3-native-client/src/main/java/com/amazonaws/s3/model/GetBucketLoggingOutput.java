// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketLoggingOutput {
    LoggingEnabled loggingEnabled;

    GetBucketLoggingOutput() {
        this.loggingEnabled = null;
    }

    protected GetBucketLoggingOutput(BuilderImpl builder) {
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
        return Objects.hash(GetBucketLoggingOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetBucketLoggingOutput);
    }

    public LoggingEnabled loggingEnabled() {
        return loggingEnabled;
    }

    public void setLoggingEnabled(final LoggingEnabled loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
    }

    public interface Builder {
        Builder loggingEnabled(LoggingEnabled loggingEnabled);
    }

    protected static class BuilderImpl implements Builder {
        LoggingEnabled loggingEnabled;

        protected BuilderImpl() {
        }

        private BuilderImpl(GetBucketLoggingOutput model) {
            loggingEnabled(model.loggingEnabled);
        }

        public GetBucketLoggingOutput build() {
            return new GetBucketLoggingOutput(this);
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

        public void setLoggingEnabled(final LoggingEnabled loggingEnabled) {
            this.loggingEnabled = loggingEnabled;
        }
    }
}
