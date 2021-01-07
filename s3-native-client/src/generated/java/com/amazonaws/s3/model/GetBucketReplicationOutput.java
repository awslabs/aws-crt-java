// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketReplicationOutput {
    ReplicationConfiguration replicationConfiguration;

    GetBucketReplicationOutput() {
        this.replicationConfiguration = null;
    }

    protected GetBucketReplicationOutput(BuilderImpl builder) {
        this.replicationConfiguration = builder.replicationConfiguration;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(GetBucketReplicationOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetBucketReplicationOutput);
    }

    public ReplicationConfiguration replicationConfiguration() {
        return replicationConfiguration;
    }

    public void setReplicationConfiguration(
            final ReplicationConfiguration replicationConfiguration) {
        this.replicationConfiguration = replicationConfiguration;
    }

    public interface Builder {
        Builder replicationConfiguration(ReplicationConfiguration replicationConfiguration);

        GetBucketReplicationOutput build();
    }

    protected static class BuilderImpl implements Builder {
        ReplicationConfiguration replicationConfiguration;

        protected BuilderImpl() {
        }

        private BuilderImpl(GetBucketReplicationOutput model) {
            replicationConfiguration(model.replicationConfiguration);
        }

        public GetBucketReplicationOutput build() {
            return new GetBucketReplicationOutput(this);
        }

        public final Builder replicationConfiguration(
                ReplicationConfiguration replicationConfiguration) {
            this.replicationConfiguration = replicationConfiguration;
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

        public ReplicationConfiguration replicationConfiguration() {
            return replicationConfiguration;
        }

        public void setReplicationConfiguration(
                final ReplicationConfiguration replicationConfiguration) {
            this.replicationConfiguration = replicationConfiguration;
        }
    }
}
