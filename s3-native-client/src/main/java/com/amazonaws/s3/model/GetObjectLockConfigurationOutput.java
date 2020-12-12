// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetObjectLockConfigurationOutput {
    private ObjectLockConfiguration objectLockConfiguration;

    private GetObjectLockConfigurationOutput() {
        this.objectLockConfiguration = null;
    }

    private GetObjectLockConfigurationOutput(Builder builder) {
        this.objectLockConfiguration = builder.objectLockConfiguration;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(GetObjectLockConfigurationOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetObjectLockConfigurationOutput);
    }

    public ObjectLockConfiguration objectLockConfiguration() {
        return objectLockConfiguration;
    }

    public void setObjectLockConfiguration(final ObjectLockConfiguration objectLockConfiguration) {
        this.objectLockConfiguration = objectLockConfiguration;
    }

    static final class Builder {
        private ObjectLockConfiguration objectLockConfiguration;

        private Builder() {
        }

        private Builder(GetObjectLockConfigurationOutput model) {
            objectLockConfiguration(model.objectLockConfiguration);
        }

        public GetObjectLockConfigurationOutput build() {
            return new com.amazonaws.s3.model.GetObjectLockConfigurationOutput(this);
        }

        /**
         * <p>The specified bucket's Object Lock configuration.</p>
         */
        public final Builder objectLockConfiguration(
                ObjectLockConfiguration objectLockConfiguration) {
            this.objectLockConfiguration = objectLockConfiguration;
            return this;
        }
    }
}
