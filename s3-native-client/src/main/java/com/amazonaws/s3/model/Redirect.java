// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0.
package com.amazonaws.s3.model;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;
import software.amazon.awssdk.crt.annotations.Generated;

@Generated("software.amazon.smithy.crt.java.StructureGenerator")
public class Redirect {
    private String hostName;

    private String httpRedirectCode;

    private Protocol protocol;

    private String replaceKeyPrefixWith;

    private String replaceKeyWith;

    private Redirect() {
        this.hostName = null;
        this.httpRedirectCode = null;
        this.protocol = null;
        this.replaceKeyPrefixWith = null;
        this.replaceKeyWith = null;
    }

    private Redirect(Builder builder) {
        this.hostName = builder.hostName;
        this.httpRedirectCode = builder.httpRedirectCode;
        this.protocol = builder.protocol;
        this.replaceKeyPrefixWith = builder.replaceKeyPrefixWith;
        this.replaceKeyWith = builder.replaceKeyWith;
    }

    public Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Redirect.class);
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == null) return false;
        return (rhs instanceof Redirect);
    }

    public String hostName() {
        return hostName;
    }

    public void setHostName(final String hostName) {
        this.hostName = hostName;
    }

    public String httpRedirectCode() {
        return httpRedirectCode;
    }

    public void setHttpRedirectCode(final String httpRedirectCode) {
        this.httpRedirectCode = httpRedirectCode;
    }

    public Protocol protocol() {
        return protocol;
    }

    public void setProtocol(final Protocol protocol) {
        this.protocol = protocol;
    }

    public String replaceKeyPrefixWith() {
        return replaceKeyPrefixWith;
    }

    public void setReplaceKeyPrefixWith(final String replaceKeyPrefixWith) {
        this.replaceKeyPrefixWith = replaceKeyPrefixWith;
    }

    public String replaceKeyWith() {
        return replaceKeyWith;
    }

    public void setReplaceKeyWith(final String replaceKeyWith) {
        this.replaceKeyWith = replaceKeyWith;
    }

    static final class Builder {
        private String hostName;

        private String httpRedirectCode;

        private Protocol protocol;

        private String replaceKeyPrefixWith;

        private String replaceKeyWith;

        private Builder() {
        }

        private Builder(Redirect model) {
            hostName(model.hostName);
            httpRedirectCode(model.httpRedirectCode);
            protocol(model.protocol);
            replaceKeyPrefixWith(model.replaceKeyPrefixWith);
            replaceKeyWith(model.replaceKeyWith);
        }

        public Redirect build() {
            return new com.amazonaws.s3.model.Redirect(this);
        }

        /**
         * <p>The host name to use in the redirect request.</p>
         */
        public final Builder hostName(String hostName) {
            this.hostName = hostName;
            return this;
        }

        /**
         * <p>The HTTP redirect code to use on the response. Not required if one of the siblings is
         *          present.</p>
         */
        public final Builder httpRedirectCode(String httpRedirectCode) {
            this.httpRedirectCode = httpRedirectCode;
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

        /**
         * <p>The object key prefix to use in the redirect request. For example, to redirect requests
         *          for all pages with prefix <code>docs/</code> (objects in the <code>docs/</code> folder) to
         *             <code>documents/</code>, you can set a condition block with <code>KeyPrefixEquals</code>
         *          set to <code>docs/</code> and in the Redirect set <code>ReplaceKeyPrefixWith</code> to
         *             <code>/documents</code>. Not required if one of the siblings is present. Can be present
         *          only if <code>ReplaceKeyWith</code> is not provided.</p>
         */
        public final Builder replaceKeyPrefixWith(String replaceKeyPrefixWith) {
            this.replaceKeyPrefixWith = replaceKeyPrefixWith;
            return this;
        }

        /**
         * <p>The specific object key to use in the redirect request. For example, redirect request to
         *             <code>error.html</code>. Not required if one of the siblings is present. Can be present
         *          only if <code>ReplaceKeyPrefixWith</code> is not provided.</p>
         */
        public final Builder replaceKeyWith(String replaceKeyWith) {
            this.replaceKeyWith = replaceKeyWith;
            return this;
        }
    }
}
