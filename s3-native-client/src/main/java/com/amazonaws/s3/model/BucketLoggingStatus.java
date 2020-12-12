// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class BucketLoggingStatus {
    private LoggingEnabled loggingEnabled;

    private BucketLoggingStatus() {
        this.loggingEnabled = null;
    }

    private BucketLoggingStatus(Builder builder) {
        this.loggingEnabled = builder.loggingEnabled;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setLoggingEnabled(final LoggingEnabled loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
    }

    static final class Builder {
        private LoggingEnabled loggingEnabled;

        private Builder() {
        }

        private Builder(BucketLoggingStatus model) {
            loggingEnabled(model.loggingEnabled);
        }

        public BucketLoggingStatus build() {
            return new com.amazonaws.s3.model.BucketLoggingStatus(this);
        }

        public final Builder loggingEnabled(LoggingEnabled loggingEnabled) {
            this.loggingEnabled = loggingEnabled;
            return this;
        }
    }
}
