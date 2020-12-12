// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetObjectAclRequest {
    private String bucket;

    private String key;

    private String versionId;

    private RequestPayer requestPayer;

    private String expectedBucketOwner;

    private GetObjectAclRequest() {
        this.bucket = null;
        this.key = null;
        this.versionId = null;
        this.requestPayer = null;
        this.expectedBucketOwner = null;
    }

    private GetObjectAclRequest(Builder builder) {
        this.bucket = builder.bucket;
        this.key = builder.key;
        this.versionId = builder.versionId;
        this.requestPayer = builder.requestPayer;
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
        return Objects.hash(GetObjectAclRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetObjectAclRequest);
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

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    static final class Builder {
        private String bucket;

        private String key;

        private String versionId;

        private RequestPayer requestPayer;

        private String expectedBucketOwner;

        private Builder() {
        }

        private Builder(GetObjectAclRequest model) {
            bucket(model.bucket);
            key(model.key);
            versionId(model.versionId);
            requestPayer(model.requestPayer);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public GetObjectAclRequest build() {
            return new com.amazonaws.s3.model.GetObjectAclRequest(this);
        }

        /**
         * <p>The bucket name that contains the object for which to get the ACL information. </p>
         *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>The key of the object for which to get the ACL information.</p>
         */
        public final Builder key(String key) {
            this.key = key;
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
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        public final Builder expectedBucketOwner(String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
            return this;
        }
    }
}
