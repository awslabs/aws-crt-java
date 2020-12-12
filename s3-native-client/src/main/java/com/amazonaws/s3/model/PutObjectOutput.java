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
public class PutObjectOutput {
    private String expiration;

    private String eTag;

    private ServerSideEncryption serverSideEncryption;

    private String versionId;

    private String sSECustomerAlgorithm;

    private String sSECustomerKeyMD5;

    private String sSEKMSKeyId;

    private String sSEKMSEncryptionContext;

    private Boolean bucketKeyEnabled;

    private RequestCharged requestCharged;

    private PutObjectOutput() {
        this.expiration = null;
        this.eTag = null;
        this.serverSideEncryption = null;
        this.versionId = null;
        this.sSECustomerAlgorithm = null;
        this.sSECustomerKeyMD5 = null;
        this.sSEKMSKeyId = null;
        this.sSEKMSEncryptionContext = null;
        this.bucketKeyEnabled = null;
        this.requestCharged = null;
    }

    private PutObjectOutput(Builder builder) {
        this.expiration = builder.expiration;
        this.eTag = builder.eTag;
        this.serverSideEncryption = builder.serverSideEncryption;
        this.versionId = builder.versionId;
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
        return Objects.hash(PutObjectOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof PutObjectOutput);
    }

    public String expiration() {
        return expiration;
    }

    public void setExpiration(final String expiration) {
        this.expiration = expiration;
    }

    public String eTag() {
        return eTag;
    }

    public void setETag(final String eTag) {
        this.eTag = eTag;
    }

    public ServerSideEncryption serverSideEncryption() {
        return serverSideEncryption;
    }

    public void setServerSideEncryption(final ServerSideEncryption serverSideEncryption) {
        this.serverSideEncryption = serverSideEncryption;
    }

    public String versionId() {
        return versionId;
    }

    public void setVersionId(final String versionId) {
        this.versionId = versionId;
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
        private String expiration;

        private String eTag;

        private ServerSideEncryption serverSideEncryption;

        private String versionId;

        private String sSECustomerAlgorithm;

        private String sSECustomerKeyMD5;

        private String sSEKMSKeyId;

        private String sSEKMSEncryptionContext;

        private Boolean bucketKeyEnabled;

        private RequestCharged requestCharged;

        private Builder() {
        }

        private Builder(PutObjectOutput model) {
            expiration(model.expiration);
            eTag(model.eTag);
            serverSideEncryption(model.serverSideEncryption);
            versionId(model.versionId);
            sSECustomerAlgorithm(model.sSECustomerAlgorithm);
            sSECustomerKeyMD5(model.sSECustomerKeyMD5);
            sSEKMSKeyId(model.sSEKMSKeyId);
            sSEKMSEncryptionContext(model.sSEKMSEncryptionContext);
            bucketKeyEnabled(model.bucketKeyEnabled);
            requestCharged(model.requestCharged);
        }

        public PutObjectOutput build() {
            return new com.amazonaws.s3.model.PutObjectOutput(this);
        }

        /**
         * <p> If the expiration is configured for the object (see <a href="https://docs.aws.amazon.com/AmazonS3/latest/API/API_PutBucketLifecycleConfiguration.html">PutBucketLifecycleConfiguration</a>), the response includes this header. It
         *          includes the expiry-date and rule-id key-value pairs that provide information about object
         *          expiration. The value of the rule-id is URL encoded.</p>
         */
        public final Builder expiration(String expiration) {
            this.expiration = expiration;
            return this;
        }

        /**
         * <p>Entity tag for the uploaded object.</p>
         */
        public final Builder eTag(String eTag) {
            this.eTag = eTag;
            return this;
        }

        /**
         * <p>If you specified server-side encryption either with an AWS KMS customer master key (CMK)
         *          or Amazon S3-managed encryption key in your PUT request, the response includes this header. It
         *          confirms the encryption algorithm that Amazon S3 used to encrypt the object.</p>
         */
        public final Builder serverSideEncryption(ServerSideEncryption serverSideEncryption) {
            this.serverSideEncryption = serverSideEncryption;
            return this;
        }

        /**
         * <p>Version of the object.</p>
         */
        public final Builder versionId(String versionId) {
            this.versionId = versionId;
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
         * <p>If <code>x-amz-server-side-encryption</code> is present and has the value of
         *             <code>aws:kms</code>, this header specifies the ID of the AWS Key Management Service
         *          (AWS KMS) symmetric customer managed customer master key (CMK) that was used for the
         *          object. </p>
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
         * <p>Indicates whether the uploaded object uses an S3 Bucket Key for server-side encryption with AWS KMS (SSE-KMS).</p>
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
