// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class SSEKMS {
    private String keyId;

    private SSEKMS() {
        this.keyId = null;
    }

    private SSEKMS(Builder builder) {
        this.keyId = builder.keyId;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(SSEKMS.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof SSEKMS);
    }

    public String keyId() {
        return keyId;
    }

    public void setKeyId(final String keyId) {
        this.keyId = keyId;
    }

    static final class Builder {
        private String keyId;

        private Builder() {
        }

        private Builder(SSEKMS model) {
            keyId(model.keyId);
        }

        public SSEKMS build() {
            return new com.amazonaws.s3.model.SSEKMS(this);
        }

        /**
         * <p>Specifies the ID of the AWS Key Management Service (AWS KMS) symmetric customer managed
         *          customer master key (CMK) to use for encrypting inventory reports.</p>
         */
        public final Builder keyId(String keyId) {
            this.keyId = keyId;
            return this;
        }
    }
}
