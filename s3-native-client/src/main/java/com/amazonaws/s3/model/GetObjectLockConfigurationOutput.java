// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetObjectLockConfigurationOutput {
    /**
     * <p>The specified bucket's Object Lock configuration.</p>
     */
    ObjectLockConfiguration objectLockConfiguration;

    GetObjectLockConfigurationOutput() {
        this.objectLockConfiguration = null;
    }

    protected GetObjectLockConfigurationOutput(BuilderImpl builder) {
        this.objectLockConfiguration = builder.objectLockConfiguration;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public interface Builder {
        Builder objectLockConfiguration(ObjectLockConfiguration objectLockConfiguration);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The specified bucket's Object Lock configuration.</p>
         */
        ObjectLockConfiguration objectLockConfiguration;

        protected BuilderImpl() {
        }

        private BuilderImpl(GetObjectLockConfigurationOutput model) {
            objectLockConfiguration(model.objectLockConfiguration);
        }

        public GetObjectLockConfigurationOutput build() {
            return new GetObjectLockConfigurationOutput(this);
        }

        public final Builder objectLockConfiguration(
                ObjectLockConfiguration objectLockConfiguration) {
            this.objectLockConfiguration = objectLockConfiguration;
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

        public ObjectLockConfiguration objectLockConfiguration() {
            return objectLockConfiguration;
        }

        public void setObjectLockConfiguration(
                final ObjectLockConfiguration objectLockConfiguration) {
            this.objectLockConfiguration = objectLockConfiguration;
        }
    }
}
