// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class ServerSideEncryptionByDefault {
    private ServerSideEncryption sSEAlgorithm;

    private String kMSMasterKeyID;

    private ServerSideEncryptionByDefault() {
        this.sSEAlgorithm = null;
        this.kMSMasterKeyID = null;
    }

    private ServerSideEncryptionByDefault(Builder builder) {
        this.sSEAlgorithm = builder.sSEAlgorithm;
        this.kMSMasterKeyID = builder.kMSMasterKeyID;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ServerSideEncryptionByDefault.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof ServerSideEncryptionByDefault);
    }

    public ServerSideEncryption sSEAlgorithm() {
        return sSEAlgorithm;
    }

    public void setSSEAlgorithm(final ServerSideEncryption sSEAlgorithm) {
        this.sSEAlgorithm = sSEAlgorithm;
    }

    public String kMSMasterKeyID() {
        return kMSMasterKeyID;
    }

    public void setKMSMasterKeyID(final String kMSMasterKeyID) {
        this.kMSMasterKeyID = kMSMasterKeyID;
    }

    static final class Builder {
        private ServerSideEncryption sSEAlgorithm;

        private String kMSMasterKeyID;

        private Builder() {
        }

        private Builder(ServerSideEncryptionByDefault model) {
            sSEAlgorithm(model.sSEAlgorithm);
            kMSMasterKeyID(model.kMSMasterKeyID);
        }

        public ServerSideEncryptionByDefault build() {
            return new com.amazonaws.s3.model.ServerSideEncryptionByDefault(this);
        }

        /**
         * <p>Server-side encryption algorithm to use for the default encryption.</p>
         */
        public final Builder sSEAlgorithm(ServerSideEncryption sSEAlgorithm) {
            this.sSEAlgorithm = sSEAlgorithm;
            return this;
        }

        /**
         * <p>AWS Key Management Service (KMS) customer master key ID to use for the default
         *          encryption. This parameter is allowed if and only if <code>SSEAlgorithm</code> is set to
         *             <code>aws:kms</code>.</p>
         *          <p>You can specify the key ID or the Amazon Resource Name (ARN) of the CMK. However, if you
         *          are using encryption with cross-account operations, you must use a fully qualified CMK ARN.
         *          For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/bucket-encryption.html#bucket-encryption-update-bucket-policy">Using encryption for cross-account operations</a>. </p>
         *          <p>
         *             <b>For example:</b>
         *          </p>
         *          <ul>
         *             <li>
         *                <p>Key ID: <code>1234abcd-12ab-34cd-56ef-1234567890ab</code>
         *                </p>
         *             </li>
         *             <li>
         *                <p>Key ARN:
         *                   <code>arn:aws:kms:us-east-2:111122223333:key/1234abcd-12ab-34cd-56ef-1234567890ab</code>
         *                </p>
         *             </li>
         *          </ul>
         *          <important>
         *             <p>Amazon S3 only supports symmetric CMKs and not asymmetric CMKs. For more information, see
         *                <a href="https://docs.aws.amazon.com/kms/latest/developerguide/symmetric-asymmetric.html">Using Symmetric and
         *                Asymmetric Keys</a> in the <i>AWS Key Management Service Developer
         *                Guide</i>.</p>
         *          </important>
         */
        public final Builder kMSMasterKeyID(String kMSMasterKeyID) {
            this.kMSMasterKeyID = kMSMasterKeyID;
            return this;
        }
    }
}
