// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PolicyStatus {
    private Boolean isPublic;

    private PolicyStatus() {
        this.isPublic = null;
    }

    private PolicyStatus(Builder builder) {
        this.isPublic = builder.isPublic;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(PolicyStatus.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof PolicyStatus);
    }

    public Boolean isPublic() {
        return isPublic;
    }

    public void setIsPublic(final Boolean isPublic) {
        this.isPublic = isPublic;
    }

    static final class Builder {
        private Boolean isPublic;

        private Builder() {
        }

        private Builder(PolicyStatus model) {
            isPublic(model.isPublic);
        }

        public PolicyStatus build() {
            return new com.amazonaws.s3.model.PolicyStatus(this);
        }

        /**
         * <p>The policy status for this bucket. <code>TRUE</code> indicates that this bucket is
         *          public. <code>FALSE</code> indicates that the bucket is not public.</p>
         */
        public final Builder isPublic(Boolean isPublic) {
            this.isPublic = isPublic;
            return this;
        }
    }
}
