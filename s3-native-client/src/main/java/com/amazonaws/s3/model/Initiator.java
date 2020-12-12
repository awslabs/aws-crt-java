// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Initiator {
    private String iD;

    private String displayName;

    private Initiator() {
        this.iD = null;
        this.displayName = null;
    }

    private Initiator(Builder builder) {
        this.iD = builder.iD;
        this.displayName = builder.displayName;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setID(final String iD) {
        this.iD = iD;
    }

    public String displayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    static final class Builder {
        private String iD;

        private String displayName;

        private Builder() {
        }

        private Builder(Initiator model) {
            iD(model.iD);
            displayName(model.displayName);
        }

        public Initiator build() {
            return new com.amazonaws.s3.model.Initiator(this);
        }

        /**
         * <p>If the principal is an AWS account, it provides the Canonical User ID. If the principal
         *          is an IAM User, it provides a user ARN value.</p>
         */
        public final Builder iD(String iD) {
            this.iD = iD;
            return this;
        }

        /**
         * <p>Name of the Principal.</p>
         */
        public final Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }
    }
}
