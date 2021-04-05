// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Map;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;
import software.amazon.awssdk.crt.http.HttpHeader;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class DeleteObjectRequest {
    /**
     * <p>The bucket name of the bucket containing the object. </p>
     *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     */
    String bucket;

    /**
     * <p>Key name of the object to delete.</p>
     */
    String key;

    /**
     * <p>The concatenation of the authentication device's serial number, a space, and the value
     *          that is displayed on your authentication device. Required to permanently delete a versioned
     *          object if versioning is configured with MFA delete enabled.</p>
     */
    String mFA;

    /**
     * <p>VersionId used to reference a specific version of the object.</p>
     */
    String versionId;

    RequestPayer requestPayer;

    /**
     * <p>Indicates whether S3 Object Lock should bypass Governance-mode restrictions to process
     *          this operation.</p>
     */
    Boolean bypassGovernanceRetention;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    HttpHeader[] customHeaders;

    Map<String, String> customQueryParameters;

    DeleteObjectRequest() {
        this.bucket = "";
        this.key = "";
        this.mFA = "";
        this.versionId = "";
        this.requestPayer = null;
        this.bypassGovernanceRetention = null;
        this.expectedBucketOwner = "";
        this.customHeaders = null;
        this.customQueryParameters = null;
    }

    protected DeleteObjectRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.key = builder.key;
        this.mFA = builder.mFA;
        this.versionId = builder.versionId;
        this.requestPayer = builder.requestPayer;
        this.bypassGovernanceRetention = builder.bypassGovernanceRetention;
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
        return Objects.hash(DeleteObjectRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof DeleteObjectRequest);
    }

    public String bucket() {
        return bucket;
    }

    public String key() {
        return key;
    }

    public String mFA() {
        return mFA;
    }

    public String versionId() {
        return versionId;
    }

    public RequestPayer requestPayer() {
        return requestPayer;
    }

    public Boolean bypassGovernanceRetention() {
        return bypassGovernanceRetention;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public HttpHeader[] customHeaders() {
        return customHeaders;
    }

    public Map<String, String> customQueryParameters() {
        return customQueryParameters;
    }

    public interface Builder {
        Builder bucket(String bucket);

        Builder key(String key);

        Builder mFA(String mFA);

        Builder versionId(String versionId);

        Builder requestPayer(RequestPayer requestPayer);

        Builder bypassGovernanceRetention(Boolean bypassGovernanceRetention);

        Builder expectedBucketOwner(String expectedBucketOwner);

        Builder customHeaders(HttpHeader[] customHeaders);

        Builder customQueryParameters(Map<String, String> customQueryParameters);

        DeleteObjectRequest build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The bucket name of the bucket containing the object. </p>
         *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        String bucket;

        /**
         * <p>Key name of the object to delete.</p>
         */
        String key;

        /**
         * <p>The concatenation of the authentication device's serial number, a space, and the value
         *          that is displayed on your authentication device. Required to permanently delete a versioned
         *          object if versioning is configured with MFA delete enabled.</p>
         */
        String mFA;

        /**
         * <p>VersionId used to reference a specific version of the object.</p>
         */
        String versionId;

        RequestPayer requestPayer;

        /**
         * <p>Indicates whether S3 Object Lock should bypass Governance-mode restrictions to process
         *          this operation.</p>
         */
        Boolean bypassGovernanceRetention;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        HttpHeader[] customHeaders;

        Map<String, String> customQueryParameters;

        protected BuilderImpl() {
        }

        private BuilderImpl(DeleteObjectRequest model) {
            bucket(model.bucket);
            key(model.key);
            mFA(model.mFA);
            versionId(model.versionId);
            requestPayer(model.requestPayer);
            bypassGovernanceRetention(model.bypassGovernanceRetention);
            expectedBucketOwner(model.expectedBucketOwner);
            customHeaders(model.customHeaders);
            customQueryParameters(model.customQueryParameters);
        }

        public DeleteObjectRequest build() {
            return new DeleteObjectRequest(this);
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder key(String key) {
            this.key = key;
            return this;
        }

        public final Builder mFA(String mFA) {
            this.mFA = mFA;
            return this;
        }

        public final Builder versionId(String versionId) {
            this.versionId = versionId;
            return this;
        }

        public final Builder requestPayer(RequestPayer requestPayer) {
            this.requestPayer = requestPayer;
            return this;
        }

        public final Builder bypassGovernanceRetention(Boolean bypassGovernanceRetention) {
            this.bypassGovernanceRetention = bypassGovernanceRetention;
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

        public final Builder customQueryParameters(Map<String, String> customQueryParameters) {
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

        public String mFA() {
            return mFA;
        }

        public String versionId() {
            return versionId;
        }

        public RequestPayer requestPayer() {
            return requestPayer;
        }

        public Boolean bypassGovernanceRetention() {
            return bypassGovernanceRetention;
        }

        public String expectedBucketOwner() {
            return expectedBucketOwner;
        }

        public HttpHeader[] customHeaders() {
            return customHeaders;
        }

        public Map<String, String> customQueryParameters() {
            return customQueryParameters;
        }
    }
}
