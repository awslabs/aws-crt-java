// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Map;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;
import software.amazon.awssdk.crt.http.HttpHeader;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutBucketAclRequest {
    /**
     * <p>The canned ACL to apply to the bucket.</p>
     */
    BucketCannedACL aCL;

    /**
     * <p>Contains the elements that set the ACL permissions for an object per grantee.</p>
     */
    AccessControlPolicy accessControlPolicy;

    /**
     * <p>The bucket to which to apply the ACL.</p>
     */
    String bucket;

    /**
     * <p>The base64-encoded 128-bit MD5 digest of the data. This header must be used as a message
     *          integrity check to verify that the request body was not corrupted in transit. For more
     *          information, go to <a href="http://www.ietf.org/rfc/rfc1864.txt">RFC
     *          1864.</a>
     *          </p>
     *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
     */
    String contentMD5;

    /**
     * <p>Allows grantee the read, write, read ACP, and write ACP permissions on the
     *          bucket.</p>
     */
    String grantFullControl;

    /**
     * <p>Allows grantee to list the objects in the bucket.</p>
     */
    String grantRead;

    /**
     * <p>Allows grantee to read the bucket ACL.</p>
     */
    String grantReadACP;

    /**
     * <p>Allows grantee to create, overwrite, and delete any object in the bucket.</p>
     */
    String grantWrite;

    /**
     * <p>Allows grantee to write the ACL for the applicable bucket.</p>
     */
    String grantWriteACP;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    HttpHeader[] customHeaders;

    Map<String, String> customQueryParameters;

    PutBucketAclRequest() {
        this.aCL = null;
        this.accessControlPolicy = null;
        this.bucket = "";
        this.contentMD5 = "";
        this.grantFullControl = "";
        this.grantRead = "";
        this.grantReadACP = "";
        this.grantWrite = "";
        this.grantWriteACP = "";
        this.expectedBucketOwner = "";
        this.customHeaders = null;
        this.customQueryParameters = null;
    }

    protected PutBucketAclRequest(BuilderImpl builder) {
        this.aCL = builder.aCL;
        this.accessControlPolicy = builder.accessControlPolicy;
        this.bucket = builder.bucket;
        this.contentMD5 = builder.contentMD5;
        this.grantFullControl = builder.grantFullControl;
        this.grantRead = builder.grantRead;
        this.grantReadACP = builder.grantReadACP;
        this.grantWrite = builder.grantWrite;
        this.grantWriteACP = builder.grantWriteACP;
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
        return Objects.hash(PutBucketAclRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof PutBucketAclRequest);
    }

    public BucketCannedACL aCL() {
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
        Builder aCL(BucketCannedACL aCL);

        Builder accessControlPolicy(AccessControlPolicy accessControlPolicy);

        Builder bucket(String bucket);

        Builder contentMD5(String contentMD5);

        Builder grantFullControl(String grantFullControl);

        Builder grantRead(String grantRead);

        Builder grantReadACP(String grantReadACP);

        Builder grantWrite(String grantWrite);

        Builder grantWriteACP(String grantWriteACP);

        Builder expectedBucketOwner(String expectedBucketOwner);

        Builder customHeaders(HttpHeader[] customHeaders);

        Builder customQueryParameters(Map<String, String> customQueryParameters);

        PutBucketAclRequest build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The canned ACL to apply to the bucket.</p>
         */
        BucketCannedACL aCL;

        /**
         * <p>Contains the elements that set the ACL permissions for an object per grantee.</p>
         */
        AccessControlPolicy accessControlPolicy;

        /**
         * <p>The bucket to which to apply the ACL.</p>
         */
        String bucket;

        /**
         * <p>The base64-encoded 128-bit MD5 digest of the data. This header must be used as a message
         *          integrity check to verify that the request body was not corrupted in transit. For more
         *          information, go to <a href="http://www.ietf.org/rfc/rfc1864.txt">RFC
         *          1864.</a>
         *          </p>
         *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
         */
        String contentMD5;

        /**
         * <p>Allows grantee the read, write, read ACP, and write ACP permissions on the
         *          bucket.</p>
         */
        String grantFullControl;

        /**
         * <p>Allows grantee to list the objects in the bucket.</p>
         */
        String grantRead;

        /**
         * <p>Allows grantee to read the bucket ACL.</p>
         */
        String grantReadACP;

        /**
         * <p>Allows grantee to create, overwrite, and delete any object in the bucket.</p>
         */
        String grantWrite;

        /**
         * <p>Allows grantee to write the ACL for the applicable bucket.</p>
         */
        String grantWriteACP;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        HttpHeader[] customHeaders;

        Map<String, String> customQueryParameters;

        protected BuilderImpl() {
        }

        private BuilderImpl(PutBucketAclRequest model) {
            aCL(model.aCL);
            accessControlPolicy(model.accessControlPolicy);
            bucket(model.bucket);
            contentMD5(model.contentMD5);
            grantFullControl(model.grantFullControl);
            grantRead(model.grantRead);
            grantReadACP(model.grantReadACP);
            grantWrite(model.grantWrite);
            grantWriteACP(model.grantWriteACP);
            expectedBucketOwner(model.expectedBucketOwner);
            customHeaders(model.customHeaders);
            customQueryParameters(model.customQueryParameters);
        }

        public PutBucketAclRequest build() {
            return new PutBucketAclRequest(this);
        }

        public final Builder aCL(BucketCannedACL aCL) {
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

        public BucketCannedACL aCL() {
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
