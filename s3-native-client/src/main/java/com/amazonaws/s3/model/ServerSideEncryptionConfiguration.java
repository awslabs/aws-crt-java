// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ServerSideEncryptionConfiguration {
    /**
     * <p>Container for information about a particular server-side encryption configuration
     *          rule.</p>
     */
    List<ServerSideEncryptionRule> rules;

    ServerSideEncryptionConfiguration() {
        this.rules = null;
    }

    protected ServerSideEncryptionConfiguration(BuilderImpl builder) {
        this.rules = builder.rules;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(ServerSideEncryptionConfiguration.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ServerSideEncryptionConfiguration);
    }

    public List<ServerSideEncryptionRule> rules() {
        return rules;
    }

    public void setRules(final List<ServerSideEncryptionRule> rules) {
        this.rules = rules;
    }

    public interface Builder {
        Builder rules(List<ServerSideEncryptionRule> rules);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Container for information about a particular server-side encryption configuration
         *          rule.</p>
         */
        List<ServerSideEncryptionRule> rules;

        protected BuilderImpl() {
        }

        private BuilderImpl(ServerSideEncryptionConfiguration model) {
            rules(model.rules);
        }

        public ServerSideEncryptionConfiguration build() {
            return new ServerSideEncryptionConfiguration(this);
        }

        public final Builder rules(List<ServerSideEncryptionRule> rules) {
            this.rules = rules;
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

        public List<ServerSideEncryptionRule> rules() {
            return rules;
        }

        public void setRules(final List<ServerSideEncryptionRule> rules) {
            this.rules = rules;
        }
    }
}
