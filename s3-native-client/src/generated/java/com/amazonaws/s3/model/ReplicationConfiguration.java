// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ReplicationConfiguration {
    /**
     * <p>The Amazon Resource Name (ARN) of the AWS Identity and Access Management (IAM) role that
     *          Amazon S3 assumes when replicating objects. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/replication-how-setup.html">How to Set Up
     *             Replication</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     */
    String role;

    /**
     * <p>A container for one or more replication rules. A replication configuration must have at
     *          least one rule and can contain a maximum of 1,000 rules. </p>
     */
    List<ReplicationRule> rules;

    ReplicationConfiguration() {
        this.role = "";
        this.rules = null;
    }

    protected ReplicationConfiguration(BuilderImpl builder) {
        this.role = builder.role;
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

    public List<ReplicationRule> rules() {
        return rules;
    }

    public void setRole(final String role) {
        this.role = role;
    }

    public void setRules(final List<ReplicationRule> rules) {
        this.rules = rules;
    }

    public interface Builder {
        Builder role(String role);

        Builder rules(List<ReplicationRule> rules);

        ReplicationConfiguration build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The Amazon Resource Name (ARN) of the AWS Identity and Access Management (IAM) role that
         *          Amazon S3 assumes when replicating objects. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/replication-how-setup.html">How to Set Up
         *             Replication</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        String role;

        /**
         * <p>A container for one or more replication rules. A replication configuration must have at
         *          least one rule and can contain a maximum of 1,000 rules. </p>
         */
        List<ReplicationRule> rules;

        protected BuilderImpl() {
        }

        private BuilderImpl(ReplicationConfiguration model) {
            role(model.role);
            rules(model.rules);
        }

        public ReplicationConfiguration build() {
            return new ReplicationConfiguration(this);
        }

        public final Builder role(String role) {
            this.role = role;
            return this;
        }

        public final Builder rules(List<ReplicationRule> rules) {
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

        public String role() {
            return role;
        }

        public List<ReplicationRule> rules() {
            return rules;
        }

        public void setRole(final String role) {
            this.role = role;
        }

        public void setRules(final List<ReplicationRule> rules) {
            this.rules = rules;
        }
    }
}
