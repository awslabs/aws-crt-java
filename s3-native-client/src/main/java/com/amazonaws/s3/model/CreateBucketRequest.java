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
public class CreateBucketRequest {
    private BucketCannedACL aCL;

    private String bucket;

    private CreateBucketConfiguration createBucketConfiguration;

    private String grantFullControl;

    private String grantRead;

    private String grantReadACP;

    private String grantWrite;

    private String grantWriteACP;

    private Boolean objectLockEnabledForBucket;

    private CreateBucketRequest() {
        this.aCL = null;
        this.bucket = null;
        this.createBucketConfiguration = null;
        this.grantFullControl = null;
        this.grantRead = null;
        this.grantReadACP = null;
        this.grantWrite = null;
        this.grantWriteACP = null;
        this.objectLockEnabledForBucket = null;
    }

    private CreateBucketRequest(Builder builder) {
        this.aCL = builder.aCL;
        this.bucket = builder.bucket;
        this.createBucketConfiguration = builder.createBucketConfiguration;
        this.grantFullControl = builder.grantFullControl;
        this.grantRead = builder.grantRead;
        this.grantReadACP = builder.grantReadACP;
        this.grantWrite = builder.grantWrite;
        this.grantWriteACP = builder.grantWriteACP;
        this.objectLockEnabledForBucket = builder.objectLockEnabledForBucket;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(CreateBucketRequest.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof CreateBucketRequest);
    }

    public BucketCannedACL aCL() {
        return aCL;
    }

    public void setACL(final BucketCannedACL aCL) {
        this.aCL = aCL;
    }

    public String bucket() {
        return bucket;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public CreateBucketConfiguration createBucketConfiguration() {
        return createBucketConfiguration;
    }

    public void setCreateBucketConfiguration(
            final CreateBucketConfiguration createBucketConfiguration) {
        this.createBucketConfiguration = createBucketConfiguration;
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

    public Boolean objectLockEnabledForBucket() {
        return objectLockEnabledForBucket;
    }

    public void setObjectLockEnabledForBucket(final Boolean objectLockEnabledForBucket) {
        this.objectLockEnabledForBucket = objectLockEnabledForBucket;
    }

    static final class Builder {
        private BucketCannedACL aCL;

        private String bucket;

        private CreateBucketConfiguration createBucketConfiguration;

        private String grantFullControl;

        private String grantRead;

        private String grantReadACP;

        private String grantWrite;

        private String grantWriteACP;

        private Boolean objectLockEnabledForBucket;

        private Builder() {
        }

        private Builder(CreateBucketRequest model) {
            aCL(model.aCL);
            bucket(model.bucket);
            createBucketConfiguration(model.createBucketConfiguration);
            grantFullControl(model.grantFullControl);
            grantRead(model.grantRead);
            grantReadACP(model.grantReadACP);
            grantWrite(model.grantWrite);
            grantWriteACP(model.grantWriteACP);
            objectLockEnabledForBucket(model.objectLockEnabledForBucket);
        }

        public CreateBucketRequest build() {
            return new com.amazonaws.s3.model.CreateBucketRequest(this);
        }

        /**
         * <p>The canned ACL to apply to the bucket.</p>
         */
        public final Builder aCL(BucketCannedACL aCL) {
            this.aCL = aCL;
            return this;
        }

        /**
         * <p>The name of the bucket to create.</p>
         */
        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        /**
         * <p>The configuration information for the bucket.</p>
         */
        public final Builder createBucketConfiguration(
                CreateBucketConfiguration createBucketConfiguration) {
            this.createBucketConfiguration = createBucketConfiguration;
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
         * <p>Specifies whether you want S3 Object Lock to be enabled for the new bucket.</p>
         */
        public final Builder objectLockEnabledForBucket(Boolean objectLockEnabledForBucket) {
            this.objectLockEnabledForBucket = objectLockEnabledForBucket;
            return this;
        }
    }
}
