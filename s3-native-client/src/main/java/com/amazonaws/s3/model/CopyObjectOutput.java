// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class CopyObjectOutput {
    private CopyObjectResult copyObjectResult;

    private String expiration;

    private String copySourceVersionId;

    private String versionId;

    private ServerSideEncryption serverSideEncryption;

    private String sSECustomerAlgorithm;

    private String sSECustomerKeyMD5;

    private String sSEKMSKeyId;

    private String sSEKMSEncryptionContext;

    private Boolean bucketKeyEnabled;

    private RequestCharged requestCharged;

    private CopyObjectOutput() {
        this.copyObjectResult = null;
        this.expiration = null;
        this.copySourceVersionId = null;
        this.versionId = null;
        this.serverSideEncryption = null;
        this.sSECustomerAlgorithm = null;
        this.sSECustomerKeyMD5 = null;
        this.sSEKMSKeyId = null;
        this.sSEKMSEncryptionContext = null;
        this.bucketKeyEnabled = null;
        this.requestCharged = null;
    }

    private CopyObjectOutput(Builder builder) {
        this.copyObjectResult = builder.copyObjectResult;
        this.expiration = builder.expiration;
        this.copySourceVersionId = builder.copySourceVersionId;
        this.versionId = builder.versionId;
        this.serverSideEncryption = builder.serverSideEncryption;
        this.sSECustomerAlgorithm = builder.sSECustomerAlgorithm;
        this.sSECustomerKeyMD5 = builder.sSECustomerKeyMD5;
        this.sSEKMSKeyId = builder.sSEKMSKeyId;
        this.sSEKMSEncryptionContext = builder.sSEKMSEncryptionContext;
        this.bucketKeyEnabled = builder.bucketKeyEnabled;
        this.requestCharged = builder.requestCharged;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(CopyObjectOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof CopyObjectOutput);
    }

    public CopyObjectResult copyObjectResult() {
        return copyObjectResult;
    }

    public void setCopyObjectResult(final CopyObjectResult copyObjectResult) {
        this.copyObjectResult = copyObjectResult;
    }

    public String expiration() {
        return expiration;
    }

    public void setExpiration(final String expiration) {
        this.expiration = expiration;
    }

    public String copySourceVersionId() {
        return copySourceVersionId;
    }

    public void setCopySourceVersionId(final String copySourceVersionId) {
        this.copySourceVersionId = copySourceVersionId;
    }

    public String versionId() {
        return versionId;
    }

    public void setVersionId(final String versionId) {
        this.versionId = versionId;
    }

    public ServerSideEncryption serverSideEncryption() {
        return serverSideEncryption;
    }

    public void setServerSideEncryption(final ServerSideEncryption serverSideEncryption) {
        this.serverSideEncryption = serverSideEncryption;
    }

    public String sSECustomerAlgorithm() {
        return sSECustomerAlgorithm;
    }

    public void setSSECustomerAlgorithm(final String sSECustomerAlgorithm) {
        this.sSECustomerAlgorithm = sSECustomerAlgorithm;
    }

    public String sSECustomerKeyMD5() {
        return sSECustomerKeyMD5;
    }

    public void setSSECustomerKeyMD5(final String sSECustomerKeyMD5) {
        this.sSECustomerKeyMD5 = sSECustomerKeyMD5;
    }

    public String sSEKMSKeyId() {
        return sSEKMSKeyId;
    }

    public void setSSEKMSKeyId(final String sSEKMSKeyId) {
        this.sSEKMSKeyId = sSEKMSKeyId;
    }

    public String sSEKMSEncryptionContext() {
        return sSEKMSEncryptionContext;
    }

    public void setSSEKMSEncryptionContext(final String sSEKMSEncryptionContext) {
        this.sSEKMSEncryptionContext = sSEKMSEncryptionContext;
    }

    public Boolean bucketKeyEnabled() {
        return bucketKeyEnabled;
    }

    public void setBucketKeyEnabled(final Boolean bucketKeyEnabled) {
        this.bucketKeyEnabled = bucketKeyEnabled;
    }

    public RequestCharged requestCharged() {
        return requestCharged;
    }

    public void setRequestCharged(final RequestCharged requestCharged) {
        this.requestCharged = requestCharged;
    }

    static final class Builder {
        private CopyObjectResult copyObjectResult;

        private String expiration;

        private String copySourceVersionId;

        private String versionId;

        private ServerSideEncryption serverSideEncryption;

        private String sSECustomerAlgorithm;

        private String sSECustomerKeyMD5;

        private String sSEKMSKeyId;

        private String sSEKMSEncryptionContext;

        private Boolean bucketKeyEnabled;

        private RequestCharged requestCharged;

        private Builder() {
        }

        private Builder(CopyObjectOutput model) {
            copyObjectResult(model.copyObjectResult);
            expiration(model.expiration);
            copySourceVersionId(model.copySourceVersionId);
            versionId(model.versionId);
            serverSideEncryption(model.serverSideEncryption);
            sSECustomerAlgorithm(model.sSECustomerAlgorithm);
            sSECustomerKeyMD5(model.sSECustomerKeyMD5);
            sSEKMSKeyId(model.sSEKMSKeyId);
            sSEKMSEncryptionContext(model.sSEKMSEncryptionContext);
            bucketKeyEnabled(model.bucketKeyEnabled);
            requestCharged(model.requestCharged);
        }

        public CopyObjectOutput build() {
            return new com.amazonaws.s3.model.CopyObjectOutput(this);
        }

        /**
         * <p>Container for all response elements.</p>
         */
        public final Builder copyObjectResult(CopyObjectResult copyObjectResult) {
            this.copyObjectResult = copyObjectResult;
            return this;
        }

        /**
         * <p>If the object expiration is configured, the response includes this header.</p>
         */
        public final Builder expiration(String expiration) {
            this.expiration = expiration;
            return this;
        }

        /**
         * <p>Version of the copied object in the destination bucket.</p>
         */
        public final Builder copySourceVersionId(String copySourceVersionId) {
            this.copySourceVersionId = copySourceVersionId;
            return this;
        }

        /**
         * <p>Version ID of the newly created copy.</p>
         */
        public final Builder versionId(String versionId) {
            this.versionId = versionId;
            return this;
        }

        /**
         * <p>The server-side encryption algorithm used when storing this object in Amazon S3 (for example,
         *          AES256, aws:kms).</p>
         */
        public final Builder serverSideEncryption(ServerSideEncryption serverSideEncryption) {
            this.serverSideEncryption = serverSideEncryption;
            return this;
        }

        /**
         * <p>If server-side encryption with a customer-provided encryption key was requested, the
         *          response will include this header confirming the encryption algorithm used.</p>
         */
        public final Builder sSECustomerAlgorithm(String sSECustomerAlgorithm) {
            this.sSECustomerAlgorithm = sSECustomerAlgorithm;
            return this;
        }

        /**
         * <p>If server-side encryption with a customer-provided encryption key was requested, the
         *          response will include this header to provide round-trip message integrity verification of
         *          the customer-provided encryption key.</p>
         */
        public final Builder sSECustomerKeyMD5(String sSECustomerKeyMD5) {
            this.sSECustomerKeyMD5 = sSECustomerKeyMD5;
            return this;
        }

        /**
         * <p>If present, specifies the ID of the AWS Key Management Service (AWS KMS) symmetric
         *          customer managed customer master key (CMK) that was used for the object.</p>
         */
        public final Builder sSEKMSKeyId(String sSEKMSKeyId) {
            this.sSEKMSKeyId = sSEKMSKeyId;
            return this;
        }

        /**
         * <p>If present, specifies the AWS KMS Encryption Context to use for object encryption. The
         *          value of this header is a base64-encoded UTF-8 string holding JSON with the encryption
         *          context key-value pairs.</p>
         */
        public final Builder sSEKMSEncryptionContext(String sSEKMSEncryptionContext) {
            this.sSEKMSEncryptionContext = sSEKMSEncryptionContext;
            return this;
        }

        /**
         * <p>Indicates whether the copied object uses an S3 Bucket Key for server-side encryption with AWS KMS (SSE-KMS).</p>
         */
        public final Builder bucketKeyEnabled(Boolean bucketKeyEnabled) {
            this.bucketKeyEnabled = bucketKeyEnabled;
            return this;
        }

        public final Builder requestCharged(RequestCharged requestCharged) {
            this.requestCharged = requestCharged;
            return this;
        }
    }
}
