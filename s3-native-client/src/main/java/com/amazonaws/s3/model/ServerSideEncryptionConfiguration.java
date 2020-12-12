// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.List;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ServerSideEncryptionConfiguration {
    private List<ServerSideEncryptionRule> rules;

    private ServerSideEncryptionConfiguration() {
        this.rules = null;
    }

    private ServerSideEncryptionConfiguration(Builder builder) {
        this.rules = builder.rules;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    static final class Builder {
        private List<ServerSideEncryptionRule> rules;

        private Builder() {
        }

        private Builder(ServerSideEncryptionConfiguration model) {
            rules(model.rules);
        }

        public ServerSideEncryptionConfiguration build() {
            return new com.amazonaws.s3.model.ServerSideEncryptionConfiguration(this);
        }

        /**
         * <p>Container for information about a particular server-side encryption configuration
         *          rule.</p>
         */
        public final Builder rules(List<ServerSideEncryptionRule> rules) {
            this.rules = rules;
            return this;
        }
    }
}
