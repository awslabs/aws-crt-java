// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutBucketAclRequest {
    private BucketCannedACL aCL;

    private AccessControlPolicy accessControlPolicy;

    private String bucket;

    private String contentMD5;

    private String grantFullControl;

    private String grantRead;

    private String grantReadACP;

    private String grantWrite;

    private String grantWriteACP;

    private String expectedBucketOwner;

    private PutBucketAclRequest() {
        this.aCL = null;
        this.accessControlPolicy = null;
        this.bucket = null;
        this.contentMD5 = null;
        this.grantFullControl = null;
        this.grantRead = null;
        this.grantReadACP = null;
        this.grantWrite = null;
        this.grantWriteACP = null;
        this.expectedBucketOwner = null;
    }

    private PutBucketAclRequest(Builder builder) {
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
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setACL(final BucketCannedACL aCL) {
        this.aCL = aCL;
    }

    public AccessControlPolicy accessControlPolicy() {
        return accessControlPolicy;
    }

    public void setAccessControlPolicy(final AccessControlPolicy accessControlPolicy) {
        this.accessControlPolicy = accessControlPolicy;
    }

    public String bucket() {
        return bucket;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public String contentMD5() {
        return contentMD5;
    }

    public void setContentMD5(final String contentMD5) {
        this.contentMD5 = contentMD5;
    }

    public String grantFullControl() {
        return grantFullControl;
    }

    public void setGrantFullControl(final String grantFullControl) {
        this.grantFullControl = grantFullControl;
    }

    public String grantRead() {
        return grantRead;
    }

    public void setGrantRead(final String grantRead) {
        this.grantRead = grantRead;
    }

    public String grantReadACP() {
        return grantReadACP;
    }

    public void setGrantReadACP(final String grantReadACP) {
        this.grantReadACP = grantReadACP;
    }

    public String grantWrite() {
        return grantWrite;
    }

    public void setGrantWrite(final String grantWrite) {
        this.grantWrite = grantWrite;
    }

    public String grantWriteACP() {
        return grantWriteACP;
    }

    public void setGrantWriteACP(final String grantWriteACP) {
        this.grantWriteACP = grantWriteACP;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public void setExpectedBucketOwner(final String expectedBucketOwner) {
        this.expectedBucketOwner = expectedBucketOwner;
    }

    static final class Builder {
        private BucketCannedACL aCL;

        private AccessControlPolicy accessControlPolicy;

        private String bucket;

        private String contentMD5;

        private String grantFullControl;

        private String grantRead;

        private String grantReadACP;

        private String grantWrite;

        private String grantWriteACP;

        private String expectedBucketOwner;

        private Builder() {
        }

        private Builder(PutBucketAclRequest model) {
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
        }

        public PutBucketAclRequest build() {
            return new com.amazonaws.s3.model.PutBucketAclRequest(this);
        }

        /**
         * <p>The canned ACL to apply to the bucket.</p>
         */
        public final Builder aCL(BucketCannedACL aCL) {
            this.aCL = aCL;
            return this;
        }

        /**
         * <p>Contains the elements that set the ACL permissions for an object per grantee.</p>
         */
        public final Builder accessControlPolicy(AccessControlPolicy accessControlPolicy) {
            this.accessControlPolicy = accessControlPolicy;
            return this;
        }

        /**
         * <p>The bucket to which to apply the ACL.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>The base64-encoded 128-bit MD5 digest of the data. This header must be used as a message
         *          integrity check to verify that the request body was not corrupted in transit. For more
         *          information, go to <a href="http://www.ietf.org/rfc/rfc1864.txt">RFC
         *          1864.</a>
         *          </p>
         *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
         */
        public final Builder contentMD5(String contentMD5) {
            this.contentMD5 = contentMD5;
            return this;
        }

        /**
         * <p>Allows grantee the read, write, read ACP, and write ACP permissions on the
         *          bucket.</p>
         */
        public final Builder grantFullControl(String grantFullControl) {
            this.grantFullControl = grantFullControl;
            return this;
        }

        /**
         * <p>Allows grantee to list the objects in the bucket.</p>
         */
        public final Builder grantRead(String grantRead) {
            this.grantRead = grantRead;
            return this;
        }

        /**
         * <p>Allows grantee to read the bucket ACL.</p>
         */
        public final Builder grantReadACP(String grantReadACP) {
            this.grantReadACP = grantReadACP;
            return this;
        }

        /**
         * <p>Allows grantee to create, overwrite, and delete any object in the bucket.</p>
         */
        public final Builder grantWrite(String grantWrite) {
            this.grantWrite = grantWrite;
            return this;
        }

        /**
         * <p>Allows grantee to write the ACL for the applicable bucket.</p>
         */
        public final Builder grantWriteACP(String grantWriteACP) {
            this.grantWriteACP = grantWriteACP;
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
