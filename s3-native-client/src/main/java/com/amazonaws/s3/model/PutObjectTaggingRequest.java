// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutObjectTaggingRequest {
    /**
     * <p>The bucket name containing the object. </p>
     *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     */
    String bucket;

    /**
     * <p>Name of the object key.</p>
     */
    String key;

    /**
     * <p>The versionId of the object that the tag-set will be added to.</p>
     */
    String versionId;

    /**
     * <p>The MD5 hash for the request body.</p>
     *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
     */
    String contentMD5;

    /**
     * <p>Container for the <code>TagSet</code> and <code>Tag</code> elements</p>
     */
    Tagging tagging;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    PutObjectTaggingRequest() {
        this.bucket = "";
        this.key = "";
        this.versionId = "";
        this.contentMD5 = "";
        this.tagging = null;
        this.expectedBucketOwner = "";
    }

    protected PutObjectTaggingRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.key = builder.key;
        this.versionId = builder.versionId;
        this.contentMD5 = builder.contentMD5;
        this.tagging = builder.tagging;
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
        return Objects.hash(PutObjectTaggingRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof PutObjectTaggingRequest);
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

    public String contentMD5() {
        return contentMD5;
    }

    public Tagging tagging() {
        return tagging;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public void setVersionId(final String versionId) {
        this.versionId = versionId;
    }

    public void setContentMD5(final String contentMD5) {
        this.contentMD5 = contentMD5;
    }

    public void setTagging(final Tagging tagging) {
        this.tagging = tagging;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    public interface Builder {
        Builder bucket(String bucket);

        Builder key(String key);

        Builder versionId(String versionId);

        Builder contentMD5(String contentMD5);

        Builder tagging(Tagging tagging);

        Builder expectedBucketOwner(String expectedBucketOwner);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The bucket name containing the object. </p>
         *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        String bucket;

        /**
         * <p>Name of the object key.</p>
         */
        String key;

        /**
         * <p>The versionId of the object that the tag-set will be added to.</p>
         */
        String versionId;

        /**
         * <p>The MD5 hash for the request body.</p>
         *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
         */
        String contentMD5;

        /**
         * <p>Container for the <code>TagSet</code> and <code>Tag</code> elements</p>
         */
        Tagging tagging;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        protected BuilderImpl() {
        }

        private BuilderImpl(PutObjectTaggingRequest model) {
            bucket(model.bucket);
            key(model.key);
            versionId(model.versionId);
            contentMD5(model.contentMD5);
            tagging(model.tagging);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public PutObjectTaggingRequest build() {
            return new PutObjectTaggingRequest(this);
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

        public final Builder contentMD5(String contentMD5) {
            this.contentMD5 = contentMD5;
            return this;
        }

        public final Builder tagging(Tagging tagging) {
            this.tagging = tagging;
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

        public String contentMD5() {
            return contentMD5;
        }

        public Tagging tagging() {
            return tagging;
        }

        public String expectedBucketOwner() {
            return expectedBucketOwner;
        }

        public void setBucket(final String bucket) {
            this.bucket = bucket;
        }

        public void setKey(final String key) {
            this.key = key;
        }

        public void setVersionId(final String versionId) {
            this.versionId = versionId;
        }

        public void setContentMD5(final String contentMD5) {
            this.contentMD5 = contentMD5;
        }

        public void setTagging(final Tagging tagging) {
            this.tagging = tagging;
        }

        public void setExpectedBucketOwner(final String expectedBucketOwner) {
            this.expectedBucketOwner = expectedBucketOwner;
        }
    }
}
