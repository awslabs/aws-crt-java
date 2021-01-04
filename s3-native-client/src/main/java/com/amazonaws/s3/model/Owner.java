// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Owner {
    /**
     * <p>Container for the display name of the owner.</p>
     */
    String displayName;

    /**
     * <p>Container for the ID of the owner.</p>
     */
    String iD;

    Owner() {
        this.displayName = "";
        this.iD = "";
    }

    protected Owner(BuilderImpl builder) {
        this.displayName = builder.displayName;
        this.iD = builder.iD;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public String iD() {
        return iD;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public void setID(final String iD) {
        this.iD = iD;
    }

    public interface Builder {
        Builder displayName(String displayName);

        Builder iD(String iD);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Container for the display name of the owner.</p>
         */
        String displayName;

        /**
         * <p>Container for the ID of the owner.</p>
         */
        String iD;

        protected BuilderImpl() {
        }

        private BuilderImpl(Owner model) {
            displayName(model.displayName);
            iD(model.iD);
        }

        public Owner build() {
            return new Owner(this);
        }

        public final Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public final Builder iD(String iD) {
            this.iD = iD;
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

        public String displayName() {
            return displayName;
        }

        public String iD() {
            return iD;
        }

        public void setDisplayName(final String displayName) {
            this.displayName = displayName;
        }

        public void setID(final String iD) {
            this.iD = iD;
        }
    }
}
