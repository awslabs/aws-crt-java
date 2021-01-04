// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.aws.sdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class RedirectAllRequestsTo {
    /**
     * <p>Name of the host where requests are redirected.</p>
     */
    String hostName;

    /**
     * <p>Protocol to use when redirecting requests. The default is the protocol that is used in
     *          the original request.</p>
     */
    Protocol protocol;

    RedirectAllRequestsTo() {
        this.hostName = "";
        this.protocol = null;
    }

    protected RedirectAllRequestsTo(BuilderImpl builder) {
        this.hostName = builder.hostName;
        this.protocol = builder.protocol;
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }

    @Override
    public int hashCode() {
        return Objects.hash(RedirectAllRequestsTo.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof RedirectAllRequestsTo);
    }

    public String hostName() {
        return hostName;
    }

    public Protocol protocol() {
        return protocol;
    }

    public void setHostName(final String hostName) {
        this.hostName = hostName;
    }

    public void setProtocol(final Protocol protocol) {
        this.protocol = protocol;
    }

    public interface Builder {
        Builder hostName(String hostName);

        Builder protocol(Protocol protocol);
    }

    protected static class BuilderImpl implements Builder {
        /**
         * <p>Name of the host where requests are redirected.</p>
         */
        String hostName;

        /**
         * <p>Protocol to use when redirecting requests. The default is the protocol that is used in
         *          the original request.</p>
         */
        Protocol protocol;

        protected BuilderImpl() {
        }

        private BuilderImpl(RedirectAllRequestsTo model) {
            hostName(model.hostName);
            protocol(model.protocol);
        }

        public RedirectAllRequestsTo build() {
            return new RedirectAllRequestsTo(this);
        }

        public final Builder hostName(String hostName) {
            this.hostName = hostName;
            return this;
        }

        public final Builder protocol(Protocol protocol) {
            this.protocol = protocol;
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

        public String hostName() {
            return hostName;
        }

        public Protocol protocol() {
            return protocol;
        }

        public void setHostName(final String hostName) {
            this.hostName = hostName;
        }

        public void setProtocol(final Protocol protocol) {
            this.protocol = protocol;
        }
    }
}
