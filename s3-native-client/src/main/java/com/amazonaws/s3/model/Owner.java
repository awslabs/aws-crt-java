// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Owner {
    private String displayName;

    private String iD;

    private Owner() {
        this.displayName = null;
        this.iD = null;
    }

    private Owner(Builder builder) {
        this.displayName = builder.displayName;
        this.iD = builder.iD;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Owner.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof Owner);
    }

    public String displayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public String iD() {
        return iD;
    }

    public void setID(final String iD) {
        this.iD = iD;
    }

    static final class Builder {
        private String displayName;

        private String iD;

        private Builder() {
        }

        private Builder(Owner model) {
            displayName(model.displayName);
            iD(model.iD);
        }

        public Owner build() {
            return new com.amazonaws.s3.model.Owner(this);
        }

        /**
         * <p>Container for the display name of the owner.</p>
         */
        public final Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        /**
         * <p>Container for the ID of the owner.</p>
         */
        public final Builder iD(String iD) {
            this.iD = iD;
            return this;
        }
    }
}
