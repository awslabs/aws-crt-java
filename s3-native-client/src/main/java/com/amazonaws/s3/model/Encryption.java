// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Encryption {
    private ServerSideEncryption encryptionType;

    private String kMSKeyId;

    private String kMSContext;

    private Encryption() {
        this.encryptionType = null;
        this.kMSKeyId = null;
        this.kMSContext = null;
    }

    private Encryption(Builder builder) {
        this.encryptionType = builder.encryptionType;
        this.kMSKeyId = builder.kMSKeyId;
        this.kMSContext = builder.kMSContext;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Encryption.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof Encryption);
    }

    public ServerSideEncryption encryptionType() {
        return encryptionType;
    }

    public void setEncryptionType(final ServerSideEncryption encryptionType) {
        this.encryptionType = encryptionType;
    }

    public String kMSKeyId() {
        return kMSKeyId;
    }

    public void setKMSKeyId(final String kMSKeyId) {
        this.kMSKeyId = kMSKeyId;
    }

    public String kMSContext() {
        return kMSContext;
    }

    public void setKMSContext(final String kMSContext) {
        this.kMSContext = kMSContext;
    }

    static final class Builder {
        private ServerSideEncryption encryptionType;

        private String kMSKeyId;

        private String kMSContext;

        private Builder() {
        }

        private Builder(Encryption model) {
            encryptionType(model.encryptionType);
            kMSKeyId(model.kMSKeyId);
            kMSContext(model.kMSContext);
        }

        public Encryption build() {
            return new com.amazonaws.s3.model.Encryption(this);
        }

        /**
         * <p>The server-side encryption algorithm used when storing job results in Amazon S3 (for example,
         *          AES256, aws:kms).</p>
         */
        public final Builder encryptionType(ServerSideEncryption encryptionType) {
            this.encryptionType = encryptionType;
            return this;
        }

        /**
         * <p>If the encryption type is <code>aws:kms</code>, this optional value specifies the ID of
         *          the symmetric customer managed AWS KMS CMK to use for encryption of job results. Amazon S3 only
         *          supports symmetric CMKs. For more information, see <a href="https://docs.aws.amazon.com/kms/latest/developerguide/symmetric-asymmetric.html">Using Symmetric and
         *             Asymmetric Keys</a> in the <i>AWS Key Management Service Developer
         *             Guide</i>.</p>
         */
        public final Builder kMSKeyId(String kMSKeyId) {
            this.kMSKeyId = kMSKeyId;
            return this;
        }

        /**
         * <p>If the encryption type is <code>aws:kms</code>, this optional value can be used to
         *          specify the encryption context for the restore results.</p>
         */
        public final Builder kMSContext(String kMSContext) {
            this.kMSContext = kMSContext;
            return this;
        }
    }
}
