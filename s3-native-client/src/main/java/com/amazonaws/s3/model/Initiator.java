// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Initiator {
    /**
     * <p>If the principal is an AWS account, it provides the Canonical User ID. If the principal
     *          is an IAM User, it provides a user ARN value.</p>
     */
    String iD;

    /**
     * <p>Name of the Principal.</p>
     */
    String displayName;

    Initiator() {
        this.iD = "";
        this.displayName = "";
    }

    protected Initiator(BuilderImpl builder) {
        this.iD = builder.iD;
        this.displayName = builder.displayName;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(Initiator.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof Initiator);
    }

    public String iD() {
        return iD;
    }

    public String displayName() {
        return displayName;
    }

    public void setID(final String iD) {
        this.iD = iD;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public interface Builder {
        Builder iD(String iD);

        Builder displayName(String displayName);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>If the principal is an AWS account, it provides the Canonical User ID. If the principal
         *          is an IAM User, it provides a user ARN value.</p>
         */
        String iD;

        /**
         * <p>Name of the Principal.</p>
         */
        String displayName;

        protected BuilderImpl() {
        }

        private BuilderImpl(Initiator model) {
            iD(model.iD);
            displayName(model.displayName);
        }

        public Initiator build() {
            return new Initiator(this);
        }

        public final Builder iD(String iD) {
            this.iD = iD;
            return this;
        }

        public final Builder displayName(String displayName) {
            this.displayName = displayName;
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

        public String iD() {
            return iD;
        }

        public String displayName() {
            return displayName;
        }

        public void setID(final String iD) {
            this.iD = iD;
        }

        public void setDisplayName(final String displayName) {
            this.displayName = displayName;
        }
    }
}
