// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Grantee {
    /**
     * <p>Screen name of the grantee.</p>
     */
    String displayName;

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
    String emailAddress;

    /**
     * <p>The canonical user ID of the grantee.</p>
     */
    String iD;

    /**
     * <p>URI of the grantee group.</p>
     */
    String uRI;

    /**
     * <p>Type of grantee</p>
     */
    Type type;

    Grantee() {
        this.displayName = "";
        this.emailAddress = "";
        this.iD = "";
        this.uRI = "";
        this.type = null;
    }

    protected Grantee(BuilderImpl builder) {
        this.displayName = builder.displayName;
        this.emailAddress = builder.emailAddress;
        this.iD = builder.iD;
        this.uRI = builder.uRI;
        this.type = builder.type;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
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

    public String emailAddress() {
        return emailAddress;
    }

    public String iD() {
        return iD;
    }

    public String uRI() {
        return uRI;
    }

    public Type type() {
        return type;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public void setEmailAddress(final String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void setID(final String iD) {
        this.iD = iD;
    }

    public void setURI(final String uRI) {
        this.uRI = uRI;
    }

    public void setType(final Type type) {
        this.type = type;
    }

    public interface Builder {
        Builder displayName(String displayName);

        Builder emailAddress(String emailAddress);

        Builder iD(String iD);

        Builder uRI(String uRI);

        Builder type(Type type);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Screen name of the grantee.</p>
         */
        String displayName;

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
        String emailAddress;

        /**
         * <p>The canonical user ID of the grantee.</p>
         */
        String iD;

        /**
         * <p>URI of the grantee group.</p>
         */
        String uRI;

        /**
         * <p>Type of grantee</p>
         */
        Type type;

        protected BuilderImpl() {
        }

        private BuilderImpl(Grantee model) {
            displayName(model.displayName);
            emailAddress(model.emailAddress);
            iD(model.iD);
            uRI(model.uRI);
            type(model.type);
        }

        public Grantee build() {
            return new Grantee(this);
        }

        public final Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public final Builder emailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
            return this;
        }

        public final Builder iD(String iD) {
            this.iD = iD;
            return this;
        }

        public final Builder uRI(String uRI) {
            this.uRI = uRI;
            return this;
        }

        public final Builder type(Type type) {
            this.type = type;
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

        public String emailAddress() {
            return emailAddress;
        }

        public String iD() {
            return iD;
        }

        public String uRI() {
            return uRI;
        }

        public Type type() {
            return type;
        }

        public void setDisplayName(final String displayName) {
            this.displayName = displayName;
        }

        public void setEmailAddress(final String emailAddress) {
            this.emailAddress = emailAddress;
        }

        public void setID(final String iD) {
            this.iD = iD;
        }

        public void setURI(final String uRI) {
            this.uRI = uRI;
        }

        public void setType(final Type type) {
            this.type = type;
        }
    }
}
