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
public class CreateBucketRequest {
    /**
     * <p>The canned ACL to apply to the bucket.</p>
     */
    BucketCannedACL aCL;

    /**
     * <p>The name of the bucket to create.</p>
     */
    String bucket;

    /**
     * <p>The configuration information for the bucket.</p>
     */
    CreateBucketConfiguration createBucketConfiguration;

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
     * <p>Specifies whether you want S3 Object Lock to be enabled for the new bucket.</p>
     */
    Boolean objectLockEnabledForBucket;

    CreateBucketRequest() {
        this.aCL = null;
        this.bucket = "";
        this.createBucketConfiguration = null;
        this.grantFullControl = "";
        this.grantRead = "";
        this.grantReadACP = "";
        this.grantWrite = "";
        this.grantWriteACP = "";
        this.objectLockEnabledForBucket = null;
    }

    protected CreateBucketRequest(BuilderImpl builder) {
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

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public String bucket() {
        return bucket;
    }

    public CreateBucketConfiguration createBucketConfiguration() {
        return createBucketConfiguration;
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

    public Boolean objectLockEnabledForBucket() {
        return objectLockEnabledForBucket;
    }

    public void setACL(final BucketCannedACL aCL) {
        this.aCL = aCL;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public void setCreateBucketConfiguration(
            final CreateBucketConfiguration createBucketConfiguration) {
        this.createBucketConfiguration = createBucketConfiguration;
    }

    public void setGrantFullControl(final String grantFullControl) {
        this.grantFullControl = grantFullControl;
    }

    public void setGrantRead(final String grantRead) {
        this.grantRead = grantRead;
    }

    public void setGrantReadACP(final String grantReadACP) {
        this.grantReadACP = grantReadACP;
    }

    public void setGrantWrite(final String grantWrite) {
        this.grantWrite = grantWrite;
    }

    public void setGrantWriteACP(final String grantWriteACP) {
        this.grantWriteACP = grantWriteACP;
    }

    public void setObjectLockEnabledForBucket(final Boolean objectLockEnabledForBucket) {
        this.objectLockEnabledForBucket = objectLockEnabledForBucket;
    }

    public interface Builder {
        Builder aCL(BucketCannedACL aCL);

        Builder bucket(String bucket);

        Builder createBucketConfiguration(CreateBucketConfiguration createBucketConfiguration);

        Builder grantFullControl(String grantFullControl);

        Builder grantRead(String grantRead);

        Builder grantReadACP(String grantReadACP);

        Builder grantWrite(String grantWrite);

        Builder grantWriteACP(String grantWriteACP);

        Builder objectLockEnabledForBucket(Boolean objectLockEnabledForBucket);

        CreateBucketRequest build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The canned ACL to apply to the bucket.</p>
         */
        BucketCannedACL aCL;

        /**
         * <p>The name of the bucket to create.</p>
         */
        String bucket;

        /**
         * <p>The configuration information for the bucket.</p>
         */
        CreateBucketConfiguration createBucketConfiguration;

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
         * <p>Specifies whether you want S3 Object Lock to be enabled for the new bucket.</p>
         */
        Boolean objectLockEnabledForBucket;

        protected BuilderImpl() {
        }

        private BuilderImpl(CreateBucketRequest model) {
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
            return new CreateBucketRequest(this);
        }

        public final Builder aCL(BucketCannedACL aCL) {
            this.aCL = aCL;
            return this;
        }

        public final Builder bucket(String bucket) {
            this.bucket = bucket;
            return this;
        }

        public final Builder createBucketConfiguration(
                CreateBucketConfiguration createBucketConfiguration) {
            this.createBucketConfiguration = createBucketConfiguration;
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

        public final Builder objectLockEnabledForBucket(Boolean objectLockEnabledForBucket) {
            this.objectLockEnabledForBucket = objectLockEnabledForBucket;
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

        public String bucket() {
            return bucket;
        }

        public CreateBucketConfiguration createBucketConfiguration() {
            return createBucketConfiguration;
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

        public Boolean objectLockEnabledForBucket() {
            return objectLockEnabledForBucket;
        }

        public void setACL(final BucketCannedACL aCL) {
            this.aCL = aCL;
        }

        public void setBucket(final String bucket) {
            this.bucket = bucket;
        }

        public void setCreateBucketConfiguration(
                final CreateBucketConfiguration createBucketConfiguration) {
            this.createBucketConfiguration = createBucketConfiguration;
        }

        public void setGrantFullControl(final String grantFullControl) {
            this.grantFullControl = grantFullControl;
        }

        public void setGrantRead(final String grantRead) {
            this.grantRead = grantRead;
        }

        public void setGrantReadACP(final String grantReadACP) {
            this.grantReadACP = grantReadACP;
        }

        public void setGrantWrite(final String grantWrite) {
            this.grantWrite = grantWrite;
        }

        public void setGrantWriteACP(final String grantWriteACP) {
            this.grantWriteACP = grantWriteACP;
        }

        public void setObjectLockEnabledForBucket(final Boolean objectLockEnabledForBucket) {
            this.objectLockEnabledForBucket = objectLockEnabledForBucket;
        }
    }
}
