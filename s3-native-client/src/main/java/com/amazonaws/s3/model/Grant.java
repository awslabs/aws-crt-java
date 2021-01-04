// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Grant {
    /**
     * <p>The person being granted permissions.</p>
     */
    Grantee grantee;

    /**
     * <p>Specifies the permission given to the grantee.</p>
     */
    Permission permission;

    Grant() {
        this.grantee = null;
        this.permission = null;
    }

    protected Grant(BuilderImpl builder) {
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
        return Objects.hash(Grant.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof Grant);
    }

    public Grantee grantee() {
        return grantee;
    }

    public Permission permission() {
        return permission;
    }

    public void setGrantee(final Grantee grantee) {
        this.grantee = grantee;
    }

    public void setPermission(final Permission permission) {
        this.permission = permission;
    }

    public interface Builder {
        Builder grantee(Grantee grantee);

        Builder permission(Permission permission);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The person being granted permissions.</p>
         */
        Grantee grantee;

        /**
         * <p>Specifies the permission given to the grantee.</p>
         */
        Permission permission;

        protected BuilderImpl() {
        }

        private BuilderImpl(Grant model) {
            grantee(model.grantee);
            permission(model.permission);
        }

        public Grant build() {
            return new Grant(this);
        }

        public final Builder grantee(Grantee grantee) {
            this.grantee = grantee;
            return this;
        }

        public final Builder permission(Permission permission) {
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

        public Permission permission() {
            return permission;
        }

        public void setGrantee(final Grantee grantee) {
            this.grantee = grantee;
        }

        public void setPermission(final Permission permission) {
            this.permission = permission;
        }
    }
}
