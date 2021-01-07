// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetBucketRequestPaymentOutput {
    /**
     * <p>Specifies who pays for the download and request fees.</p>
     */
    Payer payer;

    GetBucketRequestPaymentOutput() {
        this.payer = null;
    }

    protected GetBucketRequestPaymentOutput(BuilderImpl builder) {
        this.payer = builder.payer;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public interface Builder {
        Builder payer(Payer payer);

        GetBucketRequestPaymentOutput build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Specifies who pays for the download and request fees.</p>
         */
        Payer payer;

        protected BuilderImpl() {
        }

        private BuilderImpl(GetBucketRequestPaymentOutput model) {
            payer(model.payer);
        }

        public GetBucketRequestPaymentOutput build() {
            return new GetBucketRequestPaymentOutput(this);
        }

        public final Builder payer(Payer payer) {
            this.payer = payer;
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

        public Payer payer() {
            return payer;
        }

        public void setPayer(final Payer payer) {
            this.payer = payer;
        }
    }
}
