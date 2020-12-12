// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class InventoryEncryption {
    private SSES3 sSES3;

    private SSEKMS sSEKMS;

    private InventoryEncryption() {
        this.sSES3 = null;
        this.sSEKMS = null;
    }

    private InventoryEncryption(Builder builder) {
        this.sSES3 = builder.sSES3;
        this.sSEKMS = builder.sSEKMS;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setSSES3(final SSES3 sSES3) {
        this.sSES3 = sSES3;
    }

    public SSEKMS sSEKMS() {
        return sSEKMS;
    }

    public void setSSEKMS(final SSEKMS sSEKMS) {
        this.sSEKMS = sSEKMS;
    }

    static final class Builder {
        private SSES3 sSES3;

        private SSEKMS sSEKMS;

        private Builder() {
        }

        private Builder(InventoryEncryption model) {
            sSES3(model.sSES3);
            sSEKMS(model.sSEKMS);
        }

        public InventoryEncryption build() {
            return new com.amazonaws.s3.model.InventoryEncryption(this);
        }

        /**
         * <p>Specifies the use of SSE-S3 to encrypt delivered inventory reports.</p>
         */
        public final Builder sSES3(SSES3 sSES3) {
            this.sSES3 = sSES3;
            return this;
        }

        /**
         * <p>Specifies the use of SSE-KMS to encrypt delivered inventory reports.</p>
         */
        public final Builder sSEKMS(SSEKMS sSEKMS) {
            this.sSEKMS = sSEKMS;
            return this;
        }
    }
}
