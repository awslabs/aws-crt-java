// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class RedirectAllRequestsTo {
    private String hostName;

    private Protocol protocol;

    private RedirectAllRequestsTo() {
        this.hostName = null;
        this.protocol = null;
    }

    private RedirectAllRequestsTo(Builder builder) {
        this.hostName = builder.hostName;
        this.protocol = builder.protocol;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
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

    public void setHostName(final String hostName) {
        this.hostName = hostName;
    }

    public Protocol protocol() {
        return protocol;
    }

    public void setProtocol(final Protocol protocol) {
        this.protocol = protocol;
    }

    static final class Builder {
        private String hostName;

        private Protocol protocol;

        private Builder() {
        }

        private Builder(RedirectAllRequestsTo model) {
            hostName(model.hostName);
            protocol(model.protocol);
        }

        public RedirectAllRequestsTo build() {
            return new com.amazonaws.s3.model.RedirectAllRequestsTo(this);
        }

        /**
         * <p>Name of the host where requests are redirected.</p>
         */
        public final Builder hostName(String hostName) {
            this.hostName = hostName;
            return this;
        }

        /**
         * <p>Protocol to use when redirecting requests. The default is the protocol that is used in
         *          the original request.</p>
         */
        public final Builder protocol(Protocol protocol) {
            this.protocol = protocol;
            return this;
        }
    }
}
