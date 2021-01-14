// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PutBucketVersioningRequest {
    /**
     * <p>The bucket name.</p>
     */
    String bucket;

    /**
     * <p>>The base64-encoded 128-bit MD5 digest of the data. You must use this header as a
     *          message integrity check to verify that the request body was not corrupted in transit. For
     *          more information, see <a href="http://www.ietf.org/rfc/rfc1864.txt">RFC
     *          1864</a>.</p>
     *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
     */
    String contentMD5;

    /**
     * <p>The concatenation of the authentication device's serial number, a space, and the value
     *          that is displayed on your authentication device.</p>
     */
    String mFA;

    /**
     * <p>Container for setting the versioning state.</p>
     */
    VersioningConfiguration versioningConfiguration;

    /**
     * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
     */
    String expectedBucketOwner;

    PutBucketVersioningRequest() {
        this.bucket = "";
        this.contentMD5 = "";
        this.mFA = "";
        this.versioningConfiguration = null;
        this.expectedBucketOwner = "";
    }

    protected PutBucketVersioningRequest(BuilderImpl builder) {
        this.bucket = builder.bucket;
        this.contentMD5 = builder.contentMD5;
        this.mFA = builder.mFA;
        this.versioningConfiguration = builder.versioningConfiguration;
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
        return Objects.hash(PutBucketVersioningRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof PutBucketVersioningRequest);
    }

    public String bucket() {
        return bucket;
    }

    public String contentMD5() {
        return contentMD5;
    }

    public String mFA() {
        return mFA;
    }

    public VersioningConfiguration versioningConfiguration() {
        return versioningConfiguration;
    }

    public String expectedBucketOwner() {
        return expectedBucketOwner;
    }

    public interface Builder {
        Builder bucket(String bucket);

        Builder contentMD5(String contentMD5);

        Builder mFA(String mFA);

        Builder versioningConfiguration(VersioningConfiguration versioningConfiguration);

        Builder expectedBucketOwner(String expectedBucketOwner);

        PutBucketVersioningRequest build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The bucket name.</p>
         */
        String bucket;

        /**
         * <p>>The base64-encoded 128-bit MD5 digest of the data. You must use this header as a
         *          message integrity check to verify that the request body was not corrupted in transit. For
         *          more information, see <a href="http://www.ietf.org/rfc/rfc1864.txt">RFC
         *          1864</a>.</p>
         *          <p>For requests made using the AWS Command Line Interface (CLI) or AWS SDKs, this field is calculated automatically.</p>
         */
        String contentMD5;

        /**
         * <p>The concatenation of the authentication device's serial number, a space, and the value
         *          that is displayed on your authentication device.</p>
         */
        String mFA;

        /**
         * <p>Container for setting the versioning state.</p>
         */
        VersioningConfiguration versioningConfiguration;

        /**
         * <p>The account id of the expected bucket owner. If the bucket is owned by a different account, the request will fail with an HTTP <code>403 (Access Denied)</code> error.</p>
         */
        String expectedBucketOwner;

        protected BuilderImpl() {
        }

        private BuilderImpl(PutBucketVersioningRequest model) {
            bucket(model.bucket);
            contentMD5(model.contentMD5);
            mFA(model.mFA);
            versioningConfiguration(model.versioningConfiguration);
            expectedBucketOwner(model.expectedBucketOwner);
        }

        public PutBucketVersioningRequest build() {
            return new PutBucketVersioningRequest(this);
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder contentMD5(String contentMD5) {
            this.contentMD5 = contentMD5;
            return this;
        }

        public final Builder mFA(String mFA) {
            this.mFA = mFA;
            return this;
        }

        public final Builder versioningConfiguration(
                VersioningConfiguration versioningConfiguration) {
            this.versioningConfiguration = versioningConfiguration;
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

        public String contentMD5() {
            return contentMD5;
        }

        public String mFA() {
            return mFA;
        }

        public VersioningConfiguration versioningConfiguration() {
            return versioningConfiguration;
        }

        public String expectedBucketOwner() {
            return expectedBucketOwner;
        }
    }
}
