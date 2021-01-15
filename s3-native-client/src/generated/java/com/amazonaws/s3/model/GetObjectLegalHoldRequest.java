// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class GetObjectLegalHoldRequest {
    /**
     * <p>The bucket name containing the object whose Legal Hold status you want to retrieve. </p>
     *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     */
    String bucket;

    /**
     * <p>The key name for the object whose Legal Hold status you want to retrieve.</p>
     */
    String key;

    /**
     * <p>The version ID of the object whose Legal Hold status you want to retrieve.</p>
     */
    String versionId;

    RequestPayer requestPayer;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    GetObjectLegalHoldRequest() {
        this.bucket = "";
        this.key = "";
        this.versionId = "";
        this.requestPayer = null;
        this.expectedBucketOwner = "";
    }

    protected GetObjectLegalHoldRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.key = builder.key;
        this.versionId = builder.versionId;
        this.requestPayer = builder.requestPayer;
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
        return Objects.hash(GetObjectLegalHoldRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof GetObjectLegalHoldRequest);
    }

    public String bucket() {
        return bucket;
    }

    public String key() {
        return key;
    }

    public String versionId() {
        return versionId;
    }

    public RequestPayer requestPayer() {
        return requestPayer;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public interface Builder {
        Builder bucket(String bucket);

        Builder key(String key);

        Builder versionId(String versionId);

        Builder requestPayer(RequestPayer requestPayer);

        Builder expectedBucketOwner(String expectedBucketOwner);

        GetObjectLegalHoldRequest build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The bucket name containing the object whose Legal Hold status you want to retrieve. </p>
         *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        String bucket;

        /**
         * <p>The key name for the object whose Legal Hold status you want to retrieve.</p>
         */
        String key;

        /**
         * <p>The version ID of the object whose Legal Hold status you want to retrieve.</p>
         */
        String versionId;

        RequestPayer requestPayer;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        protected BuilderImpl() {
        }

        private BuilderImpl(GetObjectLegalHoldRequest model) {
            bucket(model.bucket);
            key(model.key);
            versionId(model.versionId);
            requestPayer(model.requestPayer);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public GetObjectLegalHoldRequest build() {
            return new GetObjectLegalHoldRequest(this);
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder key(String key) {
            this.key = key;
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

        public String key() {
            return key;
        }

        public String versionId() {
            return versionId;
        }

        public RequestPayer requestPayer() {
            return requestPayer;
        }

        public String expectedBucketOwner() {
            return expectedBucketOwner;
        }
    }
}
