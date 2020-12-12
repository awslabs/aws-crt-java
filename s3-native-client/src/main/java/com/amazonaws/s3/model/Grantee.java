// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Grantee {
    private String displayName;

    private String emailAddress;

    private String iD;

    private String uRI;

    private Type type;

    private Grantee() {
        this.displayName = null;
        this.emailAddress = null;
        this.iD = null;
        this.uRI = null;
        this.type = null;
    }

    private Grantee(Builder builder) {
        this.displayName = builder.displayName;
        this.emailAddress = builder.emailAddress;
        this.iD = builder.iD;
        this.uRI = builder.uRI;
        this.type = builder.type;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Grantee.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof Grantee);
    }

    public String displayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public String emailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(final String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String iD() {
        return iD;
    }

    public void setID(final String iD) {
        this.iD = iD;
    }

    public String uRI() {
        return uRI;
    }

    public void setURI(final String uRI) {
        this.uRI = uRI;
    }

    public Type type() {
        return type;
    }

    public void setType(final Type type) {
        this.type = type;
    }

    static final class Builder {
        private String displayName;

        private String emailAddress;

        private String iD;

        private String uRI;

        private Type type;

        private Builder() {
        }

        private Builder(Grantee model) {
            displayName(model.displayName);
            emailAddress(model.emailAddress);
            iD(model.iD);
            uRI(model.uRI);
            type(model.type);
        }

        public Grantee build() {
            return new com.amazonaws.s3.model.Grantee(this);
        }

        /**
         * <p>Screen name of the grantee.</p>
         */
        public final Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        /**
         * <p>Email address of the grantee.</p>
         *          <note>
         *             <p>Using email addresses to specify a grantee is only supported in the following AWS Regions: </p> 
         *             <ul>
         *                <li>
         *                   <p>US East (N. Virginia)</p>
         *                </li>
         *                <li>
         *                   <p>US West (N. California)</p>
         *                </li>
         *                <li>
         *                   <p> US West (Oregon)</p>
         *                </li>
         *                <li>
         *                   <p> Asia Pacific (Singapore)</p>
         *                </li>
         *                <li>
         *                   <p>Asia Pacific (Sydney)</p>
         *                </li>
         *                <li>
         *                   <p>Asia Pacific (Tokyo)</p>
         *                </li>
         *                <li>
         *                   <p>Europe (Ireland)</p>
         *                </li>
         *                <li>
         *                   <p>South America (São Paulo)</p>
         *                </li>
         *             </ul> 
         *             <p>For a list of all the Amazon S3 supported Regions and endpoints, see <a href="https://docs.aws.amazon.com/general/latest/gr/rande.html#s3_region">Regions and Endpoints</a> in the AWS General Reference.</p>
         *          </note>
         */
        public final Builder emailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
            return this;
        }

        /**
         * <p>The canonical user ID of the grantee.</p>
         */
        public final Builder iD(String iD) {
            this.iD = iD;
            return this;
        }

        /**
         * <p>URI of the grantee group.</p>
         */
        public final Builder uRI(String uRI) {
            this.uRI = uRI;
            return this;
        }

        /**
         * <p>Type of grantee</p>
         */
        public final Builder type(Type type) {
            this.type = type;
            return this;
        }
    }
}
