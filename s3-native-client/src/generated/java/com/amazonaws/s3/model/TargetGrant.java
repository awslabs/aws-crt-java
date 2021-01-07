// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class TargetGrant {
    /**
     * <p>Container for the person being granted permissions.</p>
     */
    Grantee grantee;

    /**
     * <p>Logging permissions assigned to the grantee for the bucket.</p>
     */
    BucketLogsPermission permission;

    TargetGrant() {
        this.grantee = null;
        this.permission = null;
    }

    protected TargetGrant(BuilderImpl builder) {
        this.grantee = builder.grantee;
        this.permission = builder.permission;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(TargetGrant.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof TargetGrant);
    }

    public Grantee grantee() {
        return grantee;
    }

    public BucketLogsPermission permission() {
        return permission;
    }

    public void setGrantee(final Grantee grantee) {
        this.grantee = grantee;
    }

    public void setPermission(final BucketLogsPermission permission) {
        this.permission = permission;
    }

    public interface Builder {
        Builder grantee(Grantee grantee);

        Builder permission(BucketLogsPermission permission);

        TargetGrant build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Container for the person being granted permissions.</p>
         */
        Grantee grantee;

        /**
         * <p>Logging permissions assigned to the grantee for the bucket.</p>
         */
        BucketLogsPermission permission;

        protected BuilderImpl() {
        }

        private BuilderImpl(TargetGrant model) {
            grantee(model.grantee);
            permission(model.permission);
        }

        public TargetGrant build() {
            return new TargetGrant(this);
        }

        public final Builder grantee(Grantee grantee) {
            this.grantee = grantee;
            return this;
        }

        public final Builder permission(BucketLogsPermission permission) {
            this.permission = permission;
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

        public Grantee grantee() {
            return grantee;
        }

        public BucketLogsPermission permission() {
            return permission;
        }

        public void setGrantee(final Grantee grantee) {
            this.grantee = grantee;
        }

        public void setPermission(final BucketLogsPermission permission) {
            this.permission = permission;
        }
    }
}
