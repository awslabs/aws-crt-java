// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;
import software.amazon.awssdk.crt.http.HttpHeader;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutObjectAclRequest {
    /**
     * <p>The canned ACL to apply to the object. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/acl-overview.html#CannedACL">Canned ACL</a>.</p>
     */
    ObjectCannedACL aCL;

    /**
     * <p>Contains the elements that set the ACL permissions for an object per grantee.</p>
     */
    AccessControlPolicy accessControlPolicy;

    /**
     * <p>The bucket name that contains the object to which you want to attach the ACL. </p>
     *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     */
    String bucket;

    /**
     * <p>The base64-encoded 128-bit MD5 digest of the data. This header must be used as a message
     *          integrity check to verify that the request body was not corrupted in transit. For more
     *          information, go to <a href="http://www.ietf.org/rfc/rfc1864.txt">RFC
     *          1864.></a>
     *          </p>
     *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
     */
    String contentMD5;

    /**
     * <p>Allows grantee the read, write, read ACP, and write ACP permissions on the
     *          bucket.</p>
     *          <p>This action is not supported by Amazon S3 on Outposts.</p>
     */
    String grantFullControl;

    /**
     * <p>Allows grantee to list the objects in the
     *       bucket.</p>
     *          <p>This action is not supported by Amazon S3 on Outposts.</p>
     */
    String grantRead;

    /**
     * <p>Allows grantee to read the bucket ACL.</p>
     *          <p>This action is not supported by Amazon S3 on Outposts.</p>
     */
    String grantReadACP;

    /**
     * <p>Allows grantee to create, overwrite, and delete any object in the bucket.</p>
     */
    String grantWrite;

    /**
     * <p>Allows grantee to write the ACL for the applicable
     *       bucket.</p>
     *          <p>This action is not supported by Amazon S3 on Outposts.</p>
     */
    String grantWriteACP;

    /**
     * <p>Key for which the PUT operation was initiated.</p>
     *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
     */
    String key;

    RequestPayer requestPayer;

    /**
     * <p>VersionId used to reference a specific version of the object.</p>
     */
    String versionId;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    HttpHeader[] customHeaders;

    String customQueryParameters;

    PutObjectAclRequest() {
        this.aCL = null;
        this.accessControlPolicy = null;
        this.bucket = "";
        this.contentMD5 = "";
        this.grantFullControl = "";
        this.grantRead = "";
        this.grantReadACP = "";
        this.grantWrite = "";
        this.grantWriteACP = "";
        this.key = "";
        this.requestPayer = null;
        this.versionId = "";
        this.expectedBucketOwner = "";
        this.customHeaders = null;
        this.customQueryParameters = "";
    }

    protected PutObjectAclRequest(BuilderImpl builder) {
        this.aCL = builder.aCL;
        this.accessControlPolicy = builder.accessControlPolicy;
        this.bucket = builder.bucket;
        this.contentMD5 = builder.contentMD5;
        this.grantFullControl = builder.grantFullControl;
        this.grantRead = builder.grantRead;
        this.grantReadACP = builder.grantReadACP;
        this.grantWrite = builder.grantWrite;
        this.grantWriteACP = builder.grantWriteACP;
        this.key = builder.key;
        this.requestPayer = builder.requestPayer;
        this.versionId = builder.versionId;
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
        return Objects.hash(PutObjectAclRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof PutObjectAclRequest);
    }

    public ObjectCannedACL aCL() {
        return aCL;
    }

    public AccessControlPolicy accessControlPolicy() {
        return accessControlPolicy;
    }

    public String bucket() {
        return bucket;
    }

    public String contentMD5() {
        return contentMD5;
    }

    public String grantFullControl() {
        return grantFullControl;
    }

    public String grantRead() {
        return grantRead;
    }

    public String grantReadACP() {
        return grantReadACP;
    }

    public String grantWrite() {
        return grantWrite;
    }

    public String grantWriteACP() {
        return grantWriteACP;
    }

    public String key() {
        return key;
    }

    public RequestPayer requestPayer() {
        return requestPayer;
    }

    public String versionId() {
        return versionId;
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
        Builder aCL(ObjectCannedACL aCL);

        Builder accessControlPolicy(AccessControlPolicy accessControlPolicy);

        Builder bucket(String bucket);

        Builder contentMD5(String contentMD5);

        Builder grantFullControl(String grantFullControl);

        Builder grantRead(String grantRead);

        Builder grantReadACP(String grantReadACP);

        Builder grantWrite(String grantWrite);

        Builder grantWriteACP(String grantWriteACP);

        Builder key(String key);

        Builder requestPayer(RequestPayer requestPayer);

        Builder versionId(String versionId);

        Builder expectedBucketOwner(String expectedBucketOwner);

        Builder customHeaders(HttpHeader[] customHeaders);

        Builder customQueryParameters(String customQueryParameters);

        PutObjectAclRequest build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The canned ACL to apply to the object. For more information, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/acl-overview.html#CannedACL">Canned ACL</a>.</p>
         */
        ObjectCannedACL aCL;

        /**
         * <p>Contains the elements that set the ACL permissions for an object per grantee.</p>
         */
        AccessControlPolicy accessControlPolicy;

        /**
         * <p>The bucket name that contains the object to which you want to attach the ACL. </p>
         *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        String bucket;

        /**
         * <p>The base64-encoded 128-bit MD5 digest of the data. This header must be used as a message
         *          integrity check to verify that the request body was not corrupted in transit. For more
         *          information, go to <a href="http://www.ietf.org/rfc/rfc1864.txt">RFC
         *          1864.></a>
         *          </p>
         *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
         */
        String contentMD5;

        /**
         * <p>Allows grantee the read, write, read ACP, and write ACP permissions on the
         *          bucket.</p>
         *          <p>This action is not supported by Amazon S3 on Outposts.</p>
         */
        String grantFullControl;

        /**
         * <p>Allows grantee to list the objects in the
         *       bucket.</p>
         *          <p>This action is not supported by Amazon S3 on Outposts.</p>
         */
        String grantRead;

        /**
         * <p>Allows grantee to read the bucket ACL.</p>
         *          <p>This action is not supported by Amazon S3 on Outposts.</p>
         */
        String grantReadACP;

        /**
         * <p>Allows grantee to create, overwrite, and delete any object in the bucket.</p>
         */
        String grantWrite;

        /**
         * <p>Allows grantee to write the ACL for the applicable
         *       bucket.</p>
         *          <p>This action is not supported by Amazon S3 on Outposts.</p>
         */
        String grantWriteACP;

        /**
         * <p>Key for which the PUT operation was initiated.</p>
         *          <p>When using this API with an access point, you must direct requests to the access point hostname. The access point hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.s3-accesspoint.<i>Region</i>.amazonaws.com. When using this operation with an access point through the AWS SDKs, you provide the access point ARN in place of the bucket name. For more information about access point ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/using-access-points.html">Using Access Points</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         *          <p>When using this API with Amazon S3 on Outposts, you must direct requests to the S3 on Outposts hostname. The S3 on Outposts hostname takes the form <i>AccessPointName</i>-<i>AccountId</i>.<i>outpostID</i>.s3-outposts.<i>Region</i>.amazonaws.com. When using this operation using S3 on Outposts through the AWS SDKs, you provide the Outposts bucket ARN in place of the bucket name. For more information about S3 on Outposts ARNs, see <a href="https://docs.aws.amazon.com/AmazonS3/latest/dev/S3onOutposts.html">Using S3 on Outposts</a> in the <i>Amazon Simple Storage Service Developer Guide</i>.</p>
         */
        String key;

        RequestPayer requestPayer;

        /**
         * <p>VersionId used to reference a specific version of the object.</p>
         */
        String versionId;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        HttpHeader[] customHeaders;

        String customQueryParameters;

        protected BuilderImpl() {
        }

        private BuilderImpl(PutObjectAclRequest model) {
            aCL(model.aCL);
            accessControlPolicy(model.accessControlPolicy);
            bucket(model.bucket);
            contentMD5(model.contentMD5);
            grantFullControl(model.grantFullControl);
            grantRead(model.grantRead);
            grantReadACP(model.grantReadACP);
            grantWrite(model.grantWrite);
            grantWriteACP(model.grantWriteACP);
            key(model.key);
            requestPayer(model.requestPayer);
            versionId(model.versionId);
            expectedBucketOwner(model.expectedBucketOwner);
            customHeaders(model.customHeaders);
            customQueryParameters(model.customQueryParameters);
        }

        public PutObjectAclRequest build() {
            return new PutObjectAclRequest(this);
        }

        public final Builder aCL(ObjectCannedACL aCL) {
            this.aCL = aCL;
            return this;
        }

        public final Builder accessControlPolicy(AccessControlPolicy accessControlPolicy) {
            this.accessControlPolicy = accessControlPolicy;
            return this;
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder contentMD5(String contentMD5) {
            this.contentMD5 = contentMD5;
            return this;
        }

        public final Builder grantFullControl(String grantFullControl) {
            this.grantFullControl = grantFullControl;
            return this;
        }

        public final Builder grantRead(String grantRead) {
            this.grantRead = grantRead;
            return this;
        }

        public final Builder grantReadACP(String grantReadACP) {
            this.grantReadACP = grantReadACP;
            return this;
        }

        public final Builder grantWrite(String grantWrite) {
            this.grantWrite = grantWrite;
            return this;
        }

        public final Builder grantWriteACP(String grantWriteACP) {
            this.grantWriteACP = grantWriteACP;
            return this;
        }

        public final Builder key(String key) {
            this.key = key;
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

        public ObjectCannedACL aCL() {
            return aCL;
        }

        public AccessControlPolicy accessControlPolicy() {
            return accessControlPolicy;
        }

        public String bucket() {
            return bucket;
        }

        public String contentMD5() {
            return contentMD5;
        }

        public String grantFullControl() {
            return grantFullControl;
        }

        public String grantRead() {
            return grantRead;
        }

        public String grantReadACP() {
            return grantReadACP;
        }

        public String grantWrite() {
            return grantWrite;
        }

        public String grantWriteACP() {
            return grantWriteACP;
        }

        public String key() {
            return key;
        }

        public RequestPayer requestPayer() {
            return requestPayer;
        }

        public String versionId() {
            return versionId;
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
