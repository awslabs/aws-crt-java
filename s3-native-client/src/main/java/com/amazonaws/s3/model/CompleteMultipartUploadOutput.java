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
public class CompleteMultipartUploadOutput {
    /**
     * <p>The URI that identifies the newly created object.</p>
     */
    String location;

    /**
     * <p>The name of the bucket that contains the newly created object.</p>
     *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     */
    String bucket;

    /**
     * <p>The object key of the newly created object.</p>
     */
    String key;

    /**
     * <p>If the object expiration is configured, this will contain the expiration date
     *          (expiry-date) and rule ID (rule-id). The value of rule-id is URL encoded.</p>
     */
    String expiration;

    /**
     * <p>Entity tag that identifies the newly created object's data. Objects with different
     *          object data will have different entity tags. The entity tag is an opaque string. The entity
     *          tag may or may not be an MD5 digest of the object data. If the entity tag is not an MD5
     *          digest of the object data, it will contain one or more nonhexadecimal characters and/or
     *          will consist of less than 32 or more than 32 hexadecimal digits.</p>
     */
    String eTag;

    /**
     * <p>If you specified server-side encryption either with an Amazon S3-managed encryption key or an
     *          AWS KMS customer master key (CMK) in your initiate multipart upload request, the response
     *          includes this header. It confirms the encryption algorithm that Amazon S3 used to encrypt the
     *          object.</p>
     */
    ServerSideEncryption serverSideEncryption;

    /**
     * <p>Version ID of the newly created object, in case the bucket has versioning turned
     *          on.</p>
     */
    String versionId;

    /**
     * <p>If present, specifies the ID of the AWS Key Management Service (AWS KMS) symmetric
     *          customer managed customer master key (CMK) that was used for the object.</p>
     */
    String sSEKMSKeyId;

    /**
     * <p>Indicates whether the multipart upload uses an S3 Bucket Key for server-side encryption with AWS KMS (SSE-KMS).</p>
     */
    Boolean bucketKeyEnabled;

    RequestCharged requestCharged;

    CompleteMultipartUploadOutput() {
        this.location = "";
        this.bucket = "";
        this.key = "";
        this.expiration = "";
        this.eTag = "";
        this.serverSideEncryption = null;
        this.versionId = "";
        this.sSEKMSKeyId = "";
        this.bucketKeyEnabled = null;
        this.requestCharged = null;
    }

    protected CompleteMultipartUploadOutput(BuilderImpl builder) {
        this.location = builder.location;
        this.bucket = builder.bucket;
        this.key = builder.key;
        this.expiration = builder.expiration;
        this.eTag = builder.eTag;
        this.serverSideEncryption = builder.serverSideEncryption;
        this.versionId = builder.versionId;
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
        return Objects.hash(CompleteMultipartUploadOutput.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof CompleteMultipartUploadOutput);
    }

    public String location() {
        return location;
    }

    public String bucket() {
        return bucket;
    }

    public String key() {
        return key;
    }

    public String expiration() {
        return expiration;
    }

    public String eTag() {
        return eTag;
    }

    public ServerSideEncryption serverSideEncryption() {
        return serverSideEncryption;
    }

