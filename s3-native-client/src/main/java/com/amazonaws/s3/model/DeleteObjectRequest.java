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
public class DeleteObjectRequest {
    private String bucket;

    private String key;

    private String mFA;

    private String versionId;

    private RequestPayer requestPayer;

    private Boolean bypassGovernanceRetention;

    private String expectedBucketOwner;

    private DeleteObjectRequest() {
        this.bucket = null;
        this.key = null;
        this.mFA = null;
        this.versionId = null;
        this.requestPayer = null;
        this.bypassGovernanceRetention = null;
        this.expectedBucketOwner = null;
    }

    private DeleteObjectRequest(Builder builder) {
        this.bucket = builder.bucket;
        this.key = builder.key;
        this.mFA = builder.mFA;
        this.versionId = builder.versionId;
        this.requestPayer = builder.requestPayer;
        this.bypassGovernanceRetention = builder.bypassGovernanceRetention;
        this.expectedBucketOwner = builder.expectedBucketOwner;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public String key() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public String mFA() {
        return mFA;
    }

    public void setMFA(final String mFA) {
        this.mFA = mFA;
    }

    public String versionId() {
        return versionId;
    }

    public void setVersionId(final String versionId) {
        this.versionId = versionId;
    }

    public RequestPayer requestPayer() {
        return requestPayer;
    }

    public void setRequestPayer(final RequestPayer requestPayer) {
        this.requestPayer = requestPayer;
    }

    public Boolean bypassGovernanceRetention() {
        return bypassGovernanceRetention;
    }

    public void setBypassGovernanceRetention(final Boolean bypassGovernanceRetention) {
        this.bypassGovernanceRetention = bypassGovernanceRetention;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    static final class Builder {
        private String bucket;

        private String key;

        private String mFA;

        private String versionId;

        private RequestPayer requestPayer;

        private Boolean bypassGovernanceRetention;

        private String expectedBucketOwner;

        private Builder() {
        }

        private Builder(DeleteObjectRequest model) {
            bucket(model.bucket);
            key(model.key);
            mFA(model.mFA);
            versionId(model.versionId);
            requestPayer(model.requestPayer);
            bypassGovernanceRetention(model.bypassGovernanceRetention);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public DeleteObjectRequest build() {
            return new com.amazonaws.s3.model.DeleteObjectRequest(this);
        }

        /**
         * <p>The bucket name of the bucket containing the object. </p>
         *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>Key name of the object to delete.</p>
         */
        public final Builder key(String key) {
            this.key = key;
            return this;
        }

        /**
         * <p>The concatenation of the authentication device's serial number, a space, and the value
         *          that is displayed on your authentication device. Required to permanently delete a versioned
         *          object if versioning is configured with MFA delete enabled.</p>
         */
        public final Builder mFA(String mFA) {
            this.mFA = mFA;
            return this;
        }

        /**
         * <p>VersionId used to reference a specific version of the object.</p>
         */
        public final Builder versionId(String versionId) {
            this.versionId = versionId;
            return this;
        }

        public final Builder requestPayer(RequestPayer requestPayer) {
            this.requestPayer = requestPayer;
            return this;
        }

        /**
         * <p>Indicates whether S3 Object Lock should bypass Governance-mode restrictions to process
         *          this operation.</p>
         */
        public final Builder bypassGovernanceRetention(Boolean bypassGovernanceRetention) {
            this.bypassGovernanceRetention = bypassGovernanceRetention;
            return this;
        }

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        public final Builder expectedBucketOwner(String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
            return this;
        }
    }
}
