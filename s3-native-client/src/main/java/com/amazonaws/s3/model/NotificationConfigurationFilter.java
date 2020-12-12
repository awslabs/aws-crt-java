// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class NotificationConfigurationFilter {
    private S3KeyFilter key;

    private NotificationConfigurationFilter() {
        this.key = null;
    }

    private NotificationConfigurationFilter(Builder builder) {
        this.key = builder.key;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    static final class Builder {
        private S3KeyFilter key;

        private Builder() {
        }

        private Builder(NotificationConfigurationFilter model) {
            key(model.key);
        }

        public NotificationConfigurationFilter build() {
            return new com.amazonaws.s3.model.NotificationConfigurationFilter(this);
        }

        public final Builder key(S3KeyFilter key) {
            this.key = key;
            return this;
        }
    }
}
