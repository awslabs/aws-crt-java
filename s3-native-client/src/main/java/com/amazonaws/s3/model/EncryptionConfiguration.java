// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class EncryptionConfiguration {
    private String replicaKmsKeyID;

    private EncryptionConfiguration() {
        this.replicaKmsKeyID = null;
    }

    private EncryptionConfiguration(Builder builder) {
        this.replicaKmsKeyID = builder.replicaKmsKeyID;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(EncryptionConfiguration.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof EncryptionConfiguration);
    }

    public String replicaKmsKeyID() {
        return replicaKmsKeyID;
    }

    public void setReplicaKmsKeyID(final String replicaKmsKeyID) {
        this.replicaKmsKeyID = replicaKmsKeyID;
    }

    static final class Builder {
        private String replicaKmsKeyID;

        private Builder() {
        }

        private Builder(EncryptionConfiguration model) {
            replicaKmsKeyID(model.replicaKmsKeyID);
        }

        public EncryptionConfiguration build() {
            return new com.amazonaws.s3.model.EncryptionConfiguration(this);
        }

        /**
         * <p>Specifies the ID (Key ARN or Alias ARN) of the customer managed customer master key
         *          (CMK) stored in AWS Key Management Service (KMS) for the destination bucket. Amazon S3 uses
         *          this key to encrypt replica objects. Amazon S3 only supports symmetric customer managed CMKs.
         *          For more information, see <a href="https://docs.aws.amazon.com/kms/latest/developerguide/symmetric-asymmetric.html">Using Symmetric and
         *             Asymmetric Keys</a> in the <i>AWS Key Management Service Developer
         *             Guide</i>.</p>
         */
        public final Builder replicaKmsKeyID(String replicaKmsKeyID) {
            this.replicaKmsKeyID = replicaKmsKeyID;
            return this;
        }
    }
}
