// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class SSEKMS {
    /**
     * <p>Specifies the ID of the AWS Key Management Service (AWS KMS) symmetric customer managed
     *          customer master key (CMK) to use for encrypting inventory reports.</p>
     */
    String keyId;

    SSEKMS() {
        this.keyId = "";
    }

    protected SSEKMS(BuilderImpl builder) {
        this.keyId = builder.keyId;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public interface Builder {
        Builder keyId(String keyId);

        SSEKMS build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Specifies the ID of the AWS Key Management Service (AWS KMS) symmetric customer managed
         *          customer master key (CMK) to use for encrypting inventory reports.</p>
         */
        String keyId;

        protected BuilderImpl() {
        }

        private BuilderImpl(SSEKMS model) {
            keyId(model.keyId);
        }

        public SSEKMS build() {
            return new SSEKMS(this);
        }

        public final Builder keyId(String keyId) {
            this.keyId = keyId;
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

        public String keyId() {
            return keyId;
        }

        public void setKeyId(final String keyId) {
            this.keyId = keyId;
        }
    }
}
