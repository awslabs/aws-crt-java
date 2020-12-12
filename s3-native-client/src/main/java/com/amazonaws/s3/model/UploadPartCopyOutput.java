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
public class UploadPartCopyOutput {
    private String copySourceVersionId;

    private CopyPartResult copyPartResult;

    private ServerSideEncryption serverSideEncryption;

    private String sSECustomerAlgorithm;

    private String sSECustomerKeyMD5;

    private String sSEKMSKeyId;

    private Boolean bucketKeyEnabled;

    private RequestCharged requestCharged;

    private UploadPartCopyOutput() {
        this.copySourceVersionId = null;
        this.copyPartResult = null;
        this.serverSideEncryption = null;
        this.sSECustomerAlgorithm = null;
        this.sSECustomerKeyMD5 = null;
        this.sSEKMSKeyId = null;
        this.bucketKeyEnabled = null;
        this.requestCharged = null;
    }

    private UploadPartCopyOutput(Builder builder) {
        this.copySourceVersionId = builder.copySourceVersionId;
        this.copyPartResult = builder.copyPartResult;
        this.serverSideEncryption = builder.serverSideEncryption;
        this.sSECustomerAlgorithm = builder.sSECustomerAlgorithm;
        this.sSECustomerKeyMD5 = builder.sSECustomerKeyMD5;
        this.sSEKMSKeyId = builder.sSEKMSKeyId;
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
        return Objects.hash(UploadPartCopyOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof UploadPartCopyOutput);
    }

    public String copySourceVersionId() {
        return copySourceVersionId;
    }

    public void setCopySourceVersionId(final String copySourceVersionId) {
        this.copySourceVersionId = copySourceVersionId;
    }

    public CopyPartResult copyPartResult() {
        return copyPartResult;
    }

    public void setCopyPartResult(final CopyPartResult copyPartResult) {
        this.copyPartResult = copyPartResult;
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
        private String copySourceVersionId;

        private CopyPartResult copyPartResult;

        private ServerSideEncryption serverSideEncryption;

        private String sSECustomerAlgorithm;

        private String sSECustomerKeyMD5;

        private String sSEKMSKeyId;

        private Boolean bucketKeyEnabled;

        private RequestCharged requestCharged;

        private Builder() {
        }

        private Builder(UploadPartCopyOutput model) {
            copySourceVersionId(model.copySourceVersionId);
            copyPartResult(model.copyPartResult);
            serverSideEncryption(model.serverSideEncryption);
            sSECustomerAlgorithm(model.sSECustomerAlgorithm);
            sSECustomerKeyMD5(model.sSECustomerKeyMD5);
            sSEKMSKeyId(model.sSEKMSKeyId);
            bucketKeyEnabled(model.bucketKeyEnabled);
            requestCharged(model.requestCharged);
        }

        public UploadPartCopyOutput build() {
            return new com.amazonaws.s3.model.UploadPartCopyOutput(this);
        }

        /**
         * <p>The version of the source object that was copied, if you have enabled versioning on the
         *          source bucket.</p>
         */
        public final Builder copySourceVersionId(String copySourceVersionId) {
            this.copySourceVersionId = copySourceVersionId;
            return this;
        }

        /**
         * <p>Container for all response elements.</p>
         */
        public final Builder copyPartResult(CopyPartResult copyPartResult) {
            this.copyPartResult = copyPartResult;
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
         * <p>Indicates whether the multipart upload uses an S3 Bucket Key for server-side encryption with AWS KMS (SSE-KMS).</p>
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
