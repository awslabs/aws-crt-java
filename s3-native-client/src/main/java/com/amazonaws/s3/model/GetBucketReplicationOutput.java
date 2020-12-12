// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketReplicationOutput {
    private ReplicationConfiguration replicationConfiguration;

    private GetBucketReplicationOutput() {
        this.replicationConfiguration = null;
    }

    private GetBucketReplicationOutput(Builder builder) {
        this.replicationConfiguration = builder.replicationConfiguration;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    static final class Builder {
        private ReplicationConfiguration replicationConfiguration;

        private Builder() {
        }

        private Builder(GetBucketReplicationOutput model) {
            replicationConfiguration(model.replicationConfiguration);
        }

        public GetBucketReplicationOutput build() {
            return new com.amazonaws.s3.model.GetBucketReplicationOutput(this);
        }

        public final Builder replicationConfiguration(
                ReplicationConfiguration replicationConfiguration) {
            this.replicationConfiguration = replicationConfiguration;
            return this;
        }
    }
}
