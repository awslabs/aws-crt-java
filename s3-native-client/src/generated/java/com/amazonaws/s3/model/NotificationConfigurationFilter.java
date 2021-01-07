// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class NotificationConfigurationFilter {
    S3KeyFilter key;

    NotificationConfigurationFilter() {
        this.key = null;
    }

    protected NotificationConfigurationFilter(BuilderImpl builder) {
        this.key = builder.key;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(NotificationConfigurationFilter.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof NotificationConfigurationFilter);
    }

    public S3KeyFilter key() {
        return key;
    }

    public void setKey(final S3KeyFilter key) {
        this.key = key;
    }

    public interface Builder {
        Builder key(S3KeyFilter key);

        NotificationConfigurationFilter build();
    }

    protected static class BuilderImpl implements Builder {
        S3KeyFilter key;

        protected BuilderImpl() {
        }

        private BuilderImpl(NotificationConfigurationFilter model) {
            key(model.key);
        }

        public NotificationConfigurationFilter build() {
            return new NotificationConfigurationFilter(this);
        }

        public final Builder key(S3KeyFilter key) {
            this.key = key;
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

        public S3KeyFilter key() {
            return key;
        }

        public void setKey(final S3KeyFilter key) {
            this.key = key;
        }
    }
}
