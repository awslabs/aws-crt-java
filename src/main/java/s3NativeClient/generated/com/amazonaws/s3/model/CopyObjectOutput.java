// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class CopyObjectOutput {
    /**
     * <p>Container for all response elements.</p>
     */
    CopyObjectResult copyObjectResult;

    /**
     * <p>If the object expiration is configured, the response includes this header.</p>
     */
    String expiration;

    /**
     * <p>Version of the copied object in the destination bucket.</p>
     */
    String copySourceVersionId;

    /**
     * <p>Version ID of the newly created copy.</p>
     */
    String versionId;

    /**
     * <p>The server-side encryption algorithm used when storing this object in Amazon S3 (for example,
     *          AES256, aws:kms).</p>
     */
    ServerSideEncryption serverSideEncryption;

    /**
     * <p>If server-side encryption with a customer-provided encryption key was requested, the
     *          response will include this header confirming the encryption algorithm used.</p>
     */
    String sSECustomerAlgorithm;

    /**
     * <p>If server-side encryption with a customer-provided encryption key was requested, the
     *          response will include this header to provide round-trip message integrity verification of
     *          the customer-provided encryption key.</p>
     */
    String sSECustomerKeyMD5;

    /**
     * <p>If present, specifies the ID of the AWS Key Management Service (AWS KMS) symmetric
     *          customer managed customer master key (CMK) that was used for the object.</p>
     */
    String sSEKMSKeyId;

    /**
     * <p>If present, specifies the AWS KMS Encryption Context to use for object encryption. The
     *          value of this header is a base64-encoded UTF-8 string holding JSON with the encryption
     *          context key-value pairs.</p>
     */
    String sSEKMSEncryptionContext;

    /**
     * <p>Indicates whether the copied object uses an S3 Bucket Key for server-side encryption with AWS KMS (SSE-KMS).</p>
     */
    Boolean bucketKeyEnabled;

    RequestCharged requestCharged;

    CopyObjectOutput() {
        this.copyObjectResult = null;
        this.expiration = "";
        this.copySourceVersionId = "";
        this.versionId = "";
        this.serverSideEncryption = null;
        this.sSECustomerAlgorithm = "";
        this.sSECustomerKeyMD5 = "";
        this.sSEKMSKeyId = "";
        this.sSEKMSEncryptionContext = "";
        this.bucketKeyEnabled = null;
        this.requestCharged = null;
    }

    protected CopyObjectOutput(BuilderImpl builder) {
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

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public String expiration() {
        return expiration;
    }

    public String copySourceVersionId() {
        return copySourceVersionId;
    }

    public String versionId() {
        return versionId;
    }

    public ServerSideEncryption serverSideEncryption() {
        return serverSideEncryption;
    }

    public String sSECustomerAlgorithm() {
        return sSECustomerAlgorithm;
    }

    public String sSECustomerKeyMD5() {
        return sSECustomerKeyMD5;
    }

    public String sSEKMSKeyId() {
        return sSEKMSKeyId;
    }

    public String sSEKMSEncryptionContext() {
        return sSEKMSEncryptionContext;
    }

    public Boolean bucketKeyEnabled() {
        return bucketKeyEnabled;
    }

    public RequestCharged requestCharged() {
        return requestCharged;
    }

    public interface Builder {
        Builder copyObjectResult(CopyObjectResult copyObjectResult);

        Builder expiration(String expiration);

        Builder copySourceVersionId(String copySourceVersionId);

        Builder versionId(String versionId);

        Builder serverSideEncryption(ServerSideEncryption serverSideEncryption);

        Builder sSECustomerAlgorithm(String sSECustomerAlgorithm);

        Builder sSECustomerKeyMD5(String sSECustomerKeyMD5);

        Builder sSEKMSKeyId(String sSEKMSKeyId);

        Builder sSEKMSEncryptionContext(String sSEKMSEncryptionContext);

        Builder bucketKeyEnabled(Boolean bucketKeyEnabled);

        Builder requestCharged(RequestCharged requestCharged);

        CopyObjectOutput build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Container for all response elements.</p>
         */
        CopyObjectResult copyObjectResult;

        /**
         * <p>If the object expiration is configured, the response includes this header.</p>
         */
        String expiration;

        /**
         * <p>Version of the copied object in the destination bucket.</p>
         */
        String copySourceVersionId;

        /**
         * <p>Version ID of the newly created copy.</p>
         */
        String versionId;

        /**
         * <p>The server-side encryption algorithm used when storing this object in Amazon S3 (for example,
         *          AES256, aws:kms).</p>
         */
        ServerSideEncryption serverSideEncryption;

        /**
         * <p>If server-side encryption with a customer-provided encryption key was requested, the
         *          response will include this header confirming the encryption algorithm used.</p>
         */
        String sSECustomerAlgorithm;

        /**
         * <p>If server-side encryption with a customer-provided encryption key was requested, the
         *          response will include this header to provide round-trip message integrity verification of
         *          the customer-provided encryption key.</p>
         */
        String sSECustomerKeyMD5;

        /**
         * <p>If present, specifies the ID of the AWS Key Management Service (AWS KMS) symmetric
         *          customer managed customer master key (CMK) that was used for the object.</p>
         */
        String sSEKMSKeyId;

        /**
         * <p>If present, specifies the AWS KMS Encryption Context to use for object encryption. The
         *          value of this header is a base64-encoded UTF-8 string holding JSON with the encryption
         *          context key-value pairs.</p>
         */
        String sSEKMSEncryptionContext;

        /**
         * <p>Indicates whether the copied object uses an S3 Bucket Key for server-side encryption with AWS KMS (SSE-KMS).</p>
         */
        Boolean bucketKeyEnabled;

        RequestCharged requestCharged;

        protected BuilderImpl() {
        }

        private BuilderImpl(CopyObjectOutput model) {
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
            return new CopyObjectOutput(this);
        }

        public final Builder copyObjectResult(CopyObjectResult copyObjectResult) {
            this.copyObjectResult = copyObjectResult;
            return this;
        }

        public final Builder expiration(String expiration) {
            this.expiration = expiration;
            return this;
        }

        public final Builder copySourceVersionId(String copySourceVersionId) {
            this.copySourceVersionId = copySourceVersionId;
            return this;
        }

        public final Builder versionId(String versionId) {
            this.versionId = versionId;
            return this;
        }

        public final Builder serverSideEncryption(ServerSideEncryption serverSideEncryption) {
            this.serverSideEncryption = serverSideEncryption;
            return this;
        }

        public final Builder sSECustomerAlgorithm(String sSECustomerAlgorithm) {
            this.sSECustomerAlgorithm = sSECustomerAlgorithm;
            return this;
        }

        public final Builder sSECustomerKeyMD5(String sSECustomerKeyMD5) {
            this.sSECustomerKeyMD5 = sSECustomerKeyMD5;
            return this;
        }

        public final Builder sSEKMSKeyId(String sSEKMSKeyId) {
            this.sSEKMSKeyId = sSEKMSKeyId;
            return this;
        }

        public final Builder sSEKMSEncryptionContext(String sSEKMSEncryptionContext) {
            this.sSEKMSEncryptionContext = sSEKMSEncryptionContext;
            return this;
        }

        public final Builder bucketKeyEnabled(Boolean bucketKeyEnabled) {
            this.bucketKeyEnabled = bucketKeyEnabled;
            return this;
        }

        public final Builder requestCharged(RequestCharged requestCharged) {
            this.requestCharged = requestCharged;
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

        public CopyObjectResult copyObjectResult() {
            return copyObjectResult;
        }

        public String expiration() {
            return expiration;
        }

        public String copySourceVersionId() {
            return copySourceVersionId;
        }

        public String versionId() {
            return versionId;
        }

        public ServerSideEncryption serverSideEncryption() {
            return serverSideEncryption;
        }

        public String sSECustomerAlgorithm() {
            return sSECustomerAlgorithm;
        }

        public String sSECustomerKeyMD5() {
            return sSECustomerKeyMD5;
        }

        public String sSEKMSKeyId() {
            return sSEKMSKeyId;
        }

        public String sSEKMSEncryptionContext() {
            return sSEKMSEncryptionContext;
        }

        public Boolean bucketKeyEnabled() {
            return bucketKeyEnabled;
        }

        public RequestCharged requestCharged() {
            return requestCharged;
        }
    }
}
