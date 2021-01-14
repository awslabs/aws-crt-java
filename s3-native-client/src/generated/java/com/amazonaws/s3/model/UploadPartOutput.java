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
public class UploadPartOutput {
    /**
     * <p>The server-side encryption algorithm used when storing this object in Amazon S3 (for example,
     *          AES256, aws:kms).</p>
     */
    ServerSideEncryption serverSideEncryption;

    /**
     * <p>Entity tag for the uploaded object.</p>
     */
    String eTag;

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
     *          customer managed customer master key (CMK) was used for the object.</p>
     */
    String sSEKMSKeyId;

    /**
     * <p>Indicates whether the multipart upload uses an S3 Bucket Key for server-side encryption with AWS KMS (SSE-KMS).</p>
     */
    Boolean bucketKeyEnabled;

    RequestCharged requestCharged;

    UploadPartOutput() {
        this.serverSideEncryption = null;
        this.eTag = "";
        this.sSECustomerAlgorithm = "";
        this.sSECustomerKeyMD5 = "";
        this.sSEKMSKeyId = "";
        this.bucketKeyEnabled = null;
        this.requestCharged = null;
    }

    protected UploadPartOutput(BuilderImpl builder) {
        this.serverSideEncryption = builder.serverSideEncryption;
        this.eTag = builder.eTag;
        this.sSECustomerAlgorithm = builder.sSECustomerAlgorithm;
        this.sSECustomerKeyMD5 = builder.sSECustomerKeyMD5;
        this.sSEKMSKeyId = builder.sSEKMSKeyId;
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
        return Objects.hash(UploadPartOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof UploadPartOutput);
    }

    public ServerSideEncryption serverSideEncryption() {
        return serverSideEncryption;
    }

    public String eTag() {
        return eTag;
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

    public Boolean bucketKeyEnabled() {
        return bucketKeyEnabled;
    }

    public RequestCharged requestCharged() {
        return requestCharged;
    }

    public interface Builder {
        Builder serverSideEncryption(ServerSideEncryption serverSideEncryption);

        Builder eTag(String eTag);

        Builder sSECustomerAlgorithm(String sSECustomerAlgorithm);

        Builder sSECustomerKeyMD5(String sSECustomerKeyMD5);

        Builder sSEKMSKeyId(String sSEKMSKeyId);

        Builder bucketKeyEnabled(Boolean bucketKeyEnabled);

        Builder requestCharged(RequestCharged requestCharged);

        UploadPartOutput build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The server-side encryption algorithm used when storing this object in Amazon S3 (for example,
         *          AES256, aws:kms).</p>
         */
        ServerSideEncryption serverSideEncryption;

        /**
         * <p>Entity tag for the uploaded object.</p>
         */
        String eTag;

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
         *          customer managed customer master key (CMK) was used for the object.</p>
         */
        String sSEKMSKeyId;

        /**
         * <p>Indicates whether the multipart upload uses an S3 Bucket Key for server-side encryption with AWS KMS (SSE-KMS).</p>
         */
        Boolean bucketKeyEnabled;

        RequestCharged requestCharged;

        protected BuilderImpl() {
        }

        private BuilderImpl(UploadPartOutput model) {
            serverSideEncryption(model.serverSideEncryption);
            eTag(model.eTag);
            sSECustomerAlgorithm(model.sSECustomerAlgorithm);
            sSECustomerKeyMD5(model.sSECustomerKeyMD5);
            sSEKMSKeyId(model.sSEKMSKeyId);
            bucketKeyEnabled(model.bucketKeyEnabled);
            requestCharged(model.requestCharged);
        }

        public UploadPartOutput build() {
            return new UploadPartOutput(this);
        }

        public final Builder serverSideEncryption(ServerSideEncryption serverSideEncryption) {
            this.serverSideEncryption = serverSideEncryption;
            return this;
        }

        public final Builder eTag(String eTag) {
            this.eTag = eTag;
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

        public ServerSideEncryption serverSideEncryption() {
            return serverSideEncryption;
        }

        public String eTag() {
            return eTag;
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

        public Boolean bucketKeyEnabled() {
            return bucketKeyEnabled;
        }

        public RequestCharged requestCharged() {
            return requestCharged;
        }
    }
}
