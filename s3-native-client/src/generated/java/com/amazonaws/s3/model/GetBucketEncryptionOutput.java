// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketEncryptionOutput {
    ServerSideEncryptionConfiguration serverSideEncryptionConfiguration;

    GetBucketEncryptionOutput() {
        this.serverSideEncryptionConfiguration = null;
    }

    protected GetBucketEncryptionOutput(BuilderImpl builder) {
        this.serverSideEncryptionConfiguration = builder.serverSideEncryptionConfiguration;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public interface Builder {
        Builder serverSideEncryptionConfiguration(
                ServerSideEncryptionConfiguration serverSideEncryptionConfiguration);

        GetBucketEncryptionOutput build();
    }

    protected static class BuilderImpl implements Builder {
        ServerSideEncryptionConfiguration serverSideEncryptionConfiguration;

        protected BuilderImpl() {
        }

        private BuilderImpl(GetBucketEncryptionOutput model) {
            serverSideEncryptionConfiguration(model.serverSideEncryptionConfiguration);
        }

        public GetBucketEncryptionOutput build() {
            return new GetBucketEncryptionOutput(this);
        }

        public final Builder serverSideEncryptionConfiguration(
                ServerSideEncryptionConfiguration serverSideEncryptionConfiguration) {
            this.serverSideEncryptionConfiguration = serverSideEncryptionConfiguration;
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

        public ServerSideEncryptionConfiguration serverSideEncryptionConfiguration() {
            return serverSideEncryptionConfiguration;
        }
    }
}
