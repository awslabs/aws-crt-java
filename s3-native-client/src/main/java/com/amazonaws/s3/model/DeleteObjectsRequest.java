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
public class DeleteObjectsRequest {
    /**
     * <p>The bucket name containing the objects to delete. </p>
     *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     */
    String bucket;

    /**
     * <p>Container for the request.</p>
     */
    Delete delete;

    /**
     * <p>The concatenation of the authentication device's serial number, a space, and the value
     *          that is displayed on your authentication device. Required to permanently delete a versioned
     *          object if versioning is configured with MFA delete enabled.</p>
     */
    String mFA;

    RequestPayer requestPayer;

    /**
     * <p>Specifies whether you want to delete this object even if it has a Governance-type Object
     *          Lock in place. You must have sufficient permissions to perform this operation.</p>
     */
    Boolean bypassGovernanceRetention;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    DeleteObjectsRequest() {
        this.bucket = "";
        this.delete = null;
        this.mFA = "";
        this.requestPayer = null;
        this.bypassGovernanceRetention = null;
        this.expectedBucketOwner = "";
    }

    protected DeleteObjectsRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.delete = builder.delete;
        this.mFA = builder.mFA;
        this.requestPayer = builder.requestPayer;
        this.bypassGovernanceRetention = builder.bypassGovernanceRetention;
        this.expectedBucketOwner = builder.expectedBucketOwner;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public Delete delete() {
        return delete;
    }

    public String mFA() {
        return mFA;
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

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public void setDelete(final Delete delete) {
        this.delete = delete;
    }

    public void setMFA(final String mFA) {
        this.mFA = mFA;
    }

    public void setRequestPayer(final RequestPayer requestPayer) {
        this.requestPayer = requestPayer;
    }

    public void setBypassGovernanceRetention(final Boolean bypassGovernanceRetention) {
        this.bypassGovernanceRetention = bypassGovernanceRetention;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    public interface Builder {
        Builder bucket(String bucket);

        Builder delete(Delete delete);

        Builder mFA(String mFA);

        Builder requestPayer(RequestPayer requestPayer);

        Builder bypassGovernanceRetention(Boolean bypassGovernanceRetention);

        Builder expectedBucketOwner(String expectedBucketOwner);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The bucket name containing the objects to delete. </p>
         *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        String bucket;

        /**
         * <p>Container for the request.</p>
         */
        Delete delete;

        /**
         * <p>The concatenation of the authentication device's serial number, a space, and the value
         *          that is displayed on your authentication device. Required to permanently delete a versioned
         *          object if versioning is configured with MFA delete enabled.</p>
         */
        String mFA;

        RequestPayer requestPayer;

        /**
         * <p>Specifies whether you want to delete this object even if it has a Governance-type Object
         *          Lock in place. You must have sufficient permissions to perform this operation.</p>
         */
        Boolean bypassGovernanceRetention;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        protected BuilderImpl() {
        }

        private BuilderImpl(DeleteObjectsRequest model) {
            bucket(model.bucket);
            delete(model.delete);
            mFA(model.mFA);
            requestPayer(model.requestPayer);
            bypassGovernanceRetention(model.bypassGovernanceRetention);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public DeleteObjectsRequest build() {
            return new DeleteObjectsRequest(this);
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder delete(Delete delete) {
            this.delete = delete;
            return this;
        }

        public final Builder mFA(String mFA) {
            this.mFA = mFA;
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

        public Delete delete() {
            return delete;
        }

        public String mFA() {
            return mFA;
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

        public void setBucket(final String bucket) {
            this.bucket = bucket;
        }

        public void setDelete(final Delete delete) {
            this.delete = delete;
        }

        public void setMFA(final String mFA) {
            this.mFA = mFA;
        }

        public void setRequestPayer(final RequestPayer requestPayer) {
            this.requestPayer = requestPayer;
        }

        public void setBypassGovernanceRetention(final Boolean bypassGovernanceRetention) {
            this.bypassGovernanceRetention = bypassGovernanceRetention;
        }

        public void setExpectedBucketOwner(final String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
        }
    }
}
