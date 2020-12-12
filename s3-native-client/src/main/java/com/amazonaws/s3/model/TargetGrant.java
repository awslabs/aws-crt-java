// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class TargetGrant {
    private Grantee grantee;

    private BucketLogsPermission permission;

    private TargetGrant() {
        this.grantee = null;
        this.permission = null;
    }

    private TargetGrant(Builder builder) {
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

    public void setGrantee(final Grantee grantee) {
        this.grantee = grantee;
    }

    public BucketLogsPermission permission() {
        return permission;
    }

    public void setPermission(final BucketLogsPermission permission) {
        this.permission = permission;
    }

    static final class Builder {
        private Grantee grantee;

        private BucketLogsPermission permission;

        private Builder() {
        }

        private Builder(TargetGrant model) {
            grantee(model.grantee);
            permission(model.permission);
        }

        public TargetGrant build() {
            return new com.amazonaws.s3.model.TargetGrant(this);
        }

        /**
         * <p>Container for the person being granted permissions.</p>
         */
        public final Builder grantee(Grantee grantee) {
            this.grantee = grantee;
            return this;
        }

        /**
         * <p>Logging permissions assigned to the grantee for the bucket.</p>
         */
        public final Builder permission(BucketLogsPermission permission) {
            this.permission = permission;
            return this;
        }
    }
}
