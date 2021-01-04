// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.time.Instant;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class CreateMultipartUploadOutput {
    /**
     * <p>If the bucket has a lifecycle rule configured with an action to abort incomplete
     *          multipart uploads and the prefix in the lifecycle rule matches the object name in the
     *          request, the response includes this header. The header indicates when the initiated
     *          multipart upload becomes eligible for an abort operation. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/mpuoverview.html#mpu-abort-incomplete-mpu-lifecycle-config">
     *             Aborting Incomplete Multipart Uploads Using a Bucket Lifecycle Policy</a>.</p>
     *
     *          <p>The response also includes the <code>x-amz-abort-rule-id</code> header that provides the
     *          ID of the lifecycle configuration rule that defines this action.</p>
     */
    Instant abortDate;

    /**
     * <p>This header is returned along with the <code>x-amz-abort-date</code> header. It
     *          identifies the applicable lifecycle configuration rule that defines the action to abort
     *          incomplete multipart uploads.</p>
     */
    String abortRuleId;

    /**
     * <p>The name of the bucket to which the multipart upload was initiated. </p>
     *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     */
    String bucket;

    /**
     * <p>Object key for which the multipart upload was initiated.</p>
     */
    String key;

    /**
     * <p>ID for the initiated multipart upload.</p>
     */
    String uploadId;

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
     * <p>Indicates whether the multipart upload uses an S3 Bucket Key for server-side encryption with AWS KMS (SSE-KMS).</p>
     */
    Boolean bucketKeyEnabled;

    RequestCharged requestCharged;

    CreateMultipartUploadOutput() {
        this.abortDate = null;
        this.abortRuleId = "";
        this.bucket = "";
        this.key = "";
        this.uploadId = "";
        this.serverSideEncryption = null;
        this.sSECustomerAlgorithm = "";
        this.sSECustomerKeyMD5 = "";
        this.sSEKMSKeyId = "";
        this.sSEKMSEncryptionContext = "";
        this.bucketKeyEnabled = null;
        this.requestCharged = null;
    }

    protected CreateMultipartUploadOutput(BuilderImpl builder) {
        this.abortDate = builder.abortDate;
        this.abortRuleId = builder.abortRuleId;
        this.bucket = builder.bucket;
        this.key = builder.key;
        this.uploadId = builder.uploadId;
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
        return Objects.hash(CreateMultipartUploadOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof CreateMultipartUploadOutput);
    }

    public Instant abortDate() {
        return abortDate;
    }

    public String abortRuleId() {
        return abortRuleId;
    }

    public String bucket() {
        return bucket;
    }

    public String key() {
        return key;
    }

    public String uploadId() {
        return uploadId;
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

    public void setAbortDate(final Instant abortDate) {
        this.abortDate = abortDate;
    }

    public void setAbortRuleId(final String abortRuleId) {
        this.abortRuleId = abortRuleId;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public void setUploadId(final String uploadId) {
        this.uploadId = uploadId;
    }

    public void setServerSideEncryption(final ServerSideEncryption serverSideEncryption) {
        this.serverSideEncryption = serverSideEncryption;
    }

    public void setSSECustomerAlgorithm(final String sSECustomerAlgorithm) {
        this.sSECustomerAlgorithm = sSECustomerAlgorithm;
    }

    public void setSSECustomerKeyMD5(final String sSECustomerKeyMD5) {
        this.sSECustomerKeyMD5 = sSECustomerKeyMD5;
    }

    public void setSSEKMSKeyId(final String sSEKMSKeyId) {
        this.sSEKMSKeyId = sSEKMSKeyId;
    }

    public void setSSEKMSEncryptionContext(final String sSEKMSEncryptionContext) {
        this.sSEKMSEncryptionContext = sSEKMSEncryptionContext;
    }

    public void setBucketKeyEnabled(final Boolean bucketKeyEnabled) {
        this.bucketKeyEnabled = bucketKeyEnabled;
    }

    public void setRequestCharged(final RequestCharged requestCharged) {
        this.requestCharged = requestCharged;
    }

    public interface Builder {
        Builder abortDate(Instant abortDate);

        Builder abortRuleId(String abortRuleId);

        Builder bucket(String bucket);

        Builder key(String key);

        Builder uploadId(String uploadId);

        Builder serverSideEncryption(ServerSideEncryption serverSideEncryption);

        Builder sSECustomerAlgorithm(String sSECustomerAlgorithm);

        Builder sSECustomerKeyMD5(String sSECustomerKeyMD5);

        Builder sSEKMSKeyId(String sSEKMSKeyId);

        Builder sSEKMSEncryptionContext(String sSEKMSEncryptionContext);

        Builder bucketKeyEnabled(Boolean bucketKeyEnabled);

        Builder requestCharged(RequestCharged requestCharged);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>If the bucket has a lifecycle rule configured with an action to abort incomplete
         *          multipart uploads and the prefix in the lifecycle rule matches the object name in the
         *          request, the response includes this header. The header indicates when the initiated
         *          multipart upload becomes eligible for an abort operation. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/mpuoverview.html#mpu-abort-incomplete-mpu-lifecycle-config">
         *             Aborting Incomplete Multipart Uploads Using a Bucket Lifecycle Policy</a>.</p>
         *
         *          <p>The response also includes the <code>x-amz-abort-rule-id</code> header that provides the
         *          ID of the lifecycle configuration rule that defines this action.</p>
         */
        Instant abortDate;

        /**
         * <p>This header is returned along with the <code>x-amz-abort-date</code> header. It
         *          identifies the applicable lifecycle configuration rule that defines the action to abort
         *          incomplete multipart uploads.</p>
         */
        String abortRuleId;

        /**
         * <p>The name of the bucket to which the multipart upload was initiated. </p>
         *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        String bucket;

        /**
         * <p>Object key for which the multipart upload was initiated.</p>
         */
        String key;

        /**
         * <p>ID for the initiated multipart upload.</p>
         */
        String uploadId;

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
         * <p>Indicates whether the multipart upload uses an S3 Bucket Key for server-side encryption with AWS KMS (SSE-KMS).</p>
         */
        Boolean bucketKeyEnabled;

        RequestCharged requestCharged;

        protected BuilderImpl() {
        }

        private BuilderImpl(CreateMultipartUploadOutput model) {
            abortDate(model.abortDate);
            abortRuleId(model.abortRuleId);
            bucket(model.bucket);
            key(model.key);
            uploadId(model.uploadId);
            serverSideEncryption(model.serverSideEncryption);
            sSECustomerAlgorithm(model.sSECustomerAlgorithm);
            sSECustomerKeyMD5(model.sSECustomerKeyMD5);
            sSEKMSKeyId(model.sSEKMSKeyId);
            sSEKMSEncryptionContext(model.sSEKMSEncryptionContext);
            bucketKeyEnabled(model.bucketKeyEnabled);
            requestCharged(model.requestCharged);
        }

        public CreateMultipartUploadOutput build() {
            return new CreateMultipartUploadOutput(this);
        }

        public final Builder abortDate(Instant abortDate) {
            this.abortDate = abortDate;
            return this;
        }

        public final Builder abortRuleId(String abortRuleId) {
            this.abortRuleId = abortRuleId;
            return this;
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder key(String key) {
            this.key = key;
            return this;
        }

        public final Builder uploadId(String uploadId) {
            this.uploadId = uploadId;
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

        public Instant abortDate() {
            return abortDate;
        }

        public String abortRuleId() {
            return abortRuleId;
        }

        public String bucket() {
            return bucket;
        }

        public String key() {
            return key;
        }

        public String uploadId() {
            return uploadId;
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

        public void setAbortDate(final Instant abortDate) {
            this.abortDate = abortDate;
        }

        public void setAbortRuleId(final String abortRuleId) {
            this.abortRuleId = abortRuleId;
        }

        public void setBucket(final String bucket) {
            this.bucket = bucket;
        }

        public void setKey(final String key) {
            this.key = key;
        }

        public void setUploadId(final String uploadId) {
            this.uploadId = uploadId;
        }

        public void setServerSideEncryption(final ServerSideEncryption serverSideEncryption) {
            this.serverSideEncryption = serverSideEncryption;
        }

        public void setSSECustomerAlgorithm(final String sSECustomerAlgorithm) {
            this.sSECustomerAlgorithm = sSECustomerAlgorithm;
        }

        public void setSSECustomerKeyMD5(final String sSECustomerKeyMD5) {
            this.sSECustomerKeyMD5 = sSECustomerKeyMD5;
        }

        public void setSSEKMSKeyId(final String sSEKMSKeyId) {
            this.sSEKMSKeyId = sSEKMSKeyId;
        }

        public void setSSEKMSEncryptionContext(final String sSEKMSEncryptionContext) {
            this.sSEKMSEncryptionContext = sSEKMSEncryptionContext;
        }

        public void setBucketKeyEnabled(final Boolean bucketKeyEnabled) {
            this.bucketKeyEnabled = bucketKeyEnabled;
        }

        public void setRequestCharged(final RequestCharged requestCharged) {
            this.requestCharged = requestCharged;
        }
    }
}
