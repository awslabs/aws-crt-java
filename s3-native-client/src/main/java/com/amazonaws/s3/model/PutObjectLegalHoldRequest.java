// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutObjectLegalHoldRequest {
    private String bucket;

    private String key;

    private ObjectLockLegalHold legalHold;

    private RequestPayer requestPayer;

    private String versionId;

    private String contentMD5;

    private String expectedBucketOwner;

    private PutObjectLegalHoldRequest() {
        this.bucket = null;
        this.key = null;
        this.legalHold = null;
        this.requestPayer = null;
        this.versionId = null;
        this.contentMD5 = null;
        this.expectedBucketOwner = null;
    }

    private PutObjectLegalHoldRequest(Builder builder) {
        this.bucket = builder.bucket;
        this.key = builder.key;
        this.legalHold = builder.legalHold;
        this.requestPayer = builder.requestPayer;
        this.versionId = builder.versionId;
        this.contentMD5 = builder.contentMD5;
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
        return Objects.hash(PutObjectLegalHoldRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof PutObjectLegalHoldRequest);
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

    public ObjectLockLegalHold legalHold() {
        return legalHold;
    }

    public void setLegalHold(final ObjectLockLegalHold legalHold) {
        this.legalHold = legalHold;
    }

    public RequestPayer requestPayer() {
        return requestPayer;
    }

    public void setRequestPayer(final RequestPayer requestPayer) {
        this.requestPayer = requestPayer;
    }

    public String versionId() {
        return versionId;
    }

    public void setVersionId(final String versionId) {
        this.versionId = versionId;
    }

    public String contentMD5() {
        return contentMD5;
    }

    public void setContentMD5(final String contentMD5) {
        this.contentMD5 = contentMD5;
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

        private ObjectLockLegalHold legalHold;

        private RequestPayer requestPayer;

        private String versionId;

        private String contentMD5;

        private String expectedBucketOwner;

        private Builder() {
        }

        private Builder(PutObjectLegalHoldRequest model) {
            bucket(model.bucket);
            key(model.key);
            legalHold(model.legalHold);
            requestPayer(model.requestPayer);
            versionId(model.versionId);
            contentMD5(model.contentMD5);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public PutObjectLegalHoldRequest build() {
            return new com.amazonaws.s3.model.PutObjectLegalHoldRequest(this);
        }

        /**
         * <p>The bucket name containing the object that you want to place a Legal Hold on. </p>
         *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>The key name for the object that you want to place a Legal Hold on.</p>
         */
        public final Builder key(String key) {
            this.key = key;
            return this;
        }

        /**
         * <p>Container element for the Legal Hold configuration you want to apply to the specified
         *          object.</p>
         */
        public final Builder legalHold(ObjectLockLegalHold legalHold) {
            this.legalHold = legalHold;
            return this;
        }

        public final Builder requestPayer(RequestPayer requestPayer) {
            this.requestPayer = requestPayer;
            return this;
        }

        /**
         * <p>The version ID of the object that you want to place a Legal Hold on.</p>
         */
        public final Builder versionId(String versionId) {
            this.versionId = versionId;
            return this;
        }

        /**
         * <p>The MD5 hash for the request body.</p>
         *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
         */
        public final Builder contentMD5(String contentMD5) {
            this.contentMD5 = contentMD5;
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
