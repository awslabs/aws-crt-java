// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ReplicationConfiguration {
    private String role;

    private List<ReplicationRule> rules;

    private ReplicationConfiguration() {
        this.role = null;
        this.rules = null;
    }

    private ReplicationConfiguration(Builder builder) {
        this.role = builder.role;
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
        return Objects.hash(ReplicationConfiguration.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ReplicationConfiguration);
    }

    public String role() {
        return role;
    }

    public void setRole(final String role) {
        this.role = role;
    }

    public List<ReplicationRule> rules() {
        return rules;
    }

    public void setRules(final List<ReplicationRule> rules) {
        this.rules = rules;
    }

    static final class Builder {
        private String role;

        private List<ReplicationRule> rules;

        private Builder() {
        }

        private Builder(ReplicationConfiguration model) {
            role(model.role);
            rules(model.rules);
        }

        public ReplicationConfiguration build() {
            return new com.amazonaws.s3.model.ReplicationConfiguration(this);
        }

        /**
         * <p>The Amazon Resource Name (ARN) of the AWS Identity and Access Management (IAM) role that
         *          Amazon S3 assumes when replicating objects. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/replication-how-setup.html">How to Set Up
         *             Replication</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        public final Builder role(String role) {
            this.role = role;
            return this;
        }

        /**
         * <p>A container for one or more replication rules. A replication configuration must have at
         *          least one rule and can contain a maximum of 1,000 rules. </p>
         */
        public final Builder rules(List<ReplicationRule> rules) {
            this.rules = rules;
            return this;
        }
    }
}
