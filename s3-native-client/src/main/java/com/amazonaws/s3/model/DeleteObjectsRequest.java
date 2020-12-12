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
public class DeleteObjectsRequest {
    private String bucket;

    private Delete delete;

    private String mFA;

    private RequestPayer requestPayer;

    private Boolean bypassGovernanceRetention;

    private String expectedBucketOwner;

    private DeleteObjectsRequest() {
        this.bucket = null;
        this.delete = null;
        this.mFA = null;
        this.requestPayer = null;
        this.bypassGovernanceRetention = null;
        this.expectedBucketOwner = null;
    }

    private DeleteObjectsRequest(Builder builder) {
        this.bucket = builder.bucket;
        this.delete = builder.delete;
        this.mFA = builder.mFA;
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
        return Objects.hash(DeleteObjectsRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof DeleteObjectsRequest);
    }

    public String bucket() {
        return bucket;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public Delete delete() {
        return delete;
    }

    public void setDelete(final Delete delete) {
        this.delete = delete;
    }

    public String mFA() {
        return mFA;
    }

    public void setMFA(final String mFA) {
        this.mFA = mFA;
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

        private Delete delete;

        private String mFA;

        private RequestPayer requestPayer;

        private Boolean bypassGovernanceRetention;

        private String expectedBucketOwner;

        private Builder() {
        }

        private Builder(DeleteObjectsRequest model) {
            bucket(model.bucket);
            delete(model.delete);
            mFA(model.mFA);
            requestPayer(model.requestPayer);
            bypassGovernanceRetention(model.bypassGovernanceRetention);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public DeleteObjectsRequest build() {
            return new com.amazonaws.s3.model.DeleteObjectsRequest(this);
        }

        /**
         * <p>The bucket name containing the objects to delete. </p>
         *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>Container for the request.</p>
         */
        public final Builder delete(Delete delete) {
            this.delete = delete;
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

        public final Builder requestPayer(RequestPayer requestPayer) {
            this.requestPayer = requestPayer;
            return this;
        }

        /**
         * <p>Specifies whether you want to delete this object even if it has a Governance-type Object
         *          Lock in place. You must have sufficient permissions to perform this operation.</p>
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
