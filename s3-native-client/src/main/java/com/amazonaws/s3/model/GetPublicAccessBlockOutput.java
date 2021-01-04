// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetPublicAccessBlockOutput {
    /**
     * <p>The <code>PublicAccessBlock</code> configuration currently in effect for this Amazon S3
     *          bucket.</p>
     */
    PublicAccessBlockConfiguration publicAccessBlockConfiguration;

    GetPublicAccessBlockOutput() {
        this.publicAccessBlockConfiguration = null;
    }

    protected GetPublicAccessBlockOutput(BuilderImpl builder) {
        this.publicAccessBlockConfiguration = builder.publicAccessBlockConfiguration;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(GetPublicAccessBlockOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetPublicAccessBlockOutput);
    }

    public PublicAccessBlockConfiguration publicAccessBlockConfiguration() {
        return publicAccessBlockConfiguration;
    }

    public void setPublicAccessBlockConfiguration(
            final PublicAccessBlockConfiguration publicAccessBlockConfiguration) {
        this.publicAccessBlockConfiguration = publicAccessBlockConfiguration;
    }

    public interface Builder {
        Builder publicAccessBlockConfiguration(
                PublicAccessBlockConfiguration publicAccessBlockConfiguration);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The <code>PublicAccessBlock</code> configuration currently in effect for this Amazon S3
         *          bucket.</p>
         */
        PublicAccessBlockConfiguration publicAccessBlockConfiguration;

        protected BuilderImpl() {
        }

        private BuilderImpl(GetPublicAccessBlockOutput model) {
            publicAccessBlockConfiguration(model.publicAccessBlockConfiguration);
        }

        public GetPublicAccessBlockOutput build() {
            return new GetPublicAccessBlockOutput(this);
        }

        public final Builder publicAccessBlockConfiguration(
                PublicAccessBlockConfiguration publicAccessBlockConfiguration) {
            this.publicAccessBlockConfiguration = publicAccessBlockConfiguration;
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

        public PublicAccessBlockConfiguration publicAccessBlockConfiguration() {
            return publicAccessBlockConfiguration;
        }

        public void setPublicAccessBlockConfiguration(
                final PublicAccessBlockConfiguration publicAccessBlockConfiguration) {
            this.publicAccessBlockConfiguration = publicAccessBlockConfiguration;
        }
    }
}
