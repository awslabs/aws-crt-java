// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class PolicyStatus {
    /**
     * <p>The policy status for this bucket. <code>TRUE</code> indicates that this bucket is
     *          public. <code>FALSE</code> indicates that the bucket is not public.</p>
     */
    Boolean isPublic;

    PolicyStatus() {
        this.isPublic = null;
    }

    protected PolicyStatus(BuilderImpl builder) {
        this.isPublic = builder.isPublic;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public interface Builder {
        Builder isPublic(Boolean isPublic);

        PolicyStatus build();
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>The policy status for this bucket. <code>TRUE</code> indicates that this bucket is
         *          public. <code>FALSE</code> indicates that the bucket is not public.</p>
         */
        Boolean isPublic;

        protected BuilderImpl() {
        }

        private BuilderImpl(PolicyStatus model) {
            isPublic(model.isPublic);
        }

        public PolicyStatus build() {
            return new PolicyStatus(this);
        }

        public final Builder isPublic(Boolean isPublic) {
            this.isPublic = isPublic;
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

        public Boolean isPublic() {
            return isPublic;
        }

        public void setIsPublic(final Boolean isPublic) {
            this.isPublic = isPublic;
        }
    }
}
