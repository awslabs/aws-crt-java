// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetPublicAccessBlockOutput {
    private PublicAccessBlockConfiguration publicAccessBlockConfiguration;

    private GetPublicAccessBlockOutput() {
        this.publicAccessBlockConfiguration = null;
    }

    private GetPublicAccessBlockOutput(Builder builder) {
        this.publicAccessBlockConfiguration = builder.publicAccessBlockConfiguration;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    static final class Builder {
        private PublicAccessBlockConfiguration publicAccessBlockConfiguration;

        private Builder() {
        }

        private Builder(GetPublicAccessBlockOutput model) {
            publicAccessBlockConfiguration(model.publicAccessBlockConfiguration);
        }

        public GetPublicAccessBlockOutput build() {
            return new com.amazonaws.s3.model.GetPublicAccessBlockOutput(this);
        }

        /**
         * <p>The <code>PublicAccessBlock</code> configuration currently in effect for this Amazon S3
         *          bucket.</p>
         */
        public final Builder publicAccessBlockConfiguration(
                PublicAccessBlockConfiguration publicAccessBlockConfiguration) {
            this.publicAccessBlockConfiguration = publicAccessBlockConfiguration;
            return this;
        }
    }
}