    public String versionId() {
        return versionId;
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

    public void setLocation(final String location) {
        this.location = location;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public void setExpiration(final String expiration) {
        this.expiration = expiration;
    }

    public void setETag(final String eTag) {
        this.eTag = eTag;
    }

    public void setServerSideEncryption(final ServerSideEncryption serverSideEncryption) {
        this.serverSideEncryption = serverSideEncryption;
    }

    public void setVersionId(final String versionId) {
        this.versionId = versionId;
    }

    public void setSSEKMSKeyId(final String sSEKMSKeyId) {
        this.sSEKMSKeyId = sSEKMSKeyId;
    }

    public void setBucketKeyEnabled(final Boolean bucketKeyEnabled) {
        this.bucketKeyEnabled = bucketKeyEnabled;
    }

    public void setRequestCharged(final RequestCharged requestCharged) {
        this.requestCharged = requestCharged;
    }

    public interface Builder {
        Builder location(String location);

        Builder bucket(String bucket);

        Builder key(String key);

        Builder expiration(String expiration);

        Builder eTag(String eTag);

        Builder serverSideEncryption(ServerSideEncryption serverSideEncryption);

        Builder versionId(String versionId);

        Builder sSEKMSKeyId(String sSEKMSKeyId);

        Builder bucketKeyEnabled(Boolean bucketKeyEnabled);

        Builder requestCharged(RequestCharged requestCharged);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The URI that identifies the newly created object.</p>
         */
        String location;

        /**
         * <p>The name of the bucket that contains the newly created object.</p>
         *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        String bucket;

        /**
         * <p>The object key of the newly created object.</p>
         */
        String key;

        /**
         * <p>If the object expiration is configured, this will contain the expiration date
         *          (expiry-date) and rule ID (rule-id). The value of rule-id is URL encoded.</p>
         */
        String expiration;

        /**
         * <p>Entity tag that identifies the newly created object's data. Objects with different
         *          object data will have different entity tags. The entity tag is an opaque string. The entity
         *          tag may or may not be an MD5 digest of the object data. If the entity tag is not an MD5
         *          digest of the object data, it will contain one or more nonhexadecimal characters and/or
         *          will consist of less than 32 or more than 32 hexadecimal digits.</p>
         */
        String eTag;

        /**
         * <p>If you specified server-side encryption either with an Amazon S3-managed encryption key or an
         *          AWS KMS customer master key (CMK) in your initiate multipart upload request, the response
         *          includes this header. It confirms the encryption algorithm that Amazon S3 used to encrypt the
         *          object.</p>
         */
        ServerSideEncryption serverSideEncryption;

        /**
         * <p>Version ID of the newly created object, in case the bucket has versioning turned
         *          on.</p>
         */
        String versionId;

        /**
         * <p>If present, specifies the ID of the AWS Key Management Service (AWS KMS) symmetric
         *          customer managed customer master key (CMK) that was used for the object.</p>
         */
        String sSEKMSKeyId;

        /**
         * <p>Indicates whether the multipart upload uses an S3 Bucket Key for server-side encryption with AWS KMS (SSE-KMS).</p>
         */
        Boolean bucketKeyEnabled;

        RequestCharged requestCharged;

        protected BuilderImpl() {
        }

        private BuilderImpl(CompleteMultipartUploadOutput model) {
            location(model.location);
            bucket(model.bucket);
            key(model.key);
            expiration(model.expiration);
            eTag(model.eTag);
            serverSideEncryption(model.serverSideEncryption);
            versionId(model.versionId);
            sSEKMSKeyId(model.sSEKMSKeyId);
            bucketKeyEnabled(model.bucketKeyEnabled);
            requestCharged(model.requestCharged);
        }

        public CompleteMultipartUploadOutput build() {
            return new CompleteMultipartUploadOutput(this);
        }

        public final Builder location(String location) {
            this.location = location;
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

        public final Builder expiration(String expiration) {
            this.expiration = expiration;
            return this;
        }

        public final Builder eTag(String eTag) {
            this.eTag = eTag;
            return this;
        }

        public final Builder serverSideEncryption(ServerSideEncryption serverSideEncryption) {
            this.serverSideEncryption = serverSideEncryption;
            return this;
        }

        public final Builder versionId(String versionId) {
            this.versionId = versionId;
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

        public String location() {
            return location;
        }

        public String bucket() {
            return bucket;
        }

        public String key() {
            return key;
        }

        public String expiration() {
            return expiration;
        }

        public String eTag() {
            return eTag;
        }

        public ServerSideEncryption serverSideEncryption() {
            return serverSideEncryption;
        }

        public String versionId() {
            return versionId;
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

        public void setLocation(final String location) {
            this.location = location;
        }

        public void setBucket(final String bucket) {
            this.bucket = bucket;
        }

        public void setKey(final String key) {
            this.key = key;
        }

        public void setExpiration(final String expiration) {
            this.expiration = expiration;
        }

        public void setETag(final String eTag) {
            this.eTag = eTag;
        }

        public void setServerSideEncryption(final ServerSideEncryption serverSideEncryption) {
            this.serverSideEncryption = serverSideEncryption;
        }

        public void setVersionId(final String versionId) {
            this.versionId = versionId;
        }

        public void setSSEKMSKeyId(final String sSEKMSKeyId) {
            this.sSEKMSKeyId = sSEKMSKeyId;
        }

        public void setBucketKeyEnabled(final Boolean bucketKeyEnabled) {
            this.bucketKeyEnabled = bucketKeyEnabled;
        }

        public void setRequestCharged(final RequestCharged requestCharged) {
            this.requestCharged = requestCharged;
        }
    }
}
