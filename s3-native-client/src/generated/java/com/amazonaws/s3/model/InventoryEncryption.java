// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class InventoryEncryption {
    /**
     * <p>Specifies the use of SSE-S3 to encrypt delivered inventory reports.</p>
     */
    SSES3 sSES3;

    /**
     * <p>Specifies the use of SSE-KMS to encrypt delivered inventory reports.</p>
     */
    SSEKMS sSEKMS;

    InventoryEncryption() {
        this.sSES3 = null;
        this.sSEKMS = null;
    }

    protected InventoryEncryption(BuilderImpl builder) {
        this.sSES3 = builder.sSES3;
        this.sSEKMS = builder.sSEKMS;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(InventoryEncryption.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof InventoryEncryption);
    }

    public SSES3 sSES3() {
        return sSES3;
    }

    public SSEKMS sSEKMS() {
        return sSEKMS;
    }

    public void setSSES3(final SSES3 sSES3) {
        this.sSES3 = sSES3;
    }

    public void setSSEKMS(final SSEKMS sSEKMS) {
        this.sSEKMS = sSEKMS;
    }

    public interface Builder {
        Builder sSES3(SSES3 sSES3);

        Builder sSEKMS(SSEKMS sSEKMS);

        InventoryEncryption build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Specifies the use of SSE-S3 to encrypt delivered inventory reports.</p>
         */
        SSES3 sSES3;

        /**
         * <p>Specifies the use of SSE-KMS to encrypt delivered inventory reports.</p>
         */
        SSEKMS sSEKMS;

        protected BuilderImpl() {
        }

        private BuilderImpl(InventoryEncryption model) {
            sSES3(model.sSES3);
            sSEKMS(model.sSEKMS);
        }

        public InventoryEncryption build() {
            return new InventoryEncryption(this);
        }

        public final Builder sSES3(SSES3 sSES3) {
            this.sSES3 = sSES3;
            return this;
        }

        public final Builder sSEKMS(SSEKMS sSEKMS) {
            this.sSEKMS = sSEKMS;
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

        public SSES3 sSES3() {
            return sSES3;
        }

        public SSEKMS sSEKMS() {
            return sSEKMS;
        }

        public void setSSES3(final SSES3 sSES3) {
            this.sSES3 = sSES3;
        }

        public void setSSEKMS(final SSEKMS sSEKMS) {
            this.sSEKMS = sSEKMS;
        }
    }
}
