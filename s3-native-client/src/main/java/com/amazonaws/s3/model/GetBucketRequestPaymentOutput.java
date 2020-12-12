// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketRequestPaymentOutput {
    private Payer payer;

    private GetBucketRequestPaymentOutput() {
        this.payer = null;
    }

    private GetBucketRequestPaymentOutput(Builder builder) {
        this.payer = builder.payer;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(GetBucketRequestPaymentOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetBucketRequestPaymentOutput);
    }

    public Payer payer() {
        return payer;
    }

    public void setPayer(final Payer payer) {
        this.payer = payer;
    }

    static final class Builder {
        private Payer payer;

        private Builder() {
        }

        private Builder(GetBucketRequestPaymentOutput model) {
            payer(model.payer);
        }

        public GetBucketRequestPaymentOutput build() {
            return new com.amazonaws.s3.model.GetBucketRequestPaymentOutput(this);
        }

        /**
         * <p>Specifies who pays for the download and request fees.</p>
         */
        public final Builder payer(Payer payer) {
            this.payer = payer;
            return this;
        }
    }
}
