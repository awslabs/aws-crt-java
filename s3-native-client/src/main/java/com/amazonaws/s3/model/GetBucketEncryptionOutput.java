// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketEncryptionOutput {
    private ServerSideEncryptionConfiguration serverSideEncryptionConfiguration;

    private GetBucketEncryptionOutput() {
        this.serverSideEncryptionConfiguration = null;
    }

    private GetBucketEncryptionOutput(Builder builder) {
        this.serverSideEncryptionConfiguration = builder.serverSideEncryptionConfiguration;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(GetBucketEncryptionOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetBucketEncryptionOutput);
    }

    public ServerSideEncryptionConfiguration serverSideEncryptionConfiguration() {
        return serverSideEncryptionConfiguration;
    }

    public void setServerSideEncryptionConfiguration(
            final ServerSideEncryptionConfiguration serverSideEncryptionConfiguration) {
        this.serverSideEncryptionConfiguration = serverSideEncryptionConfiguration;
    }

    static final class Builder {
        private ServerSideEncryptionConfiguration serverSideEncryptionConfiguration;

        private Builder() {
        }

        private Builder(GetBucketEncryptionOutput model) {
            serverSideEncryptionConfiguration(model.serverSideEncryptionConfiguration);
        }

        public GetBucketEncryptionOutput build() {
            return new com.amazonaws.s3.model.GetBucketEncryptionOutput(this);
        }

        public final Builder serverSideEncryptionConfiguration(
                ServerSideEncryptionConfiguration serverSideEncryptionConfiguration) {
            this.serverSideEncryptionConfiguration = serverSideEncryptionConfiguration;
            return this;
        }
    }
}
