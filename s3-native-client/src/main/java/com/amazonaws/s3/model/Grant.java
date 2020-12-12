// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Grant {
    private Grantee grantee;

    private Permission permission;

    private Grant() {
        this.grantee = null;
        this.permission = null;
    }

    private Grant(Builder builder) {
        this.grantee = builder.grantee;
        this.permission = builder.permission;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setGrantee(final Grantee grantee) {
        this.grantee = grantee;
    }

    public Permission permission() {
        return permission;
    }

    public void setPermission(final Permission permission) {
        this.permission = permission;
    }

    static final class Builder {
        private Grantee grantee;

        private Permission permission;

        private Builder() {
        }

        private Builder(Grant model) {
            grantee(model.grantee);
            permission(model.permission);
        }

        public Grant build() {
            return new com.amazonaws.s3.model.Grant(this);
        }

        /**
         * <p>The person being granted permissions.</p>
         */
        public final Builder grantee(Grantee grantee) {
            this.grantee = grantee;
            return this;
        }

        /**
         * <p>Specifies the permission given to the grantee.</p>
         */
        public final Builder permission(Permission permission) {
            this.permission = permission;
            return this;
        }
    }
}
