// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PublicAccessBlockConfiguration {
    /**
     * <p>Specifies whether Amazon S3 should block public access control lists (ACLs) for this bucket
     *          and objects in this bucket. Setting this element to <code>TRUE</code> causes the following
     *          behavior:</p>
     *          <ul>
     *             <li>
     *                <p>PUT Bucket acl and PUT Object acl calls fail if the specified ACL is
     *                public.</p>
     *             </li>
     *             <li>
     *                <p>PUT Object calls fail if the request includes a public ACL.</p>
     *             </li>
     *             <li>
     *                <p>PUT Bucket calls fail if the request includes a public ACL.</p>
     *             </li>
     *          </ul>
     *          <p>Enabling this setting doesn't affect existing policies or ACLs.</p>
     */
    Boolean blockPublicAcls;

    /**
     * <p>Specifies whether Amazon S3 should ignore public ACLs for this bucket and objects in this
     *          bucket. Setting this element to <code>TRUE</code> causes Amazon S3 to ignore all public ACLs on
     *          this bucket and objects in this bucket.</p>
     *          <p>Enabling this setting doesn't affect the persistence of any existing ACLs and doesn't
     *          prevent new public ACLs from being set.</p>
     */
    Boolean ignorePublicAcls;

    /**
     * <p>Specifies whether Amazon S3 should block public bucket policies for this bucket. Setting this
     *          element to <code>TRUE</code> causes Amazon S3 to reject calls to PUT Bucket policy if the
     *          specified bucket policy allows public access. </p>
     *          <p>Enabling this setting doesn't affect existing bucket policies.</p>
     */
    Boolean blockPublicPolicy;

    /**
     * <p>Specifies whether Amazon S3 should restrict public bucket policies for this bucket. Setting
     *          this element to <code>TRUE</code> restricts access to this bucket to only AWS service
     *          principals and authorized users within this account if the bucket has a public
     *          policy.</p>
     *          <p>Enabling this setting doesn't affect previously stored bucket policies, except that
     *          public and cross-account access within any public bucket policy, including non-public
     *          delegation to specific accounts, is blocked.</p>
     */
    Boolean restrictPublicBuckets;

    PublicAccessBlockConfiguration() {
        this.blockPublicAcls = null;
        this.ignorePublicAcls = null;
        this.blockPublicPolicy = null;
        this.restrictPublicBuckets = null;
    }

    protected PublicAccessBlockConfiguration(BuilderImpl builder) {
        this.blockPublicAcls = builder.blockPublicAcls;
        this.ignorePublicAcls = builder.ignorePublicAcls;
        this.blockPublicPolicy = builder.blockPublicPolicy;
        this.restrictPublicBuckets = builder.restrictPublicBuckets;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(PublicAccessBlockConfiguration.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof PublicAccessBlockConfiguration);
    }

    public Boolean blockPublicAcls() {
        return blockPublicAcls;
    }

    public Boolean ignorePublicAcls() {
        return ignorePublicAcls;
    }

    public Boolean blockPublicPolicy() {
        return blockPublicPolicy;
    }

    public Boolean restrictPublicBuckets() {
        return restrictPublicBuckets;
    }

    public void setBlockPublicAcls(final Boolean blockPublicAcls) {
        this.blockPublicAcls = blockPublicAcls;
    }

    public void setIgnorePublicAcls(final Boolean ignorePublicAcls) {
        this.ignorePublicAcls = ignorePublicAcls;
    }

    public void setBlockPublicPolicy(final Boolean blockPublicPolicy) {
        this.blockPublicPolicy = blockPublicPolicy;
    }

    public void setRestrictPublicBuckets(final Boolean restrictPublicBuckets) {
        this.restrictPublicBuckets = restrictPublicBuckets;
    }

    public interface Builder {
        Builder blockPublicAcls(Boolean blockPublicAcls);

        Builder ignorePublicAcls(Boolean ignorePublicAcls);

        Builder blockPublicPolicy(Boolean blockPublicPolicy);

        Builder restrictPublicBuckets(Boolean restrictPublicBuckets);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Specifies whether Amazon S3 should block public access control lists (ACLs) for this bucket
         *          and objects in this bucket. Setting this element to <code>TRUE</code> causes the following
         *          behavior:</p>
         *          <ul>
         *             <li>
         *                <p>PUT Bucket acl and PUT Object acl calls fail if the specified ACL is
         *                public.</p>
         *             </li>
         *             <li>
         *                <p>PUT Object calls fail if the request includes a public ACL.</p>
         *             </li>
         *             <li>
         *                <p>PUT Bucket calls fail if the request includes a public ACL.</p>
         *             </li>
         *          </ul>
         *          <p>Enabling this setting doesn't affect existing policies or ACLs.</p>
         */
        Boolean blockPublicAcls;

        /**
         * <p>Specifies whether Amazon S3 should ignore public ACLs for this bucket and objects in this
         *          bucket. Setting this element to <code>TRUE</code> causes Amazon S3 to ignore all public ACLs on
         *          this bucket and objects in this bucket.</p>
         *          <p>Enabling this setting doesn't affect the persistence of any existing ACLs and doesn't
         *          prevent new public ACLs from being set.</p>
         */
        Boolean ignorePublicAcls;

        /**
         * <p>Specifies whether Amazon S3 should block public bucket policies for this bucket. Setting this
         *          element to <code>TRUE</code> causes Amazon S3 to reject calls to PUT Bucket policy if the
         *          specified bucket policy allows public access. </p>
         *          <p>Enabling this setting doesn't affect existing bucket policies.</p>
         */
        Boolean blockPublicPolicy;

        /**
         * <p>Specifies whether Amazon S3 should restrict public bucket policies for this bucket. Setting
         *          this element to <code>TRUE</code> restricts access to this bucket to only AWS service
         *          principals and authorized users within this account if the bucket has a public
         *          policy.</p>
         *          <p>Enabling this setting doesn't affect previously stored bucket policies, except that
         *          public and cross-account access within any public bucket policy, including non-public
         *          delegation to specific accounts, is blocked.</p>
         */
        Boolean restrictPublicBuckets;

        protected BuilderImpl() {
        }

        private BuilderImpl(PublicAccessBlockConfiguration model) {
            blockPublicAcls(model.blockPublicAcls);
            ignorePublicAcls(model.ignorePublicAcls);
            blockPublicPolicy(model.blockPublicPolicy);
            restrictPublicBuckets(model.restrictPublicBuckets);
        }

        public PublicAccessBlockConfiguration build() {
            return new PublicAccessBlockConfiguration(this);
        }

        public final Builder blockPublicAcls(Boolean blockPublicAcls) {
            this.blockPublicAcls = blockPublicAcls;
            return this;
        }

        public final Builder ignorePublicAcls(Boolean ignorePublicAcls) {
            this.ignorePublicAcls = ignorePublicAcls;
            return this;
        }

        public final Builder blockPublicPolicy(Boolean blockPublicPolicy) {
            this.blockPublicPolicy = blockPublicPolicy;
            return this;
        }

        public final Builder restrictPublicBuckets(Boolean restrictPublicBuckets) {
            this.restrictPublicBuckets = restrictPublicBuckets;
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

        public Boolean blockPublicAcls() {
            return blockPublicAcls;
        }

        public Boolean ignorePublicAcls() {
            return ignorePublicAcls;
        }

        public Boolean blockPublicPolicy() {
            return blockPublicPolicy;
        }

        public Boolean restrictPublicBuckets() {
            return restrictPublicBuckets;
        }

        public void setBlockPublicAcls(final Boolean blockPublicAcls) {
            this.blockPublicAcls = blockPublicAcls;
        }

        public void setIgnorePublicAcls(final Boolean ignorePublicAcls) {
            this.ignorePublicAcls = ignorePublicAcls;
        }

        public void setBlockPublicPolicy(final Boolean blockPublicPolicy) {
            this.blockPublicPolicy = blockPublicPolicy;
        }

        public void setRestrictPublicBuckets(final Boolean restrictPublicBuckets) {
            this.restrictPublicBuckets = restrictPublicBuckets;
        }
    }
}
