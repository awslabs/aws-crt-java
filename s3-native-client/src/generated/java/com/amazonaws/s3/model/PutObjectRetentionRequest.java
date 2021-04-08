// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;
import software.amazon.awssdk.crt.http.HttpHeader;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutObjectRetentionRequest {
    /**
     * <p>The bucket name that contains the object you want to apply this Object Retention
     *          configuration to. </p>
     *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     */
    String bucket;

    /**
     * <p>The key name for the object that you want to apply this Object Retention configuration
     *          to.</p>
     */
    String key;

    /**
     * <p>The container element for the Object Retention configuration.</p>
     */
    ObjectLockRetention retention;

    RequestPayer requestPayer;

    /**
     * <p>The version ID for the object that you want to apply this Object Retention configuration
     *          to.</p>
     */
    String versionId;

    /**
     * <p>Indicates whether this operation should bypass Governance-mode restrictions.</p>
     */
    Boolean bypassGovernanceRetention;

    /**
     * <p>The MD5 hash for the request body.</p>
     *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
     */
    String contentMD5;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    HttpHeader[] customHeaders;

    String customQueryParameters;

    PutObjectRetentionRequest() {
        this.bucket = "";
        this.key = "";
        this.retention = null;
        this.requestPayer = null;
        this.versionId = "";
        this.bypassGovernanceRetention = null;
        this.contentMD5 = "";
        this.expectedBucketOwner = "";
        this.customHeaders = null;
        this.customQueryParameters = "";
    }

    protected PutObjectRetentionRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.key = builder.key;
        this.retention = builder.retention;
        this.requestPayer = builder.requestPayer;
        this.versionId = builder.versionId;
        this.bypassGovernanceRetention = builder.bypassGovernanceRetention;
        this.contentMD5 = builder.contentMD5;
        this.expectedBucketOwner = builder.expectedBucketOwner;
        this.customHeaders = builder.customHeaders;
        this.customQueryParameters = builder.customQueryParameters;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(PutObjectRetentionRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof PutObjectRetentionRequest);
    }

    public String bucket() {
        return bucket;
    }

    public String key() {
        return key;
    }

    public ObjectLockRetention retention() {
        return retention;
    }

    public RequestPayer requestPayer() {
        return requestPayer;
    }

    public String versionId() {
        return versionId;
    }

    public Boolean bypassGovernanceRetention() {
        return bypassGovernanceRetention;
    }

    public String contentMD5() {
        return contentMD5;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public HttpHeader[] customHeaders() {
        return customHeaders;
    }

    public String customQueryParameters() {
        return customQueryParameters;
    }

    public interface Builder {
        Builder bucket(String bucket);

        Builder key(String key);

        Builder retention(ObjectLockRetention retention);

        Builder requestPayer(RequestPayer requestPayer);

        Builder versionId(String versionId);

        Builder bypassGovernanceRetention(Boolean bypassGovernanceRetention);

        Builder contentMD5(String contentMD5);

        Builder expectedBucketOwner(String expectedBucketOwner);

        Builder customHeaders(HttpHeader[] customHeaders);

        Builder customQueryParameters(String customQueryParameters);

        PutObjectRetentionRequest build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The bucket name that contains the object you want to apply this Object Retention
         *          configuration to. </p>
         *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        String bucket;

        /**
         * <p>The key name for the object that you want to apply this Object Retention configuration
         *          to.</p>
         */
        String key;

        /**
         * <p>The container element for the Object Retention configuration.</p>
         */
        ObjectLockRetention retention;

        RequestPayer requestPayer;

        /**
         * <p>The version ID for the object that you want to apply this Object Retention configuration
         *          to.</p>
         */
        String versionId;

        /**
         * <p>Indicates whether this operation should bypass Governance-mode restrictions.</p>
         */
        Boolean bypassGovernanceRetention;

        /**
         * <p>The MD5 hash for the request body.</p>
         *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
         */
        String contentMD5;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        HttpHeader[] customHeaders;

        String customQueryParameters;

        protected BuilderImpl() {
        }

        private BuilderImpl(PutObjectRetentionRequest model) {
            bucket(model.bucket);
            key(model.key);
            retention(model.retention);
            requestPayer(model.requestPayer);
            versionId(model.versionId);
            bypassGovernanceRetention(model.bypassGovernanceRetention);
            contentMD5(model.contentMD5);
            expectedBucketOwner(model.expectedBucketOwner);
            customHeaders(model.customHeaders);
            customQueryParameters(model.customQueryParameters);
        }

        public PutObjectRetentionRequest build() {
            return new PutObjectRetentionRequest(this);
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder key(String key) {
            this.key = key;
            return this;
        }

        public final Builder retention(ObjectLockRetention retention) {
            this.retention = retention;
            return this;
        }

        public final Builder requestPayer(RequestPayer requestPayer) {
            this.requestPayer = requestPayer;
            return this;
        }

        public final Builder versionId(String versionId) {
            this.versionId = versionId;
            return this;
        }

        public final Builder bypassGovernanceRetention(Boolean bypassGovernanceRetention) {
            this.bypassGovernanceRetention = bypassGovernanceRetention;
            return this;
        }

        public final Builder contentMD5(String contentMD5) {
            this.contentMD5 = contentMD5;
            return this;
        }

        public final Builder expectedBucketOwner(String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
            return this;
        }

        public final Builder customHeaders(HttpHeader[] customHeaders) {
            this.customHeaders = customHeaders;
            return this;
        }

        public final Builder customQueryParameters(String customQueryParameters) {
            this.customQueryParameters = customQueryParameters;
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

        public String bucket() {
            return bucket;
        }

        public String key() {
            return key;
        }

        public ObjectLockRetention retention() {
            return retention;
        }

        public RequestPayer requestPayer() {
            return requestPayer;
        }

        public String versionId() {
            return versionId;
        }

        public Boolean bypassGovernanceRetention() {
            return bypassGovernanceRetention;
        }

        public String contentMD5() {
            return contentMD5;
        }

        public String expectedBucketOwner() {
            return expectedBucketOwner;
        }

        public HttpHeader[] customHeaders() {
            return customHeaders;
        }

        public String customQueryParameters() {
            return customQueryParameters;
        }
    }
}
